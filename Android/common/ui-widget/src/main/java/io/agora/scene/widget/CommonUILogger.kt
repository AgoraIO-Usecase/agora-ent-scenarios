package io.agora.scene.widget

import io.agora.scene.base.EntLogger


internal object CommonUILogger {

    private val entLogger = EntLogger(EntLogger.Config("CommonUI"))

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