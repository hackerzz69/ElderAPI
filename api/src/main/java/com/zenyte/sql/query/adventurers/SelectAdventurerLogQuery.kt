package com.zenyte.sql.query.adventurers

import com.zenyte.api.model.AdventurerLogEntry
import com.zenyte.sql.HikariPool
import com.zenyte.sql.NoneResult
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet
import java.util.*
import java.util.concurrent.TimeUnit

class SelectAdventurerLogQuery(private val username: String) : SQLRunnable() {

    private val selectQuery = """
        SELECT icon, message, date
        FROM advlog_game
        WHERE user = ?
    """.trimIndent()

    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        return try {
            HikariPool.getConnection(auth, "zenyte_main").use { con ->
                con.prepareStatement(selectQuery).use { stmt ->
                    stmt.setString(1, username.replace(" ", "_"))
                    AdventurerLogQueryResults(stmt.executeQuery(), username) to null
                }
            }
        } catch (e: Exception) {
            // swap out println for KotlinLogging if available
            e.printStackTrace()
            NoneResult() to e
        }
    }

    data class AdventurerLogQueryResults(
        override val results: ResultSet?,
        private val username: String
    ) : SQLResults {
        val logEntries: List<AdventurerLogEntry> = buildList {
            if (results != null) {
                while (results.next()) {
                    add(
                        AdventurerLogEntry(
                            user = username,
                            icon = results.getString("icon"),
                            message = results.getString("message")
                                .replace("(<(img|col|shad)=.*?>)".toRegex(), ""),
                            date = Date(TimeUnit.SECONDS.toMillis(results.getLong("date")))
                        )
                    )
                }
            }
        }
    }
}
