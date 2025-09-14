package com.zenyte.sql.query

import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet

/**
 * @author Corey, Noele
 * @since 23/10/18
 */
class ApiAuthenticationQuery(private val token: String, private val ip: String) : SQLRunnable() {
    
    //	val query = "SELECT COUNT(*) as total FROM api_auth WHERE token = ? AND ip = ?"
    val query = "SELECT COUNT(*) as total FROM api_auth WHERE token = ?"
	
	val VALID_RESULT = ApiAuthenticationResult(null, true) to null
	val INVALID_RESULT = ApiAuthenticationResult(null, false) to null
	
	override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
		try {
			HikariPool.getConnection(auth, "zenyte_main").use { con ->
				con.prepareStatement(query).use {
					it.setString(1, token)
//					it.setString(2, ip)
					it.execute()
					if (it.resultSet.next())
						return if (it.resultSet.getInt("total") > 0) VALID_RESULT else INVALID_RESULT
					else
						return INVALID_RESULT
				}
			}
		} catch (e: Exception) {
			return INVALID_RESULT
		}
	}
	
	data class ApiAuthenticationResult(override val results: ResultSet?, val successful: Boolean) : SQLResults
}