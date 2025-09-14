package com.zenyte.discord.cores

import mu.KotlinLogging

/**
 * Exception handler for logging uncaught exceptions in the slow executor pool.
 *
 * Ensures that silent thread death is logged with full context.
 *
 * @author David O'Neill
 */
internal class SlowThreadHandler : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        logger.error(throwable) { "Uncaught exception in thread '${thread.name}' (slow pool)" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
