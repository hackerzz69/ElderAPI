package com.zenyte.discord.listeners.command.impl

import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
class TopicCommand : Command {
    
    override val identifiers = arrayOf("topic")
    
    override val description = "This will return a topic with the id you enter from the forums (usage: ::topic 73)"
    
    override fun execute(message: Message, identifier: String) {
        val channel = message.channel
        val body = message.contentDisplay
        val memberTag = message.author.asMention
    
        try {
            val topicId = Integer.parseInt(body.substring(8))
            channel.sendMessage("Here you go, $memberTag!\nhttps://forums.zenyte.com/topic/$topicId-undefined").queue()
        } catch (e: NumberFormatException) {
            channel.sendMessage("Sorry, $memberTag, we couldn't find that topic!").queue()
        }
    }
}
