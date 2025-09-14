package com.zenyte.sql.query.user

import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet
import java.util.*

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
class ForumJoinDateQuery(private val user: String) : SQLRunnable() {

    val query = "SELECT joined FROM core_members WHERE name = ?"

    val EMPTY_RESULT = ForumJoinDateResult(null, "") to null

    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_forum").use { con ->
                con.prepareStatement(query).use {
                    it.setString(1, user)
                    it.execute()

                    if(it.resultSet.next()) {
                        val joinSecs = it.resultSet.getLong("joined")
                        val joinDate = Date(joinSecs * 1000)
                        return Pair(ForumJoinDateResult(it.resultSet, joinDate.toString()), null)
                    }

                    return EMPTY_RESULT
                }
            }
        } catch (e: Exception) {
            return EMPTY_RESULT
        }
    }

    data class ForumJoinDateResult(override val results: ResultSet?, val date: String) : SQLResults
}