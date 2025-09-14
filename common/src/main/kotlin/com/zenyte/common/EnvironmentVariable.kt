package com.zenyte.common

/**
 * @author Corey
 * @since 03/11/2019
 */
inline class EnvironmentVariable(val key: String) {
    override fun toString() = key
    
    val value: String?
        get() = value()
    
    private fun value() = System.getenv(key)
}

typealias EnvVar = EnvironmentVariable
