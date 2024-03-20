//import okhttp3.Call
//import okhttp3.Callback
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.Response
//import okhttp3.internal.headersContentLength
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//
//interface DownloadManager {
//    fun startDownloadFile(
//        url: String,
//        md5: String?,
//        destinationPath: String,
//        progressHandler: (Double) -> Unit,
//        completionHandler: (String?, IOException?) -> Unit
//    )
//}
//
//class RetrofitDownloadManager : DownloadManager {
//    private val client = OkHttpClient()
//
//    override fun startDownloadFile(
//        url: String,
//        md5: String?,
//        destinationPath: String,
//        progressHandler: (Double) -> Unit,
//        completionHandler: (String?, IOException?) -> Unit
//    ) {
//        val request = Request.Builder().url(url).build()
//        val call = client.newCall(request)
//
//        call.enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                completionHandler(null, e)
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    val totalLength = response.headersContentLength()
//                    var downloadedLength = 0L
//
//                    val file = File(destinationPath)
//                    val outputStream = FileOutputStream(file)
//
//                    response.body?.source()?.use { source ->
//                        val buffer = ByteArray(1024)
//                        var read: Int
//                        while (source.read(buffer).also { read = it } != -1) {
//                            outputStream.write(buffer, 0, read)
//                            downloadedLength += read
//                            progressHandler(downloadedLength.toDouble() / totalLength)
//                        }
//                    }
//
//                    outputStream.close()
//                    completionHandler(file.absolutePath, null)
//                } else {
//                    completionHandler(null, IOException("Failed to download file: ${response.message}"))
//                }
//            }
//        })
//    }
//}