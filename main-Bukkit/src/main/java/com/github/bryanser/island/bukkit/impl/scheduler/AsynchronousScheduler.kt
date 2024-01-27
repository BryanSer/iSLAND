package com.github.bryanser.island.bukkit.impl.scheduler

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

class AsynchronousScheduler(
    val plugin: Plugin
): AbstractScheduler() {
    val scheduler = Bukkit.getScheduler()
    override fun schedule(runnable: Runnable): BukkitTask {
        return scheduler.runTaskAsynchronously(plugin, runnable)
    }

    override fun schedule(runnable: Runnable, delay: Long): BukkitTask {
        return scheduler.runTaskLaterAsynchronously(plugin, runnable, delay)
    }

    override fun schedule(runnable: Runnable, delay: Long, interval: Long): BukkitTask {
        return scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, interval)
    }
}