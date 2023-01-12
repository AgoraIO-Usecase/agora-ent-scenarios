package io.agora.scene.show

import io.agora.scene.base.EntLogger

object ShowLogger {

    private val entLogger = EntLogger(EntLogger.Config("ShowLive"))

    fun d(tag: String, message: String, vararg args: Any){
        entLogger.d(tag, message, args)
    }



}