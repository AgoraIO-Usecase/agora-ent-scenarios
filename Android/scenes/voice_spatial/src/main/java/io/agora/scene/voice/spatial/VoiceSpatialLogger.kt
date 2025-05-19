package io.agora.scene.voice.spatial

import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.Printer
import io.agora.scene.base.AgoraLogger
import io.agora.scene.base.AgoraScenes

object VoiceSpatialLogger {
    private val printers: List<Printer> by lazy {
        AgoraLogger.getPrinter(AgoraScenes.Voice_Spatial)
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
}