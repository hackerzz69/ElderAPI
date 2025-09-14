package com.zenyte.discord.listeners.command.impl

import com.zenyte.api.model.AdventurerLogEntry
import com.zenyte.common.getFriendlyTimeSince
import com.zenyte.common.getJsonResponseOrNull
import com.zenyte.common.CommonConfig.gson
import com.zenyte.discord.Api
import com.zenyte.discord.getCommandArgs
import com.zenyte.discord.isStaff
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import okhttp3.Request
import java.time.Instant

/**
 * @author Corey
 * @since 19/02/2020
 */
class AdvLogCommand : Command {

    companion object {
        private val bannedChannels = listOf(
            373833867934826498L // #general
        )
    }

    override val identifiers = arrayOf("adv", "log")

    override val description = "Get adventurer's log for the given user."

    override fun canExecute(message: Message): Boolean {
        if (message.member?.isStaff() == true) return true
        return !bannedChannels.contains(message.channel.idLong)
    }

    override fun execute(message: Message, identifier: String) {
        val username = message.getCommandArgs(identifier)
        val entries = getEntries(username).sortedByDescending { it.date }.take(5)

        if (entries.isEmpty()) {
            message.channel.sendMessage("No adventurer log found for `$username`.").queue()
            return
        }

        val embed = EmbedBuilder()
            .setColor(0xF1A2A7) // same as 15837287
            .setTimestamp(Instant.now())
            .setFooter("Zenyte Adventurers Log", "https://zenyte.com/img/ic_launcher.png")
            .setTitle(
                "$username's Adventurer Log",
                "https://zenyte.com/account/user/${username.replace(" ", "_")}"
            )
            .setDescription("-")

        entries.forEach {
            embed.addField(it.message, "${getFriendlyTimeSince(it.date.toInstant())} ago", false)
        }

        // ✅ JDA 5 requires sendMessageEmbeds for embeds
        message.channel.sendMessageEmbeds(embed.build()).queue()
    }

    private fun getEntries(username: String): Array<AdventurerLogEntry> {
        val request = Request.Builder()
            .url(
                Api.getApiRoot()
                    .newBuilder()
                    .addPathSegment("user")
                    .addPathSegment("adv")
                    .addPathSegment(username)
                    .build()
            )
            .get()
            .build()

        val response = Api.client.getJsonResponseOrNull(request)
        return gson.fromJson(response, Array<AdventurerLogEntry>::class.java)
    }
}
