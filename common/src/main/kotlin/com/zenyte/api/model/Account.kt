package com.zenyte.api.model

import java.sql.Timestamp
import java.util.*

data class LoginRequest(val username: String, val password: String)

data class RegisterRequest(val username: String, val password: String, val ip: String)

data class SubmitGamelogRequest(val user: String, val icon: String, val message: String, val pvp: Boolean)

data class AdventurerLogEntry(val user: String, val icon: String, val message: String, val pvp: Boolean = false, val date: Date)

data class SubmitAwardRequest(val userId: Int, val award: Int)

data class ClaimBondRequest @JvmOverloads constructor(val userId: Int, val username: String, val bondType: Int, val ipAddress: String?, val token: Long? = null)

data class PlayerInformation @JvmOverloads constructor(val userId: Int,
                                                       val username: String,
                                                       val totalLevel: Int,
                                                       val donatorRole: Role?,
                                                       val ironmanMode: IronmanMode,
                                                       val expMode: ExpMode,
                                                       val lastActive: Timestamp? = null) {
    
    fun redisKey() = redisKey(username)
    
    companion object {
        fun redisKey(username: String) = "player_info:${username.toLowerCase()}"
    }
    
    init {
        if (donatorRole != null && !Role.DONATOR_ROLES.contains(donatorRole)) {
            throw IllegalArgumentException("'donatorRole' must be a donator role; given: ${donatorRole.name}")
        }
    }
    
}
