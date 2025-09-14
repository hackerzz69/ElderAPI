package com.zenyte.discord.ipb

import com.zenyte.common.clientBuilder
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * @author Corey
 * @since 03/10/2020
 */

private const val IPB_API_KEY = "db52ea1e88760d4acc975309921c6635"

/**
 * Shared OkHttp client for IPB requests.
 * Automatically injects auth + User-Agent headers.
 */
val client = clientBuilder
    .addNetworkInterceptor { chain ->
        val req = chain.request()
            .newBuilder()
            .addHeader("Authorization", Credentials.basic(IPB_API_KEY, ""))
            .addHeader("User-Agent", "Zenyte Discord Bot (https://zenyte.com/, 0.1)")
            .build()
        chain.proceed(req)
    }
    .build()

/**
 * Base URL builder for IPB API.
 * Example: urlBuilder().addPathSegment("calendar").addPathSegment("events")
 */
fun urlBuilder(): HttpUrl.Builder =
    "https://forums.zenyte.com/api".toHttpUrl().newBuilder()
