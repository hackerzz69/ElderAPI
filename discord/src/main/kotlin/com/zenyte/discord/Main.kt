package com.zenyte.discord

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starting Discord bot…" }
    try {
        DiscordBot.init()
        logger.info { "Discord bot started successfully." }
    } catch (ex: Exception) {
        logger.error(ex) { "Failed to start Discord bot." }
        throw ex
    }
}
