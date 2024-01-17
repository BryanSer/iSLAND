package com.github.bryanser.island.bungee

import com.github.bryanser.island.bungee.scheduler.initRxJava
import net.md_5.bungee.api.plugin.Plugin

class Main : Plugin() {
    override fun onEnable() {
        initRxJava(this)
        // Plugin startup logic
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}