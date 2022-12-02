package io.agora.voice.common.utils

import android.util.Log

object LogTools {
     @JvmStatic
    fun String.logD(tag: String = "Agora_VoiceChat") {
        Log.d(tag, this)
    }

    @JvmStatic
    fun String.logW(tag: String = "Agora_VoiceChat") {
        Log.w(tag, this)
    }

    @JvmStatic
    fun String.logE(tag: String = "Agora_VoiceChat") {
        Log.e(tag, this)
    }
}