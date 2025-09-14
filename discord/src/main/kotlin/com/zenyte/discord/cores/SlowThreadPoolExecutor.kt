package com.zenyte.discord.cores

import mu.KotlinLogging
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory

/**
 * A subtyped [ScheduledThreadPoolExecutor] with error logging
 * cabailities.
 * @author David O'Neill
 */
internal class SlowThreadPoolExecutor
/**
 * Construct a [SlowThreadPoolExecutor] object backed
 * by a [SlowThreadFactory].
 * @param corePoolSize the number of threads to hold in the pool
 * @param threadFactory the `ThreadFactory`
 */
(corePoolSize: Int, threadFactory: ThreadFactory, private val name: String) : ScheduledThreadPoolExecutor(corePoolSize, threadFactory) {
    
    init {
        logger.info("$name open. Fixed thread pool size: $corePoolSize")
    }
    
    public override fun afterExecute(r: Runnable?, t: Throwable?) {
        super.afterExecute(r, t)
        if (t != null) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            t.printStackTrace(pw)
            logger.info("$name caught an exception.")
            logger.error(sw.toString())
        }
    }
    
    companion object {
        private val logger = KotlinLogging.logger {}
    }
}