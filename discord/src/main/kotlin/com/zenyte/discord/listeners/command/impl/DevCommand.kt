package com.zenyte.discord.listeners.command.impl

import com.zenyte.api.model.Role
import com.zenyte.discord.DiscordBot
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Message

/**
 * @author Corey
 * @since 22/10/18
 */
class DevCommand : Command {

    override fun canExecute(message: Message): Boolean {
        val role = message.jda.getRoleById(Role.STAFF.discordRoleId)
        return role != null && message.guild.getMembersWithRoles(role).contains(message.member)
    }

    override val identifiers = arrayOf("dev")

    override val description = "Developer utility commands (say, copy, roles, playing, togglegreet)."

    override fun execute(message: Message, identifier: String) {
        val effectiveMessage = message.contentDisplay.removePrefix("::${identifiers[0]}").trim()
        val channel = message.channel

        when {
            effectiveMessage.startsWith("say", true) -> {
                channel.sendMessage(
                    message.contentRaw.removePrefix("::${identifiers[0]} say").trim()
                ).queue()
                message.delete().queue()
            }

            effectiveMessage.startsWith("copy", true) -> {
                channel.sendMessage(
                    message.contentRaw.removePrefix("::${identifiers[0]} copy").trim()
                ).queue()
            }

            effectiveMessage.startsWith("roles", true) -> {
                val msg = effectiveMessage.removePrefix("roles").trim()
                message.guild.getRolesByName(msg, true).forEach {
                    channel.sendMessage("${it.id} - ${it.name}").queue()
                }
            }

            effectiveMessage.startsWith("playing", true) -> {
                val msg = effectiveMessage.removePrefix("playing").trim()
                // ✅ JDA 5 way of setting presence
                DiscordBot.jda.presence.activity = Activity.of(Activity.ActivityType.PLAYING, msg)
                channel.sendMessage("Presence updated: now playing **$msg**").queue()
            }

            effectiveMessage.startsWith("togglegreet", true) -> {
                DiscordBot.greetNewMembers = !DiscordBot.greetNewMembers
                channel.sendMessage("Greet new members: ${DiscordBot.greetNewMembers}").queue()
            }
        }
    }
}
