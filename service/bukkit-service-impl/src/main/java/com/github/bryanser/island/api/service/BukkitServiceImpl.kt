package com.github.bryanser.island.api.service

import com.github.bryanser.island.api.BungeeAPI
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

class BukkitServiceImpl(
    val plugin: JavaPlugin
) : PluginMessageListener {
    val gson = GsonBuilder().create()

    init {
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, ServiceManager.CHANNEL_BUKKIT_BUNGEE)
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, ServiceManager.CHANNEL_BUNGEE_BUKKIT, this)
        ServiceManager.bungeeAPIRegister = {
            createBungeeAPIDelegate(it)
        }

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
                    plugin, ServiceManager.CHANNEL_BUKKIT_BUNGEE, it
                )
            })
            return Single.just(true)
        }
        return Single.create<Any> {
            emitters[uuid] = singleType to it
            request.subscribeBy({
                Bukkit.getServer().sendPluginMessage(
                    plugin, ServiceManager.CHANNEL_BUKKIT_BUNGEE, it
                )
            })
        }.doOnDispose {
            emitters.remove(uuid)
        }

    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != ServiceManager.CHANNEL_BUNGEE_BUKKIT) {
            return
        }
        Observable.fromCallable {
            val byteIn = ByteArrayInputStream(message)
            val input = ObjectInputStream(byteIn)
            val uuid = UUID(input.readLong(), input.readLong())
            val (singleType, emitter) = emitters.remove(uuid) ?: throw IllegalStateException()
            val result: Any = if (singleType != null) {
                when (singleType) {
                    Int::class.java -> {
                        input.readInt()
                    }

                    Short::class.java -> {
                        input.readShort()
                    }

                    Byte::class.java -> {
                        input.readBoolean()
                    }

                    Long::class.java -> {
                        input.readLong()
                    }

                    Float::class.java -> {
                        input.readFloat()
                    }

                    Double::class.java -> {
                        input.readDouble()
                    }

                    Boolean::class.java -> {
                        input.readBoolean()
                    }

                    String::class.java -> {
                        input.readUTF()
                    }

                    else -> {
                        gson.fromJson(input.readUTF(), singleType)
                    }
                }
            } else {
                Any()
            }
            result to emitter
        }.subscribeOn(Schedulers.io())
            .subscribe {
                it.second.onSuccess(it.first)
            }

    }
}