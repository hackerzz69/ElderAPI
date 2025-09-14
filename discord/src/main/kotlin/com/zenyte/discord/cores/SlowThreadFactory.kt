package com.zenyte.discord.cores

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Thread factory for spawning threads in the slow executor service.
 *
 * Threads are:
 * - Grouped into the current thread's [ThreadGroup]
 * - Named "Slow Pool-{pool}-thread-{n}"
 * - Non-daemon
 * - Minimum priority
 * - Bound to a provided [Thread.UncaughtExceptionHandler]
 *
 * @author David O'Neill
 */
internal class SlowThreadFactory(
    private val handler: Thread.UncaughtExceptionHandler
) : ThreadFactory {

    private val group: ThreadGroup = Thread.currentThread().threadGroup
    private val threadNumber = AtomicInteger(1)
    private val namePrefix = "Slow Pool-${poolNumber.getAndIncrement()}-thread-"

    override fun newThread(r: Runnable): Thread {
        return Thread(group, r, "$namePrefix${threadNumber.getAndIncrement()}").apply {
            isDaemon = false
            priority = Thread.MIN_PRIORITY
            uncaughtExceptionHandler = handler
        }
    }

    companion object {
        private val poolNumber = AtomicInteger(1)
    }
}
