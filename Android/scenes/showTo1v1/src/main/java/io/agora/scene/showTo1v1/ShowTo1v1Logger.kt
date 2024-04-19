package io.agora.scene.showTo1v1

import io.agora.scene.base.EntLogger

/*
 * 场景日志模块
 */
object ShowTo1v1Logger {

    private val entLogger = EntLogger(EntLogger.Config("ShowTo1v1"))

    @JvmStatic
    fun d(tag: String, message: String) {
        entLogger.d(tag, message)
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