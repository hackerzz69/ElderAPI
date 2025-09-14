package com.zenyte.discord

import BotConfig
import com.zenyte.common.WorldInfo
import com.zenyte.common.getFriendlyTimeUntil
import com.zenyte.discord.ipb.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private object TaskVars {
    @Volatile var playersOnline: Int = 0
    val announcedEvents: MutableSet<Int> = ConcurrentHashMap.newKeySet()
}

private val logger = KotlinLogging.logger {}
private val scope = CoroutineScope(Dispatchers.Default)

/**
 * Schedule repeating background tasks for Discord integration.
 */
fun scheduleTasks(cfg: BotConfig, bot: DiscordBot) {
    if (System.getenv("DEVELOPER_MODE") == "true") {
        logger.info { "Developer mode enabled — skipping scheduled tasks" }
        return
    }
    scheduleRichPresence(bot)
    scheduleUpcomingEvents(cfg, bot)
}

/**
 * Update the bot presence with total players online.
 */
private fun scheduleRichPresence(bot: DiscordBot) {
    scope.launch {
        while (true) {
            val totalPlayers = WorldInfo.getTotalPlayerCount()
            if (totalPlayers != TaskVars.playersOnline) {
                TaskVars.playersOnline = totalPlayers
                bot.setPresence("Elder | $totalPlayers online")
                logger.info { "Presence updated: $totalPlayers players online" }
            }
            delay(20.seconds)
        }
    }
}

/**
 * Announce upcoming events to configured channels 1 hour before start.
 */
private fun scheduleUpcomingEvents(cfg: BotConfig, bot: DiscordBot) {
    scope.launch {
        while (true) {
            logger.info { "Checking upcoming events…" }
            val events = Calendar.events()
            val now = LocalDateTime.now()
            val nowDate = Date()

            events.results.forEach { event ->
                if (!TaskVars.announcedEvents.contains(event.id)) {
                    val eventStart = LocalDateTime.ofInstant(event.start.toInstant(), ZoneId.of("Europe/London"))
                    val isWithinHour = now.plusHours(1).isAfter(eventStart)

                    if (nowDate.before(event.start) && isWithinHour) {
                        // Inline embed creation (no asEmbed needed)
                        val embed = EmbedBuilder()
                            .setTitle("Upcoming Event: ${event.title}")
                            .setDescription(event.description ?: "No description available.")
                            .addField(
                                "Starts",
                                event.start.toInstant().atZone(ZoneId.of("Europe/London")).toString(),
                                false
                            )
                            .setColor(0x5865F2) // Discord blurple
                            .build()

                        val msg = MessageCreateBuilder()
                            .setContent(
                                "An event is about to start in " +
                                        getFriendlyTimeUntil(event.start.toInstant()) + "!"
                            )
                            .setEmbeds(embed)
                            .build()

                        cfg.eventAnnouncementChannels.forEach { channelId ->
                            bot.jda.getTextChannelById(channelId)?.sendMessage(msg)?.queue()
                        }

                        TaskVars.announcedEvents.add(event.id)
                        logger.info { "Announced event ${event.id}" }
                    }
                }
            }

            delay(5.minutes)
        }
    }
}
