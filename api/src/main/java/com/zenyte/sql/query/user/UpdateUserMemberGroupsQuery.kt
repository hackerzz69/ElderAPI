package com.zenyte.sql.query.user

import com.zenyte.sql.HikariPool
import com.zenyte.sql.NoneResult
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential

/**
 * @author Corey
 * @since 23:07 - 24/06/2019
 */
class UpdateUserMemberGroupsQuery(private val userId: Int,
                                  private val primaryGroupId: String,
                                  private val secondaryMemberGroups: String) : SQLRunnable() {
    
    override fun execute(auth: DatabaseCredential): Pair<NoneResult, Exception?> {
        val updateQuery = "UPDATE core_members SET member_group_id = ?, mgroup_others = ? WHERE member_id = ?"
        
        try {
            HikariPool.getConnection(auth, "elder_forum").use { con ->
                con.prepareStatement(updateQuery).use {
                    it.setString(1, primaryGroupId)
                    it.setString(2, secondaryMemberGroups)
                    it.setInt(3, userId)
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