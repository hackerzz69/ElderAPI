package com.zenyte.discord.cores

import java.util.UUID

/**
 * A [Runnable] subtype intended to run for a dynamic iteration period
 * in the context of a [java.util.concurrent.ScheduledExecutorService].
 *
 * Each runnable is assigned a unique [trackingKey] so it can be
 * managed (e.g. cancelled) via [CoresManager.ServiceProvider].
 *
 * @author David O'Neill
 */
abstract class TrackedRunnable : Runnable {

    /**
     * Unique tracking key for this runnable.
     * Used by [CoresManager.ServiceProvider] to identify and cancel tasks.
     */
    val trackingKey: String = UUID.randomUUID().toString()

    /**
     * Defines the intended logic for this task.
     */
    abstract override fun run()
}
