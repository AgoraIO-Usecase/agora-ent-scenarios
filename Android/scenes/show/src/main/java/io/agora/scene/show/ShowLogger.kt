package io.agora.scene.show

import io.agora.scene.base.EntLogger

object ShowLogger {

    private val entLogger = EntLogger(EntLogger.Config("ShowLive"))

    fun d(tag: String, message: String) {
        entLogger.d(tag, message)
    }

    fun e(tag: String, throwable: Throwable? = null, message: String = "") {
        if (throwable != null) {
            entLogger.e(tag, throwable, message)
        } else {
            entLogger.e(tag, message)
        }
    }

}