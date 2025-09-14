package com.zenyte.discord.listeners.command.impl.tickets

import com.zenyte.api.model.Role
import com.zenyte.discord.DiscordBot
import com.zenyte.discord.asJDARole
import com.zenyte.discord.getRoleById
import com.zenyte.discord.listeners.command.Command
import com.zenyte.discord.tickets.TicketManager
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

/**
 * @author Corey
 * @since 14/10/2019
 */
class TicketBanCommand : Command {

    override val identifiers = arrayOf("ticketban", "tban")

    override val description = "Toggles the ticket-ban role on mentioned members."

    override fun execute(message: Message, identifier: String) {
        val channel = message.channel.asTextChannel()
        val membersToBan = message.mentions.members

        if (membersToBan.isEmpty()) {
            channel.sendMessage("Wrong format! Correct usage: `::$identifier [list of mentioned members to ban]`").queue()
            return
        }

        val bannedRole = TicketManager.TICKET_BAN_ROLE_ID.getRoleById()!!
        val bannedMembers = mutableListOf<Member>()
        val unbannedMembers = mutableListOf<Member>()

        membersToBan.forEach { member ->
            if (member.roles.contains(bannedRole)) {
                unbannedMembers.add(member)
                DiscordBot.getZenyteGuild().removeRoleFromMember(member, bannedRole).queue()
            } else {
                bannedMembers.add(member)
                DiscordBot.getZenyteGuild().addRoleToMember(member, bannedRole).queue()
            }
        }

        if (bannedMembers.isNotEmpty()) {
            channel.sendMessage("Members banned: ${bannedMembers.joinToString(", ") { it.asMention }}").queue()
        }

        if (unbannedMembers.isNotEmpty()) {
            channel.sendMessage("Members unbanned: ${unbannedMembers.joinToString(", ") { it.asMention }}").queue()
        }
    }

    override fun canExecute(message: Message): Boolean {
        val member = message.member ?: return false
        val channel = message.channel as? TextChannel ?: return false

        if (channel.idLong != TicketManager.TICKET_COMMANDS_CHANNEL) {
            return false
        }
        return member.roles.contains(Role.STAFF.asJDARole())
    }
}
