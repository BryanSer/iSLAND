package com.github.bryanser.island.api.service

import com.github.bryanser.island.api.BukkitAPI
import org.bukkit.plugin.Plugin
import java.io.ObjectInputStream
import java.util.*

class BukkitImplDelegate(
    val plugin: Plugin,
    val impl: BukkitAPI
) {
    private val methods = hashMapOf<String, (Array<Any?>) -> Unit>()

    init {

    }

    fun onReceivedCall(uuid: UUID, input: ObjectInputStream) {

    }
}