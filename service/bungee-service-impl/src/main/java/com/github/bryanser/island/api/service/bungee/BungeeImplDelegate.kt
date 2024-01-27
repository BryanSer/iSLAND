package com.github.bryanser.island.api.service.bungee

import com.github.bryanser.island.api.BungeeAPI
import com.github.bryanser.island.api.service.MethodInfo
import com.github.bryanser.island.base.ConsoleLogger
import io.reactivex.schedulers.Schedulers
import net.md_5.bungee.api.connection.Server
import java.io.ObjectInputStream
import java.util.*

class BungeeImplDelegate(
    val service: BungeeServiceImpl,
    val impl: BungeeAPI
) {
    val methodList: List<MethodInfo<out BungeeAPI>>
    init {
        val methods = impl.javaClass.methods
        methodList = methods.mapIndexed { index, method ->
            MethodInfo(index, method, impl)
        }
    }

    /**
     * 协议:
     * i32 uuid-most consumed
     * i32 uuid-least consumed
     * utf className consumed
     * utf methodName
     * i32 method index
     * i32 arg size
     * var arg
     *
     */
    fun onReceivedCall(uuid: UUID, input: ObjectInputStream, from:Server) {
        val methodName = input.readUTF()
        val index = input.readInt()
        val info = methodList.getOrNull(index) ?: run {
            ConsoleLogger.logger.logE("unknown method $methodName@$index")
            return
        }
        Schedulers.io().scheduleDirect {
            info.callMethod(uuid, input)?.subscribe { it->
                service.sendResultToBukkit(from.info, it)
            }
        }


    }
}