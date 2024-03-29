package com.github.bryanser.island.api.service

import com.github.bryanser.island.api.BukkitAPI
import com.github.bryanser.island.api.BungeeAPI
import java.util.concurrent.ConcurrentHashMap

object ServiceManager {
    //schema:type:sender
    const val CHANNEL_BUNGEEAPI_BUKKIT_BUNGEE = "island:bungeeAPI:bukkit"
    const val CHANNEL_BUNGEEAPI_BUNGEE_BUKKIT = "island:bungeeAPI:bungee"

    const val CHANNEL_BUKKITAPI_BUKKIT_BUNGEE = "island:bukkitAPI:bukkit"
    const val CHANNEL_BUKKITAPI_BUNGEE_BUKKIT = "island:bukkitAPI:bungee"

    @PublishedApi
    internal val bungeeAPICache = ConcurrentHashMap<Class<out BungeeAPI>, BungeeAPI>()

    @PublishedApi
    internal lateinit var bungeeAPICreator: (Class<out BungeeAPI>) -> BungeeAPI
    @PublishedApi
    internal lateinit var bukkitAPICreator: (Class<out BukkitAPI>) -> BukkitAPI

    inline fun <reified API : BungeeAPI> getBungeeAPI(): API {
        val cache = bungeeAPICache[API::class.java] as? API
        if (cache != null) {
            return cache
        } else {
            synchronized(API::class.java) {
                (bungeeAPICache[API::class.java] as? API)?.let {
                    return it
                }
                val api = bungeeAPICreator(API::class.java) as API
                bungeeAPICache[API::class.java] = api
                return api
            }
        }
    }

    inline fun <reified API : BukkitAPI> getBukkitAPI(): API {
        return bukkitAPICreator(API::class.java) as API
    }

}