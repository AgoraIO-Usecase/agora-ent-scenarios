package io.agora.scene.base.utils.resourceManager

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import io.agora.scene.base.CommonBaseLogger
import kotlinx.coroutines.*
import java.io.File

// Define resource model
data class AGResource(
    val url: String = "",
    val uri: String = "",
    val md5: String = "",
    val size: Long = 0,
    val autodownload: Boolean = true,
    val encrypt: Boolean = false,
    val group: String = "",
    val desc: String = ""
) {
    override fun equals(other: Any?): Boolean {
        return (other as AGResource).uri == uri
    }
}

// Define manifest model
data class AGManifest(
    val files: List<AGResource> = emptyList(),
    val customMsg: String = "",
    val timestamp: Long = 0
)

// AGResourceManager class
class AGResourceManager(private val context: Context) {
    private val tag = "DownloadUtils"

    private val manifestFileList = mutableListOf<AGResource>()
    private val manifestList = mutableListOf<AGManifest>()

    fun checkResource(manifestUrl: String) {
        val destinationPath = getCachePath(context, "manifest") ?: return
        val manifestFile = File(destinationPath, manifestUrl.substringAfterLast("/"))
        if (manifestFile.exists()) {
            val resourcePath = getCachePath(context, "assets") ?: return
            try {
                val fileList = parseManifest(manifestFile.absolutePath)
                fileList.files.forEach { resource ->
                    val inputFile = File(resourcePath, resource.url.substringAfterLast("/"))
                    if (inputFile.length() == resource.size && !manifestFileList.contains(resource)) {
                        manifestFileList.add(resource)
                    }
                }
            } catch (e: Exception) {
                CommonBaseLogger.e(tag, "checkResource ${e.message}")
            }
        }
    }

    // Download single manifest
    suspend fun downloadManifest(
        url: String,
        progressHandler: (Int) -> Unit,
        completionHandler: (AGManifest?, Exception?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val destinationPath = getCachePath(context, "manifest") ?: return@withContext completionHandler(null, null)
        val manifestFile = File(destinationPath, url.substringAfterLast("/"))
        if (manifestFile.exists()) {
            manifestFile.delete()
        }
        CommonBaseLogger.d(tag, "downloadManifest start")
        DownloadManager.instance.download(
            url = url,
            destinationPath = destinationPath,
            callback = object : DownloadManager.FileDownloadCallback {
                override fun onProgress(file: File, progress: Int) {
                    progressHandler.invoke(progress)
                }

                override fun onSuccess(file: File) {
                    CommonBaseLogger.d(tag, "downloadManifest success")
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

    // Download and unzip single resource file
    suspend fun downloadAndUnZipResource(
        resource: AGResource,
        progressHandler: (Int) -> Unit,
        completionHandler: (File?, Exception?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val destinationPath = getCachePath(context, "assets") ?: run {
            CommonBaseLogger.e(tag, "downloadAndUnZipResource Invalid destination path")
            completionHandler(null, IllegalStateException("Invalid destination path"))
            return@withContext
        }

        try {
            val inputFile = File(destinationPath, resource.url.substringAfterLast("/"))
            val oldResource = manifestFileList.firstOrNull { it.uri == resource.uri }

            if (oldResource?.md5 != resource.md5 || inputFile.length() != resource.size) {
                innerDownloadResource(
                    resource,
                    inputFile,
                    oldResource,
                    destinationPath,
                    progressHandler,
                    completionHandler
                )
            } else {
                completionHandler.invoke(inputFile, null)
            }

            if (inputFile.exists() && inputFile.length() == resource.size) {
                Log.d(tag, "unzipFile $destinationPath 11")
                if (!checkUnzipFolderExists(inputFile.path)) {
                    DownloadManager.instance.unzipFile(inputFile.path, destinationPath)
                    Log.d(tag, "unzipFile $destinationPath 22")
                }
            }
        } catch (e: Exception) {
            CommonBaseLogger.e(tag, "Failed to process resource ${resource.uri} ${e.message}")
            completionHandler.invoke(null, e)
        }
    }

    private suspend fun innerDownloadResource(
        resource: AGResource,
        inputFile: File,
        oldResource: AGResource?,
        destinationPath: String,
        progressHandler: (Int) -> Unit,
        completionHandler: (File?, Exception?) -> Unit
    ) {
        oldResource?.let {
            val oldFile = File(destinationPath, it.url.substringAfterLast("/"))
            oldFile.delete()
            deleteRecursively(File(oldFile.path.substringBeforeLast(".zip")))
            manifestFileList.remove(it)
        }

        if (inputFile.exists()) {
            inputFile.delete()
            val unzipFolder = File(inputFile.path.substringBeforeLast(".zip"))
            if (unzipFolder.exists()) {
                deleteRecursively(unzipFolder)
            }
        }

        DownloadManager.instance.download(
            url = resource.url,
            destinationPath = destinationPath,
            callback = object : DownloadManager.FileDownloadCallback {
                override fun onProgress(file: File, progress: Int) {
                    progressHandler.invoke(progress)
                }

                override fun onSuccess(file: File) {
                    manifestFileList.add(resource)
                    completionHandler.invoke(file, null)
                }

                override fun onFailed(exception: Exception) {
                    completionHandler.invoke(null, exception)
                }
            }
        )
    }

    private fun checkUnzipFolderExists(zipFilePath: String): Boolean {
        val unzipFolderName = zipFilePath.substringBeforeLast(".zip")
        val unzipFolder = File(unzipFolderName)
        return unzipFolder.exists() && unzipFolder.isDirectory
    }

    // Get cache path
    private fun getCachePath(context: Context, relativePath: String): String? {
        // Implement get cache path logic
        val folder = context.getExternalFilesDir(relativePath)
        return folder?.absolutePath
    }

    // Parse manifest file and return resource model
    private fun parseManifest(path: String): AGManifest {
        val fileContent = File(path).readText()
        val gson = Gson()
        return gson.fromJson(fileContent, AGManifest::class.java)
    }

    // Delete all files in a folder
    private fun deleteRecursively(fileOrDirectory: File): Boolean {
        return try {
            if (fileOrDirectory.isDirectory) {
                fileOrDirectory.listFiles()?.forEach { child ->
                    if (!deleteRecursively(child)) {
                        CommonBaseLogger.w(tag, "Failed to delete child: ${child.absolutePath}")
                        return false
                    }
                }
            }
            if (!fileOrDirectory.delete()) {
                CommonBaseLogger.w(tag, "Failed to delete: ${fileOrDirectory.absolutePath}")
                return false
            }
            true
        } catch (e: Exception) {
            CommonBaseLogger.e(tag, "Error deleting ${fileOrDirectory.absolutePath} ${e.message}")
            false
        }
    }
}