package com.github.bryanser.island.api.service

import com.github.bryanser.island.api.BukkitAPI
import com.github.bryanser.island.api.BungeeAPI
import java.util.concurrent.ConcurrentHashMap

object ServiceManager {
    const val CHANNEL_BUKKIT_BUNGEE = "island:bukkit"
    const val CHANNEL_BUNGEE_BUKKIT = "island:bungee"

    @PublishedApi
    internal val bukkitAPICache = ConcurrentHashMap<Class<out BukkitAPI>, BukkitAPI>()
    @PublishedApi
    internal val bungeeAPICache = ConcurrentHashMap<Class<out BungeeAPI>, BungeeAPI>()

    lateinit var bukkitAPIRegister: (Class<out BukkitAPI>) -> BukkitAPI
    lateinit var bungeeAPIRegister: (Class<out BungeeAPI>) -> BungeeAPI

    inline fun <reified API : BukkitAPI> getBukkitAPI(): API {
        val cache = bukkitAPICache[API::class.java] as? API
        if (cache != null) {
            return cache
        } else {
            synchronized(API::class.java){
                (bukkitAPICache[API::class.java] as? API)?.let {
                    return it
                }
                val api = bukkitAPIRegister(API::class.java) as API
                bukkitAPICache[API::class.java] = api
                return api
            }
        }
    }

    inline fun <reified API : BungeeAPI> getBungeeAPI(): API {
        val cache = bungeeAPICache[API::class.java] as? API
        if (cache != null) {
            return cache
        } else {
            synchronized(API::class.java){
                (bungeeAPICache[API::class.java] as? API)?.let {
                    return it
                }
                val api = bungeeAPIRegister(API::class.java) as API
                bungeeAPICache[API::class.java] = api
                return api
            }
        }
    }


}