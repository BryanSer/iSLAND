package com.github.bryanser.island.api.service

import com.github.bryanser.island.api.BukkitAPI
import com.github.bryanser.island.api.BungeeAPI
import com.github.bryanser.island.api.service.bungee.ProxyInvocationHandlers
import net.md_5.bungee.api.config.ServerInfo
import java.util.concurrent.ConcurrentHashMap

object ServiceManager {
    //schema:type:sender
    const val CHANNEL_BUNGEEAPI_BUKKIT_BUNGEE = "island:bungeeAPI:bukkit"
    const val CHANNEL_BUNGEEAPI_BUNGEE_BUKKIT = "island:bungeeAPI:bungee"

    const val CHANNEL_BUKKITAPI_BUKKIT_BUNGEE = "island:bukkitAPI:bukkit"
    const val CHANNEL_BUKKITAPI_BUNGEE_BUKKIT = "island:bukkitAPI:bungee"

    @PublishedApi
    internal val bukkitAPICache = ConcurrentHashMap<Pair<Class<out BukkitAPI>,ServerInfo>, BukkitAPI>()

    @PublishedApi
    internal lateinit var bukkitAPICreator: (ServerInfo, Class<out BukkitAPI>) -> BukkitAPI

    inline fun <reified API : BukkitAPI> getBukkitAPI(info: ServerInfo): API {
        val key = API::class.java to info
        val cache = bukkitAPICache[API::class.java to info] as? API
        if (cache != null) {
            return cache
        } else {
            synchronized(API::class.java) {
                (bukkitAPICache[key] as? API)?.let {
                    return it
                }
                val api = bukkitAPICreator(info, API::class.java) as API
                bukkitAPICache[key] = api
                return api
            }
        }
    }


}