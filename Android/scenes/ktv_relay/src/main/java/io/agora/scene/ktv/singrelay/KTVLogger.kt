package io.agora.scene.ktv.singrelay

import io.agora.scene.base.EntLogger

object KTVLogger {

    private val entLogger = EntLogger(EntLogger.Config("KTV_RELAY"))

    @JvmStatic
    fun d(tag: String, message: String, vararg args: Any) {
        entLogger.d(tag, message, args)
    }

    @JvmStatic
    fun w(tag: String, message: String, vararg args: Any) {
        entLogger.w(tag, message, args)
    }

    @JvmStatic
    fun e(tag: String, message: String, vararg args: Any) {
        entLogger.e(tag, message, args)
    }

}