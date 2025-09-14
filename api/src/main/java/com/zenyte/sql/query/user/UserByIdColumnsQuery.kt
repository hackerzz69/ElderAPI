package com.zenyte.sql.query.user

import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet

/**
 * @author Corey
 * @since 30/05/19
 */
class UserByIdColumnsQuery(private val memberId: Int, private val columns: Array<String>) : SQLRunnable() {
    
    private val query = "SELECT ${columns.joinToString(", ")} FROM core_members WHERE member_id = ?"
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "elder_forum").use { con ->
                con.prepareStatement(query).use {
                    it.setInt(1, memberId)
                    return Pair(UserColumnResults(it.executeQuery()), null)
                }
            }
        } catch (e: Exception) {
            return Pair(UserColumnResults(null), e)
        }
    }
    
    data class UserColumnResults(override val results: ResultSet?) : SQLResults {
        val queryResults = HashMap<String, String>()
        
        init {
            results?.let {
                val resultsMeta = it.metaData
                while (it.next()) {
                    for (columnIndex in 1..resultsMeta.columnCount) {
                        val column = resultsMeta.getColumnName(columnIndex)
                        val value = it.getString(columnIndex)
    
                        if (column.equals("mfa_details", true)) {
                            val mfaEnabled = !value.isNullOrBlank() && !value.equals("null", true) && value != "[]"
                            queryResults[column] = if (mfaEnabled) "enabled" else "disabled"
                        } else {
                            queryResults[column] = value
                        }
                    }
                }
            }
        }
    }
    
}
