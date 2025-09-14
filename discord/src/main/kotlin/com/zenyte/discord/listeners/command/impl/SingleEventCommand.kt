package com.zenyte.discord.listeners.command.impl

import com.zenyte.discord.getCommandArgs
import com.zenyte.discord.ipb.Calendar
import com.zenyte.discord.ipb.Calendar.asEmbed
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import java.awt.Color
import java.time.Instant

/**
 * @author Corey
 * @since 03/10/2020
 */
class SingleEventCommand : Command {
    
    override val identifiers = arrayOf("event")
    
    override val description = "Get more information about a particular event."
    
    override fun execute(message: Message, identifier: String) {
        val channel = message.channel
        
        val embed = EmbedBuilder()
                .setColor(15837287)
                .setTimestamp(Instant.now())
                .setTitle("Zenyte Event Calendar", "https://forums.zenyte.com/calendar/1-community-calendar/")
                .setFooter("Zenyte Events", "https://cdn.zenyte.com/zenyte.png")
        
        channel.sendMessage(embed.setDescription("Fetching current events...").build()).queue {
            try {
                val eventId = Integer.parseInt(message.getCommandArgs(identifier))
                val events = Calendar.events()
                val event = events.results.firstOrNull { it.id == eventId } ?: throw Exception()
    
                it.editMessage(event.asEmbed().build()).queue()
            } catch (e: Exception) {
                it.editMessage(embed.setDescription("Sorry, ${message.author.asMention}, we couldn't find that event!").setColor(Color.RED).build()).queue()
            }
        }
        
    }
    
}
