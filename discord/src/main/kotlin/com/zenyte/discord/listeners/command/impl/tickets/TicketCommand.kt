package com.zenyte.discord.listeners.command.impl.tickets

import com.zenyte.discord.getRoleById
import com.zenyte.discord.listeners.CommandListener
import com.zenyte.discord.listeners.command.Command
import com.zenyte.discord.tickets.Ticket
import com.zenyte.discord.tickets.TicketManager
import net.dv8tion.jda.api.entities.Message
import java.util.concurrent.TimeUnit

/**
 * @author Corey
 * @since 08/10/2019
 */
class TicketCommand : Command {
    
    private val adminIdentifiers = setOf("adminticket", "adminhelp")
    
    override val identifiers = arrayOf("ticket", *adminIdentifiers.toTypedArray())
    
    override val description = "Create a support request - prepend with 'admin' to request an admin"
    
    override fun execute(message: Message, identifier: String) {
        val member = message.member ?: return
        val query = message.contentDisplay.substring(identifier.length + CommandListener.COMMAND_PREFIX.length).trim()
        
        if (query.isEmpty()) {
            message.delete().queue()
            message.textChannel.sendMessage("${message.member!!.asMention} You need to specify a question/query; use `::$identifier your query here`.").queue {
                it.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }
        
        message.delete().queue {
            TicketManager.create(
                    Ticket(
                            adminRequested = adminIdentifiers.contains(identifier.toLowerCase()),
                            creatorUserId = member.idLong,
                            query = query
                    )
            )
        }
    }
    
    override fun canExecute(message: Message): Boolean {
        val member = message.member ?: return false
        return !member.roles.contains(TicketManager.TICKET_BAN_ROLE_ID.getRoleById())
    }
    
}
