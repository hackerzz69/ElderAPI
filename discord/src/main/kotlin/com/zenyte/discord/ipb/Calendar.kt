package com.zenyte.discord.ipb

import com.zenyte.api.model.Event
import com.zenyte.common.CommonConfig.gson
import com.zenyte.common.stripHtml
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import okhttp3.Request
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author Corey
 * @since 03/10/2020
 */
object Calendar {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val logger = KotlinLogging.logger {}

    /**
     * Fetch events from IPB calendar.
     */
    fun events(): CalendarObject {
        val request = Request.Builder()
            .url(
                urlBuilder()
                    .addPathSegment("calendar")
                    .addPathSegment("events")
                    .addQueryParameter("calendars", "1")
                    .addQueryParameter("sortBy", "start")
                    .addQueryParameter("sortDir", "desc")
                    .addQueryParameter("rangeStart", LocalDateTime.now().minusMonths(1).format(formatter))
                    .addQueryParameter("rangeEnd", LocalDateTime.now().plusMonths(1).format(formatter))
                    .addQueryParameter("perPage", "100")
                    .build()
            )
            .get()
            .build()

        logger.info { "Getting calendar events from IPB" }

        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            require(!body.isNullOrBlank()) { "Empty response from IPB calendar" }
            return gson.fromJson(body, CalendarObject::class.java)
        }
    }

    /**
     * Clean up IPB HTML descriptions for safe display.
     */
    fun Event.sanitisedDescription(): String = this.description.stripHtml()

    /**
     * Convert an Event into a Discord embed.
     */
    fun Event.asEmbed(): EmbedBuilder {
        val status = when {
            start.after(Date()) -> "Upcoming"
            end != null && start.before(Date()) && end!!.after(Date()) -> "In progress"
            else -> "Ended"
        }

        return EmbedBuilder()
            .setColor(0xF1A2A7)
            .setTimestamp(Instant.now())
            .setTitle(title, url)
            .setDescription(sanitisedDescription())
            .setFooter("Zenyte Events", "https://cdn.zenyte.com/zenyte.png")
            .addField("Author", author.name, true)
            .addBlankField(true)
            .addField("Status", status, true)
            .addField("Start", start.formatted(), true)
            .addBlankField(true)
            .addField("End", end?.formatted() ?: "No end date", true)
    }

    /**
     * Format a Date into a user-friendly UTC string.
     */
    fun Date.formatted(): String {
        val sdf = SimpleDateFormat("HH:mm z - dd MMM yy", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(this)
    }
}

typealias CalendarObject = com.zenyte.api.model.Calendar
