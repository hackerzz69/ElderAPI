package com.zenyte.discord.ipb

import com.zenyte.common.clientBuilder
import okhttp3.Credentials
import okhttp3.HttpUrl

/**
 * @author Corey
 * @since 03/10/2020
 */

const val IPB_API_KEY = "db52ea1e88760d4acc975309921c6635"

val client = clientBuilder
        .addNetworkInterceptor { chain ->
            val userAgentRequest = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", Credentials.basic(IPB_API_KEY, ""))
                    .addHeader("User-Agent", "Zenyte Discord Bot (https://zenyte.com/, 0.1)")
                    .build()
            chain.proceed(userAgentRequest)
        }
        .build()

fun urlBuilder() = HttpUrl.Builder()
        .scheme("https")
        .host("forums.zenyte.com")
        .addPathSegment("api")
