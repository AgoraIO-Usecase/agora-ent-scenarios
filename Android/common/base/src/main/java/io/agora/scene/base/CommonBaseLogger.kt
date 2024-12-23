package io.agora.scene.base

import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.Printer

internal object CommonBaseLogger {

    private val printers: List<Printer> by lazy {
        AgoraLogger.getPrinter(AgoraScenes.CommonBase,false)
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
    fun json(tag: String, json: String) {
        XLog.tag(tag)
            .printers(*printers.toTypedArray())
            .json(json)
    }

    @JvmStatic
    fun xml(tag: String, xml: String) {
        XLog.tag(tag)
            .printers(*printers.toTypedArray())
            .xml(xml)
    }
}