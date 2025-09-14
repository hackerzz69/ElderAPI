package com.zenyte.discord.listeners.command.impl

import com.zenyte.discord.getCommandArgs
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji

/**
 * @author Corey
 * @since 17/02/2020
 */
class PollCommand : Command {

    override val identifiers = arrayOf("poll")

    override val description = "Creates a poll and posts the given message."

    override fun execute(message: Message, identifier: String) {
        val pollText = message.getCommandArgs(identifier).trim()
        if (pollText.isEmpty()) {
            message.channel.sendMessage("⚠️ Please provide a question for the poll.").queue()
            return
        }

        // Delete the command message to keep channel clean
        message.delete().queue()

        // Post the poll and add 👍 / 👎 reactions
        message.channel.sendMessage(pollText).queue { msg ->
            msg.addReaction(Emoji.fromUnicode("👍")).queue()
            msg.addReaction(Emoji.fromUnicode("👎")).queue()
        }
    }
}
