package com.github.bryanser.island.bukkit.impl

import com.github.bryanser.island.base.ConsoleLogger
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class BukkitConsoleLogger(
    val plugin: JavaPlugin
) : ConsoleLogger{
    override fun logD(msg: String) {
        Bukkit.getConsoleSender().sendMessage("${plugin.name} [D] $msg")
    }

    override fun logE(msg: String, throwable: Throwable?) {
        Bukkit.getConsoleSender().sendMessage("${plugin.name} [E] $msg")
        throwable?.printStackTrace()
    }
}