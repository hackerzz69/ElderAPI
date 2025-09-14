package com.zenyte.discord

import com.zenyte.common.EnvironmentVariable
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * HTTP client wrapper for communicating with the Elder API service.
 */
object Api {

    private val logger = LoggerFactory.getLogger(Api::class.java)

    val DEVELOPER_MODE: Boolean =
        EnvironmentVariable("DEV_MODE").value?.toBooleanStrictOrNull() ?: false

    val API_TOKEN_ENV_VAR = EnvironmentVariable("API_TOKEN")
    val API_URL_ENV_VAR = EnvironmentVariable("API_URL")

    private val token: String = API_TOKEN_ENV_VAR.value.orEmpty()

    private val json = "application/json; charset=utf-8".toMediaType()

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .addNetworkInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("User-Agent", "ElderAPI/DiscordBot")
                .build()
            chain.proceed(req)
        }
        .build()

    /**
     * Build the root API URL.
     */
    fun getApiRoot(): HttpUrl {
        val baseUrl = API_URL_ENV_VAR.value ?: "http://localhost:8080/"
        return baseUrl.toHttpUrl()  // throws IllegalArgumentException if invalid
    }

    /**
     * Simple health check — posts `payload=ping` to `/ping` and expects "pong".
     */
    fun ping(): Boolean {
        val body = FormBody.Builder()
            .add("payload", "ping")
            .build()

        val request = Request.Builder()
            .url(getApiRoot().newBuilder().addPathSegment("ping").build())
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val success = response.isSuccessful && response.body?.string() == "pong"
            if (!success) {
                logger.warn("API ping failed: HTTP ${response.code}")
            }
            return success
        }
    }
}
