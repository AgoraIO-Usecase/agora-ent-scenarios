package io.agora.scene.showTo1v1

import android.util.Log
import io.agora.scene.base.EntLogger

object ShowTo1v1Logger {

    private val entLogger = EntLogger(EntLogger.Config("ShowTo1v1"))

    @JvmStatic
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    @JvmStatic
    fun e(tag: String, throwable: Throwable? = null, message: String = "") {
        if (throwable != null) {
            Log.e(tag, message)
        } else {
            Log.e(tag, message)
        }
    }

}