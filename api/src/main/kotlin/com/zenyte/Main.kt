package com.zenyte

import com.zenyte.api.API
import com.zenyte.sql.SQLThread

fun main(args: Array<String>) {
    SQLThread().start()
    API.start(args)
}