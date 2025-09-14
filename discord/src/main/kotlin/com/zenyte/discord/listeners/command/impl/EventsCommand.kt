package com.zenyte.discord.listeners.command.impl

import com.zenyte.common.getFriendlyTimeSince
import com.zenyte.common.getFriendlyTimeUntil
import com.zenyte.discord.ipb.Calendar
import com.zenyte.discord.ipb.Calendar.formatted
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import java.time.Instant
import java.util.*

/**
 * @author Corey
 * @since 03/10/2020
 */
class EventsCommand : Command {
    
    override val identifiers = arrayOf("events")
    
    override val description = "Returns a list of current or upcoming events."
    
    override fun execute(message: Message, identifier: String) {
        val channel = message.channel
        
        val embed = EmbedBuilder()
                .setColor(15837287)
                .setTimestamp(Instant.now())
                .setTitle("Elvarg Event Calendar", "https://playelvarg.com/")
                .setFooter("Elvarg Events", "https://cdn.discordapp.com/attachments/1084338151167905946/1146438508374917161/ElvarglogoNew.png")
        
        channel.sendMessage(embed.setDescription("Fetching current events...").build()).queue {
            val events = Calendar.events()
            val now = Date()
            
            val relevantEvents = events.results.filter {
                it.start.after(now) || it.end?.after(now) ?: false
            }.sortedBy { it.start }.take(10)
            
            embed.setDescription("Found ${relevantEvents.size} events!")
            
            relevantEvents.forEach { event ->
                val title = if (event.start.before(now)) "${event.id} - ${event.title} - Happening now!" else "${event.id} - ${event.title}"
    
                val starts = StringBuilder()
    
                if (event.start.before(now)) {
                    starts.append("Started ${getFriendlyTimeSince(event.start.toInstant())} ago")
                    if (event.end != null) {
                        starts.append("\n").append("Finishes in ${getFriendlyTimeUntil(event.end!!.toInstant())}")
                    }
                } else {
                    starts.append("Starts in ${getFriendlyTimeUntil(event.start.toInstant())}\n${event.start.formatted()}")
                }
    
                embed.addField(title, starts.toString(), false)
            }
            
            it.editMessage(embed.build()).queue()
        }
        
    }
    
}
