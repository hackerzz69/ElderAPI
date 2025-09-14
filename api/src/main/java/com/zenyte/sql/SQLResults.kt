package com.zenyte.sql

import java.sql.ResultSet

/**
 * @author Corey
 * @since 11/12/18
 */
interface SQLResults {
    val results: ResultSet?
}

data class NoneResult(override val results: ResultSet? = null) : SQLResults