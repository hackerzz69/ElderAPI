package com.zenyte.discord.listeners.command.impl

import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message

/**
 * @author Corey
 * @since 07/10/2018
 */
class PingCommand : Command {
    
    override val identifiers = arrayOf("ping")
    
    override val description = "Returns a pong with the time taken to respond."
    
    override fun execute(message: Message, identifier: String) {
        val channel = message.channel
    
        val now = System.currentTimeMillis()
        val messageCreation = message.timeCreated.toInstant()
    
        val timeDifferenceInMilli = (now - messageCreation.toEpochMilli()).toInt()
        val memberTag = "<@" + message.author.idLong + ">"
    
        channel.sendMessage(memberTag + " :ping_pong: Pong! `" + timeDifferenceInMilli + "ms`").queue()
    }
    
}
