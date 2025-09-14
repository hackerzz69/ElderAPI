package com.zenyte.discord.listeners

import com.zenyte.discord.DiscordBot
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener

/**
 * @author Corey
 * @since 27/04/19
 */
class CommandListener : EventListener {
    
    companion object {
        const val COMMAND_PREFIX = "::"
    }
    
    override fun onEvent(event: GenericEvent) {
        if (event !is MessageReceivedEvent) {
            return
        }
        val message = event.message
        
        if (!isCommand(message)) {
            return
        }
        
        if (event.channelType == ChannelType.PRIVATE) {
            return
        }
        
        val identifier = getCommandContents(message).split(" ").first()
        
        val command = DiscordBot.getCommands().firstOrNull {
            val identifiers = it.identifiers.map { identifier -> identifier.toLowerCase() }.toList()
            identifiers.contains(identifier.toLowerCase())
        }
        
        command?.executeCommand(message, identifier)
    }
    
    private fun isCommand(message: Message): Boolean {
        if (!message.contentDisplay.startsWith(COMMAND_PREFIX)) {
            return false
        }
        return !message.author.isBot
    }
    
    private fun getCommandContents(message: Message) = message.contentDisplay.substring(COMMAND_PREFIX.length)
    
}