package com.github.bryanser.island.api.service.bukkit

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
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.lang.IllegalStateException
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

class BukkitServiceImpl(
    val plugin: JavaPlugin
) : PluginMessageListener {
    companion object{
        val gson = GsonBuilder().create()
    }
    private val registerBukkitImpl = HashMap<String, BukkitImplDelegate>()

    init {
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, ServiceManager.CHANNEL_BUNGEEAPI_BUKKIT_BUNGEE)
        Bukkit.getMessenger()
            .registerIncomingPluginChannel(plugin, ServiceManager.CHANNEL_BUNGEEAPI_BUNGEE_BUKKIT, this)
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, ServiceManager.CHANNEL_BUKKITAPI_BUKKIT_BUNGEE)
        Bukkit.getMessenger()
            .registerIncomingPluginChannel(plugin, ServiceManager.CHANNEL_BUKKITAPI_BUNGEE_BUKKIT, this)
        ServiceManager.bungeeAPICreator = {
            createBungeeAPIDelegate(it)
        }
        ServiceManager.bukkitAPICreator = {
            registerBukkitImpl[it.name]!!.impl
        }

    }

    fun registerBukkitAPI(api: BukkitAPI) {
        registerBukkitImpl[api.javaClass.name] = BukkitImplDelegate(this, api)
    }

    private fun <T : BungeeAPI> createBungeeAPIDelegate(clazz: Class<T>): T {
        return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), ProxyInvocationHandler(this, clazz)) as T
    }

    private val emitters = ConcurrentHashMap<UUID, Pair<Type?, SingleEmitter<Any>>>()

    internal fun sendRequestToBungee(
        uuid: UUID,
        request: Single<ByteArray>,
        isSingle: Boolean,
        singleType: Type?
    ): Single<Any> {
        if (!isSingle) {
            request.subscribeBy({
                Bukkit.getServer().sendPluginMessage(
                    plugin, ServiceManager.CHANNEL_BUNGEEAPI_BUKKIT_BUNGEE, it
                )
            })
            return Single.just(true)
        }
        return Single.create<Any> {
            emitters[uuid] = singleType to it
            request.subscribeBy({
                Bukkit.getServer().sendPluginMessage(
                    plugin, ServiceManager.CHANNEL_BUNGEEAPI_BUKKIT_BUNGEE, it
                )
            })
        }.doOnDispose {
            emitters.remove(uuid)
        }

    }

    internal fun sendResultToBungee(message: ByteArray){
        Bukkit.getServer().sendPluginMessage(
            plugin, ServiceManager.CHANNEL_BUKKITAPI_BUKKIT_BUNGEE, message
        )
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

    private fun onRPCReceived(message: ByteArray) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            val byteIn = ByteArrayInputStream(message)
            val input = ObjectInputStream(byteIn)
            val uuid = UUID(input.readLong(), input.readLong())
            val className = input.readUTF()
            registerBukkitImpl[className]?.onReceivedCall(
                uuid, input
            )
        }

    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel == ServiceManager.CHANNEL_BUKKITAPI_BUNGEE_BUKKIT) {
            onRPCReceived(message)
        }
        if (channel == ServiceManager.CHANNEL_BUNGEEAPI_BUNGEE_BUKKIT) {
            onCallbackReceived(message)
        }

    }
}

