package com.zenyte.discord.cores

import java.util.concurrent.Future

/**
 * A [Runnable] subtype intended to run for a fixed iteration period
 * in the context of a [java.util.concurrent.ScheduledExecutorService].
 * @author David O'Neill
 */
abstract class FixedLengthRunnable : Runnable {
    
    private var future: Future<*>? = null
    
    /**
     * Iteratively runs `repeat()` until
     * it returns `false`, at which point
     * this runnable is cancelled from the executor.
     */
    override fun run() {
        if (!repeat()) cancel()
    }
    
    /**
     * Defines the intended logic to be repeated, and
     * should return `false` when the runnable
     * should be cancelled and dequeued.
     * @return whether or not to run another iteration
     */
    abstract fun repeat(): Boolean
    
    /**
     * Assign this runnables [Future] reference.
     * @param future - the future associated with this runnable
     */
    internal fun assignFuture(future: Future<*>) {
        this.future = future
    }
    
    /**
     * Unschedule the runnable, and ask the [SlowThreadPoolExecutor]
     * to purge the cancelled task from the pool's queue.
     */
    private fun cancel() {
        future!!.cancel(false)
        CoresManager.purgeSlowExecutor()
    }
    
    /**
     *
     * Unschedule the runnable, and ask the [SlowThreadPoolExecutor]
     * to purge the cancelled task from the pool's queue.
     *
     * @param interrupt boolean indicating whether or not current execution
     * state should be killed regardless of whether or
     * not it has finished
     */
    fun stopNow(interrupt: Boolean) {
        future!!.cancel(interrupt)
        CoresManager.purgeSlowExecutor()
    }
}
