package com.zenyte.common.datastore

import com.zenyte.common.CommonConfig.gson
import com.zenyte.common.util.getenv
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisConnectionException
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import mu.KotlinLogging

object RedisCache {

    private val logger = KotlinLogging.logger {}

    private val redisClient: RedisClient by lazy {
        val host = getenv("REDIS_HOST", "localhost")
        val port = getenv("REDIS_PORT", "6379").toInt()
        val password = getenv("REDIS_PASSWORD", "")
        val database = getenv("REDIS_DB", "0").toInt()

        val uriBuilder = RedisURI.Builder.redis(host, port).withDatabase(database)
        if (password.isNotBlank()) uriBuilder.withPassword(password.toCharArray())

        val uri = uriBuilder.build()
        logger.info { "Initializing Redis client for $host:$port (db=$database)" }
        RedisClient.create(uri)
    }

    val redis: StatefulRedisConnection<String, String> by lazy {
        try {
            logger.info { "Connecting to Redis..." }
            val conn = redisClient.connect()
            conn.sync().ping()
            logger.info { "Successfully connected to Redis" }
            conn
        } catch (e: RedisConnectionException) {
            logger.error(e) { "Could not connect to Redis" }
            throw e
        }
    }

    fun <T> RedisCommands<String, String>.getObject(key: String, clazz: Class<T>): T? {
        val value = this.get(key) ?: return null
        return try {
            gson.fromJson(value, clazz)
        } catch (e: Exception) {
            logger.error(e) { "Failed to deserialize Redis key=$key into ${clazz.simpleName}" }
            null
        }
    }

    fun RedisCommands<String, String>.setObject(key: String, obj: Any, timeout: Int = 0) {
        val value = gson.toJson(obj)
        if (timeout > 0) {
            this.setex(key, timeout.toLong(), value)
        } else {
            this.set(key, value)
        }
    }

    fun shutdown() {
        try {
            logger.info { "Closing Redis connection and client..." }
            if (redis.isOpen) redis.close()
            redisClient.shutdown()
        } catch (e: Exception) {
            logger.warn(e) { "Error shutting down Redis client" }
        }
    }
}
