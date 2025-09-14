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
            .setColor(0xF1A2A7) // same numeric color, clearer hex format
            .setTimestamp(Instant.now())
            .setTitle("Zenyte Event Calendar", "https://forums.zenyte.com/calendar/1-community-calendar/")
            .setFooter("Zenyte Events", "https://cdn.zenyte.com/zenyte.png")

        // Send placeholder "fetching" message first
        channel.sendMessageEmbeds(embed.setDescription("Fetching current events...").build()).queue { msg ->
            try {
                val eventId = message.getCommandArgs(identifier).toInt()
                val events = Calendar.events()
                val event = events.results.firstOrNull { it.id == eventId } ?: throw Exception()

                msg.editMessageEmbeds(event.asEmbed().build()).queue()
            } catch (e: Exception) {
                msg.editMessageEmbeds(
                    embed.setDescription("Sorry, ${message.author.asMention}, we couldn't find that event!")
                        .setColor(Color.RED)
                        .build()
                ).queue()
            }
        }
    }
}
