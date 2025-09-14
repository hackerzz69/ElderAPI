package com.zenyte.discord.listeners.command.impl

import com.zenyte.common.WorldInfo
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message

/**
 * @author Corey
 * @since 01/05/19
 */
class PlayersOnlineCommand : Command {

    override val identifiers = arrayOf("players")

    override val description = "Gets the current total player count."

    override fun execute(message: Message, identifier: String) {
        val players = WorldInfo.getTotalPlayerCount()
        val response = when (players) {
            0 -> "There are currently no players online."
            1 -> "There is currently 1 player online."
            else -> "There are currently $players players online."
        }

        message.channel.sendMessage(response).queue()
    }
}
