package com.zenyte.discord.listeners.command.impl.tickets

import com.zenyte.api.model.Role
import com.zenyte.discord.asJDARole
import com.zenyte.discord.listeners.command.Command
import com.zenyte.discord.tickets.TicketManager
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

/**
 * @author Corey
 * @since 14/10/2019
 */
class DowngradeCommand : Command {

    override val identifiers = arrayOf("downgrade")

    override val description = "Downgrade a ticket from admin-only visibility to staff visibility."

    override fun execute(message: Message, identifier: String) {
        val channel = message.channel.asTextChannel()
        val member = message.member ?: return

        if (!TicketManager.downgrade(channel, member)) {
            message.delete().queue()
            channel.sendMessage("${member.asMention} There was a problem downgrading the ticket.").queue()
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
