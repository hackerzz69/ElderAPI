package com.zenyte.discord

import com.zenyte.api.model.Role
import com.zenyte.discord.listeners.CommandListener
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message


/**
 * @author Corey
 * @since 08/10/2019
 */

fun Long.getRoleById() = DiscordBot.getZenyteGuild().getRoleById(this)

fun Role.asJDARole(): net.dv8tion.jda.api.entities.Role? {
    if (!this.isDiscordRole()) {
        return null
    }
    return this.discordRoleId.getRoleById()
}

fun Message.getCommandArgs(identifier: String) = this.contentRaw.substring(identifier.length + CommandListener.COMMAND_PREFIX.length).trim()

fun Member.isStaff(): Boolean {
    return this.roles.contains(Role.STAFF.asJDARole())
}
