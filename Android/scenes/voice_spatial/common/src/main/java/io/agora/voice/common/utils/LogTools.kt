package io.agora.voice.common.utils

import io.agora.scene.base.EntLogger

object LogTools {
    private val entLogger = EntLogger(EntLogger.Config("Voice"))

     @JvmStatic
    fun String.logD(tag: String = "Agora_VoiceChat") {
         entLogger.d(tag, this)
    }

    @JvmStatic
    fun String.logE(tag: String = "Agora_VoiceChat") {
        entLogger.e(tag, this)
    }

    @JvmStatic
    fun String.logI(tag: String = "Agora_VoiceChat") {
        entLogger.i(tag, this)
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        entLogger.d(tag, message)
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