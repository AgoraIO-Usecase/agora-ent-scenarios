package io.agora.scene.showTo1v1

import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.Printer
import io.agora.scene.base.AgoraLogger
import io.agora.scene.base.AgoraScenes

/*
 * Scene log module
 */
object ShowTo1v1Logger {

    private val printers: List<Printer> by lazy {
        AgoraLogger.getPrinter(AgoraScenes.ShowTo1v1)
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