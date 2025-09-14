package com.zenyte.sql.query.game

import com.zenyte.api.model.WorldEvent
import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet
import java.util.*
import kotlin.collections.ArrayList

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
class WorldEventsQuery(private val worldName: String): SQLRunnable() {

    val FETCH_QUERY = "SELECT * FROM promotions WHERE world = ?"
    val EMPTY_RESULT = WorldEventResult(null, null) to null

    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_main").use { con ->
                con.prepareStatement(FETCH_QUERY).use { stmt ->
                    stmt.setString(1, worldName)
                    stmt.execute()

                    val list = ArrayList<WorldEvent>()

                    while(stmt.resultSet.next()) {
                        val id = stmt.resultSet.getInt(stmt.resultSet.findColumn("id"))
                        val world = stmt.resultSet.getString(stmt.resultSet.findColumn("world"))
                        val type = stmt.resultSet.getString(stmt.resultSet.findColumn("type"))
                        val title = stmt.resultSet.getString(stmt.resultSet.findColumn("title"))
                        val data = stmt.resultSet.getString(stmt.resultSet.findColumn("data"))
                        val time = stmt.resultSet.getTimestamp(stmt.resultSet.findColumn("time"))

                        if(time.before(Date())) {
                            continue
                        }

                        list.add(WorldEvent(id, world, type, title, data, time))
                    }

                    return WorldEventResult(stmt.resultSet, list) to null
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            return EMPTY_RESULT
        }
    }

    data class WorldEventResult(override val results: ResultSet?, val events: ArrayList<WorldEvent>?): SQLResults
}