package com.zenyte.common

import com.zenyte.api.model.World
import com.zenyte.common.CommonConfig.gson
import com.zenyte.common.datastore.RedisCache
import io.lettuce.core.api.sync.RedisCommands

object WorldInfo {

    enum class Field {
        COUNT,
        UPTIME,
        JSON,
        PLAYERS
    }

    private const val OBJ_KEY_PREFIX = "world:"

    private val redis: RedisCommands<String, String>
        get() = RedisCache.redis.sync()

    fun World.getKey(): String = "$OBJ_KEY_PREFIX$name"
    private fun String.getWorldKey(): String = "$OBJ_KEY_PREFIX$this"

    private fun getWorldForKey(key: String): String? =
        redis.hget(key, Field.JSON.name)

    fun getAllWorlds(): List<World> {
        val worldKeys = redis.keys("$OBJ_KEY_PREFIX*")
        val worldJsons = worldKeys.mapNotNull { redis.hget(it, Field.JSON.name) }

        // Build a proper JSON array for deserialization
        val jsonArray = worldJsons.joinToString(prefix = "[", postfix = "]")
        return gson.fromJson(jsonArray, Array<World>::class.java).toList()
    }

    fun getTotalPlayerCount(): Int {
        val worldKeys = redis.keys("$OBJ_KEY_PREFIX*")
        return worldKeys
            .mapNotNull { redis.hget(it, Field.COUNT.name)?.toIntOrNull() }
            .sum()
    }

    fun getWorld(name: String): String? =
        getWorldForKey(name.getWorldKey())

    fun isOnline(name: String): Boolean =
        redis.exists(name.getWorldKey()) > 0

    fun getPlayerCountForWorld(name: String): Int? =
        redis.hget(name.getWorldKey(), Field.COUNT.name)?.toIntOrNull()

    fun getWorldUptime(name: String): Long? =
        redis.hget(name.getWorldKey(), Field.UPTIME.name)?.toLongOrNull()

    fun getPlayersForWorld(name: String): List<String> {
        val playersJson = redis.hget(name.getWorldKey(), Field.PLAYERS.name) ?: return emptyList()
        return gson.fromJson(playersJson, Array<String>::class.java).toList()
    }
}
