package com.github.bryanser.island.bukkit.scheduler

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit
import java.util.logging.Level


fun initRxJava(plugin: Plugin){
    val syncScheduler = SynchronousScheduler(plugin)
    val asyncScheduler = AsynchronousScheduler(plugin)
    RxJavaPlugins.setErrorHandler { throwable: Throwable? ->
        plugin.logger.log(Level.SEVERE, "Unhandled exception. ", throwable)
    }
    RxJavaPlugins.setInitComputationSchedulerHandler {
        syncScheduler
    }
    RxJavaPlugins.setInitIoSchedulerHandler {
        asyncScheduler
    }
    RxJavaPlugins.setInitNewThreadSchedulerHandler {
        asyncScheduler
    }
    RxJavaPlugins.setComputationSchedulerHandler {
        syncScheduler
    }
    RxJavaPlugins.setIoSchedulerHandler {
        asyncScheduler
    }
    RxJavaPlugins.setNewThreadSchedulerHandler {
        asyncScheduler
    }
}

abstract class AbstractScheduler : Scheduler() {


    protected abstract fun schedule(runnable: Runnable): BukkitTask

    protected abstract fun schedule(runnable: Runnable, delay: Long): BukkitTask

    protected abstract fun schedule(runnable: Runnable, delay: Long, interval: Long): BukkitTask

    override fun createWorker(): Worker {
        return BukkitRxWorker()
    }

    inner class BukkitRxWorker : Worker() {
        private val compositeDisposable = CompositeDisposable()

        override fun schedule(runnable: Runnable, delay: Long, unit: TimeUnit): Disposable {
            val bukkitTask: BukkitTask = this@AbstractScheduler.schedule(runnable, convertTimeToTicks(delay, unit))
            val disposable: Disposable = DisposableBukkitTask(bukkitTask)
            compositeDisposable.add(disposable)
            return disposable
        }

        override fun schedulePeriodically(
            runnable: Runnable,
            initialDelay: Long,
            period: Long,
            unit: TimeUnit
        ): Disposable {
            val bukkitTask: BukkitTask = this@AbstractScheduler.schedule(
                runnable,
                convertTimeToTicks(initialDelay, unit),
                convertTimeToTicks(period, unit)
            )
            val disposable: Disposable = DisposableBukkitTask(bukkitTask)
            compositeDisposable.add(disposable)
            return disposable
        }

        override fun schedule(runnable: Runnable): Disposable {
            val bukkitTask: BukkitTask = this@AbstractScheduler.schedule(runnable)
            val disposable: Disposable = DisposableBukkitTask(bukkitTask)
            compositeDisposable.add(disposable)
            return disposable
        }

        override fun dispose() {
            compositeDisposable.dispose();
        }

        override fun isDisposed(): Boolean {
            return compositeDisposable.isDisposed();
        }

        private fun convertTimeToTicks(time: Long, timeUnit: TimeUnit): Long {
            return Math.round(timeUnit.toMillis(time).toDouble() / 50.0).toLong()
        }

        private inner class DisposableBukkitTask constructor(
            private val bukkitTask: BukkitTask
        ) : Disposable {
            /**
             * If the task is disposed.
             */
            private var disposed = false
            override fun dispose() {
                disposed = true
                bukkitTask.cancel()
            }

            override fun isDisposed(): Boolean {
                return disposed && !bukkitTask.owner.server.scheduler.isCurrentlyRunning(bukkitTask.taskId)
            }
        }


    }
}