package com.zenyte.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Shared constants and utilities for Zenyte.
 * Provides a global Gson instance with HTML escaping disabled.
 */
object CommonConfig {
    val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    const val ZENYTE_USER_MEMBER_ID = 1272
}
