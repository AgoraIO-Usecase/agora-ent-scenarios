package io.agora.scene.playzone

import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.Printer
import io.agora.scene.base.AgoraLogger
import io.agora.scene.base.AgoraScenes

object PlayLogger {

    private val printers: List<Printer> by lazy {
        AgoraLogger.getPrinter(AgoraScenes.PlayZone)
    }

    @JvmStatic
    fun d(tag: String, message: String, vararg args: Any) {
        XLog.tag(tag)
            .printers(*printers.toTypedArray())
            .d(message, args)
    }

    @JvmStatic
    fun w(tag: String, message: String, vararg args: Any) {
        XLog.tag(tag)
            .printers(*printers.toTypedArray())
            .w(message, args)
    }

    @JvmStatic
    fun e(tag: String, message: String, vararg args: Any) {
        XLog.tag(tag)
            .printers(*printers.toTypedArray())
            .e(message, args)
    }

    @JvmStatic
    fun e(tag: String, throwable: Throwable? = null, message: String = "") {
        XLog.tag(tag)
            .printers(*printers.toTypedArray())
            .e(message, throwable)
    }

}