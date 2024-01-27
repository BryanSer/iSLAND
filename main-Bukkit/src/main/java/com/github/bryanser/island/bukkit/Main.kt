package com.github.bryanser.island.bukkit

import com.github.bryanser.island.base.ConsoleLogger
import com.github.bryanser.island.bukkit.impl.BukkitConsoleLogger
import com.github.bryanser.island.bukkit.impl.scheduler.initRxJava
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        ConsoleLogger.injectLogger(BukkitConsoleLogger(this))
        initRxJava(this)
    }

    override fun onDisable() {
    }
}