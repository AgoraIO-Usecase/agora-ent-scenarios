package io.agora.scene.base.utils

import android.util.Log
import io.agora.scene.base.BuildConfig

object LogUtils {
    private var isPrintLog = BuildConfig.DEBUG

    fun d(
        tag: String,
        msg: String
    ): Int {
        return if (isPrintLog) {
            Log.d(tag, msg)
        } else 0
    }

    fun d(msg: String): Int {
        return if (isPrintLog) {
            Log.d("CWT", msg)
        } else 0
    }

    fun i(
        tag: String,
        msg: String
    ): Int {
        return if (isPrintLog) {
            Log.i(tag, msg)
        } else 0
    }

    fun e(
        tag: String,
        msg: String
    ): Int {
        return if (isPrintLog) {
            Log.e(tag, msg)
        } else 0
    }

    fun v(
        tag: String,
        msg: String
    ): Int {
        return if (isPrintLog) {
            Log.v(tag, msg)
        } else 0
    }

    fun w(
        tag: String,
        msg: String
    ): Int {
        return if (isPrintLog) {
            Log.w(tag, msg)
        } else 0
    }

}
