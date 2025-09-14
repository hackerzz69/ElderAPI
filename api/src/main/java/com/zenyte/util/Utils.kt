package com.zenyte.util

import java.nio.charset.Charset
import java.security.SecureRandom

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */

fun randomString(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return String(bytes, Charset.forName("UTF-8"))
}
