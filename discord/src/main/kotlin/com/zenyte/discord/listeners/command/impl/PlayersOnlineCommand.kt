package com.zenyte.discord.listeners.command.impl

import com.zenyte.common.WorldInfo
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message

/**
 * @author Corey
 * @since 01/05/19
 */
class PlayersOnlineCommand : Command {
    
    override val identifiers = arrayOf("players")
    
    override val description = "Gets the current total player count."
    
    override fun execute(message: Message, identifier: String) {
        val channel = message.channel
    
        when (val players = WorldInfo.getTotalPlayerCount()) {
            0 -> channel.sendMessage("There are currently no players online.").queue()
            1 -> channel.sendMessage("There is currently $players player online.").queue()
            else -> channel.sendMessage("There are currently $players players online.").queue()
        }
    
    }
    
}