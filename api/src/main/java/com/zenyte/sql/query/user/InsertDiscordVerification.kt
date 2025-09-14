package com.zenyte.sql.query.user

import com.zenyte.sql.HikariPool
import com.zenyte.sql.NoneResult
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential

class InsertDiscordVerification(private val discordId: Long, private val memberId: Int) : SQLRunnable() {
    
    private val insertQuery = "INSERT INTO discord_verifications (discord_id, member_id) VALUES (?, ?)"
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_main").use { con ->
                con.prepareStatement(insertQuery).use { pst ->
                    pst.setLong(1, discordId)
                    pst.setInt(2, memberId)
                    pst.execute()
                    return Pair(NoneResult(pst.resultSet), null)
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(NoneResult(null), e)
        }
    }
    
}
