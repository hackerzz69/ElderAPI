package com.zenyte.sql.query

import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet

/**
 * @author Corey
 * @since 01/06/19
 */
class AuthSecretTokenQuery(private val memberId: Int) : SQLRunnable() {
    
    val query = "SELECT mfa_details FROM core_members WHERE member_id = ?"
    
    val emptyResult = AuthTokenResult(null) to null
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_forum").use { con ->
                con.prepareStatement(query).use {
                    it.setInt(1, memberId)
                    return Pair(AuthTokenResult(it.executeQuery()), null)
                }
            }
        } catch (e: Exception) {
            return emptyResult
        }
    }
    
    data class AuthTokenResult(override val results: ResultSet?) : SQLResults {
        val token: String
        
        init {
            if (results != null) {
                if (results.next()) {
                    val json = results.getString("mfa_details")
                    
                    token = if (json.isNullOrBlank()) {
                        ""
                    } else {
                        try {
                            val jsonObj = JsonParser().parse(json).asJsonObject
                            jsonObj.get("google").asString ?: ""
                        } catch (e: JsonSyntaxException) {
                            ""
                        }
                    }
                } else {
                    token = ""
                }
            } else {
                token = ""
            }
            
        }
    }
}
    
