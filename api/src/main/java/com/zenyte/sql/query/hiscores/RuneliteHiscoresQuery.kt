package com.zenyte.sql.query.hiscores

import com.zenyte.api.model.ExpMode
import com.zenyte.api.model.IronmanMode
import com.zenyte.api.model.Skill
import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet

/**
 * @author Noele, Corey
 */
class RuneliteHiscoresQuery(private val username: String,
                            private val ironmanMode: IronmanMode? = null,
                            private val expMode: ExpMode? = null) : SQLRunnable() {
    
    val emptySet = """
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1, 1, 0
            -1,-1
            -1,-1
            -1,-1
            -1,-1
            -1,-1
            -1,-1
            -1,-1
            -1,-1
            -1,-1""".trimIndent()
    
    private val ironmanMarker = "{ironman}"
    private val expModeMarker = "{expMode}"
    
    private val query = """
        
                SELECT skill_id,
                       skill_name,
                       rank,
                       level,
                       experience
                       
                FROM   (SELECT ( @rank := @rank + 1 ) rank,
                               skill_id,
                               skill_name,
                               experience,
                               last_modified,
                               username,
                               level
                               
                        FROM   skill_hiscores A,
                               (SELECT @rank := 0) B

                        WHERE skill_name = ? $ironmanMarker $expModeMarker
                        
                        ORDER  BY experience DESC,
                                  last_modified ASC) skill_hiscores
                                  
                WHERE  username = ?
                ORDER  BY skill_id
                """.trimMargin()
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_main").use { con ->
                val responseBuilder = StringBuilder()
    
                var offset = 0
                var queryBuilder = query
    
                if (ironmanMode != null) {
                    queryBuilder = queryBuilder.replace(ironmanMarker, "AND mode = ?")
                    offset++
                } else {
                    queryBuilder = queryBuilder.replace(ironmanMarker, "")
                }
    
                if (expMode != null) {
                    queryBuilder = queryBuilder.replace(expModeMarker, "AND xp_mode = ?")
                    offset++
                } else {
                    queryBuilder = queryBuilder.replace(expModeMarker, "")
                }
    
                for (skill in Skill.VALUES) {
                    con.prepareStatement(queryBuilder).use { pst ->
                        pst.setString(1, skill.toString().toLowerCase())
            
                        if (ironmanMode != null) {
                            pst.setInt(2, ironmanMode.id)
                        }
            
                        if (expMode != null) {
                            pst.setInt(1 + offset, expMode.index)
                        }
            
                        pst.setString(2 + offset, username)
            
                        pst.execute()
            
                        if (!pst.resultSet.isBeforeFirst) {
                            // no results
                            return Pair(RuneliteHiscoreResult(pst.resultSet, null), null)
                        }
            
                        if (pst.resultSet.next()) {
                            val set = pst.resultSet.getInt("rank").toString()
                                    .plus(",")
                                    .plus(pst.resultSet.getInt("level").toString())
                                    .plus(",")
                                    .plus(pst.resultSet.getLong("experience").toString())
                                    .plus("\n")
                            responseBuilder.append(set)
                        }
                    }
                }
                
                responseBuilder.append("-1,-1\n".repeat(9))
                
                return Pair(RuneliteHiscoreResult(null, responseBuilder.toString()), null)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(RuneliteHiscoreResult(null, emptySet), e)
        }
        
    }
    
    data class RuneliteHiscoreResult(override val results: ResultSet?, val response: String?) : SQLResults
    
}