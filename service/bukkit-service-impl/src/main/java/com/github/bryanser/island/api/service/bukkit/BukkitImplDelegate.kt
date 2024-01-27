package com.github.bryanser.island.api.service.bukkit

import com.github.bryanser.island.api.BukkitAPI
import com.github.bryanser.island.api.service.MethodInfo
import com.github.bryanser.island.base.ConsoleLogger
import io.reactivex.schedulers.Schedulers
import java.io.ObjectInputStream
import java.util.*

class BukkitImplDelegate(
    val service: BukkitServiceImpl,
    val impl: BukkitAPI
) {
    val methodList: List<MethodInfo<out BukkitAPI>>

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
    fun onReceivedCall(uuid: UUID, input: ObjectInputStream) {
        val methodName = input.readUTF()
        val index = input.readInt()
        val info = methodList.getOrNull(index) ?: run {
            ConsoleLogger.logger.logE("unknown method $methodName@$index")
            return
        }
        Schedulers.io().scheduleDirect {
            info.callMethod(uuid, input)?.subscribe { it->
                service.sendResultToBungee(it)
            }
        }


    }
}