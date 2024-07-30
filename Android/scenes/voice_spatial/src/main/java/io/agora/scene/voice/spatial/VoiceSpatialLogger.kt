package io.agora.scene.voice.spatial

import io.agora.scene.base.EntLogger

object VoiceSpatialLogger {
    private val entLogger = EntLogger(EntLogger.Config("Voice_Spatial"))

    @JvmStatic
    fun d(tag: String, message: String) {
        entLogger.d(tag, message)
    }

    @JvmStatic
    fun w(tag: String, message: String, vararg args: Any) {
        entLogger.w(tag, message, args)
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        entLogger.e(tag, message)
    }

    @JvmStatic
    fun i(tag: String, message: String) {
        entLogger.i(tag, message)
    }
}