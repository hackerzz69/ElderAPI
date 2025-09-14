package com.zenyte.discord.cores

import java.util.concurrent.Future

/**
 * A [Runnable] subtype intended to run for a fixed iteration period
 * in the context of a [java.util.concurrent.ScheduledExecutorService].
 *
 * Iteratively calls [repeat] until it returns false, then self-cancels.
 *
 * @author David O'Neill
 */
abstract class FixedLengthRunnable : Runnable {

    private var future: Future<*>? = null

    override fun run() {
        try {
            if (!repeat()) cancel()
        } catch (e: Exception) {
            // If repeat() throws, fail fast and cancel the task
            cancel()
            throw e
        }
    }

    /**
     * Defines the logic to repeat. Return `false` when the task
     * should stop running.
     */
    protected abstract fun repeat(): Boolean

    /** Assign this runnable's [Future]. Called by [CoresManager]. */
    internal fun assignFuture(future: Future<*>) {
        this.future = future
    }

    /** Cancel this runnable without interrupting. */
    private fun cancel() {
        future?.cancel(false)
        CoresManager.purgeSlowExecutor()
    }

    /**
     * Cancel this runnable, with optional interruption of current execution.
     *
     * @param interrupt whether to stop execution immediately
     */
    fun stopNow(interrupt: Boolean) {
        future?.cancel(interrupt)
        CoresManager.purgeSlowExecutor()
    }
}
