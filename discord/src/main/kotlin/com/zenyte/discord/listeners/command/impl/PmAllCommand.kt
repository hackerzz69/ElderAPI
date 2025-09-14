package com.zenyte.discord.listeners.command.impl

import com.zenyte.api.model.Role
import com.zenyte.discord.listeners.command.Command
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import java.util.concurrent.TimeUnit

/**
 * @author Corey
 * @since 28/04/19
 */
class PmAllCommand : Command {

    private val logger = KotlinLogging.logger {}

    override fun canExecute(message: Message): Boolean {
        val role = message.jda.getRoleById(Role.ADMINISTRATOR.discordRoleId)
        return role != null && message.guild.getMembersWithRoles(role).contains(message.member)
    }

    override val identifiers = arrayOf("pmall")

    override val description = "Send a direct message to all guild members."

    override fun execute(message: Message, identifier: String) {
        val effectiveMessage = message.contentDisplay.removePrefix("::${identifiers[0]}").trim()
        val channel = message.channel

        channel.sendMessage("Sending PMs…").queue()

        var delay = 0L
        message.guild.members.forEach { member ->
            member.user.openPrivateChannel().queue({ privateChannel ->
                privateChannel.sendMessage(effectiveMessage)
                    .queueAfter(delay, TimeUnit.MILLISECONDS,
                        { logger.info { "Sent PM to ${member.user.asTag}" } },
                        { e ->
                            if (e is ErrorResponseException) {
                                logger.warn { "Could not send PM to ${member.user.asTag}: ${e.errorResponse}" }
                            }
                        }
                    )
            }, { e ->
                logger.warn { "Could not open private channel for ${member.user.asTag}: ${e.message}" }
            })
            delay += 1000 // 1 second between DMs to avoid rate-limits
        }
    }
}
