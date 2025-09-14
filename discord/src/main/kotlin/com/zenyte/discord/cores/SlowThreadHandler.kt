package com.zenyte.discord.cores

import mu.KotlinLogging

/**
 * A hidden exception handler for logging silent thread death from the slow executor pool.
 * @author David O'Neill (dlo3)
 */
internal class SlowThreadHandler : Thread.UncaughtExceptionHandler {
    
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        logger.error("(" + thread.name + ", slow pool) - Printing trace")
        throwable.printStackTrace()
    }
    
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
}