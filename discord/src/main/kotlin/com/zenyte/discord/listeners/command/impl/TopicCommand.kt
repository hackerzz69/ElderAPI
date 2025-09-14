package com.zenyte.discord.listeners.command.impl

import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
class TopicCommand : Command {

    override val identifiers = arrayOf("topic")

    override val description =
        "This will return a topic with the id you enter from the forums (usage: ::topic 73)"

    override fun execute(message: Message, identifier: String) {
        val channel = message.channel
        val memberTag = message.author.asMention

        val args = message.contentDisplay.trim().split("\\s+".toRegex(), 2)

        if (args.size < 2) {
            channel.sendMessage("Usage: ::topic <id>").queue()
            return
        }

        try {
            val topicId = args[1].toInt()
            channel.sendMessage("Here you go, $memberTag!\nhttps://forums.zenyte.com/topic/$topicId-undefined").queue()
        } catch (e: NumberFormatException) {
            channel.sendMessage("Sorry, $memberTag, that’s not a valid topic ID!").queue()
        }
    }
}
