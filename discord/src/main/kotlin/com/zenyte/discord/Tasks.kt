package com.zenyte.discord

import com.zenyte.common.WorldInfo
import com.zenyte.common.getFriendlyTimeUntil
import com.zenyte.discord.cores.CoresManager
import com.zenyte.discord.ipb.Calendar
import com.zenyte.discord.ipb.Calendar.asEmbed
import mu.KotlinLogging
import net.dv8tion.jda.api.MessageBuilder
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

private object TaskVars {
    var playersOnline = 0
    val announcedEvents = mutableSetOf<Int>()
}

private val logger = KotlinLogging.logger {}

fun scheduleTasks() {
    if (Api.DEVELOPER_MODE) {
        return
    }
    updateRichPresence()
    announceUpcomingEvents()
}

private fun updateRichPresence() {
    CoresManager.serviceProvider.scheduleRepeatingTask(Runnable {
        val totalPlayers = WorldInfo.getTotalPlayerCount()
    
        if (totalPlayers != TaskVars.playersOnline) {
            TaskVars.playersOnline = totalPlayers
            DiscordBot.setPresence("Elvarg | $totalPlayers online")
        }
    }, 1, 20, TimeUnit.SECONDS)
}

private fun announceUpcomingEvents() {
    CoresManager.serviceProvider.scheduleRepeatingTask(Runnable {
        logger.info { "Announcing upcoming events" }
        val events = Calendar.events()
        val now = LocalDateTime.now()
        val nowDate = Date()
    
        events.results.forEach {
            if (!TaskVars.announcedEvents.contains(it.id)) {
                if (nowDate.before(it.start) && now.plusHours(1L).isAfter(LocalDateTime.ofInstant(it.start.toInstant(), ZoneId.of("Europe/London")))) {
                
                    // Events Team #events-team
                    DiscordBot.getZenyteGuild().getTextChannelById(671146510917959691)!!.sendMessage(MessageBuilder()
                            .setContent("""
                                <@&671148089188417567>
                                An event is about to start in ${getFriendlyTimeUntil(it.start.toInstant())}!
                            """.trimIndent())
                            .setEmbed(it.asEmbed().build()).build()
                    ).queue()
                
                    // Server Events #notifications
                    DiscordBot.getZenyteGuild().getTextChannelById(677272449213005835)!!.sendMessage(MessageBuilder()
                            .setContent("""
                                <@&677253546801889290>
                                An event is about to start in ${getFriendlyTimeUntil(it.start.toInstant())}!
                            """.trimIndent())
                            .setEmbed(it.asEmbed().build()).build()
                    ).queue()

//                    DiscordBot.getZenyteGuild().getTextChannelById(498173822541758474)!!.sendMessage(MessageBuilder()
//                            .setContent("""
//                                <@&762402019674685441>
//                                An event is about to start in ${getFriendlyTimeUntil(it.start.toInstant())}!
//                            """.trimIndent())
//                            .setEmbed(it.asEmbed().build()).build()
//                    ).queue()
                
                
                    TaskVars.announcedEvents.add(it.id)
                }
            }
        }
    
    }, 1, 5, TimeUnit.MINUTES)
}
