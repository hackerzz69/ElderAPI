package com.zenyte.api.out

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.zenyte.api.model.Role
import com.zenyte.common.clientBuilder
import com.zenyte.common.getJsonResponseOrNull
import com.zenyte.common.CommonConfig.gson
import com.zenyte.common.json
import mu.KotlinLogging
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * @author Corey
 * @since 02/10/19
 */
object Discord {
    
    private val logger = KotlinLogging.logger {}
    private const val API_VERSION = 10 // discord api version https://discordapp.com/developers/docs/reference#api-versioning
    private const val GUILD = 1351292737499955210 // Elder guild
    private val TOKEN = System.getenv("BOT_TOKEN") ?: error("BOT_TOKEN not set")
    
    private val client = clientBuilder
            .addNetworkInterceptor { chain ->
                val userAgentRequest = chain.request()
                        .newBuilder()
                        .addHeader("Authorization", "Bot $TOKEN")
                        .addHeader("User-Agent", "ZenyteApi (https://api.zenyte.com/, 0.1)")
                        .build()
                chain.proceed(userAgentRequest)
            }
            .build()
    
    private fun urlBuilder() = HttpUrl.Builder()
            .scheme("https")
            .host("discordapp.com")
            .addPathSegment("api")
            .addPathSegment("v$API_VERSION")
    
    private fun jsonBody(payload: Any) = gson.toJson(payload).toRequestBody(json)
    
    fun getMemberRoles(memberId: Long): Set<Long> {
        val user = getMember(memberId) ?: return setOf()
        val jsonObject = JsonParser.parseString(user).asJsonObject
        val roles = jsonObject.getAsJsonArray("roles")
        
        if (roles.size() == 0) {
            return emptySet()
        }
        
        val rolesList = gson.fromJson(roles, Array<String>::class.java)
        
        return rolesList.map { it.toLong() }.toHashSet()
    }
    
    fun getMember(memberId: Long): String? {
        val request = Request.Builder()
                .url(urlBuilder()
                        .addPathSegment("guilds")
                        .addPathSegment(GUILD.toString())
                        .addPathSegment("members")
                        .addPathSegment(memberId.toString())
                        .build())
                .get()
                .build()
    
        return client.getJsonResponseOrNull(request)
    }
    
    fun memberHasRole(memberId: Long, role: Role): Boolean {
        if (!role.isDiscordRole()) {
            return false
        }
        return getMemberRoles(memberId).contains(role.discordRoleId)
    }
    
    fun assignRoles(memberId: Long, vararg roles: Role): Boolean {
        return assignRoles(memberId, *roles.map { it.discordRoleId }.toLongArray())
    }
    
    fun assignRoles(memberId: Long, vararg roles: Long): Boolean {
        if (roles.size == 1) {
            return assignSingleRole(memberId, roles.first())
        } else if (roles.isEmpty()) {
            return false // return error?
        }
        
        val rolesToSend = getMemberRoles(memberId).toMutableSet().apply {
            addAll(roles.asList())
        }
        
        val body = JsonObject().apply {
            add("roles", gson.toJsonTree(rolesToSend.map { it.toString() }).asJsonArray)
        }
        
        val request = Request.Builder()
                .url(urlBuilder()
                        .addPathSegment("guilds")
                        .addPathSegment(GUILD.toString())
                        .addPathSegment("members")
                        .addPathSegment(memberId.toString())
                        .build())
                .patch(jsonBody(body))
                .build()
        
        client.newCall(request).execute().use { response ->
            if (response.code == 204) {
                return true
            }
            throw RuntimeException("Failed assigning roles; $response")
        }
    }
    
    private fun assignSingleRole(memberId: Long, role: Long): Boolean {
        val request = Request.Builder()
                .url(urlBuilder()
                        .addPathSegment("guilds")
                        .addPathSegment(GUILD.toString())
                        .addPathSegment("members")
                        .addPathSegment(memberId.toString())
                        .addPathSegment("roles")
                        .addPathSegment(role.toString())
                        .build())
                .put("".toRequestBody())
                .build()
        
        client.newCall(request).execute().use { response ->
            if (response.code == 204) {
                return true
            }
            throw RuntimeException("Failed assigning role; $response")
        }
    }
    
    fun removeRoles(memberId: Long, vararg roles: Role): Boolean {
        return removeRoles(memberId, *roles.map { it.discordRoleId }.toLongArray())
    }
    
    fun removeRoles(memberId: Long, vararg roles: Long): Boolean {
        if (roles.size == 1) {
            return removeSingleRole(memberId, roles.first())
        } else if (roles.isEmpty()) {
            return false // return error?
        }
        
        val rolesToSend = getMemberRoles(memberId).toMutableSet().apply {
            removeAll(roles.asList())
        }
        
        val body = JsonObject().apply {
            add("roles", gson.toJsonTree(rolesToSend.map { it.toString() }).asJsonArray)
        }
        
        val request = Request.Builder()
                .url(urlBuilder()
                        .addPathSegment("guilds")
                        .addPathSegment(GUILD.toString())
                        .addPathSegment("members")
                        .addPathSegment(memberId.toString())
                        .build())
                .patch(jsonBody(body))
                .build()
        
        client.newCall(request).execute().use { response ->
            if (response.code == 204) {
                return true
            }
            throw RuntimeException("Failed removing roles; $response")
        }
    }
    
    private fun removeSingleRole(memberId: Long, role: Long): Boolean {
        val request = Request.Builder()
                .url(urlBuilder()
                        .addPathSegment("guilds")
                        .addPathSegment(GUILD.toString())
                        .addPathSegment("members")
                        .addPathSegment(memberId.toString())
                        .addPathSegment("roles")
                        .addPathSegment(role.toString())
                        .build())
                .delete("".toRequestBody())
                .build()
        
        client.newCall(request).execute().use { response ->
            if (response.code == 204) {
                return true
            }
            throw RuntimeException("Failed removing role; $response")
        }
    }
    
}
