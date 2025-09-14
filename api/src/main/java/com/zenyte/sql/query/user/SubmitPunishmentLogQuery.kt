package com.zenyte.sql.query.user

import com.zenyte.api.model.PunishmentLog
import com.zenyte.sql.HikariPool
import com.zenyte.sql.NoneResult
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential

/**
 * @author Corey
 * @since 19/06/19
 */
class SubmitPunishmentLogQuery(private val punishment: PunishmentLog) : SQLRunnable() {
    
    private val insertQuery = "INSERT INTO actions (mod_name, offender, action_type, expires, reason, ip_address, mac_address) VALUES (?, ?, ?, ?, ?, ?, ?)"
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "elder_main").use { con ->
                con.prepareStatement(insertQuery).use {
                    it.setString(1, punishment.modName)
                    it.setString(2, punishment.offender)
                    it.setString(3, punishment.actionType)
                    it.setString(4, punishment.expires)
                    it.setString(5, punishment.reason)
                    it.setString(6, punishment.ipAddress)
                    it.setString(7, punishment.macAddress)
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
