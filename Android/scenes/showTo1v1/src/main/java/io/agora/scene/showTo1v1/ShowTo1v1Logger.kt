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
    fun e(tag: String, throwable: Throwable? = null, message: String = "") {
        entLogger.e(tag, message)
    }

}