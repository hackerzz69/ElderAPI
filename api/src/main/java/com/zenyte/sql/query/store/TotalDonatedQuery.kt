package com.zenyte.sql.query.store

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
class TotalDonatedQuery(val username: String): SQLRunnable() {

    val TOTAL_DONATED = "SELECT SUM(paid) as total FROM store_payments WHERE username = ?"

    val EMPTY_RESULT = TotalDonatedResult(null, 0) to null

    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "elder_main").use { con ->
                con.prepareStatement(TOTAL_DONATED).use { stmt ->
                    stmt.setString(1, username)
                    stmt.execute()

                    if(stmt.resultSet.next()) {
                        return TotalDonatedResult(null, stmt.resultSet.getInt("total")) to null
                    }

                    return EMPTY_RESULT
                }
            }

        } catch(e: Exception) {
            e.printStackTrace()
            return EMPTY_RESULT
        }
    }

    data class TotalDonatedResult(override val results: ResultSet?, val total: Int): SQLResults
}