package io.agora.scene.show

import io.agora.scene.base.EntLogger

/*
 * 场景日志模块
 */
object ShowLogger {

    private val entLogger = EntLogger(EntLogger.Config("ShowLive"))

    @JvmStatic
    fun d(tag: String, message: String) {
        entLogger.d(tag, message)
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