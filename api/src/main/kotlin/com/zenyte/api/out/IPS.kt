package com.zenyte.api.out

import com.zenyte.common.clientBuilder
import mu.KotlinLogging
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Request

/**
 * @author Corey
 * @since 20/11/19
 */
object IPS {
    
    private val logger = KotlinLogging.logger {}
    private const val API_KEY = "ad5eb15fb19e617ee909456b766e36a7" // TODO convert to env var
    
    private val client = clientBuilder
            .addNetworkInterceptor { chain ->
                val userAgentRequest = chain.request()
                        .newBuilder()
                        .addHeader("Authorization", Credentials.basic(API_KEY, ""))
                        .addHeader("User-Agent", "ZenyteApi (https://api.zenyte.com/, 0.1)")
                        .build()
                chain.proceed(userAgentRequest)
            }
            .build()
    
    private fun urlBuilder() = HttpUrl.Builder()
            .scheme("https")
            .host("forums.zenyte.com")
            .addPathSegment("api")
            .addPathSegment("core")
    
    fun hello() {
        val request = Request.Builder()
                .url(urlBuilder()
                        .addPathSegment("hello")
                        .build())
                .get()
                .build()
        
        client.newCall(request).execute().use { response ->
            val responseBody = response.body
            logger.info { responseBody?.string() }
        }
    }
    
    fun sendMessage(title: String, body: String, fromMember: Int, vararg toMembers: Int) {
        assert(toMembers.isNotEmpty())
        logger.info { "Sending IPS message from member $fromMember to member(s) ${toMembers.joinToString { "," }}" }
        val formBody = FormBody.Builder()
                .addEncoded("from", fromMember.toString())
                .addEncoded("title", title)
                .addEncoded("body", body)
        
        toMembers.forEach {
            formBody.addEncoded("to[]", it.toString())
        }
        
        val request = Request.Builder()
                .url(urlBuilder()
                        .addPathSegment("messages")
                        .build())
                .post(formBody.build())
                .build()
        
        client.newCall(request).execute().use { response ->
            val responseBody = response.body
            
            if (response.isSuccessful) {
                logger.debug { "Successfully sent message to member(s) ${toMembers.joinToString { "," }} from member $fromMember" }
            } else {
                logger.error { "Failed to send message, response:\n$response" }
            }
            
        }
    }
    
}