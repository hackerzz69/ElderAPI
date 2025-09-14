package com.zenyte.discord.cores

import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class CoresManager {
    
    companion object {
        private lateinit var slowExecutor: ScheduledExecutorService
        lateinit var serviceProvider: ServiceProvider
        
        private val logger = KotlinLogging.logger {}
        
        fun purgeSlowExecutor() {
            (slowExecutor as SlowThreadPoolExecutor).purge()
        }
        
        fun init() {
            slowExecutor = SlowThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                    SlowThreadFactory(SlowThreadHandler()), "Slow thread pool executor")
            serviceProvider = ServiceProvider(false)
        }
    }
    
    /**
     * Serves as a centralized hub for executor services in the context of the game engine. New developers should not have to know which
     * executor to use, but should rather be able to call wrapper methods with generic names and descriptions, and let the
     * `ServiceProvider` choose the correct [java.util.concurrent.ExecutorService];
     *
     * @author David O'Neill
     */
    class ServiceProvider internal constructor(private val verbose: Boolean) {
        
        private val trackedFutures: MutableMap<String, Future<*>>
        
        init {
            trackedFutures = ConcurrentHashMap()
            logger.info("ServiceProvider active and waiting for requests.")
        }
        
        /**
         * Schedules a `Runnable` to be executed after the supplied start delay, and continuously executed thereafter at some
         * specified frequency. This method should be used when there is no intention of stopping the task before server shutdown.<br></br>
         * The start delay and repetition frequency time unit must be supplied.
         *
         * @param r
         * a [Runnable] to repeat
         * @param startDelay
         * time delay before execution begins
         * @param delayCount
         * frequency at which the `run()` method is called.
         * @param unit
         * the specified time unit
         */
        fun scheduleRepeatingTask(r: Runnable, startDelay: Long, delayCount: Long, unit: TimeUnit) {
            slowExecutor.scheduleWithFixedDelay({
                try {
                    r.run()
                } catch (e: Exception) {
                    logger.error(e) { "" }
                }
            }, startDelay, delayCount, unit)
        }
        
        /**
         * Schedules a `Runnable` to be executed after the supplied start delay, and continuously executed thereafter at some
         * specified frequency. This method should be used when there is no intention of stopping the task before server shutdown.<br></br>
         * The start delay and repetition frequency time unit is assumed to be [TimeUnit.SECONDS].
         *
         * @param r
         * a [Runnable] to repeat
         * @param startDelay
         * time delay before execution begins
         * @param delayCount
         * frequency at which the `run()` method is called.
         */
        fun scheduleRepeatingTask(r: Runnable, startDelay: Long, delayCount: Long) {
            slowExecutor.scheduleWithFixedDelay({
                try {
                    r.run()
                } catch (e: Exception) {
                    logger.error(e) { "" }
                }
            }, startDelay, delayCount, TimeUnit.SECONDS)
        }
        
        /**
         * Schedules a [FixedLengthRunnable] to be executed after the supplied start delay, and continuously executed thereafter until
         * [FixedLengthRunnable.repeat] returns false. This method should be used when there is absolute certainty the task will
         * stop executing based on a future condition.<br></br>
         * The start delay and repetition frequency time unit must be supplied.
         *
         * @param r
         * a [FixedLengthRunnable] to repeat
         * @param startDelay
         * time delay before execution begins
         * @param delayCount
         * frequency at which the `run()` method is called.
         * @param unit
         * the specified time unit
         */
        fun scheduleFixedLengthTask(r: FixedLengthRunnable, startDelay: Long, delayCount: Long,
                                    unit: TimeUnit) {
            val f = slowExecutor.scheduleWithFixedDelay({
                try {
                    r.run()
                } catch (e: Exception) {
                    logger.error(e) { "" }
                }
            }, startDelay, delayCount, unit)
            r.assignFuture(f)
        }
        
        /**
         * Schedules a [FixedLengthRunnable] to be executed after the supplied start delay, and continuously executed thereafter until
         * [FixedLengthRunnable.repeat] returns false. This method should be used when there is absolute certainty the task will
         * stop executing based on a future condition.<br></br>
         * The start delay and repetition frequency time unit is assumed to be [TimeUnit.SECONDS].
         *
         * @param r
         * a [FixedLengthRunnable] to repeat
         * @param startDelay
         * time delay before execution begins
         * @param delayCount
         * frequency at which the `run()` method is called.
         */
        fun scheduleFixedLengthTask(r: FixedLengthRunnable, startDelay: Long, delayCount: Long) {
            val f = slowExecutor.scheduleWithFixedDelay({
                try {
                    r.run()
                } catch (e: Exception) {
                    logger.error(e) { "" }
                }
            }, startDelay, delayCount, TimeUnit.SECONDS)
            r.assignFuture(f)
        }
        
        /**
         * Schedules a [TrackedRunnable] to be executed after the supplied start delay, and continuously executes it thereafter at
         * some specified frequency. Furthermore, the associated [Future] is registered with the `ServiceProvider` via the
         * runnables tracking key. The [Future] can then be accessed with the key at a later time. This method should be used when the
         * task will not necessarily be cancelled after a fixed iteration period, but may need to be shutdown at a later, unknown time. In
         * order to retrieve the tracking key, you must have a reference to the [TrackedRunnable], so using an anonymous first
         * argument is discouraged.<br></br>
         * If the String key supplied is already registered with the `ServiceProvider`, the task will NOT be scheduled!<br></br>
         * The start delay and repetition frequency time unit must be supplied.
         *
         * @param r
         * a [Runnable] to repeat
         * @param startDelay
         * time delay before execution begins
         * @param delayCount
         * frequency at which the `run()` method is called.
         * @param unit
         * the specified time unit
         */
        fun scheduleAndTrackRepeatingTask(r: TrackedRunnable, startDelay: Long, delayCount: Long,
                                          unit: TimeUnit) {
            if (trackedFutures.containsKey(r.trackingKey)) {
                System.err.println(log("Attempted to enqueue Future to tracking map, but duplicate key was found. Aborting."))
                return
            }
            val future = slowExecutor.scheduleWithFixedDelay({
                try {
                    r.run()
                } catch (e: Exception) {
                    logger.error(e) { "" }
                }
            }, startDelay, delayCount, unit)
            trackedFutures[r.trackingKey] = future
            if (verbose) {
                logger.info(log("Tracking new future with key: " + r.trackingKey))
            }
        }
        
        /**
         * Attempts to retrieve a [Future] mapped to the supplied key. If the [Future] is present in the `ServiceProvider`
         * mapping, it will be cancelled and purged from the executor pool.
         *
         * @param key
         * the String key (acquired via [TrackedRunnable.getTrackingKey] to lookup a mapped [Future]
         * @param interrupt
         * whether or not the executor service should stop the current execution of the [Future]'s associated
         * [Runnable] if an execution is in progress.
         */
        fun cancelTrackedTask(key: String, interrupt: Boolean) {
            val future = trackedFutures.remove(key)
            if (future != null) {
                future.cancel(interrupt)
                purgeSlowExecutor()
                if (verbose) {
                    logger.info(log("Cancelled future with key: $key"))
                }
            }
        }
        
        /**
         * Schedules a `Runnable` for a one-time execution, but only after a specified start delay. The start delay time unit must be
         * supplied.
         *
         * @param r
         * a [Runnable] to execute once
         * @param startDelay
         * time delay before execution begins
         * @param unit
         * the specified time unit
         */
        fun executeWithDelay(r: Runnable, startDelay: Long, unit: TimeUnit) {
            slowExecutor.schedule({
                try {
                    r.run()
                } catch (e: Exception) {
                    logger.error(e) { "" }
                }
            }, startDelay, unit)
        }
        
        /**
         * Schedules a `Runnable` for a one-time execution, but only after a specified start delay. The start delay time unit is
         * "ticks" by default, meaning units of 600ms. Calling `executeWithDelay(() -> stuff(), 2);` would execute `stuff()`
         * after 2 ticks = 600 ms * 2 = 1200 ms.
         *
         * @param r
         * a [Runnable] to execute once
         * @param ticks
         * the time delay in ticks before execution begins
         */
        fun executeWithDelay(r: Runnable, ticks: Int) {
            slowExecutor.schedule({
                try {
                    r.run()
                } catch (e: Exception) {
                    logger.error("Exception loading game", e)
                }
            }, (ticks * 600).toLong(), TimeUnit.MILLISECONDS)
        }
        
        /**
         * Immediately (as soon as a thread from the thread pool is provided) performs a one-time exeuction of a supplied `Runnable`.
         *
         * @param r
         * a [Runnable] to execute once
         */
        fun executeNow(r: Runnable) {
            slowExecutor.execute({
                try {
                    r.run()
                } catch (e: Exception) {
                    logger.error(e) { "" }
                }
            })
        }
        
        fun submit(r: Runnable): Future<*> {
            return slowExecutor.submit({
                try {
                    r.run()
                } catch (e: Exception) {
                    logger.error(e) { "" }
                }
            })
        }
        
        private fun log(message: String): String {
            val prefix = "[Service Provider] => "
            return prefix + message
        }
        
    }
    
}