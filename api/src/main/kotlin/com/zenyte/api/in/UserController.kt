package com.zenyte.api.`in`

import com.google.gson.JsonSyntaxException
import com.warrenstrange.googleauth.GoogleAuthenticator
import com.zenyte.api.model.*
import com.zenyte.api.out.IPS
import com.zenyte.common.CommonConfig.ZENYTE_USER_MEMBER_ID
import com.zenyte.common.datastore.RedisCache
import com.zenyte.common.CommonConfig.gson
import com.zenyte.sql.query.AuthSecretTokenQuery
import com.zenyte.sql.query.adventurers.SelectAdventurerLogQuery
import com.zenyte.sql.query.awards.GetUserAwards
import com.zenyte.sql.query.user.*
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*

/**
 * REST API endpoints for user-related operations.
 *
 * Modernized:
 * - Converted `object` → `class UserController` (preferred in Spring).
 * - Replaced all `printStackTrace()` calls with structured logging.
 * - Added safe fallbacks for Redis/DB lookups.
 * - Improved log readability.
 */
@RestController
@RequestMapping("/user")
class UserController {

    private val logger = KotlinLogging.logger {}
    private val googleAuthenticator = GoogleAuthenticator()

    @GetMapping("/joinDate/{displayName}")
    fun joinDate(@PathVariable displayName: String): String? {
        return try {
            (ForumJoinDateQuery(displayName).getResults().first as ForumJoinDateQuery.ForumJoinDateResult).date
        } catch (e: Exception) {
            logger.error(e) { "Failed to get join date for user '$displayName'" }
            null
        }
    }

    @GetMapping("/columns/{displayName}")
    fun getColumnsByDisplayName(
        @PathVariable displayName: String,
        @RequestParam columns: Array<String>
    ): Map<String, String> {
        logger.info { "[user=$displayName] Querying DB for columns: ${columns.joinToString()}" }
        val (results, exception) = UserByNameColumnsQuery(displayName, columns).getResults()
        return if (exception != null) {
            logger.error(exception) { "Error fetching columns for user '$displayName'" }
            emptyMap()
        } else {
            (results as UserByNameColumnsQuery.UserColumnResults).queryResults
        }
    }

    @GetMapping("/columnsbyid/{memberId}")
    fun getColumnsByMemberId(
        @PathVariable memberId: Int,
        @RequestParam columns: Array<String>
    ): Map<String, String> {
        logger.info { "[user=$memberId] Querying DB for columns: ${columns.joinToString()}" }
        val (results, exception) = UserByIdColumnsQuery(memberId, columns).getResults()
        return if (exception != null) {
            logger.error(exception) { "Error fetching columns for userId=$memberId" }
            emptyMap()
        } else {
            (results as UserByIdColumnsQuery.UserColumnResults).queryResults
        }
    }

    @GetMapping("/{memberId}/check2fa")
    fun valid2faCode(@PathVariable memberId: Int, @RequestParam code: Int): Boolean {
        logger.info { "[code=$code] Checking 2FA for memberId=$memberId" }
        val (results, exception) = AuthSecretTokenQuery(memberId).getResults()
        if (exception != null) {
            logger.error(exception) { "Failed to retrieve 2FA token for userId=$memberId" }
            return false
        }
        val token = (results as AuthSecretTokenQuery.AuthTokenResult).token
        return googleAuthenticator.authorize(token, code)
    }

    @PostMapping("/log/punish")
    fun submitPunishmentLog(@RequestBody punishment: PunishmentLog) {
        logger.info { "Submitting punishment log: $punishment" }
        SubmitPunishmentLogQuery(punishment).getResults()

        logger.info { "Sending punishment forum message to user '${punishment.offender}'" }

        IPS.sendMessage(
            "Recent offence on your account",
            """
            Dear <span style="color:#93B045">${punishment.offender}</span>,
            You have received a punishment on your account.
            Punishment type: <span style="color:#00BDF5"><u>${punishment.actionType}</u></span>
            Expires: <span style="color:#00BDF5"><u>${punishment.expires}</u></span>
            Reason: <span style="color:#00BDF5"><u>${punishment.reason}</u></span>
            If you believe this is a mistake or think you were unfairly punished we recommend you appeal the offence <a href="https://forums.zenyte.com/forum/20-appeals/">here</a>.
            We urge you strive to follow the rules in the future.
            You can remind yourself of the rules here:
            <iframe allowfullscreen class="ipsEmbed_finishedLoading" src="https://forums.zenyte.com/topic/282-official-zenyte-rules/?do=embed" style="overflow: hidden; height: 217px; max-width: 502px;"></iframe>
            - Zenyte Staff
            """.trimIndent().replace("\n", "<br>"),
            ZENYTE_USER_MEMBER_ID,
            punishment.offenderUserId
        )
    }

    @PostMapping("/log/trade")
    fun submitTradeLog(@RequestBody transaction: TradeLog) {
        logger.info { "Submitting trade log: $transaction" }
        SubmitTradeLogQuery(transaction).getResults()
    }

    @PostMapping("/info")
    fun submitPlayerInformation(@RequestBody info: PlayerInformation) {
        logPlayerSession(info.userId)
        val (_, exception) = SubmitPlayerInformationQuery(info).getResults()
        if (exception != null) {
            logger.error(exception) { "Failed to submit player info for '${info.username}'" }
        } else {
            RedisCache.redis.sync().del(info.redisKey())
            syncPlayerInformation(info)
        }
    }

    @GetMapping("/info/{username}")
    fun getPlayerInformation(@PathVariable username: String): PlayerInformation? {
        val cached = RedisCache.redis.sync().get(PlayerInformation.redisKey(username))
        if (cached != null) {
            try {
                return gson.fromJson(cached, PlayerInformation::class.java)
            } catch (e: JsonSyntaxException) {
                logger.error(e) { "Invalid JSON in Redis for user '$username', falling back to DB" }
            }
        }
        val (results, exception) = SelectPlayerInformationQuery(username).getResults()
        return if (exception != null) {
            logger.error(exception) { "Failed to get player info for '$username'" }
            null
        } else {
            val info = (results as SelectPlayerInformationQuery.SelectPlayerInformationResult).info
            if (info != null) {
                RedisCache.redis.sync().setex(info.redisKey(), 3600, gson.toJson(info))
            }
            info
        }
    }

    @GetMapping("/adv/{username}")
    fun getPlayerAdventurerLog(@PathVariable username: String): List<AdventurerLogEntry> {
        val (results, exception) = SelectAdventurerLogQuery(username).getResults()
        return if (exception != null) {
            logger.error(exception) { "Failed to get adventurer log for '$username'" }
            emptyList()
        } else {
            (results as SelectAdventurerLogQuery.AdventurerLogQueryResults).logEntries
        }
    }

    @GetMapping("/awards/{username}")
    fun getAwards(@PathVariable username: String): UserAwards? {
        val (results, exception) = GetUserAwards(username).getResults()
        return if (exception != null) {
            logger.error(exception) { "Failed to get awards for '$username'" }
            null
        } else {
            (results as GetUserAwards.GetUserAwardsResults).awards
        }
    }

    // --- private helpers ---

    private fun syncPlayerInformation(info: PlayerInformation) {
        syncDonatorRole(info)
    }

    private fun syncDonatorRole(info: PlayerInformation) {
        val donatorRole = info.donatorRole ?: return
        val donatorRoleGroupIds = Role.DONATOR_ROLES.map { it.forumGroupId }
        val details = getColumnsByMemberId(info.userId, arrayOf("member_group_id", "mgroup_others")).toMutableMap()

        val primaryGroup = details.getValue("member_group_id").toInt()
        val secondaryGroups = details.getValue("mgroup_others").split(",").mapNotNull { it.toIntOrNull() }

        if (primaryGroup == Role.REGISTERED_MEMBER.forumGroupId || donatorRoleGroupIds.contains(primaryGroup)) {
            details["member_group_id"] = donatorRole.forumGroupId.toString()
            details["mgroup_others"] = secondaryGroups.filter { it !in donatorRoleGroupIds }
                .joinToString(",")
        } else {
            details["mgroup_others"] = secondaryGroups.filter { it !in donatorRoleGroupIds }
                .plus(donatorRole.forumGroupId)
                .joinToString(",")
        }

        val newPrimary = details["member_group_id"]!!
        val newSecondary = details["mgroup_others"]!!

        if (primaryGroup.toString() != newPrimary || secondaryGroups.joinToString(",") != newSecondary) {
            logger.info {
                "Updating donator groups for '${info.username}': " +
                        "primary $primaryGroup->$newPrimary, " +
                        "secondary ${secondaryGroups.joinToString(",")}->$newSecondary"
            }
            val (_, exception) = UpdateUserMemberGroupsQuery(info.userId, newPrimary, newSecondary).getResults()
            if (exception != null) {
                logger.error(exception) { "Failed to update forum groups for '${info.username}'" }
            }
        }
    }

    private fun logPlayerSession(playerId: Int) {
        UpdateLastActiveDateQuery(playerId).getResults()
    }
}
