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
        val sb = StringBuilder("```yml\n")

        for (cmd in DiscordBot.getCommands()) {
            if (cmd.description.isBlank()) continue

            sb.append(CommandListener.COMMAND_PREFIX)
                .append(cmd.identifiers.joinToString("|"))
                .append(" - ")
                .append(cmd.description)
                .append("\n")
        }

        sb.append("```")

        message.channel.sendMessage(sb.toString()).queue()
    }
}
