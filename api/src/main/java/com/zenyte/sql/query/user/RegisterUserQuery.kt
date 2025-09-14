package com.zenyte.sql.query.user

import com.zenyte.api.model.RegisterRequest
import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */

class RegisterUserQuery(val request: RegisterRequest) : SQLRunnable() {
    
    // request.password.substring(7, 29) for hash if required
	
	val CHECK_QUERY = "SELECT * FROM core_members WHERE name = ?"
    val CREATE_QUERY = "INSERT INTO core_members (name, members_pass_hash, member_group_id, joined, ip_address, last_visit) VALUES (?, ?, ?, ?, ?, ?)"
	
	override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
	
	        HikariPool.getConnection(auth, "zenyte_forum").use { con ->
                con.prepareStatement(CHECK_QUERY).use { chk ->
                    chk.setString(1, request.username)
                    chk.execute()
	
	                if (chk.resultSet.next())
                        return Pair(RegisterResult(null, "exists"), null)
                }
		
		        con.prepareStatement(CREATE_QUERY).use { pst ->
                    pst.setString(1, request.username)
                    pst.setString(2, request.password)
			        pst.setInt(3, 3)
                    pst.setLong(4, System.currentTimeMillis() / 1000)
                    pst.setString(5, request.ip)
                    pst.setLong(6, System.currentTimeMillis() / 1000)
                    pst.execute()
                    return Pair(RegisterResult(pst.resultSet, "success"), null)
                }
            }
	
        } catch (e: Exception) {
	        e.printStackTrace()
	        return Pair(RegisterResult(null, "exception"), e)
        }
		
	}
	
	data class RegisterResult(override val results: ResultSet?, val response: String) : SQLResults
}