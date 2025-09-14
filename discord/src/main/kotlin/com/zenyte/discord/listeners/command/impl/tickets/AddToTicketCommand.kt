package com.zenyte.discord.listeners.command.impl.tickets

import com.zenyte.api.model.Role
import com.zenyte.discord.DiscordBot
import com.zenyte.discord.asJDARole
import com.zenyte.discord.getCommandArgs
import com.zenyte.discord.listeners.command.Command
import com.zenyte.discord.tickets.TicketManager
import com.zenyte.discord.tickets.TicketManager.addMemberToChannel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

/**
 * @author Corey
 * @since 14/05/2020
 */
class AddToTicketCommand : Command {
    
    override val identifiers = arrayOf("add")
    
    override val description = ""
    
    override fun execute(message: Message, identifier: String) {
        val membersToAdd = mutableListOf<Member>()
        membersToAdd.addAll(message.mentionedMembers)
        
        if (membersToAdd.size == 0) {
            if (message.getCommandArgs(identifier).isEmpty()) {
                error(message.textChannel, identifier)
            } else {
                val args = message.getCommandArgs(identifier).split(" ")
                args.forEach {
                    try {
                        val member = DiscordBot.getZenyteGuild().getMemberById(it)
                        if (member == null) {
                            error(message.textChannel, identifier)
                            return
                        } else {
                            membersToAdd.add(member)
                        }
                    } catch (e: NumberFormatException) {
                        error(message.textChannel, identifier)
                        return
                    }
                }
            }
        }
        
        membersToAdd.forEach {
            message.textChannel.addMemberToChannel(it)
            message.textChannel.sendMessage("${it.asMention} was added to the ticket.").queue()
        }
    }
    
    override fun canExecute(message: Message): Boolean {
        val member = message.member ?: return false
        if (message.textChannel.parent?.idLong != TicketManager.SUPPORT_CATEGORY) {
            return false
        }
        return member.roles.contains(Role.STAFF.asJDARole())
    }
    
    private fun error(channel: TextChannel, identifier: String) {
        channel.sendMessage("Wrong format! Correct usage: `::$identifier [list of member ids or mentions to add]`").queue()
    }
    
}
