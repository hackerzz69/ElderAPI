package com.zenyte.sql.query.user

import com.zenyte.api.model.ExpMode
import com.zenyte.api.model.IronmanMode
import com.zenyte.api.model.PlayerInformation
import com.zenyte.api.model.Role
import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet

/**
 * @author Corey
 * @since 21:10 - 23/06/2019
 */
class SelectPlayerInformationQuery(private val username: String) : SQLRunnable() {
    
    private val selectQuery = "SELECT * from player_information WHERE username = ?"
    
    override fun execute(auth: DatabaseCredential): Pair<SelectPlayerInformationResult, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_main").use { con ->
                con.prepareStatement(selectQuery).use {
                    it.setString(1, username)
                    return Pair(SelectPlayerInformationResult(it.executeQuery()), null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(SelectPlayerInformationResult(null), e)
        }
    }
    
    data class SelectPlayerInformationResult(override val results: ResultSet?) : SQLResults {
        val info: PlayerInformation?
        
        init {
            if (results != null) {
                if (!results.next()) {
                    info = null
                } else {
                    info = PlayerInformation(
                            results.getInt("user_id"),
                            results.getString("username"),
                            results.getInt("total_level"),
                            Role.DONATOR_ROLES.firstOrNull { it.name == results.getString("donator_role") },
                            IronmanMode.VALUES.first { it.id == results.getInt("ironman_mode") },
                            ExpMode.VALUES.first { it.index == results.getInt("exp_mode") },
                            results.getTimestamp("last_active")
                    )
                }
            } else {
                info = null
            }
        }
    }
    
}
