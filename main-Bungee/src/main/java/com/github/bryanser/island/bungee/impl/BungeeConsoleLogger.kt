package com.github.bryanser.island.bungee.impl

import com.github.bryanser.island.base.ConsoleLogger
import net.md_5.bungee.api.plugin.Plugin
import java.util.logging.Level

class BungeeConsoleLogger(val plugin: Plugin): ConsoleLogger {
    val name = plugin.description.name
    override fun logD(msg: String) {
        plugin.proxy.logger.log(Level.INFO, "$name [D] $msg")
    }

    override fun logE(msg: String, throwable: Throwable?) {
        plugin.proxy.logger.log(Level.WARNING, "$name [E] $msg")
        throwable?.printStackTrace()
    }
}