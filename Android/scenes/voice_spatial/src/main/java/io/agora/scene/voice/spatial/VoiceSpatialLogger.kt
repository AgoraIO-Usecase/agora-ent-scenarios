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

    /**
     * 获取打印信息所在方法名，行号等信息
     */
    private val autoJumpLogInfoArray: Array<String>
        get() {
            val infoArray = arrayOf("", "", "")
            val elements = Thread.currentThread().stackTrace
            infoArray[0] = elements[4].className.substring(elements[4].className.lastIndexOf(".") + 1)
            infoArray[1] = elements[4].methodName
            infoArray[2] = "(" + elements[4].fileName + ":" + elements[4].lineNumber + ")"
            return infoArray
        }
}