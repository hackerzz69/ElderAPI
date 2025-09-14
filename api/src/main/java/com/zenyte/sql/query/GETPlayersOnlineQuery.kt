package com.zenyte.sql.query

import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import com.zenyte.sql.struct.World
import java.sql.ResultSet

class GetPlayersOnline(private val world: World) : SQLRunnable() {
    
    override fun execute(auth: DatabaseCredential): Pair<PlayersOnlineResult, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte").use { con ->
                con.prepareStatement("SELECT online FROM players_online WHERE world = ?").use { stmt ->
                    stmt.setString(1, world.name.toLowerCase())
                    stmt.execute()
                    return Pair(PlayersOnlineResult(stmt.resultSet), null)
                }
            }
        } catch (e: Exception) {
            return Pair(PlayersOnlineResult(null), e)
        }
    }
    
}

data class PlayersOnlineResult(override val results: ResultSet?, val amount: Int = results?.getInt(1) ?: 0) : SQLResults
