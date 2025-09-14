package com.zenyte.discord.listeners.command.impl

import com.zenyte.api.model.ExpMode
import com.zenyte.api.model.IronmanMode
import com.zenyte.api.model.Skill
import com.zenyte.api.model.SkillHiscore
import com.zenyte.common.calculateCombatLevel
import com.zenyte.common.capitalizeWords
import com.zenyte.common.getJsonResponseOrNull
import com.zenyte.common.CommonConfig.gson
import com.zenyte.discord.Api
import com.zenyte.discord.Emoji
import com.zenyte.discord.getCommandArgs
import com.zenyte.discord.isStaff
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import okhttp3.Request
import java.text.DecimalFormat
import java.time.Instant

/**
 * @author Corey
 * @since 09/03/2020
 */
class HiscoresCommand : Command {
    
    companion object {
        private val bannedChannels = listOf(
                640277915090812965, // #osrs
                373833867934826498 // #general
        )
    }
    
    override val identifiers = arrayOf("stats", "hs", "hiscore")
    
    override val description = "Show hiscores for a user."
    
    override fun canExecute(message: Message): Boolean {
        if (message.member?.isStaff() == true) { // consume nullable boolean
            return true
        }
        
        return !bannedChannels.contains(message.channel.idLong)
    }
    
    override fun execute(message: Message, identifier: String) {
        val username = message.getCommandArgs(identifier)
        val skills = getEntries(username).toMutableMap()
        
        if (skills.isEmpty()) {
            message.channel.sendMessage("No hiscores found for `$username`.").queue()
            return
        }
        
        val firstEntry = skills.values.first()
        val formattedUsername = firstEntry.username.replace("_", " ").capitalizeWords()
        
        val embed = EmbedBuilder()
                .setColor(0xF1A2A7)
                .setTimestamp(Instant.now())
                .setFooter("Zenyte Hiscores", "https://zenyte.com/img/ic_launcher.png")
                .setAuthor(formattedUsername, "https://zenyte.com/hiscores/search/${username.replace(" ", "_")}")
        
        // verify all skills exist, and if not set them to zero
        Skill.VALUES_NO_TOTAL.forEach {
            if (!skills.containsKey(it.id)) {
                skills[it.id] = SkillHiscore(
                        firstEntry.userId,
                        username,
                        firstEntry.mode,
                        firstEntry.expMode,
                        it.id,
                        it.formattedName,
                        1,
                        1
                )
            }
        }
        
        embed.addField("\u200b", """
            ${Emoji.ATTACK} ${skills[Skill.ATTACK.id]!!.level}
            ${Emoji.STRENGTH} ${skills[Skill.STRENGTH.id]!!.level}
            ${Emoji.DEFENCE} ${skills[Skill.DEFENCE.id]!!.level}
            ${Emoji.RANGED} ${skills[Skill.RANGED.id]!!.level}
            ${Emoji.PRAYER} ${skills[Skill.PRAYER.id]!!.level}
            ${Emoji.MAGIC} ${skills[Skill.MAGIC.id]!!.level}
            ${Emoji.RUNECRAFT} ${skills[Skill.RUNECRAFTING.id]!!.level}
            ${Emoji.CONSTRUCTION} ${skills[Skill.CONSTRUCTION.id]!!.level}
        """.trimIndent(), true)
        
        embed.addField("\u200b", """
            ${Emoji.HITPOINTS} ${skills[Skill.HITPOINTS.id]!!.level}
            ${Emoji.AGILITY} ${skills[Skill.AGILITY.id]!!.level}
            ${Emoji.HERBLORE} ${skills[Skill.HERBLORE.id]!!.level}
            ${Emoji.THIEVING} ${skills[Skill.THIEVING.id]!!.level}
            ${Emoji.CRAFTING} ${skills[Skill.CRAFTING.id]!!.level}
            ${Emoji.FLETCHING} ${skills[Skill.FLETCHING.id]!!.level}
            ${Emoji.SLAYER} ${skills[Skill.SLAYER.id]!!.level}
            ${Emoji.HUNTER} ${skills[Skill.HUNTER.id]!!.level}
        """.trimIndent(), true)
        
        embed.addField("\u200b", """
            ${Emoji.MINING} ${skills[Skill.MINING.id]!!.level}
            ${Emoji.SMITHING} ${skills[Skill.SMITHING.id]!!.level}
            ${Emoji.FISHING} ${skills[Skill.FISHING.id]!!.level}
            ${Emoji.COOKING} ${skills[Skill.COOKING.id]!!.level}
            ${Emoji.FIREMAKING} ${skills[Skill.FIREMAKING.id]!!.level}
            ${Emoji.WOODCUTTING} ${skills[Skill.WOODCUTTING.id]!!.level}
            ${Emoji.FARMING} ${skills[Skill.FARMING.id]!!.level}
            ${Emoji.OVERALL} ${skills[Skill.TOTAL.id]!!.level}
        """.trimIndent(), true)
        
        embed.addField("${Emoji.OVERALL} Overall", """
            **Level:** ${skills[Skill.TOTAL.id]!!.level}
            **Exp:** ${String.format("%,d", skills[Skill.TOTAL.id]!!.experience)}
            **Exp Mode:** ${when (firstEntry.expMode) {
            ExpMode.FIFTY -> "x50/25"
            ExpMode.TEN -> "x10"
            ExpMode.FIVE -> "x5"
        }
        }
            **Combat Level:** ${DecimalFormat("###.##").format(calculateCombatLevel(
                attack = skills[Skill.ATTACK.id]!!.level,
                strength = skills[Skill.STRENGTH.id]!!.level,
                defence = skills[Skill.DEFENCE.id]!!.level,
                hitpoints = skills[Skill.HITPOINTS.id]!!.level,
                ranged = skills[Skill.RANGED.id]!!.level,
                magic = skills[Skill.MAGIC.id]!!.level,
                prayer = skills[Skill.PRAYER.id]!!.level
        ))}
            **Mode:** ${
        when (firstEntry.mode) {
            IronmanMode.REGULAR -> "Regular"
            IronmanMode.IRONMAN -> Emoji.IRONMAN
            IronmanMode.ULTIMATE_IRONMAN -> Emoji.ULTIMATE_IRONMAN
            IronmanMode.HARDCORE_IRONMAN -> Emoji.HARDCORE_IRONMAN
            IronmanMode.DEAD_HARDCORE_IRONMAN -> Emoji.IRONMAN
        }
        }
        """.trimIndent(), true)
        
        message.channel.sendMessageEmbeds(embed.build()).queue()
        
    }
    
    private fun getEntries(username: String): Map<Int, SkillHiscore> {
        val request = Request.Builder()
                .url(Api.getApiRoot()
                        .newBuilder()
                        .addPathSegment("hiscores")
                        .addPathSegment("user")
                        .addPathSegment(username.replace(" ", "_"))
                        .build())
                .get()
                .build()
        
        val response = Api.client.getJsonResponseOrNull(request)
        
        if (response.isNullOrEmpty()) {
            return emptyMap()
        }
        
        val entryArray = gson.fromJson(response, Array<SkillHiscore>::class.java)

        return entryArray.associateBy { it.skillId }
    }
    
}