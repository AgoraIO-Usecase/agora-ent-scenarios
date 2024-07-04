package io.agora.scene.base.utils.resourceManager

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.File

// 定义资源状态枚举
enum class AGResourceStatus {
    INVALID, NEED_DOWNLOAD, NEED_UPDATE, DOWNLOADING, DOWNLOADED
}

// 定义资源模型
data class AGResource(
    var url: String = "",
    val uri: String = "",
    val md5: String = "",
    val size: Long = 0,
    val autodownload: Boolean = true,
    val encrypt: Boolean = false,
    val group: String = "",
    val desc: String = ""
)

// 定义清单模型
data class AGManifest(
    val files: List<AGResource> = emptyList(),
    val customMsg: String = "",
    val timestamp: Long = 0
)

// AGResourceManager 类
class AGResourceManager(private val context: Context) {
    private val tag = "DownloadUtils"

    private val manifestFileList = mutableListOf<AGResource>()
    private val manifestList = mutableListOf<AGManifest>()

    // 下载清单列表文件
    suspend fun downloadManifestList(
        url: String,
        md5: String? = null,
        progressHandler: (Int) -> Unit,
        completionHandler: (List<AGResource>?, Exception?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val destinationPath = getCachePath(context, "manifest") ?: return@withContext completionHandler(null, null)
        DownloadManager.instance.download(
            url = url,
            destinationPath = destinationPath,
            callback = object : DownloadManager.FileDownloadCallback {
                override fun onProgress(file: File, progress: Int) {
                    Log.d(tag, "downloading... $url progress:$progress")
                    progressHandler.invoke(progress)
                }

                override fun onSuccess(file: File) {
                    val fileList = parseResourceList(file.absolutePath)
                    completionHandler(fileList, null)
                }

                override fun onFailed(exception: Exception) {
                    completionHandler.invoke(null, exception)
                }
            }
        )
    }

    // 下载单个清单
    suspend fun downloadManifest(
        url: String,
        progressHandler: (Int) -> Unit,
        completionHandler: (AGManifest?, Exception?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val destinationPath = getCachePath(context, "manifest") ?: return@withContext completionHandler(null, null)
        DownloadManager.instance.download(
            url = url,
            destinationPath = destinationPath,
            callback = object : DownloadManager.FileDownloadCallback {
                override fun onProgress(file: File, progress: Int) {
                    Log.d(tag, "downloading... $url progress:$progress")
                    progressHandler.invoke(progress)
                }

                override fun onSuccess(file: File) {
                    val manifest = parseManifest(file.absolutePath)
                    manifestList.add(manifest)
                    completionHandler(manifest, null)
                }

                override fun onFailed(exception: Exception) {
                    completionHandler.invoke(null, exception)
                }
            }
        )
    }

    // 下载并解压单个资源文件
    suspend fun downloadAndUnZipResource(
        resource: AGResource,
        progressHandler: (Int) -> Unit,
        downloadedHandler: (File) -> Unit,
        unzipHandler: (String) -> Unit,
        errorHandler: (Exception?) -> Unit
    ) {
        val destinationPath = getCachePath(context, "assets") ?: return
        try {
            val inputFileName = resource.url.substringAfterLast("/")
            val inputFile = File(destinationPath, inputFileName)
            Log.d(tag, "downloadAndUnZipResource resource:$resource, inputFileName:${inputFileName} ${inputFile.path}")
            // 下载文件
            if (!inputFile.exists()) {
                DownloadManager.instance.download(
                    url = resource.url,
                    destinationPath = destinationPath,
                    callback = object : DownloadManager.FileDownloadCallback {
                        override fun onProgress(file: File, progress: Int) {
                            // Log.d(tag, "downloading... $resource progress:$progress")
                            progressHandler.invoke(progress)
                        }

                        override fun onSuccess(file: File) {
                            downloadedHandler(file)
                        }

                        override fun onFailed(exception: Exception) {
                            errorHandler.invoke(exception)
                        }
                    }
                )
            }

            // 解压文件
            if (!checkUnzipFolderExists(inputFile.path) /*&& inputFile.length() == resource.size*/) {
                DownloadManager.instance.unzipFile(inputFile.path, destinationPath)
            }
            val imageExtensions = arrayOf(".jpg", ".jpeg", ".png", ".gif", ".bmp")
            var isImage = false
            imageExtensions.forEach {
                if (inputFileName.contains(it)) {
                    isImage = true
                }
            }
            if (isImage) { // 图片解压后没有文件夹，直接返回
                unzipHandler.invoke(destinationPath)
            } else {
                // 文件解压后，返回解压后的文件夹路径
                unzipHandler.invoke(destinationPath + File.separator + inputFileName.substringBeforeLast("."))
            }
        } catch (e: Exception) {
            // 处理异常
            Log.e(tag, "Error processing file: $e")
            withContext(Dispatchers.Main) {
                errorHandler.invoke(e)
            }
        }
    }

    private fun checkUnzipFolderExists(zipFilePath: String): Boolean {
        val unzipFolderName = zipFilePath.substringBeforeLast(".zip")
        val unzipFolder = File(unzipFolderName)
        return unzipFolder.exists() && unzipFolder.isDirectory
    }

    // 根据uri获取清单
    fun getManifest(uri: String): AGResource? {
        return manifestFileList.firstOrNull { it.uri == uri }
    }

    // 根据uri获取资源对象
    fun getResource(uri: String): AGResource? {
        for (manifest in manifestList) {
            for (resource in manifest.files) {
                if (resource.uri == uri) {
                    return resource
                }
            }
        }
        return null
    }

    // 根据资源查询当前资源状态
    fun getStatus(resource: AGResource): AGResourceStatus {
        // 实现资源状态查询逻辑
        return AGResourceStatus.INVALID
    }

    // 获取缓存路径
    private fun getCachePath(context: Context, relativePath: String): String? {
        // 实现获取缓存路径逻辑
        val folder = context.getExternalFilesDir(relativePath)
        return folder?.absolutePath
    }

    // 解析清单文件并返回资源列表
    private fun parseResourceList(path: String): List<AGResource> {
        val fileContent = File(path).readText()
        val gson = Gson()
        return gson.fromJson(fileContent, Array<AGResource>::class.java).toList()
    }

    // 解析清单文件并返回资源模型
    private fun parseManifest(path: String): AGManifest {
        val fileContent = File(path).readText()
        val gson = Gson()
        return gson.fromJson(fileContent, AGManifest::class.java)
    }
}