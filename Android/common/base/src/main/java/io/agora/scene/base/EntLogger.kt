package io.agora.scene.base

import android.os.*
import com.orhanobut.logger.*
import io.agora.scene.base.component.AgoraApplication
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EntLogger(private val config: Config) {

    companion object {
        private val LogFolder = AgoraApplication.the().getExternalFilesDir("")!!.absolutePath
        private val LogFileWriteThread by lazy {
            HandlerThread("AndroidFileLogger.$LogFolder").apply {
                start()
            }
        }
    }

    data class Config(
        val sceneName: String,
        val fileSize: Int = 2 * 1024 * 1024, // 2M，单位Byte
        val fileName: String = "agora_ent_${sceneName}_Android_${SimpleDateFormat("yyyy-MM-DD", Locale.US).format(Date())}_log".lowercase()
    )

    private val dataFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    init {
        if (BuildConfig.DEBUG) {
            Logger.addLogAdapter(
                object: AndroidLogAdapter(
                    PrettyFormatStrategy.newBuilder()
                        .showThreadInfo(true)          // (Optional) Whether to show thread info or not. Default true
                        .methodCount(1)                 // (Optional) How many method line to show. Default 2
                        .methodOffset(2)                // (Optional) Hides internal method calls up to offset. Default 5
                        .logStrategy(LogcatLogStrategy())   // (Optional) Changes the log strategy to print out. Default LogCat
                        .tag(config.sceneName)                     // (Optional) Global tag for every log. Default PRETTY_LOGGER
                        .build()
                ){
                    override fun isLoggable(priority: Int, tag: String?): Boolean {
                        return tag == config.sceneName
                    }
                }
            )
        }
        Logger.addLogAdapter(
            object: DiskLogAdapter(
                CsvFormatStrategy
                    .newBuilder()
                    .logStrategy(DiskLogStrategy(WriteHandler()))
                    .tag(config.sceneName)
                    .build()
            ){
                override fun isLoggable(priority: Int, tag: String?): Boolean {
                    return tag == config.sceneName
                }
            }
        )
    }


    fun i(tag: String? = null, message: String, vararg args: Any) {
        Logger.t(config.sceneName).i(formatMessage("INFO", tag, message), args)
    }

    fun w(tag: String? = null, message: String, vararg args: Any) {
        Logger.t(config.sceneName).w(formatMessage("Warn", tag, message), args)
    }

    fun d(tag: String? = null, message: String, vararg args: Any) {
        Logger.t(config.sceneName).d(formatMessage("Debug", tag, message), args)
    }

    fun e(tag: String? = null, message: String, vararg args: Any) {
        Logger.t(config.sceneName).e(formatMessage("Error", tag, message), args)
    }

    fun e(tag: String? = null, throwable: Throwable, message: String, vararg args: Any) {
        Logger.t(config.sceneName).e(throwable, formatMessage("Error", tag, message), args)
    }

    private fun formatMessage(level: String, tag: String?, message: String): String {
        val sb = StringBuilder("[Agora][${level}][${config.sceneName}]")
        tag?.let { sb.append("[${tag}]"); }
        sb.append(" : (${dataFormat.format(Date())}) : $message")
        return sb.toString()
    }


    private inner class WriteHandler : Handler(LogFileWriteThread.looper) {

        override fun handleMessage(msg: Message) {
            val content = msg.obj as String
            var fileWriter: FileWriter? = null
            val logFile = getLogFile(LogFolder, config.fileName)
            try {
                fileWriter = FileWriter(logFile, true)
                writeLog(fileWriter, content)
                fileWriter.flush()
                fileWriter.close()
            } catch (e: IOException) {
                if (fileWriter != null) {
                    try {
                        fileWriter.flush()
                        fileWriter.close()
                    } catch (e1: IOException) { /* fail silently */
                    }
                }
            }
        }

        @Throws(IOException::class)
        private fun writeLog(fileWriter: FileWriter, content: String) {
            var writeContent = content
            val agoraTag = writeContent.indexOf("[Agora]")
            if (agoraTag > 0) {
                writeContent = writeContent.substring(agoraTag)
            }
            fileWriter.append(writeContent)
        }

        private fun getLogFile(folderName: String, fileName: String): File {
            val folder = File(folderName)
            if (!folder.exists()) {
                folder.mkdirs()
            }
            var newFileCount = 0
            var newFile: File
            var existingFile: File? = null
            newFile = File(folder, getLogFileFullName(fileName, newFileCount))
            while (newFile.exists()) {
                existingFile = newFile
                newFileCount++
                newFile = File(folder, getLogFileFullName(fileName, newFileCount))
            }
            if (existingFile != null && existingFile.length() < config.fileSize) {
                return existingFile
            } else {
                return newFile
            }
        }

        private fun getLogFileFullName(fileName: String, count: Int) : String{
            if(count == 0){
                return "${fileName}.txt"
            }
            return "${fileName}_${count}.txt"
        }
    }

}

