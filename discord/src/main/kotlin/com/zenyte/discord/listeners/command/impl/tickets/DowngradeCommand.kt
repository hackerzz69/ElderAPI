package com.zenyte.discord.listeners.command.impl.tickets

import com.zenyte.api.model.Role
import com.zenyte.discord.asJDARole
import com.zenyte.discord.listeners.command.Command
import com.zenyte.discord.tickets.TicketManager
import net.dv8tion.jda.api.entities.Message

/**
 * @author Corey
 * @since 14/10/2019
 */
class DowngradeCommand : Command {
    
    override val identifiers = arrayOf("downgrade")
    
    override val description = ""
    
    override fun execute(message: Message, identifier: String) {
        val channel = message.textChannel
        if (!TicketManager.downgrade(channel, message.member!!)) {
            message.delete().queue()
            channel.sendMessage("${message.member!!.asMention} There was a problem downgrading the ticket.").queue()
            return
        }
    }
    
    override fun canExecute(message: Message): Boolean {
        val member = message.member ?: return false
        if (message.textChannel.parent?.idLong != TicketManager.SUPPORT_CATEGORY) {
            return false
        }
        return member.roles.contains(Role.STAFF.asJDARole())
    }
    
}
