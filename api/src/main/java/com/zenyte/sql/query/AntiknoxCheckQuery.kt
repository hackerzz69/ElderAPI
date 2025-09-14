package com.zenyte.sql.query

import com.google.gson.JsonParser
import com.zenyte.asn.api.AntiknoxQuery
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
class AntiknoxCheckQuery(private val ip: String): SQLRunnable() {

    companion object {
        var parser = JsonParser()
    }

    val CHECK_QUERY = "SELECT * FROM antiknox_cache WHERE ipAddress = ?"
    val CACHE_IP = "INSERT INTO antiknox_cache (ipAddress, messageDigest, legitimate) VALUES (?, ?, ?)"

    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_main").use { con ->
                con.prepareStatement(CHECK_QUERY).use { stmt ->
                    stmt.setString(1, ip)
                    stmt.execute()

                    if(stmt.resultSet.next()) {
                        val digest = stmt.resultSet.getString("messageDigest")
                        val legit = stmt.resultSet.getBoolean("legitimate")
                        return Pair(AntiknoxResult(stmt.resultSet, digest, legit), null)
                    } else {
                        var response = AntiknoxQuery().execute(ip)

                        con.prepareStatement(CACHE_IP).use { stmt ->
                            stmt.setString(1, ip)
                            stmt.setString(2, response.digest)
                            stmt.setBoolean(3, response.legit)
                            stmt.execute()
                            return Pair(AntiknoxResult(null, response.digest, response.legit), null)
                        }
                    }
                }
            }
        } catch(e: Exception) {
            return Pair(AntiknoxResult(null, "empty", false), e)
        }
    }

    data class AntiknoxResult(override val results: ResultSet?, val digest: String, val legit: Boolean) : SQLResults

}