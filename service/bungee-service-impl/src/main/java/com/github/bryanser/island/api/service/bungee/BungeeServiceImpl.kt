package com.github.bryanser.island.api.service.bungee

import com.github.bryanser.island.api.BukkitAPI
import com.github.bryanser.island.api.BungeeAPI
import com.github.bryanser.island.api.service.ServiceManager
import com.github.bryanser.island.api.service.readNext
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.lang.IllegalStateException
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

class BungeeServiceImpl(
    val plugin: Plugin
) : Listener {

    companion object {
        val gson = GsonBuilder().create()
    }

    private val emitters = ConcurrentHashMap<UUID, Pair<Type?, SingleEmitter<Any>>>()
    private val handlerCache = ConcurrentHashMap<Class<out BukkitAPI>, ProxyInvocationHandlers>()
    private val registerBungeeImpl = HashMap<String, BungeeImplDelegate>()

    init {
        plugin.proxy.registerChannel(ServiceManager.CHANNEL_BUNGEEAPI_BUKKIT_BUNGEE)
        plugin.proxy.registerChannel(ServiceManager.CHANNEL_BUNGEEAPI_BUNGEE_BUKKIT)
        plugin.proxy.registerChannel(ServiceManager.CHANNEL_BUKKITAPI_BUKKIT_BUNGEE)
        plugin.proxy.registerChannel(ServiceManager.CHANNEL_BUKKITAPI_BUNGEE_BUKKIT)
        plugin.proxy.pluginManager.registerListener(plugin, this)
        ServiceManager.bukkitAPICreator = { info, clazz ->
            createBukkitAPIDelegate(info, clazz)
        }
    }

    private fun <T : BukkitAPI> createBukkitAPIDelegate(info: ServerInfo, clazz: Class<T>): T {
        val cache = handlerCache[clazz]
        if (cache != null) {
            return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), cache.createInvocationHandler(info)) as T
        }
        val handlers = ProxyInvocationHandlers(this, clazz)
        handlerCache[clazz] = handlers
        return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), handlers.createInvocationHandler(info)) as T
    }

    fun registerBungeeAPI(api: BungeeAPI) {
        registerBungeeImpl[api.javaClass.name] = BungeeImplDelegate(this, api)
    }

    private fun onRPCReceived(message: ByteArray, from: Server) {
        Schedulers.io().scheduleDirect {
            val byteIn = ByteArrayInputStream(message)
            val input = ObjectInputStream(byteIn)
            val uuid = UUID(input.readLong(), input.readLong())
            val className = input.readUTF()
            registerBungeeImpl[className]?.onReceivedCall(
                uuid, input, from
            )
        }

    }


    private fun onCallbackReceived(message: ByteArray) {
        Observable.fromCallable {
            val byteIn = ByteArrayInputStream(message)
            val input = ObjectInputStream(byteIn)
            val uuid = UUID(input.readLong(), input.readLong())
            val (singleType, emitter) = emitters.remove(uuid) ?: throw IllegalStateException()
            val result: Any = if (singleType != null) {
                input.readNext(singleType) ?: throw IllegalStateException()
            } else {
                Any()
            }
            result to emitter
        }.subscribeOn(Schedulers.io())
            .subscribe {
                it.second.onSuccess(it.first)
            }

    }

    @EventHandler
    fun onMessage(evt: PluginMessageEvent) {
        val sender = evt.sender
        if (sender !is Server) {
            return
        }
        val channel = evt.tag
        if (channel == ServiceManager.CHANNEL_BUKKITAPI_BUKKIT_BUNGEE) {
            onCallbackReceived(evt.data)
        } else if (channel == ServiceManager.CHANNEL_BUNGEEAPI_BUKKIT_BUNGEE) {
            onRPCReceived(evt.data, sender)
        }
    }

    fun sendResultToBukkit(server: ServerInfo, message: ByteArray) {
        server.sendData(
            ServiceManager.CHANNEL_BUNGEEAPI_BUNGEE_BUKKIT, message
        )
    }

    fun sendRequestToBukkit(
        server: ServerInfo,
        uuid: UUID,
        request: Single<ByteArray>,
        isSingle: Boolean,
        singleType: Type?
    ): Single<Any> {
        if (!isSingle) {
            request.subscribeBy({
                server.sendData(
                    ServiceManager.CHANNEL_BUKKITAPI_BUNGEE_BUKKIT, it
                )
            })
            return Single.just(true)
        }
        return Single.create<Any> {
            emitters[uuid] = singleType to it
            request.subscribeBy({
                server.sendData(
                    ServiceManager.CHANNEL_BUKKITAPI_BUNGEE_BUKKIT, it
                )
            })
        }.doOnDispose {
            emitters.remove(uuid)
        }
    }
}