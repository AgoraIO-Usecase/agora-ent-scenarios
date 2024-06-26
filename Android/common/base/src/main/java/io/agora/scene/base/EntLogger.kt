package io.agora.scene.base

import android.os.*
import com.orhanobut.logger.*
import io.agora.scene.base.component.AgoraApplication
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EntLogger constructor(private val config: Config) {

    companion object {
        private val MAX_FILE_COUNT = 2 // 默认最大文件个数 2
        private val mLogFolder = AgoraApplication.the().getExternalFilesDir("")!!.absolutePath + File.separator + "ent"
        private val mLogFileWriteThread by lazy {
            HandlerThread("AndroidFileLogger.$mLogFolder").apply {
                start()
            }
        }
    }

    data class Config constructor(
        val sceneName: String, // 场景名
        val fileSize: Long = 1 * 1024 * 1024, // 1M，单位Byte
        val fileName: String = "agora_ent_${sceneName}_log".lowercase(), // 文件名
        val maxFileCount: Int = MAX_FILE_COUNT, // 该场景最大文件数
    )

    private val dataFormat:SimpleDateFormat = SimpleDateFormat("yy/MM/dd HH:mm:ss.SSS", Locale.getDefault())

    init {
        if (BuildConfig.DEBUG) {
            Logger.addLogAdapter(
                object : AndroidLogAdapter(
                    PrettyFormatStrategy.newBuilder()
                        .showThreadInfo(true)          // (Optional) Whether to show thread info or not. Default true
                        .methodCount(1)                 // (Optional) How many method line to show. Default 2
                        .methodOffset(2)                // (Optional) Hides internal method calls up to offset. Default 5
                        .logStrategy(LogcatLogStrategy())   // (Optional) Changes the log strategy to print out. Default LogCat
                        .tag(config.sceneName)                     // (Optional) Global tag for every log. Default PRETTY_LOGGER
                        .build()
                ) {
                    override fun isLoggable(priority: Int, tag: String?): Boolean {
                        return tag == config.sceneName
                    }
                }
            )
        }
        Logger.addLogAdapter(
            object : DiskLogAdapter(
                CsvFormatStrategy
                    .newBuilder()
                    .logStrategy(DiskLogStrategy(WriteHandler()))
                    .tag(config.sceneName)
                    .build()
            ) {
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
        // [AgoraEnt] 标记截取，orhanobut logger 有默认前缀需要截取掉
        val sb = StringBuilder("[****]")
        sb.append("[${dataFormat.format(Date(System.currentTimeMillis()))}]")
        sb.append("[Agora][${level}][${config.sceneName}]")
        tag?.let { sb.append("[${tag}]"); }
        sb.append(" : $message")
        return sb.toString()
    }


    private inner class WriteHandler : Handler(mLogFileWriteThread.looper) {

        override fun handleMessage(msg: Message) {
            val content = msg.obj as String
            var fileWriter: FileWriter? = null
            try {
                val logDirectory = File(mLogFolder)
                val fileName = config.fileName
                val logFile = getLogFile(logDirectory, fileName, content)
                fileWriter = FileWriter(logFile, true)
                writeLog(fileWriter, content)
                fileWriter.flush()
                fileWriter.close()
                deleteExtraLogFiles(logDirectory, fileName)
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
            val agoraTag = writeContent.indexOf("[****]")
            if (agoraTag + 6 > 0) {
                writeContent = writeContent.substring(agoraTag + 6)
            }
            fileWriter.append(writeContent)
        }

        private fun getLogFile(logDirectory: File, fileName: String, content: String): File {
            if (!logDirectory.exists()) logDirectory.mkdirs()

            // 获取场景日志文件列表
            var logFiles = mutableListOf<File>().apply {
                logDirectory.listFiles()?.forEach { file ->
                    if (file.isFile() && file.getName().startsWith(fileName)) {
                        this.add(file)
                    }
                }
            }

            if (logFiles.isEmpty()) createNewLogFile(logDirectory, fileName)
            logFiles = getSortedLogFiles(logDirectory, fileName)
            if (checkFirstLogFile(logFiles, fileName)) {
                logFiles = getSortedLogFiles(logDirectory, fileName)
            }
            var firstLogFile = logFiles[0]
            if (firstLogFile.length() + content.length > config.fileSize) {
                renameLogFile(logFiles)
                createNewLogFile(logDirectory, fileName)
                logFiles = getSortedLogFiles(logDirectory, fileName)
                firstLogFile = logFiles[0]
            }
            return firstLogFile
        }

        private fun checkFirstLogFile(files: List<File>?, fileName: String): Boolean {
            if (files.isNullOrEmpty()) return false

            val firstLogFile = files[0]
            val currentLogFileName: String = "$fileName.log"
            if (firstLogFile.getName() != currentLogFileName) {
                var logFile: File
                var newFileName: String
                for (i in files.indices.reversed()) {
                    logFile = files[i]
                    newFileName = if (i == 0) {
                        currentLogFileName
                    } else {
                        "$fileName.$i.log"
                    }
                    val newFile = File(mLogFolder + File.separator + newFileName)
                    logFile.renameTo(newFile)
                }
                return true
            }
            return false
        }

        private fun createNewLogFile(logDirectory: File, fileName: String) {
            if (!logDirectory.exists()) logDirectory.mkdirs()
            val newFileName: String = logDirectory.path + File.separator + fileName + ".log"
            val file = File(newFileName)
            file.createNewFile()
            file.setLastModified(System.currentTimeMillis())
        }

        private fun getSortedLogFiles(logDirectory: File, fileName: String): MutableList<File> {
            val logFiles = mutableListOf<File>().apply {
                logDirectory.listFiles()?.forEach { file ->
                    if (file.isFile() && file.getName().startsWith(fileName)) {
                        this.add(file)
                    }
                }
            }

            Collections.sort(logFiles, object : Comparator<File> {
                override fun compare(file1: File, file2: File): Int {
                    try {
                        var name1 = file1.getName()
                        var name2 = file2.getName()
                        // 去掉文件名最后的后缀
                        name1 = name1.substring(0, name1.lastIndexOf('.'))
                        name2 = name2.substring(0, name2.lastIndexOf('.'))
                        name1 = if (name1.length > fileName.length) {
                            name1.substring(fileName.length + 1)
                        } else {
                            "0"
                        }
                        name2 = if (name2.length > fileName.length) {
                            name2.substring(fileName.length + 1)
                        } else {
                            "0"
                        }
                        return (name1.toIntOrNull() ?: 0) - (name2.toIntOrNull() ?: 0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return 0
                }
            })
            return logFiles
        }

        private fun renameLogFile(files: List<File>) {
            var logFile: File
            var newFileName: String
            for (i in files.indices.reversed()) {
                logFile = files[i]
                newFileName = config.fileName + "." + (i + 1) + ".log"
                val newFile: File = File(mLogFolder + File.separator + newFileName)
                logFile.renameTo(newFile)
            }
        }

        private fun deleteExtraLogFiles(logDirectory: File, fileName: String) {
            val logFiles: List<File> = getSortedLogFiles(logDirectory, fileName)
            if (logFiles.size > config.maxFileCount) {
                for (i in config.maxFileCount until logFiles.size) {
                    logFiles[i].delete()
                }
            }
        }
    }
}