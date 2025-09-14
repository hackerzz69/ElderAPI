package com.zenyte.common

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * @author Corey
 * @since 19/02/2020
 */

fun OkHttpClient.getJsonResponseOrNull(request: Request): String? {
    try {
        this.newCall(request).execute().use { response ->
            val responseBody = response.body
            if (!response.isSuccessful) {
                return null
            }
            return responseBody?.string()
        }
    } catch (e: IOException) {
        return null
    }
}

val json = "application/json; charset=utf-8".toMediaType()
val urlEncoded = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()

val clientBuilder = OkHttpClient.Builder()
        .connectTimeout(5000, TimeUnit.MILLISECONDS)
        .readTimeout(5000, TimeUnit.MILLISECONDS)
        .writeTimeout(5000, TimeUnit.MILLISECONDS)
