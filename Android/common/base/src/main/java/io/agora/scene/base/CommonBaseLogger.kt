package io.agora.scene.base

import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.Printer

internal object CommonBaseLogger {

    private val printers: List<Printer> by lazy {
        AgoraLogger.getPrinter(AgoraScenes.Common_Base,false)
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        XLog.tag(tag)
            .printers(*printers.toTypedArray())
            .d(message)
    }

    @JvmStatic
    fun w(tag: String, message: String) {
        XLog.tag(tag)
            .printers(*printers.toTypedArray())
            .w(message)
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        XLog.tag(tag)
            .printers(*printers.toTypedArray())
            .e(message)
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