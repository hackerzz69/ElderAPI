package com.zenyte.discord.tickets

import com.zenyte.api.model.Role
import com.zenyte.common.getFriendlyTimeSince
import com.zenyte.discord.DiscordBot
import com.zenyte.discord.asJDARole
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * @author Corey
 * @since 08/10/2019
 */
object TicketManager {
    
    const val SUPPORT_CATEGORY = 631133209312362515
    const val TICKET_COMMANDS_CHANNEL = 633394383038971924
    
    private const val STAFF_TEXT_CHANNEL = 402793610007019521
    private const val ADMINISTRATOR_TEXT_CHANNEL = 426711604705624074
    
    const val TICKET_BAN_ROLE_ID = 631136118145941525
    
    fun create(ticket: Ticket) {
        val creator = DiscordBot.getZenyteGuild().getMemberById(ticket.creatorUserId)!!
    
        DiscordBot.getZenyteGuild().createTextChannel(ticket.channelTitle())
                .setTopic(ticket.channelTopic())
                .setParent(DiscordBot.getZenyteGuild().getCategoryById(SUPPORT_CATEGORY))
                .queue {
                    if (ticket.adminRequested) {
                        it.putPermissionOverride(Role.STAFF.asJDARole()!!)
                                .setDeny(Permission.MESSAGE_READ)
                                .queue()
                        it.putPermissionOverride(Role.ADMINISTRATOR.asJDARole()!!)
                                .setAllow(Permission.MESSAGE_READ)
                                .queue()
                    }
                    
                    it.addMemberToChannel(creator)
                    
                    it.sendMessage("${creator.asMention} has requested support!")
                            .append("\nQuery: `${ticket.query}`")
                            .queue()
                }
    }
    
    fun resolve(channel: TextChannel, resolver: Member, resolveReason: String): Boolean {
        val ticket = ticketFromChannel(channel) ?: return false
        val staffChannel = if (ticket.adminRequested) ADMINISTRATOR_TEXT_CHANNEL else STAFF_TEXT_CHANNEL
    
        DiscordBot.getZenyteGuild().getTextChannelById(staffChannel)
                ?.sendMessage(EmbedBuilder()
                        .setDescription("Resolved ticket: ${ticket.id}")
                        .setColor(15837287)
                        .setTimestamp(Instant.now())
                        .addField("Created By", "<@${ticket.creatorUserId}>", true)
                        .addField("Closed By", resolver.asMention, true)
                        .addField("Admin Requested", ticket.adminRequested.toString(), true)
                        .addField("Duration", getFriendlyTimeSince(channel.timeCreated.toInstant()), true)
                        .addField("Query", "`${ticket.query}`", true)
                        .addField("Resolve Reason", "`$resolveReason`", false)
                        .build())
                ?.addFile(transcriptFromChannel(channel).toByteArray(), "transcript_${ticket.id}.txt")
                ?.queue()
    
        channel.delete().queue {
            DiscordBot.getZenyteGuild().getMemberById(ticket.creatorUserId)?.user?.openPrivateChannel()?.queue {
                val sb = StringBuilder("Hi there,\n")
            
                sb.append("Your ticket was resolved; response:")
                        .append("\n")
                        .append("> $resolveReason")
                        .append("\n")
                        .append("Original query:")
                        .append("\n")
                        .append("> ${ticket.query}")
            
                it.sendMessage(sb.toString()).queue(null) {
                    DiscordBot.getZenyteGuild().getTextChannelById(staffChannel)?.sendMessage("Failed to send ticket closed confirmation message!")?.queue()
                }
            }
            
        }
        return true
    }
    
    fun downgrade(channel: TextChannel, downgrader: Member): Boolean {
        val ticket = ticketFromChannel(channel) ?: return false
        
        if (!ticket.adminRequested) {
            channel.sendMessage("Channel is already at the lowest possible level!").queue()
            return true
        }
        
        ticket.adminRequested = false
        
        channel.manager.setName(ticket.channelTitle())
                .setTopic(ticket.channelTopic())
                .putPermissionOverride(Role.STAFF.asJDARole()!!, Permission.MESSAGE_READ.rawValue, 0L)
                .queue()
    
        channel.sendMessage("This ticket has been downgraded by ${downgrader.asMention}")
                .append("\n")
                .append("Original query: `${ticket.query}`")
                .queue()
        
        return true
    }
    
    fun TextChannel.addMemberToChannel(member: Member) {
        this.putPermissionOverride(member)
                .setAllow(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)
                .queue()
    }
    
    private fun ticketFromChannel(channel: TextChannel): Ticket? {
        val topic = channel.topic ?: return null
        if (topic.trim().isEmpty()) {
            return null
        }
        if (channel.parent?.idLong != SUPPORT_CATEGORY) {
            return null
        }
        return try {
            DiscordBot.gson.fromJson(topic, Ticket::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun transcriptFromChannel(channel: MessageChannel): String {
        val sb = StringBuilder()
        
        channel.iterableHistory.sortedBy { it.timeCreated }
                .forEach { msg ->
                    sb.append("[")
                            .append(msg.timeCreated.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)))
                            .append("]")
                            .append(" ")
                            .append(msg.author.name)
                            .append(": ")
                            .appendln(msg.contentDisplay)
                    
                    if (!msg.attachments.isNullOrEmpty()) {
                        msg.attachments.forEach {
                            sb.appendln("${msg.author.name} sent a file: ${it.proxyUrl}")
                        }
                    }
                }
        
        return sb.toString()
    }
    
}
