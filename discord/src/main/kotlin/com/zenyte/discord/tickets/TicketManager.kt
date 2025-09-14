package com.zenyte.discord.tickets

import com.zenyte.api.model.Role
import com.zenyte.common.getFriendlyTimeSince
import com.zenyte.discord.DiscordBot
import com.zenyte.discord.asJDARole
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import net.dv8tion.jda.api.utils.FileUpload


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
        val guild = DiscordBot.getZenyteGuild()
        val creator = guild.getMemberById(ticket.creatorUserId) ?: return

        guild.createTextChannel(ticket.channelTitle())
            .setTopic(ticket.channelTopic())
            .setParent(guild.getCategoryById(SUPPORT_CATEGORY))
            .queue { channel ->
                if (ticket.adminRequested) {
                    Role.STAFF.asJDARole()?.let {
                        channel.upsertPermissionOverride(it)
                            .deny(Permission.VIEW_CHANNEL)
                            .queue()
                    }
                    Role.ADMINISTRATOR.asJDARole()?.let {
                        channel.upsertPermissionOverride(it)
                            .grant(Permission.VIEW_CHANNEL)
                            .queue()
                    }
                }

                channel.addMemberToChannel(creator)

                channel.sendMessage("${creator.asMention} has requested support!\nQuery: `${ticket.query}`")
                    .queue()
            }
    }

    fun resolve(channel: TextChannel, resolver: Member, resolveReason: String): Boolean {
        val ticket = ticketFromChannel(channel) ?: return false
        val staffChannelId = if (ticket.adminRequested) ADMINISTRATOR_TEXT_CHANNEL else STAFF_TEXT_CHANNEL

        DiscordBot.getZenyteGuild().getTextChannelById(staffChannelId)
            ?.sendMessageEmbeds(
                EmbedBuilder()
                    .setDescription("Resolved ticket: ${ticket.id}")
                    .setColor(0xF1C40F) // gold-ish
                    .setTimestamp(Instant.now())
                    .addField("Created By", "<@${ticket.creatorUserId}>", true)
                    .addField("Closed By", resolver.asMention, true)
                    .addField("Admin Requested", ticket.adminRequested.toString(), true)
                    .addField("Duration", getFriendlyTimeSince(channel.timeCreated.toInstant()), true)
                    .addField("Query", "`${ticket.query}`", true)
                    .addField("Resolve Reason", "`$resolveReason`", false)
                    .build()
            )
            ?.addFiles(FileUpload.fromData(transcriptFromChannel(channel).toByteArray(), "transcript_${ticket.id}.txt"))
            ?.queue()

        channel.delete().queue {
            DiscordBot.getZenyteGuild().getMemberById(ticket.creatorUserId)?.user?.openPrivateChannel()?.queue { pm ->
                val sb = buildString {
                    append("Hi there,\n")
                    append("Your ticket was resolved; response:\n")
                    append("> $resolveReason\n")
                    append("Original query:\n")
                    append("> ${ticket.query}")
                }

                pm.sendMessage(sb).queue(null) {
                    DiscordBot.getZenyteGuild()
                        .getTextChannelById(staffChannelId)
                        ?.sendMessage("⚠️ Failed to send ticket closed confirmation message!")
                        ?.queue()
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

        channel.manager
            .setName(ticket.channelTitle())
            .setTopic(ticket.channelTopic())
            .queue()

        Role.STAFF.asJDARole()?.let {
            channel.upsertPermissionOverride(it)
                .grant(Permission.VIEW_CHANNEL)
                .queue()
        }

        channel.sendMessage("This ticket has been downgraded by ${downgrader.asMention}\nOriginal query: `${ticket.query}`")
            .queue()

        return true
    }

    fun TextChannel.addMemberToChannel(member: Member) {
        this.upsertPermissionOverride(member)
            .grant(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
            .queue()
    }

    private fun ticketFromChannel(channel: TextChannel): Ticket? {
        val topic = channel.topic?.trim().orEmpty()
        if (topic.isEmpty()) return null
        if (channel.parentCategory?.idLong != SUPPORT_CATEGORY) return null

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
                    .append("] ")
                    .append(msg.author.name)
                    .append(": ")
                    .appendLine(msg.contentDisplay)

                if (msg.attachments.isNotEmpty()) {
                    msg.attachments.forEach {
                        sb.appendLine("${msg.author.name} sent a file: ${it.proxyUrl}")
                    }
                }
            }

        return sb.toString()
    }
}
