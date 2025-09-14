package com.zenyte.api.model

import java.sql.Timestamp

enum class WorldType(val mask: Int, val override: Boolean = false) {
    FREE(0),
    MEMBERS(1),
    PVP(1 shl 2),
    BOUNTY(1 shl 5),
    SKILL_TOTAL(1 shl 7),
    PVP_HIGH_RISK(1 shl 10),
    LAST_MAN_STANDING(1 shl 14),
    BETA(1 shl 25),
    DEADMAN_TOURNAMENT(1 shl 26),
    DEADMAN(1 shl 29),
    SEASONAL_DEADMAN(1 shl 30),
    
    // custom types
    TOURNAMENT(1 shl 29)
}

enum class WorldLocation(val id: Int) {
    UNITED_STATES(0),
    UNITED_KINGDOM(1),
    CANADA(2),
    AUSTRALIA(3),
    NETHERLANDS(4),
    SWEDEN(5),
    FINLAND(6),
    GERMANY(7)
}

data class World(val id: Int,
                 val name: String,
                 val address: String,
                 val uptime: Int,
                 val activity: String,
                 val playerCount: Int,
                 val playersOnline: List<String>,
                 val flags: List<WorldType>,
                 val location: WorldLocation)

data class WorldEvent(
        val id: Int,
        val world: String,
        val type: String,
        val title: String,
        val data: String,
        val time: Timestamp
)