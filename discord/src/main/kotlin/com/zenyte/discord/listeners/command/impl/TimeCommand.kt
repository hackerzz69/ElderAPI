package com.zenyte.discord.listeners.command.impl

import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * @author Corey
 * @since 04/10/2020
 */
class TimeCommand : Command {

    override val identifiers = arrayOf("time")

    override val description = "Returns the current server time."

    override fun execute(message: Message, identifier: String) {
        val channel = message.channel

        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss z - dd MMM yy")
        val nowUtc = LocalDateTime.now(ZoneId.of("UTC")).format(formatter)

        channel.sendMessage(
            "${message.author.asMention} :alarm_clock: The current server time is `$nowUtc`"
        ).queue()
    }
}
