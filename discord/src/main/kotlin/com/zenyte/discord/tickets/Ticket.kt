package com.zenyte.discord.tickets

import com.zenyte.common.generateRandomString
import com.zenyte.discord.DiscordBot

/**
 * @author Corey
 * @since 08/10/2019
 */
data class Ticket(val id: String = generateRandomString(length = 5), var adminRequested: Boolean, val creatorUserId: Long, val query: String) {
    
    fun channelTitle() = "${if (adminRequested) "admin-" else ""}ticket-$id"
    
    fun channelTopic() = DiscordBot.gson.toJson(this)
    
}