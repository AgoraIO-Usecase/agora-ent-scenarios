package io.agora.scene.base

import android.app.Application
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.ClassicFlattener
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.Printer
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy2
import com.elvishew.xlog.printer.file.naming.ChangelessFileNameGenerator
import java.io.File
import java.util.*

enum class AgoraScenes constructor(val value: Int) {

    Common_Base(100),
    Voice_Common(101),
    Voice_Spatial(102),

    ShowLive(110),
    ShowPure(111),
    ShowTo1v1(112),

    KTV_Common(120),
    KTV_Cantata(121),
    KTV_BATTLE(122),
    KTV_RELAY(123),

    Play_Joy(130),
    Play_Zone(131),
}

object AgoraLogger {
    private var isInitialized = false
    private const val LOGCAT = "logcat"

    private val mPrinters = mutableMapOf<String, Printer>()

    @Synchronized
    @JvmStatic
    fun initXLog(app: Application) {
        if (isInitialized) return

        val logDir = File(app.getExternalFilesDir(""), "ent")
        if (!logDir.exists()) logDir.mkdirs()

        val logConfig = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)
            .tag("Agora")
            .build()

        for (scene in AgoraScenes.entries) {
            val filePrinter = FilePrinter.Builder(logDir.absolutePath)
                .fileNameGenerator(ChangelessFileNameGenerator("agora_ent_${scene.name.lowercase()}.log"))
                .backupStrategy(FileSizeBackupStrategy2(1024 * 1024L, 1))
                .flattener(ClassicFlattener())
                .build()

            mPrinters[scene.name] = filePrinter
        }
        mPrinters[LOGCAT] = AndroidPrinter(true)
        XLog.init(logConfig, *mPrinters.values.toTypedArray())

        isInitialized = true
    }

    fun getPrinter(scene: AgoraScenes, needLogcat: Boolean = true): List<Printer> {
        if (!isInitialized) {
            throw RuntimeException("init xlog first!")
        }
        val result = ArrayList<Printer>()
        mPrinters[scene.name]?.let { printer ->
            result.add(printer)
        }
        if (needLogcat && BuildConfig.DEBUG) {
            mPrinters[LOGCAT]?.let { printer ->
                result.add(printer)
            }
        }
        return result
    }
}