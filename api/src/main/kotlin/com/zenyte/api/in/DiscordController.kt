package com.zenyte.api.`in`

import com.zenyte.api.model.Role
import com.zenyte.api.out.Discord as DiscordOut
import com.zenyte.common.Discord.VERIFIED_DISCORD_KEY_PREFIX
import com.zenyte.common.datastore.RedisCache
import com.zenyte.sql.query.user.InsertDiscordVerification
import com.zenyte.sql.query.user.UserByIdColumnsQuery
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/discord")
class DiscordController {

    private val logger = KotlinLogging.logger {}

    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Error querying Discord")
    class DiscordQueryException : RuntimeException()

    @PostMapping("/user/{userId}/grantrole/{roleId}")
    fun grantUserRole(@PathVariable userId: Long, @PathVariable roleId: Long) {
        try {
            DiscordOut.assignRoles(userId, roleId)
        } catch (e: RuntimeException) {
            logger.error(e) { "Failed to assign role $roleId to user $userId" }
            throw DiscordQueryException()
        }
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid verification code")
    class InvalidVerificationCodeException : RuntimeException()

    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "User already verified")
    class AlreadyVerifiedException : RuntimeException()

    @PostMapping("/verify/{memberId}")
    fun verifyDiscordAccount(
        @PathVariable memberId: Int,
        @RequestParam verificationCode: String
    ) {
        val discordId = RedisCache.redis.sync()
            .get("discord_verification_code:$verificationCode")
            ?.toLong() ?: throw InvalidVerificationCodeException()

        if (isVerified(memberId)) {
            throw AlreadyVerifiedException()
        }

        if (syncRoles(memberId, discordId)) {
            InsertDiscordVerification(discordId, memberId).getResults()
            RedisCache.redis.sync().set("$VERIFIED_DISCORD_KEY_PREFIX:$memberId:$discordId", "true")
            RedisCache.redis.sync().del("discord_verification_code:$verificationCode")
        }
    }

    private fun isVerified(memberId: Int): Boolean {
        val redis = RedisCache.redis.sync()

        // check redis cache
        if (redis.keys("$VERIFIED_DISCORD_KEY_PREFIX:*:$memberId").isNotEmpty()) {
            return true
        }
        if (redis.keys("$VERIFIED_DISCORD_KEY_PREFIX:$memberId:*").isNotEmpty()) {
            return true
        }

        // check discord roles
        return DiscordOut.memberHasRole(memberId.toLong(), Role.VERIFIED)
    }

    @PostMapping("/sync")
    fun syncRoles(
        @RequestParam memberId: Int,
        @RequestParam discordId: Long
    ): Boolean {
        logger.info { "Syncing roles for user $memberId with Discord id $discordId" }

        val (results, exception) =
            UserByIdColumnsQuery(memberId, arrayOf("member_group_id", "mgroup_others")).getResults()

        if (exception != null) {
            logger.error(exception) { "Failed to fetch forum groups for userId=$memberId" }
            return false
        }

        val details = (results as UserByIdColumnsQuery.UserColumnResults).queryResults
        val roleIds = mutableSetOf(Role.VERIFIED.discordRoleId)

        details.getValue("mgroup_others")
            .split(",")
            .plus(details.getValue("member_group_id"))
            .filter { it.isNotBlank() }
            .map { it.toInt() }
            .mapNotNull { it.getDiscordRoleFromForumGroup() }
            .forEach { roleIds.add(it) }

        // remove old donator roles
        val donatorRoles = Role.DONATOR_ROLES.map { it.discordRoleId }.minus(roleIds)
        DiscordOut.removeRoles(discordId, *donatorRoles.toLongArray())

        try {
            DiscordOut.assignRoles(discordId, *roleIds.toLongArray())
        } catch (e: RuntimeException) {
            logger.error(e) { "Failed to assign roles $roleIds to user $discordId" }
        }

        return true
    }

    private fun Int.getDiscordRoleFromForumGroup(): Long? {
        return Role.FORUM_GROUPS[this]?.discordRoleId
    }
}
