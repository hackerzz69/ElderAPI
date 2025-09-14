package com.zenyte.sql.query.user

import com.zenyte.api.model.PlayerInformation
import com.zenyte.sql.HikariPool
import com.zenyte.sql.NoneResult
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.Timestamp

/**
 * @author Corey
 * @since 23/06/19
 */
class SubmitPlayerInformationQuery(private val info: PlayerInformation) : SQLRunnable() {
    
    private val insertQuery = """INSERT INTO player_information (user_id, username, total_level, donator_role, ironman_mode, exp_mode)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            username = ?,
            total_level = ?,
            donator_role = ?,
            ironman_mode = ?,
            exp_mode = ?,
            last_active = ?"""
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_main").use { con ->
                con.prepareStatement(insertQuery).use {
                    // insert
                    it.setInt(1, info.userId)
                    it.setString(2, info.username)
                    it.setInt(3, info.totalLevel)
                    it.setString(4, info.donatorRole?.name)
                    it.setInt(5, info.ironmanMode.id)
                    it.setInt(6, info.expMode.index)
                    
                    // update
                    it.setString(7, info.username)
                    it.setInt(8, info.totalLevel)
                    it.setString(9, info.donatorRole?.name)
                    it.setInt(10, info.ironmanMode.id)
                    it.setInt(11, info.expMode.index)
                    it.setTimestamp(12, Timestamp(System.currentTimeMillis()))
    
                    it.execute()
                    return Pair(NoneResult(it.resultSet), null)
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(NoneResult(null), e)
        }
    }
    
}
