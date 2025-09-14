package com.zenyte.api.model

import java.sql.Timestamp

/**
 * @author Corey
 * @since 02/12/18
 */

data class ExpModeUpdate(val oldMode: ExpMode, val newMode: ExpMode, val gameMode: IronmanMode)

data class GameModeUpdate(val oldMode: IronmanMode, val newMode: IronmanMode, val expMode: ExpMode)

data class HiscoreByColumnEntry(val rank: Int,
                                val measurement: String,
                                val username: String,
                                val level: Int,
                                val experience: Long,
                                val mode: IronmanMode,
                                val expMode: ExpMode,
                                val lastModified: Timestamp)

data class HiscoresByColumn(val measurement: String, val page: Int, val results: List<HiscoreByColumnEntry>)

data class SkillHiscore(val userId: Int,
                        val username: String,
                        val mode: IronmanMode,
                        val expMode: ExpMode,
                        val skillId: Int,
                        val skillName: String,
                        val level: Int,
                        val experience: Long)

enum class Skill(val id: Int) {
    TOTAL(25),
    ATTACK(0),
    DEFENCE(1),
    STRENGTH(2),
    HITPOINTS(3),
    RANGED(4),
    PRAYER(5),
    MAGIC(6),
    COOKING(7),
    WOODCUTTING(8),
    FLETCHING(9),
    FISHING(10),
    FIREMAKING(11),
    CRAFTING(12),
    SMITHING(13),
    MINING(14),
    HERBLORE(15),
    AGILITY(16),
    THIEVING(17),
    SLAYER(18),
    FARMING(19),
    RUNECRAFTING(20),
    HUNTER(21),
    CONSTRUCTION(22);
    
    val formattedName = this.name.toLowerCase()
    
    companion object {
        @JvmField
        val VALUES = values().asList()
    
        @JvmField
        val VALUES_NO_TOTAL = values().filter { it.id != TOTAL.id }
    }
    
}