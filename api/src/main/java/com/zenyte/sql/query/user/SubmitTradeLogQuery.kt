package com.zenyte.sql.query.user

import com.zenyte.api.model.TradeLog
import com.zenyte.common.CommonConfig.gson
import com.zenyte.sql.HikariPool
import com.zenyte.sql.NoneResult
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential

/**
 * @author Corey
 * @since 13:53 - 21/07/2019
 */
class SubmitTradeLogQuery(private val transaction: TradeLog) : SQLRunnable() {
    
    private val columns = listOf("user", "user_ip", "given", "partner", "partner_ip", "received", "world")
    private val insertQuery = "INSERT INTO logs_trades (${columns.joinToString(",")}) VALUES (${columns.joinToString(",") { "?" }})"
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "elder_main").use { con ->
                con.prepareStatement(insertQuery).use {
                    it.setString(1, transaction.user)
                    it.setString(2, transaction.userIp)
                    it.setString(3, gson.toJson(transaction.given))
                    it.setString(4, transaction.partner)
                    it.setString(5, transaction.partnerIp)
                    it.setString(6, gson.toJson(transaction.received))
                    it.setInt(7, transaction.world)
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
