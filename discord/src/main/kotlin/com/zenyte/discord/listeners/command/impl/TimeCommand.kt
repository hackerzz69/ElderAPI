package com.zenyte.discord.listeners.command.impl

import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Corey
 * @since 04/10/2020
 */
class TimeCommand : Command {
    
    override val identifiers = arrayOf("time")
    
    override val description = "Returns the current server time."
    
    override fun execute(message: Message, identifier: String) {
        val channel = message.channel
        
        val sdf = SimpleDateFormat("HH:mm:ss z - dd MMM YY")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        
        channel.sendMessage("${message.author.asMention} :alarm_clock: The current server time is `${sdf.format(Date())}`").queue()
    }
    
}
