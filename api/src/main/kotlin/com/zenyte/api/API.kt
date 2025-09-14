package com.zenyte.api

import com.zenyte.common.datastore.RedisCache
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class Application

object API {
    fun start(args: Array<String>) {
        try {
            RedisCache.redis.sync().ping()
            println("✅ Redis connection successful at startup")
        } catch (e: Exception) {
            println("⚠️ Redis connection failed: ${e.message}")
        }

        runApplication<Application>(*args)
    }
}
