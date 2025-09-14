package com.zenyte.sql.query.adventurers

import com.zenyte.api.model.SubmitGamelogRequest
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
class SubmitGameLogQuery(val request: SubmitGamelogRequest): SQLRunnable() {

    val query = "INSERT INTO advlog_game (user, icon, message, date) VALUES (?, ?, ?, ?)"
    val pvpQuery = "INSERT INTO advlog_pvp (user, icon, message, date) VALUES (?, ?, ?, ?)"

    val VALID_RESULT = SubmitGameLogResult(null, true) to null
    val INVALID_RESULT = SubmitGameLogResult(null, false) to null

    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_main").use { con ->
                con.prepareStatement(if (!request.pvp) query else pvpQuery).use {
                    it.setString(1, request.user)
                    it.setString(2, request.icon)
                    it.setString(3, request.message)
                    it.setLong(4, System.currentTimeMillis()/1000)
                    it.execute()

                    return VALID_RESULT
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            return INVALID_RESULT
        }
    }

    data class SubmitGameLogResult(override val results: ResultSet?, val success: Boolean) : SQLResults
}