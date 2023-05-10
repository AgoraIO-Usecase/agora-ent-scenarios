package io.agora.scene.ktv.singbattle

import io.agora.scene.base.EntLogger

object KTVLogger {

    private val entLogger = EntLogger(EntLogger.Config("KTV"))

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