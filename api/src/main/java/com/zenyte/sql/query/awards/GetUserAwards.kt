package com.zenyte.sql.query.awards

import com.zenyte.api.model.Award
import com.zenyte.api.model.AwardUser
import com.zenyte.api.model.AwardedAward
import com.zenyte.api.model.UserAwards
import com.zenyte.common.stripHtml
import com.zenyte.sql.HikariPool
import com.zenyte.sql.NoneResult
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet
import java.util.*

/**
 * @author Corey
 * @since 25/07/2020
 */
class GetUserAwards(private val username: String) : SQLRunnable() {
    
    private val selectQuery = """
(SELECT core_members.member_id as userId,
        core_members.name      as username,
        -1                     as id,
        ""                     as reason,
        0                      as date,
        -1                     as awardId,
        ""                     as awardTitle,
        ""                     as awardIcon,
        ""                     as awardDescription,
        -1                     as giverId,
        ""                     as giverUsername

 FROM core_members

 WHERE name = ?)

UNION

(SELECT core_members.member_id                 as userId,
        core_members.name                      as username,

        awards_awarded.awarded_id              as id,
        awards_awarded.awarded_reason          as reason,
        awards_awarded.awarded_date            as date,

        awards_awarded.awarded_award           as awardId,
        awards_awards.award_title              as awardTitle,
        awards_awards.award_icon               as awardIcon,
        awards_awards.award_desc               as awardDescription,

        awards_awarded.awarded_giver           as giverId,
        (SELECT core_members.name
         FROM core_members,
              awards_awarded
         WHERE core_members.member_id = awards_awarded.awarded_giver
           AND awards_awarded.awarded_id = id) as giverUsername

 FROM core_members,
      awards_awarded,
      awards_awards

 WHERE name = ?
   AND awards_awarded.awarded_member = core_members.member_id
   AND awards_awards.award_id = awards_awarded.awarded_award
 ORDER BY date)
 """.trimIndent()
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "elder_forum").use { con ->
                con.prepareStatement(selectQuery).use {
                    it.setString(1, username)
                    it.setString(2, username)
                    
                    return GetUserAwardsResults(it.executeQuery()) to null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return NoneResult() to e
        }
    }
    
    data class GetUserAwardsResults(override val results: ResultSet?) : SQLResults {
        val awards: UserAwards?
        
        init {
            if (results != null) {
                val user = if (results.next()) AwardUser(results.getInt("userId"), results.getString("username")) else null
                val userAwards = mutableListOf<AwardedAward>()
                
                while (results.next()) {
                    userAwards.add(AwardedAward(
                            id = results.getInt("id"),
                            date = Date(results.getLong("date") * 1000),
                            awardedBy = AwardUser(results.getInt("giverId"), results.getString("giverUsername")),
                            reason = results.getString("reason").stripHtml(),
                            award = Award(
                                    results.getInt("awardId"),
                                    results.getString("awardTitle"),
                                    results.getString("awardIcon"),
                                    results.getString("awardDescription").stripHtml()
                            )
                    ))
                }
                
                awards = if (user == null) {
                    null
                } else {
                    UserAwards(user, userAwards)
                }
            } else {
                awards = null
            }
        }
        
    }
    
}