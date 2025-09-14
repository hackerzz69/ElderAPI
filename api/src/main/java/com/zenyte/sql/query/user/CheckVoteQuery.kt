package com.zenyte.sql.query.user

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
class CheckVoteQuery(val displayName: String) : SQLRunnable() {

    val QUERY = "SELECT COUNT(*) as amount FROM votes WHERE username = ? AND claimed = 0 AND voted_on IS NOT NULL"
    val CLAIM_VOTES = "UPDATE votes SET claimed = 1 WHERE username = ? AND voted_on IS NOT NULL"

    val EMPTY_RESULT = VoteCheckResults(null, 0) to null

    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "elder_main").use { con ->
                con.prepareStatement(QUERY).use { stmt ->
                    stmt.setString(1, displayName)
                    stmt.execute()

                    if(stmt.resultSet.next()) {
                        val amount = stmt.resultSet.getInt("amount")

                        if(amount > 0) {
                            con.prepareStatement(CLAIM_VOTES).use { claim ->
                                claim.setString(1, displayName)
                                claim.execute()
                            }
                        }

                        return VoteCheckResults(null, amount) to null
                    }
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            return EMPTY_RESULT
        }

        return EMPTY_RESULT
    }

    data class VoteCheckResults(override val results: ResultSet?, val amount: Int) : SQLResults
}