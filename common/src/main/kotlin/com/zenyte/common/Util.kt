package com.zenyte.common

import org.jsoup.Jsoup
import java.time.Instant
import java.util.*
import kotlin.math.max
import kotlin.streams.asSequence

/**
 * @author Corey
 * @since 30/10/2019
 */

private const val CODE_SOURCE = "abcdefghijkmnopqrstuvwxyz23456789"

fun calculateCombatLevel(attack: Int, strength: Int, defence: Int, hitpoints: Int, ranged: Int, magic: Int, prayer: Int): Double {
    val base = 0.25 * (defence + hitpoints + (prayer / 2))
    val melee = 0.325 * (attack + strength)
    val range = 0.325 * (3 * ranged / 2)
    val mage = 0.325 * (3 * magic / 2)
    
    return base + max(melee, max(mage, range))
}

fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")

fun generateRandomString(length: Int = 6) = Random().ints(length.toLong(), 0, CODE_SOURCE.length)
        .asSequence()
        .map(CODE_SOURCE::get)
        .joinToString("")

fun secondsAsFriendlyDuration(timeInSeconds: Long): String {
    val sb = StringBuffer()
    var diffInSeconds = timeInSeconds
    
    val sec = diffInSeconds % 60
    
    diffInSeconds /= 60
    val min = diffInSeconds % 60
    
    diffInSeconds /= 60
    val hrs = diffInSeconds % 24
    
    diffInSeconds /= 24
    val days = diffInSeconds % 30
    
    diffInSeconds /= 30
    val months = diffInSeconds % 12
    
    diffInSeconds /= 12
    val years = diffInSeconds % 12
    
    if (years > 0) {
        if (years == 1L) {
            sb.append("a year")
        } else {
            sb.append("$years years")
        }
        if (years <= 6 && months > 0) {
            if (months == 1L) {
                sb.append(" and a month")
            } else {
                sb.append(" and $months months")
            }
        }
    } else if (months > 0) {
        if (months == 1L) {
            sb.append("a month")
        } else {
            sb.append("$months months")
        }
        if (months <= 6 && days > 0) {
            if (days == 1L) {
                sb.append(" and a day")
            } else {
                sb.append(" and $days days")
            }
        }
    } else if (days > 0) {
        if (days == 1L) {
            sb.append("a day")
        } else {
            sb.append("$days days")
        }
        if (days <= 3 && hrs > 0) {
            if (hrs == 1L) {
                sb.append(" and an hour")
            } else {
                sb.append(" and $hrs hours")
            }
        }
    } else if (hrs > 0) {
        if (hrs == 1L) {
            sb.append("an hour")
        } else {
            sb.append("$hrs hours")
        }
        if (min > 1) {
            sb.append(" and $min minutes")
        }
    } else if (min > 0) {
        if (min == 1L) {
            sb.append("a minute")
        } else {
            sb.append("$min minutes")
        }
        if (sec > 1) {
            sb.append(" and $sec seconds")
        }
    } else {
        if (sec <= 1) {
            sb.append("about a second")
        } else {
            sb.append("about $sec seconds")
        }
    }
    
    return sb.toString().trim()
}

fun getFriendlyTimeSince(instant: Instant): String {
    val current = Calendar.getInstance().toInstant()
    return secondsAsFriendlyDuration(current.epochSecond - instant.epochSecond)
}

fun getFriendlyTimeUntil(instant: Instant): String {
    val current = Calendar.getInstance().toInstant()
    return secondsAsFriendlyDuration(instant.epochSecond - current.epochSecond)
}

/**
 * Strips all HTML and escape characters
 */
fun String.stripHtml(): String {
    if (!this.contains("<") && !this.contains("\\")) return this
    return Jsoup.parse(this).text()
}