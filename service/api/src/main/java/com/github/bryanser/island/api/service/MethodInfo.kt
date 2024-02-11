package com.github.bryanser.island.api.service

import com.github.bryanser.island.api.RemoteAPI
import com.github.bryanser.island.base.ConsoleLogger
import com.github.bryanser.island.base.globalJson
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.serializer
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.IllegalArgumentException
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

class MethodInfo<T : RemoteAPI>(
    val index: Int,
    val method: Method,
    val invokeThis: T
) {
    val argSize: Int
    val argTypeList: List<Type>
    val isSingle: Boolean
    val singleType: Type?
    val name: String

    init {
        val args = method.parameterTypes
        argSize = args.size
        argTypeList = args.toList()
        name = method.name
        val returnType = method.returnType

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
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun callMethod(uuid: UUID, input: ObjectInputStream):Single<ByteArray>?{
        val size = input.readInt()
        if (size != argSize) {
            ConsoleLogger.logger.logE("method($name@$index) call error, input arg size = $size, request size = $argSize")
            return null
        }
        val argArray = arrayOfNulls<Any?>(size)
        for ((i, type) in argTypeList.withIndex()) {
            val arg = input.readNext(type)
            argArray[i] = arg
        }
        val result = method.invoke(invokeThis, *argArray)
        if (isSingle && singleType != null && result is Single<*>) {
            return result.subscribeOn(Schedulers.io()).map {
                val byteOut = ByteArrayOutputStream()
                val output = ObjectOutputStream(byteOut)
                output.writeLong(uuid.mostSignificantBits)
                output.writeLong(uuid.leastSignificantBits)
                output.writeNext(it)
                output.flush()
                byteOut.toByteArray()
            }
        }else{
            return null
        }
    }
}

fun ObjectOutputStream.writeNext(arg: Any?) {
    if (arg != null) {
        this.writeBoolean(true)
    } else {
        this.writeBoolean(false)
        return
    }

    when (arg) {
        is Int -> {
            this.writeInt(arg)
        }

        is Byte -> {
            this.writeByte(arg.toInt())
        }

        is Short -> {
            this.writeShort(arg.toInt())
        }

        is Long -> {
            this.writeLong(arg)
        }

        is Float -> {
            this.writeFloat(arg)
        }

        is Double -> {
            this.writeDouble(arg)
        }

        is Boolean -> {
            this.writeBoolean(arg)
        }

        is String -> {
            this.writeUTF(arg)
        }

        else -> {
            val json = globalJson.encodeToString(arg)
            this.writeUTF(json)
        }
    }
}

fun ObjectInputStream.readNext(type: Type): Any? {
    val isNotNull = this.readBoolean()
    if (!isNotNull) {
        return null
    }
    return when (type) {
        Int::class.java -> {
            this.readInt()
        }

        Short::class.java -> {
            this.readShort()
        }

        Byte::class.java -> {
            this.readBoolean()
        }

        Long::class.java -> {
            this.readLong()
        }

        Float::class.java -> {
            this.readFloat()
        }

        Double::class.java -> {
            this.readDouble()
        }

        Boolean::class.java -> {
            this.readBoolean()
        }

        String::class.java -> {
            this.readUTF()
        }

        else -> {
            globalJson.decodeFromString(serializer(type), this.readUTF())
        }
    }
}
