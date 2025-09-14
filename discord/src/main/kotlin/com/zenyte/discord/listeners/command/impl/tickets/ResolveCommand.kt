package com.zenyte.discord.listeners.command.impl.tickets

import com.zenyte.api.model.Role
import com.zenyte.discord.asJDARole
import com.zenyte.discord.getCommandArgs
import com.zenyte.discord.listeners.command.Command
import com.zenyte.discord.tickets.TicketManager
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

/**
 * @author Corey
 * @since 08/10/2019
 */
class ResolveCommand : Command {

    override val identifiers = arrayOf("resolve")

    override val description = "Resolve a support ticket, logging the reason and closing the channel."

    override fun execute(message: Message, identifier: String) {
        val channel = message.channel.asTextChannel()
        val member = message.member ?: return
        val reason = message.getCommandArgs(identifier).trim()

        if (reason.isEmpty()) {
            message.delete().queue()
            channel.sendMessage("${member.asMention} You must specify a reason for resolving the ticket.").queue()
            return
        }

        if (!TicketManager.resolve(channel, member, reason)) {
            message.delete().queue()
            channel.sendMessage("${member.asMention} There was a problem resolving the ticket.").queue()
        }
    }

    override fun canExecute(message: Message): Boolean {
        val member = message.member ?: return false
        val channel = message.channel as? TextChannel ?: return false

        if (channel.parentCategoryIdLong != TicketManager.SUPPORT_CATEGORY) {
            return false
        }
        return member.roles.contains(Role.STAFF.asJDARole())
    }
}
