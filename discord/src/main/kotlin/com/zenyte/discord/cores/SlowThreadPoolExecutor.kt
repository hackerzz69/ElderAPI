package com.zenyte.discord.cores

import mu.KotlinLogging
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory

/**
 * ScheduledThreadPoolExecutor with error logging capabilities.
 *
 * Ensures uncaught exceptions in tasks are logged instead of silently discarded.
 *
 * @author David O'Neill
 */
internal class SlowThreadPoolExecutor(
    corePoolSize: Int,
    threadFactory: ThreadFactory,
    private val name: String
) : ScheduledThreadPoolExecutor(corePoolSize, threadFactory) {

    init {
        logger.info { "$name open. Fixed thread pool size: $corePoolSize" }
    }

    override fun afterExecute(r: Runnable?, t: Throwable?) {
        super.afterExecute(r, t)
        if (t != null) {
            logger.error(t) { "$name caught an exception in task ${r?.javaClass?.simpleName}" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
