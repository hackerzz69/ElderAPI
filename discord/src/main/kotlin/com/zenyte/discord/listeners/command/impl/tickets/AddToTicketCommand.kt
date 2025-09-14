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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

/**
 * @author Corey
 * @since 14/05/2020
 */
class AddToTicketCommand : Command {

    override val identifiers = arrayOf("add")

    override val description = "Add one or more members to the current ticket channel."

    override fun execute(message: Message, identifier: String) {
        val membersToAdd = mutableListOf<Member>()
        membersToAdd.addAll(message.mentions.members) // ✅ JDA 5 way

        // No mentions, fallback to raw args (IDs)
        if (membersToAdd.isEmpty()) {
            val args = message.getCommandArgs(identifier).split(" ").filter { it.isNotBlank() }
            if (args.isEmpty()) {
                error(message.channel.asTextChannel(), identifier)
                return
            }

            args.forEach {
                val member = DiscordBot.getZenyteGuild().getMemberById(it)
                if (member != null) {
                    membersToAdd.add(member)
                } else {
                    error(message.channel.asTextChannel(), identifier)
                    return
                }
            }
        }

        val tc = message.channel.asTextChannel()
        membersToAdd.forEach { member ->
            tc.addMemberToChannel(member)
            tc.sendMessage("${member.asMention} was added to the ticket.").queue()
        }
    }

    override fun canExecute(message: Message): Boolean {
        val member = message.member ?: return false
        val tc = message.channel as? TextChannel ?: return false
        if (tc.parentCategoryIdLong != TicketManager.SUPPORT_CATEGORY) return false
        return member.roles.contains(Role.STAFF.asJDARole())
    }

    private fun error(channel: TextChannel, identifier: String) {
        channel.sendMessage("Wrong format! Correct usage: `::$identifier [list of member ids or mentions to add]`").queue()
    }
}
