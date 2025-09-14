package com.zenyte.discord.listeners.command.impl

import com.zenyte.discord.getCommandArgs
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message

/**
 * @author Corey
 * @since 17/02/2020
 */
class PollCommand : Command {
    
    override val identifiers = arrayOf("poll")
    
    override val description = "Creates a poll and posts the given message."
    
    override fun execute(message: Message, identifier: String) {
        message.delete().queue()
        message.channel.sendMessage(message.getCommandArgs(identifier)).queue { msg ->
            msg.addReaction("\uD83D\uDC4D").queue {
                msg.addReaction("\uD83D\uDC4E").queue()
            }
        }
    }
    
}