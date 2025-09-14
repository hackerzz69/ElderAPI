package com.zenyte.discord.cores

import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Centralized hub for executor services in the context of the game engine.
 * Developers should use [ServiceProvider] wrapper methods rather than directly
 * accessing executors.
 *
 * @author David O'Neill
 * @since original implementation
 */
class CoresManager {

    companion object {
        private lateinit var slowExecutor: ScheduledExecutorService
        lateinit var serviceProvider: ServiceProvider
            private set

        private val logger = KotlinLogging.logger {}

        fun purgeSlowExecutor() {
            (slowExecutor as? SlowThreadPoolExecutor)?.purge()
        }

        fun init() {
            slowExecutor = SlowThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                SlowThreadFactory(SlowThreadHandler()),
                "Slow thread pool executor"
            )
            serviceProvider = ServiceProvider(verbose = false)
        }
    }

    /**
     * Provides scheduling and tracking APIs for executors.
     */
    class ServiceProvider internal constructor(private val verbose: Boolean) {

        private val trackedFutures: MutableMap<String, Future<*>> = ConcurrentHashMap()

        init {
            logger.info { "ServiceProvider active and waiting for requests." }
        }

        fun scheduleRepeatingTask(r: Runnable, startDelay: Long, delayCount: Long, unit: TimeUnit) {
            slowExecutor.scheduleWithFixedDelay(wrap(r), startDelay, delayCount, unit)
        }

        fun scheduleRepeatingTask(r: Runnable, startDelay: Long, delayCount: Long) {
            slowExecutor.scheduleWithFixedDelay(wrap(r), startDelay, delayCount, TimeUnit.SECONDS)
        }

        fun scheduleFixedLengthTask(r: FixedLengthRunnable, startDelay: Long, delayCount: Long, unit: TimeUnit) {
            val f = slowExecutor.scheduleWithFixedDelay(wrap(r), startDelay, delayCount, unit)
            r.assignFuture(f)
        }

        fun scheduleFixedLengthTask(r: FixedLengthRunnable, startDelay: Long, delayCount: Long) {
            val f = slowExecutor.scheduleWithFixedDelay(wrap(r), startDelay, delayCount, TimeUnit.SECONDS)
            r.assignFuture(f)
        }

        fun scheduleAndTrackRepeatingTask(r: TrackedRunnable, startDelay: Long, delayCount: Long, unit: TimeUnit) {
            if (trackedFutures.containsKey(r.trackingKey)) {
                logger.warn { "[Service Provider] => Duplicate key ${r.trackingKey}, task not scheduled." }
                return
            }
            val future = slowExecutor.scheduleWithFixedDelay(wrap(r), startDelay, delayCount, unit)
            trackedFutures[r.trackingKey] = future
            if (verbose) logger.info { "[Service Provider] => Tracking new future with key: ${r.trackingKey}" }
        }

        fun cancelTrackedTask(key: String, interrupt: Boolean) {
            val future = trackedFutures.remove(key)
            if (future != null) {
                future.cancel(interrupt)
                purgeSlowExecutor()
                if (verbose) logger.info { "[Service Provider] => Cancelled future with key: $key" }
            }
        }

        fun executeWithDelay(r: Runnable, startDelay: Long, unit: TimeUnit) {
            slowExecutor.schedule(wrap(r), startDelay, unit)
        }

        fun executeWithDelay(r: Runnable, ticks: Int) {
            slowExecutor.schedule(wrap(r), (ticks * 600).toLong(), TimeUnit.MILLISECONDS)
        }

        fun executeNow(r: Runnable) {
            slowExecutor.execute(wrap(r))
        }

        fun submit(r: Runnable): Future<*> = slowExecutor.submit(wrap(r))

        private fun wrap(r: Runnable): Runnable = Runnable {
            try {
                r.run()
            } catch (e: Exception) {
                logger.error(e) { "Task execution error" }
            }
        }
    }
}
