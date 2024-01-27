package com.github.bryanser.island.bungee.impl.scheduler

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
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


    protected abstract fun schedule(runnable: Runnable): ScheduledTask

    protected abstract fun schedule(runnable: Runnable, delay: Long): ScheduledTask

    protected abstract fun schedule(runnable: Runnable, delay: Long, interval: Long): ScheduledTask

    override fun createWorker(): Worker {
        return BungeeRxWorker()
    }

    inner class BungeeRxWorker : Worker() {
        private val compositeDisposable = CompositeDisposable()

        override fun schedule(runnable: Runnable, delay: Long, unit: TimeUnit): Disposable {
            val bukkitTask: ScheduledTask = this@AbstractScheduler.schedule(runnable, convertTimeToMillis(delay, unit))
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
            val bukkitTask: ScheduledTask = this@AbstractScheduler.schedule(
                runnable,
                convertTimeToMillis(initialDelay, unit),
                convertTimeToMillis(period, unit)
            )
            val disposable: Disposable = DisposableBukkitTask(bukkitTask)
            compositeDisposable.add(disposable)
            return disposable
        }

        override fun schedule(runnable: Runnable): Disposable {
            val bukkitTask: ScheduledTask = this@AbstractScheduler.schedule(runnable)
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

        private fun convertTimeToMillis(time: Long, timeUnit: TimeUnit): Long {
            return timeUnit.toMillis(time)
        }

        private inner class DisposableBukkitTask constructor(
            private val bungeeTask: ScheduledTask
        ) : Disposable {
            /**
             * If the task is disposed.
             */
            private var disposed = false
            override fun dispose() {
                disposed = true
                bungeeTask.cancel()
            }

            override fun isDisposed(): Boolean {
                return disposed
            }
        }


    }
}