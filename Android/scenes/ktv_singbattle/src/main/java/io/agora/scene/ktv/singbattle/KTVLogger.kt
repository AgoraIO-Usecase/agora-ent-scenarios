package io.agora.scene.ktv.singbattle

import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.Printer
import io.agora.scene.base.AgoraLogger
import io.agora.scene.base.AgoraScenes

object KTVLogger {

    private val printers: List<Printer> by lazy {
        AgoraLogger.getPrinter(AgoraScenes.KTV_BATTLE)
    }

    @JvmStatic
    fun d(tag: String, message: String, vararg args: Any) {
        XLog.tag(tag)
            .printers(*printers.toTypedArray())
            .d(message, args)
    }

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
}