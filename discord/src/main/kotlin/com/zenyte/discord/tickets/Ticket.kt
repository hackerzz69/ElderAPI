package com.zenyte.discord.tickets

import com.zenyte.common.generateRandomString
import com.zenyte.discord.DiscordBot

/**
 * @author Corey
 * @since 08/10/2019
 *
 * Represents a support ticket created within the Discord bot.
 *
 * @param id unique identifier for the ticket (random if not provided)
 * @param adminRequested whether an admin was explicitly requested
 * @param creatorUserId Discord user ID of the creator
 * @param query the issue or request text
 */
data class Ticket(
    val id: String = generateRandomString(length = 5),
    var adminRequested: Boolean,
    val creatorUserId: Long,
    val query: String
) {
    /**
     * Title to use for the ticket channel in Discord.
     * Example: "admin-ticket-abc12"
     */
    fun channelTitle(): String =
        buildString {
            if (adminRequested) append("admin-")
            append("ticket-").append(id)
        }

    /**
     * Channel topic JSON payload (serialized representation of this ticket).
     */
    fun channelTopic(): String = DiscordBot.gson.toJson(this)
}
