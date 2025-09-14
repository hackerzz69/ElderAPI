package com.zenyte.discord.listeners

import com.zenyte.discord.DiscordBot
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.hooks.EventListener

/**
 * @author Corey
 * @since 22/10/18
 */
class NewMemberGreeter : EventListener {

    override fun onEvent(event: GenericEvent) {
        if (!DiscordBot.greetNewMembers) return

        if (event is GuildMemberJoinEvent) {
            val welcomeMessage =
                "Hey ${event.member.asMention}, welcome to **Zenyte** <:zenyte_icon:475633424070344704>!"

            // Use Long instead of String for channel ID
            event.jda.getTextChannelById(373833867934826498L)
                ?.sendMessage(welcomeMessage)
                ?.queue()
        }
    }

    private fun String.containsUrl(): Boolean {
        val prohibitedPhrases = arrayOf(".com", ".gg", "paypal.me", ".net", ".tv")
        return prohibitedPhrases.any { this.contains(it, ignoreCase = true) }
    }
}
