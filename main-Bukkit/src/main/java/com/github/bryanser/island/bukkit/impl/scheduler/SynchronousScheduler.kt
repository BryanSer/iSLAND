package com.github.bryanser.island.bukkit.impl.scheduler

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

class SynchronousScheduler(
    val plugin: Plugin
) : AbstractScheduler() {
    val scheduler = Bukkit.getScheduler()
    override fun schedule(runnable: Runnable): BukkitTask {
        return scheduler.runTask(plugin, runnable)
    }

    override fun schedule(runnable: Runnable, delay: Long): BukkitTask {
        return scheduler.runTaskLater(plugin, runnable, delay)
    }

    override fun schedule(runnable: Runnable, delay: Long, interval: Long): BukkitTask {
        return scheduler.runTaskTimer(plugin, runnable, delay, interval)
    }
}