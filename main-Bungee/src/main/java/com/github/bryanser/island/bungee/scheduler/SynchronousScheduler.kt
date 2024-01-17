package com.github.bryanser.island.bungee.scheduler

import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit

class SynchronousScheduler(
    val plugin: Plugin
) : AbstractScheduler() {
    override fun schedule(runnable: Runnable): ScheduledTask {
        return plugin.proxy.scheduler.schedule(
            plugin, runnable, 0, TimeUnit.SECONDS
        )
    }

    override fun schedule(runnable: Runnable, delay: Long): ScheduledTask {
        return plugin.proxy.scheduler.schedule(
            plugin, runnable, delay, TimeUnit.MILLISECONDS
        )
    }

    override fun schedule(runnable: Runnable, delay: Long, interval: Long): ScheduledTask {
        return plugin.proxy.scheduler.schedule(
            plugin, runnable, delay, interval, TimeUnit.MILLISECONDS
        )
    }
}