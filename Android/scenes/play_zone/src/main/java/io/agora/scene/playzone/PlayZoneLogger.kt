package io.agora.scene.playzone

import io.agora.scene.base.EntLogger

object PlayZoneLogger {

    private val entLogger = EntLogger(EntLogger.Config("PlayZone"))

    @JvmStatic
    fun d(tag: String, message: String, vararg args: Any) {
        entLogger.d(tag, message, args)
    }

    @JvmStatic
    fun w(tag: String, message: String, vararg args: Any) {
        entLogger.w(tag, message, args)
    }

    @JvmStatic
    fun e(tag: String, throwable: Throwable? = null, message: String = "") {
        if (throwable != null) {
            entLogger.e(tag, throwable, message)
        } else {
            entLogger.e(tag, message)
        }
    }

}