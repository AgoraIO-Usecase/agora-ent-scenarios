//import android.content.Context
//import kotlinx.coroutines.*
//import java.net.URL
//
//// 定义资源状态枚举
//enum class AGResourceStatus {
//    INVALID, NEED_DOWNLOAD, NEED_UPDATE, DOWNLOADING, DOWNLOADED
//}
//
//// 定义资源模型
//data class AGResource(
//    val url: String = "",
//    val uri: String = "",
//    val md5: String = "",
//    val size: Long = 0,
//    val autodownload: Boolean = true,
//    val encrypt: Boolean = false,
//    val group: String = ""
//)
//
//// 定义清单模型
//data class AGManifest(
//    val files: List<AGResource> = emptyList(),
//    val customMsg: String = "",
//    val timestamp: Long = 0
//)
//
//// AGResourceManager 类
//class AGResourceManager(private val context: Context, private val downloadManager: DownloadManager) {
//
//    private var manifestFileList: List<AGResource> = emptyList()
//    private var manifestList: List<AGManifest> = emptyList()
//
//    // 下载清单列表文件
//    suspend fun downloadManifestList(
//        url: String,
//        md5: String? = null,
//        progressHandler: (Double) -> Unit,
//        completionHandler: (List<AGResource>?, Throwable?) -> Unit
//    ) = withContext(Dispatchers.IO) {
//        val destinationPath = getCachePath(context, "manifest") ?: return@withContext completionHandler(null, null)
//        downloadManager.startDownloadFile(
//            url = url,
//            md5 = md5,
//            destinationPath = "$destinationPath)/manifestList",
//            progressHandler = progressHandler
//        ) { path, error ->
//            if (error != null) {
//                completionHandler(null, error)
//            } else {
//                // 解析清单文件并获取资源列表
//                path ?: return@startDownloadFile completionHandler(null, null)
//                val fileList = parseManifest(path)
//                manifestFileList = fileList
//                for (manifest in fileList) {
//                    // 使用 launch 创建一个新的协程来下载单个清单
//                    launch {
//                        downloadManifest(manifest, progressHandler) { _, err ->
//                            if (err != null) {
//                                completionHandler(null, err)
//                            } else {
//                                // 处理下载成功的逻辑
//                            }
//                        }
//                    }
//                }
//                completionHandler(fileList, null)
//            }
//        }
//    }
//
//    // 下载单个清单
//    suspend fun downloadManifest(
//        manifest: AGResource,
//        progressHandler: (Double) -> Unit,
//        completionHandler: (AGManifest?, Throwable?) -> Unit
//    ) {
//        val destinationPath = getPath(manifest)
//        downloadManager.startDownloadFile(manifest.url, manifest.md5, destinationPath, progressHandler) { path, error ->
//            if (error != null) {
//                completionHandler(null, error)
//            } else {
//                val jsonStr = path ?: return@startDownloadFile completionHandler(null, null)
//                val manifest = decodeModel(jsonStr) ?: return@startDownloadFile completionHandler(null, null)
//                completionHandler(manifest, null)
//            }
//        }
//    }
//
//    // 下载资源
//    suspend fun downloadResource(
//        resource: AGResource,
//        progressHandler: (Double) -> Unit,
//        completionHandler: (String?, Throwable?) -> Unit
//    ) {
//        val parsedUrl = URL(resource.url)
//        val destinationFolderPath = getFolderPath(resource)
//        downloadManager.startDownloadFile(parsedUrl.toString(), resource.md5, destinationFolderPath, progressHandler) { path, error ->
//            if (error != null) {
//                completionHandler(null, error)
//            } else {
//                completionHandler(path, null)
//            }
//        }
//    }
//
//    // 根据uri获取清单
//    fun getManifest(uri: String): AGResource? {
//        return manifestFileList.firstOrNull { it.uri == uri }
//    }
//
//    // 根据uri获取资源对象
//    fun getResource(uri: String): AGResource? {
//        for (manifest in manifestList) {
//            for (resource in manifest.files) {
//                if (resource.uri == uri) {
//                    return resource
//                }
//            }
//        }
//        return null
//    }
//
//    // 根据资源查询当前资源状态
//    fun getStatus(resource: AGResource): AGResourceStatus {
//        // 实现资源状态查询逻辑
//        return AGResourceStatus.INVALID
//    }
//
//    // 获取manifest的路径
//    fun getPath(manifest: AGResource): String {
//        // 实现获取路径逻辑
//        return getCachePath(context, "manifest") ?: return@withContext completionHandler(null, null)
//    }
//
//    // 获取资源目录
//    fun getFolderPath(resource: AGResource): String {
//        // 实现获取目录逻辑
//        return ""
//    }
//
//    // 解码JSON数组为资源列表
//    private fun decodeModelArray(jsonStr: String): List<AGResource>? {
//        // 实现JSON解析逻辑
//        return null
//    }
//
//    // 解码JSON对象为清单模型
//    private fun decodeModel(jsonStr: String): AGManifest? {
//        // 实现JSON解析逻辑
//        return null
//    }
//
//    // 获取缓存路径
//    private fun getCachePath(context: Context, relativePath: String): String? {
//        // 实现获取缓存路径逻辑
//        return null
//    }
//
//    // 解析清单文件并返回资源列表
//    private fun parseManifest(path: String): List<AGResource> {
//        // ... 解析JSON文件的逻辑 ...
//        return emptyList()
//    }
//}