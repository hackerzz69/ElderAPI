package com.zenyte.common.util

/**
 * @author Corey
 * @since 14/05/19
 */

/**
 * Get the value of an environment variable.
 * Returns given default if environment variable doesn't exist.
 */
fun getenv(name: String, default: String): String {
    return try {
        val env = System.getenv(name)
        if (env.isNullOrBlank()) {
            default
        } else {
            env
        }
    } catch (e: SecurityException) {
        default
    }
}