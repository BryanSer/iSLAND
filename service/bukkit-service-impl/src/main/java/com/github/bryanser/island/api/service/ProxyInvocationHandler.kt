package com.github.bryanser.island.api.service

import com.github.bryanser.island.api.BungeeAPI
import com.google.gson.GsonBuilder
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.lang.IllegalArgumentException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.UUID

class ProxyInvocationHandler(
    val service:BukkitServiceImpl,
    val targetClass: Class<out BungeeAPI>
) : InvocationHandler {

    private val methodMap = hashMapOf<Method, (Array<out Any?>) -> Any?>()
    val gson = GsonBuilder().create()

    init {
        val methods = targetClass.methods
        for ((index, method) in methods.withIndex()) {
            val returnType = method.returnType
            val isSingle: Boolean
            val singleType: Type?
            if (returnType == Single::class.java) {
                isSingle = true
                val returnResultType = method.genericReturnType
                if (returnResultType is ParameterizedType) {
                    singleType = returnResultType.actualTypeArguments.first()
                } else {
                    singleType = null
                }
            } else if (returnType == Void::class.javaPrimitiveType) {
                isSingle = false
                singleType = null
            } else {
                throw IllegalArgumentException()
            }
            val typeList = method.parameterTypes
            val methodName = method.name
            val className = targetClass.name
            methodMap[method] = { args ->
                val uid = UUID.randomUUID()
                val single = Single.fromCallable {
                    val byteOutput = ByteArrayOutputStream(128)
                    val out = ObjectOutputStream(byteOutput)
                    out.writeLong(uid.mostSignificantBits)
                    out.writeLong(uid.leastSignificantBits)
                    out.writeUTF(className)
                    out.writeUTF(methodName)
                    out.writeInt(index)
                    out.writeInt(args.size)
                    for ((i, type) in typeList.withIndex()) {
                        val arg = args[i]
                        out.writeNext(arg)
                    }
                    out.flush()
                    byteOutput.flush()
                    byteOutput.toByteArray().also {
                        out.close()
                        byteOutput.close()
                    }
                }.subscribeOn(Schedulers.io())

                val request = service.sendRequestToBungee(uid, single, isSingle, singleType)
                if(isSingle){
                    request
                }else{
                    request.subscribe()
                    null
                }
            }
        }
    }


    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
        return methodMap[method]?.invoke(args)
    }
}