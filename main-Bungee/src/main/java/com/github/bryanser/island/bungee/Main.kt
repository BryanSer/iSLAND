package com.github.bryanser.island.bungee

import com.github.bryanser.island.base.ConsoleLogger
import com.github.bryanser.island.bungee.impl.BungeeConsoleLogger
import com.github.bryanser.island.bungee.impl.scheduler.initRxJava
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration

class Main : Plugin() {
    override fun onEnable() {
        ConsoleLogger.injectLogger(BungeeConsoleLogger(this))
        initRxJava(this)
        // Plugin startup logic
        ConfigurationProvider.getProvider(YamlConfiguration::class.java).load("")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}