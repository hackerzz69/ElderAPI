package com.zenyte.common

import com.zenyte.common.datastore.RedisCache

/**
 * Utilities for Discord verification state stored in Redis.
 *
 * Keys follow the format: "verified_discord:<memberId>:<discordId>"
 */
object Discord {
    const val VERIFIED_DISCORD_KEY_PREFIX = "verified_discord"

    /**
     * Resolve a memberId from a given Discord ID.
     * @throws IllegalStateException if no matching key or multiple keys exist.
     */
    fun getVerifiedMemberId(discordId: Long): Int {
        val keys = RedisCache.redis.sync().keys("$VERIFIED_DISCORD_KEY_PREFIX:*:$discordId")

        require(keys.size == 1) {
            "Expected exactly one verified_discord key for discordId=$discordId, but got ${keys.size}"
        }

        // format: "verified_discord:<memberId>:<discordId>"
        return keys.first().split(":")[1].toInt()
    }
}
