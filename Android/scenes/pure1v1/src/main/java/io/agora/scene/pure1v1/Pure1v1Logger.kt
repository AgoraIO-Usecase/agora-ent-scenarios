package io.agora.scene.pure1v1

import io.agora.scene.base.EntLogger

/*
 * 场景日志模块
 */
object Pure1v1Logger {

    private val entLogger = EntLogger(EntLogger.Config("Pure"))

    @JvmStatic
    fun d(tag: String, message: String) {
        entLogger.d(tag, message)
    }

    @JvmStatic
    fun w(tag: String, message: String) {
        entLogger.w(tag, message)
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