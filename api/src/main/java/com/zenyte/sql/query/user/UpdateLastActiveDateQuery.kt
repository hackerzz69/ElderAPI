package com.zenyte.sql.query.user

import com.zenyte.sql.HikariPool
import com.zenyte.sql.NoneResult
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.util.concurrent.TimeUnit

/**
 * @author Corey
 * @since 18/02/20
 */
class UpdateLastActiveDateQuery(private val userId: Int, private val timestamp: Long = System.currentTimeMillis()) : SQLRunnable() {
    
    private val updateQuery = """
        UPDATE core_members
        SET last_activity = ?
        WHERE member_id = ?
    """.trimIndent()
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_forum").use { con ->
                con.prepareStatement(updateQuery).use {
                    it.setInt(1, TimeUnit.MILLISECONDS.toSeconds(timestamp).toInt())
                    it.setInt(2, userId)
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
