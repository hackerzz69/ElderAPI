package com.zenyte.discord

import com.zenyte.common.EnvironmentVariable
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * @author Corey
 * @since 03/11/2019
 */
object Api {
    
    val DEVELOPER_MODE = EnvironmentVariable("DEV_MODE").value?.toBoolean() ?: false
    val API_TOKEN_ENV_VAR = EnvironmentVariable("API_TOKEN")
    val API_URL_ENV_VAR = EnvironmentVariable("API_URL")
    
    private val token = API_TOKEN_ENV_VAR.value ?: ""
    
    private val json = "application/json; charset=utf-8".toMediaType()
    
    val client = OkHttpClient.Builder()
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .writeTimeout(5000, TimeUnit.MILLISECONDS)
            .addNetworkInterceptor { chain ->
                val userAgentRequest = chain.request()
                        .newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("User-Agent", "Zenyte/Discord Bot Service")
                        .build()
                chain.proceed(userAgentRequest)
            }
            .build()
    
    fun getApiRoot() = (API_URL_ENV_VAR.value ?: "http://localhost:8080/").toHttpUrlOrNull()!!.newBuilder()
    
    fun ping(): Boolean {
        val body = FormBody.Builder()
                .add("payload", "ping")
                .build()
        
        val request = Request.Builder()
                .url(getApiRoot()
                        .addPathSegment("ping")
                        .build())
                .post(body)
                .build()
        
        client.newCall(request).execute().use { response ->
            val responseBody = response.body
            if (!response.isSuccessful) {
                return false
            }
            return responseBody?.string() == "pong"
        }
    }
    
}