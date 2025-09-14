package com.zenyte.discord.listeners

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.entities.emoji.Emoji

/**
 * @author Corey
 * @since 22/10/18
 */
class SeventyThree : EventListener {

    override fun onEvent(event: GenericEvent) {
        if (event !is MessageReceivedEvent) return

        val msg = event.message.contentDisplay
        if (msg.matches(Regex(".*\\b73\\b.*"))) {
            event.message.addReaction(Emoji.fromUnicode("😂")).queue()
        }
    }
}
