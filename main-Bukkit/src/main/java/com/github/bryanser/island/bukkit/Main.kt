package com.github.bryanser.island.bukkit

import com.github.bryanser.island.bukkit.scheduler.initRxJava
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        initRxJava(this)
    }

    override fun onDisable() {
    }
}