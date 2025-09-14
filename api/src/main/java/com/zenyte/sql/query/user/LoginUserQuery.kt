package com.zenyte.sql.query.user

import com.zenyte.api.model.LoginRequest
import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import com.zenyte.util.AES
import com.zenyte.util.BCrypt
import java.sql.ResultSet

class LoginUserQuery(val request: LoginRequest) : SQLRunnable() {
    
    val query = "SELECT members_pass_hash FROM core_members WHERE name = ?"

    val VALID_RESULT = LoginResult(null, "true")
    val INVALID_RESULT = LoginResult(null, "false")
    val EMPTY_RESULT = LoginResult(null, "empty")
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_forum").use { con ->
                con.prepareStatement(query).use { stmt ->
                    val decryptedPassword = AES.decrypt(request.password, "p8UfL9dgr2R2x5n5")

                    stmt.setString(1, request.username)
                    stmt.execute()

                    if(stmt.resultSet.next()) {
                        val hash = stmt.resultSet.getString("members_pass_hash")
                        val valid = BCrypt.checkpw(decryptedPassword, hash)

                        return Pair(if(valid) VALID_RESULT else INVALID_RESULT, null)
                    }

                    return Pair(EMPTY_RESULT, null)
                }
            }
    
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(EMPTY_RESULT, e)
        }
    }
    
    data class LoginResult(override val results: ResultSet?, val successful: String) : SQLResults
}