package io.agora.scene.base.utils.resourceManager

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class DownloadUtils private constructor() {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "DownloadUtils"
        private const val CACHE_FOLDER = "assets"
        val instance: DownloadUtils by lazy { DownloadUtils() }
    }

    suspend fun download(context: Context, url: String, callback: FileDownloadCallback) {
        withContext(Dispatchers.IO) {
            val folder = context.getExternalFilesDir(CACHE_FOLDER) ?: return@withContext
            val file = File(folder, url.substringAfterLast("/"))
            var downloadedBytes = 0L

            val trimmedFilename = if (file.name.endsWith(".zip")) {
                file.name.substring(0, file.name.length - 4)
            } else {
                file.name
            }

            if (File(folder, trimmedFilename).isDirectory && File(folder, trimmedFilename).exists()) {
                return@withContext
            }

            if (file.exists()) {
                downloadedBytes = file.length()
            }

            val rangeHeaderValue = "bytes=$downloadedBytes-"
            Log.d(TAG, "rangeHeaderValue: $rangeHeaderValue")

            val request = Request.Builder().url(url).header("Range", rangeHeaderValue).build()
            val response = okHttpClient.newCall(request).execute()
            response.body?.let { responseBody ->
                val total = responseBody.contentLength()
                Log.d(TAG, "${file.name} download total: $total")

                if (downloadedBytes >= total) {
                    withContext(Dispatchers.Main) {
                        callback.onSuccess(file)
                    }
                } else {
                    // 支持断点重传
                    FileOutputStream(file, true).use { fos ->
                        try {
                            responseBody.source().use { source ->
                                val buffer = ByteArray(2048)
                                var bytesRead: Int
                                while (true) {
                                    bytesRead = source.read(buffer)
                                    if (bytesRead > 0) {
                                        fos.write(buffer, 0, bytesRead) // 追加写入文件末尾
                                        downloadedBytes += bytesRead

                                        val progress = ((downloadedBytes * 100) / total).toInt()
                                        withContext(Dispatchers.Main) {
                                            Log.d(TAG, "${file.name} download progress: $progress")
                                            callback.onProgress(file, progress)
                                        }
                                    } else {
                                        break
                                    }
                                }
                                fos.flush()
                                Log.d(TAG, "${file.name} download completed")
                                withContext(Dispatchers.Main) {
                                    callback.onSuccess(file)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to download file", e)
                            withContext(Dispatchers.Main) {
                                callback.onFailed(e)
                            }
                        }
                    }
                }
            } ?: run {
                Log.e(TAG, "Response body is null for $url")
                withContext(Dispatchers.Main) {
                    callback.onFailed(Exception("Response body is null"))
                }
            }
        }
    }

    suspend fun unzipFile(inputFile: String, destDirPath: String) = withContext(Dispatchers.IO) {
        val srcFile = File(inputFile)
        if (!srcFile.exists()) {
            throw Exception("所指文件不存在: $inputFile")
        }

        val zIn = ZipInputStream(FileInputStream(srcFile))
        var entry: ZipEntry? = null
        var file: File? = null
        while (zIn.nextEntry.also { entry = it } != null) {
            if (!entry!!.isDirectory) {
                file = File(destDirPath, entry!!.name)
                if (!file.parentFile?.exists()!!) {
                    file.parentFile?.mkdirs()
                }
                file.outputStream().use { out ->
                    BufferedOutputStream(out).use { bos ->
                        var len: Int
                        val buf = ByteArray(1024)
                        while (zIn.read(buf).also { len = it } != -1) {
                            bos.write(buf, 0, len)
                        }
                    }
                }
            }
        }
        zIn.close()
    }

    suspend fun processFile(context: Context, url: String, destDirPath: String, callback: FileDownloadCallback) {
        try {
            // 下载文件
            download(context, url, callback)
            // 解压文件
            val inputFile = File(context.getExternalFilesDir(CACHE_FOLDER), url.substringAfterLast("/"))
            unzipFile(inputFile.path, destDirPath)
        } catch (e: Exception) {
            // 处理异常
            Log.e(TAG, "Error processing file: $e")
            withContext(Dispatchers.Main) {
                callback.onFailed(e)
            }
        }
    }

    interface FileDownloadCallback {
        fun onProgress(file: File, progress: Int)
        fun onSuccess(file: File)
        fun onFailed(exception: Exception)
    }
}