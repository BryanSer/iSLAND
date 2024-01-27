package com.github.bryanser.island.base

interface ConsoleLogger {
    fun logD(msg: String)

    fun logE(msg: String, throwable: Throwable? = null)

    companion object {
        lateinit var logger: ConsoleLogger
        fun injectLogger(logger: ConsoleLogger){
            this.logger = logger
        }
    }
}