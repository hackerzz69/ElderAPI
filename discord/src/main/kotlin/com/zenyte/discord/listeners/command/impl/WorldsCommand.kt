package com.zenyte.discord.listeners.command.impl

import com.zenyte.common.WorldInfo
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import java.time.Instant

/**
 * @author Corey
 * @since 01/05/19
 */
class WorldsCommand : Command {
    
    override val identifiers = arrayOf("worlds")
    
    override val description = "Gets current world info."
    
    override fun execute(message: Message, identifier: String) {
        val channel = message.channel
        val worlds = WorldInfo.getAllWorlds()
    
        if (worlds.isEmpty()) {
            channel.sendMessage("No world info currently available").queue()
            return
        }
    
        val embed = EmbedBuilder()
                .setColor(750130)
                .setTimestamp(Instant.now())
                .setFooter("Elvarg Worlds", "https://cdn.discordapp.com/attachments/1084338151167905946/1146438508374917161/ElvarglogoNew.png")
                .setThumbnail("https://zenyte.com/img/world_map_icon.png")
    
        worlds.sortedBy { it.id }
                .forEach {
                    embed.addField("World ${it.id}", "**Activity**: ${it.activity}\n**Players**: ${it.playerCount}\n**Uptime**: ${it.uptime.formatDuration()}\n\u200B", true)
                }
    
        val totalPlayersOnline = when (val players = worlds.map { it.playerCount }.sum()) {
            0 -> "There are currently no players online."
            1 -> "There is currently $players player online."
            else -> "There are currently $players players online."
        }
    
        val messageBuilder = MessageBuilder()
                .setContent(totalPlayersOnline)
                .setEmbed(embed.build())
    
        channel.sendMessage(messageBuilder.build()).queue()
    }
    
    private fun Int.formatDuration(): String {
        val sb = StringBuffer()
        var diffInSeconds = this
        
        val sec = diffInSeconds % 60
        
        diffInSeconds /= 60
        val min = diffInSeconds % 60
        
        diffInSeconds /= 60
        val hrs = diffInSeconds % 24
        
        diffInSeconds /= 24
        val days = diffInSeconds % 30
        
        diffInSeconds /= 30
        val months = diffInSeconds % 12
        
        diffInSeconds /= 12
        val years = diffInSeconds % 12
        
        if (years > 0) {
            sb.append("${years}Y")
            if (years <= 6 && months > 0) {
                sb.append(" and ${months}M")
            }
        } else if (months > 0) {
            sb.append("${months}M")
            if (months <= 6 && days > 0) {
                sb.append(" and ${days}d")
            }
        } else if (days > 0) {
            sb.append("${days}d")
            if (days <= 3 && hrs > 0) {
                sb.append(" and ${hrs}h")
            }
        } else if (hrs > 0) {
            sb.append("${hrs}h")
            if (min > 1) {
                sb.append(" and ${min}min")
            }
        } else if (min > 0) {
            sb.append("${min}min")
            if (sec > 1) {
                sb.append(" and ${sec}s")
            }
        } else {
            sb.append("about ${sec}s")
        }
        
        return sb.toString().trim()
    }
    
}