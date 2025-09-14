package com.zenyte.api.model

import java.util.*

/**
 * @author Corey
 * @since 25/07/2020
 */

data class Award(
        val id: Int,
        val title: String,
        val icon: String,
        val description: String
)

data class AwardUser(
        val id: Int,
        val name: String
)

data class AwardedAward(
        val id: Int,
        val date: Date,
        val awardedBy: AwardUser,
        val reason: String,
        val award: Award
)

data class UserAwards(
        val user: AwardUser,
        val awards: List<AwardedAward>
)
