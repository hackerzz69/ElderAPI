package com.zenyte.sql.query.hiscores

import com.zenyte.api.model.SkillHiscore
import com.zenyte.sql.HikariPool
import com.zenyte.sql.NoneResult
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import com.zenyte.util.DatabaseUtil

/**
 * @author Corey
 * @since 09/12/18
 */
class UpdateSingleHiscore(private val userId: Int,
                          private val gameMode: Int,
                          private val expMode: Int,
                          private val displayName: String,
                          private val rowsToUpdate: List<SkillHiscore>) : SQLRunnable() {
    
    override fun execute(auth: DatabaseCredential): Pair<NoneResult, Exception?> {
        if (rowsToUpdate.isEmpty()) {
            throw RuntimeException("Cannot update zero rows (user=[$userId, $displayName], mode=$gameMode)")
        }
        
        val skillsToDelete = rowsToUpdate.map { it.skillId }
        val deleteQuery = "DELETE FROM skill_hiscores " +
                "WHERE userid=? " +
                "AND mode=? " +
                "AND skill_id IN (${skillsToDelete.joinToString(",") { "?" }})"
    
        val insertQuery = DatabaseUtil.buildBatch("INSERT INTO skill_hiscores (userid, username, mode, xp_mode, skill_id, skill_name, level, experience) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", rowsToUpdate.size, 8)
        
        try {
            HikariPool.getConnection(auth, "elder_main").use { con ->
                con.prepareStatement(deleteQuery).use {
                    it.setInt(1, userId)
                    it.setInt(2, gameMode)
                    skillsToDelete.forEachIndexed { index, skill ->
                        it.setInt(index + 3, skill)
                    }
                    it.execute()
                }
                
                con.prepareStatement(insertQuery).use {
                    var index = 0
                    
                    rowsToUpdate.forEach { skill ->
                        it.setInt(++index, userId)
                        it.setString(++index, displayName)
                        it.setInt(++index, gameMode)
                        it.setInt(++index, expMode)
                        it.setInt(++index, skill.skillId)
                        it.setString(++index, skill.skillName)
                        it.setInt(++index, skill.level)
                        it.setLong(++index, skill.experience)
                    }
                    
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
