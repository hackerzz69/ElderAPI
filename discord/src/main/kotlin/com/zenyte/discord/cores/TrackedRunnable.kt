package com.zenyte.discord.cores

import java.util.*
import java.util.concurrent.Future

/**
 * A [Runnable] subtype intended to run for a dynamic iteration period
 * in the context of a [java.util.concurrent.ScheduledExecutorService].
 * @author David O'Neill
 */
abstract class TrackedRunnable : Runnable {
    
    /**
     * Returns this runnables tracing key. This key
     * should be supplied to the [com.rs.cores.CoresManager.ServiceProvider]
     * to cancel the [Future] associated with this runnable.
     * @return the tracking key for this runnable
     */
    val trackingKey = UUID.randomUUID().toString()
    
    abstract override fun run()
    
}