package com.zenyte.sql.query.adventurers

import com.zenyte.api.model.SubmitAwardRequest
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
class SubmitAwardQuery(val request: SubmitAwardRequest): SQLRunnable() {

    val query = "INSERT INTO awards_awarded ( awarded_member, awarded_award, awarded_date, awarded_options, awarded_giver, awarded_reason, awarded_cat, awarded_title )" +
            " SELECT ? AS awarded_member," +
            " ? AS awarded_award," +
            " ? AS awarded_date," +
            " 'manual' AS awarded_options, " +
            " 1 AS awarded_giver, " +
            " aw.award_reason AS awarded_reason," +
            " aw.award_cat_id AS awarded_cat," +
            " aw.award_title AS awarded_title" +
            " FROM awards_awards AS aw WHERE aw.award_id=?;"

    val VALID_RESULT = SubmitAwardResult(null, true) to null
    val INVALID_RESULT = SubmitAwardResult(null, false) to null

    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "elder_forum").use { con ->
                con.prepareStatement(query).use { stmt ->
                    stmt.setInt(1, request.userId)
                    stmt.setInt(2, request.award)
                    stmt.setLong(3, System.currentTimeMillis()/1000)
                    stmt.setInt(4, 2)
                    stmt.execute()

                    return VALID_RESULT
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            return INVALID_RESULT
        }
    }

    data class SubmitAwardResult(override val results: ResultSet?, val successful: Boolean) : SQLResults
}