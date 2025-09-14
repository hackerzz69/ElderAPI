package com.zenyte.discord.listeners.command.impl

import com.zenyte.api.model.Role
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorResponseException

/**
 * @author Corey
 * @since 28/04/19
 */
class PmAllCommand : Command {
    
    override fun canExecute(message: Message): Boolean {
        val role = message.jda.getRoleById(Role.ADMINISTRATOR.discordRoleId)
        return message.guild.getMembersWithRoles(role).contains(message.member)
    }
    
    override val identifiers = arrayOf("pmall")
    
    override val description = ""
    
    override fun execute(message: Message, identifier: String) {
        val effectiveMessage = message.contentDisplay.removePrefix("::${identifiers[0]}").trim()
        val channel = message.channel
    
        channel.sendMessage("Sending pm...").queue()
    
        for (user in message.guild.members) {
            try {
                user.user.openPrivateChannel().queue { it.sendMessage(effectiveMessage).queue() }
                Thread.sleep(1000)
            } catch (e: ErrorResponseException) {
            
            }
        }
    }
}