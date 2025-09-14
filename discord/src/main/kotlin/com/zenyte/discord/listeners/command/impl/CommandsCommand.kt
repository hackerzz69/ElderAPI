package com.zenyte.discord.listeners.command.impl

import com.zenyte.discord.DiscordBot
import com.zenyte.discord.listeners.CommandListener
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message

/**
 * @author Corey
 * @since 07/10/2018
 */
class CommandsCommand : Command {
    
    override val identifiers = arrayOf("help", "commands")
    
    override val description = "Displays this message."
    
    override fun execute(message: Message, identifier: String) {
        val sb = StringBuilder("```yml")
        sb.append("\n")
    
        for (cmd in DiscordBot.getCommands()) {
            if (cmd.description.isEmpty()) {
                continue
            }
        
            sb.append(CommandListener.COMMAND_PREFIX)
            sb.append(cmd.identifiers.joinToString("|"))
            sb.append(" - ").append(cmd.description)
            sb.append("\n")
        }
        
        sb.append("```")
        message.channel.sendMessage(sb.toString()).queue()
    }
    
}
