@file:JvmName("DiscordExtensions")

package com.zenyte.discord

import com.zenyte.api.model.Role
import com.zenyte.discord.listeners.CommandListener
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role as JDARole

/**
 * Discord utility extensions for roles, members, and command parsing.
 */

// Look up a Discord Role object by its ID on the configured guild
fun Long.getRoleById(): JDARole? =
    DiscordBot.getZenyteGuild().getRoleById(this)

// Convert our internal Role to a JDA Role if mapped
fun Role.asJDARole(): JDARole? =
    if (this.isDiscordRole()) this.discordRoleId.getRoleById() else null

// Extract arguments from a message after a command identifier
fun Message.getCommandArgs(identifier: String): String =
    this.contentDisplay
        .removePrefix(CommandListener.COMMAND_PREFIX + identifier)
        .trim()

// Check whether a member is staff
fun Member.isStaff(): Boolean =
    Role.STAFF.asJDARole()?.let { it in this.roles } ?: false
