package com.zenyte.discord.listeners.command.impl.tickets

import com.zenyte.api.model.Role
import com.zenyte.discord.DiscordBot
import com.zenyte.discord.asJDARole
import com.zenyte.discord.getRoleById
import com.zenyte.discord.listeners.command.Command
import com.zenyte.discord.tickets.TicketManager
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message

/**
 * @author Corey
 * @since 14/10/2019
 */
class TicketBanCommand : Command {
    
    override val identifiers = arrayOf("ticketban", "tban")
    
    override val description = ""
    
    override fun execute(message: Message, identifier: String) {
        val membersToBan = message.mentionedMembers
        
        if (membersToBan.size == 0) {
            message.textChannel.sendMessage("Wrong format! Correct usage: `::$identifier [list of mentioned members to ban]`").queue()
            return
        }
        
        val bannedRole = TicketManager.TICKET_BAN_ROLE_ID.getRoleById()!!
        val bannedMembers = mutableListOf<Member>()
        val unbannedMembers = mutableListOf<Member>()
        
        membersToBan.forEach {
            if (it.roles.contains(bannedRole)) {
                unbannedMembers.add(it)
                DiscordBot.getZenyteGuild().removeRoleFromMember(it, bannedRole).queue()
            } else {
                bannedMembers.add(it)
                DiscordBot.getZenyteGuild().addRoleToMember(it, bannedRole).queue()
            }
        }
        
        if (bannedMembers.size > 0) {
            message.textChannel.sendMessage("Members banned: ${bannedMembers.joinToString(", ") { it.asMention }}").queue()
        }
        
        if (unbannedMembers.size > 0) {
            message.textChannel.sendMessage("Members unbanned: ${unbannedMembers.joinToString(", ") { it.asMention }}").queue()
        }
        
    }
    
    override fun canExecute(message: Message): Boolean {
        val member = message.member ?: return false
        if (message.textChannel.idLong != TicketManager.TICKET_COMMANDS_CHANNEL) {
            return false
        }
        return member.roles.contains(Role.STAFF.asJDARole())
    }
    
}
