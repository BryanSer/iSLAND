package com.github.bryanser.island.bungee.impl.scheduler

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit

class AsynchronousScheduler(
    val plugin: Plugin
) : AbstractScheduler() {
    override fun schedule(runnable: Runnable): ScheduledTask {
        return plugin.proxy.scheduler.runAsync(
            plugin, runnable
        )
    }

    override fun schedule(runnable: Runnable, delay: Long): ScheduledTask {
        return plugin.proxy.scheduler.schedule(
            plugin, {
                plugin.proxy.scheduler.runAsync(
                    plugin, runnable
                )
            }, delay, TimeUnit.MILLISECONDS
        )
    }

    override fun schedule(runnable: Runnable, delay: Long, interval: Long): ScheduledTask {
        return plugin.proxy.scheduler.schedule(
            plugin, {
                plugin.proxy.scheduler.runAsync(
                    plugin, runnable
                )
            }, delay, interval, TimeUnit.MILLISECONDS
        )
    }
}