package io.agora.scene.base.utils.resourceManager

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

class DownloadManager private constructor() {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "DownloadUtils"
        val instance: DownloadManager by lazy { DownloadManager() }
    }

    suspend fun download(url: String, destinationPath: String, callback: FileDownloadCallback) {
        withContext(Dispatchers.IO) {
            val file = File(destinationPath, url.substringAfterLast("/"))
            var downloadedBytes = 0L

            if (file.exists()) {
                downloadedBytes = file.length()
            }

            val rangeHeaderValue = "bytes=$downloadedBytes-"
            Log.d(TAG, "rangeHeaderValue: $rangeHeaderValue")

            val request = Request.Builder().url(url).header("Range", rangeHeaderValue).build()

            try {
                val response = okHttpClient.newCall(request).execute()
                response.body?.let { responseBody ->
                    val total = responseBody.contentLength()
                    val fileTotal = total + downloadedBytes
                    Log.d(TAG, "${file.name} download actual total: $total, fileTotal: $fileTotal")

                    if (file.exists() && total == downloadedBytes)  {
                        Log.d(TAG, "${file.name} already fully downloaded")
                        withContext(Dispatchers.Main) {
                            callback.onSuccess(file)
                        }
                        return@withContext
                    }

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

                                        val progress = ((downloadedBytes * 100) / fileTotal).toInt()
                                        withContext(Dispatchers.Main) {
                                            //Log.d(TAG, "${file.name} download progress: $progress")
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
                } ?: run {
                    Log.e(TAG, "Response body is null for $url")
                    withContext(Dispatchers.Main) {
                        callback.onFailed(Exception("Response body is null"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download file：${e.message}")
                withContext(Dispatchers.Main) {
                    callback.onFailed(e)
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

    interface FileDownloadCallback {
        fun onProgress(file: File, progress: Int)
        fun onSuccess(file: File)
        fun onFailed(exception: Exception)
    }
}