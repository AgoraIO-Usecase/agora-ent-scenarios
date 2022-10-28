package io.agora.scene.base.api

import android.text.TextUtils
import io.agora.scene.base.utils.LogUtils
import okhttp3.*
import okio.Buffer

import java.io.IOException

class LoggerInterceptor @JvmOverloads constructor(
    aTag: String?,
    private val showResponse: Boolean = false
) : Interceptor {
    private val tag: String

    init {
        var tag = aTag
        if (TextUtils.isEmpty(tag)) {
            tag = TAG
        }
        this.tag = tag!!
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        logForRequest(request)
        val response = chain.proceed(request)
        return logForResponse(response)
    }

    private fun logForResponse(response: Response): Response {
        try {
            //===>response log
            LogUtils.d(tag, "========response'log=======")
            val builder = response.newBuilder()
            val clone = builder.build()
            LogUtils.d(tag, "url : " + clone.request().url())
            LogUtils.d(tag, "code : " + clone.code())
            LogUtils.d(tag, "protocol : " + clone.protocol())
            if (!TextUtils.isEmpty(clone.message())) LogUtils.d(tag, "message : " + clone.message())

            if (showResponse) {
                var body = clone.body()
                if (body != null) {
                    val mediaType = body.contentType()
                    if (mediaType != null) {
                        LogUtils.d(tag, "responseBody's contentType : $mediaType")
                        if (isText(mediaType)) {
                            val resp = body.string()
                            LogUtils.d(tag, "responseBody's content : $resp")

                            body = ResponseBody.create(mediaType, resp)
                            return response.newBuilder()
                                .body(body)
                                .build()
                        } else {
                            LogUtils.d(
                                tag,
                                "responseBody's content : " + " maybe [file part] , too large too print , ignored!"
                            )
                        }
                    }
                }
            }

            LogUtils.d(tag, "========response'log=======end")
        } catch (e: Exception) {
            //            e.printStackTrace();
        }

        return response
    }

    private fun logForRequest(request: Request) {
        try {
            val url = request.url()
                .toString()
            val headers = request.headers()

            LogUtils.d(tag, "========request'log=======")
            LogUtils.d(tag, "method : " + request.method())
            LogUtils.d(tag, "url : $url")
            if (headers != null && headers.size() > 0) {
                LogUtils.d(tag, "headers : $headers")
            }
            val requestBody = request.body()
            if (requestBody != null) {
                val mediaType = requestBody.contentType()
                if (mediaType != null) {
                    LogUtils.d(tag, "requestBody's contentType : $mediaType")
                    if (isText(mediaType)) {
                        LogUtils.d(tag, "requestBody's content : " + bodyToString(request))
                    } else {
                        LogUtils.d(
                            tag,
                            "requestBody's content : " + " maybe [file part] , too large too print , ignored!"
                        )
                    }
                }
            }
            LogUtils.d(tag, "========request'log=======end")
        } catch (e: Exception) {
            //            e.printStackTrace();
        }

    }

    private fun isText(mediaType: MediaType): Boolean {
        if (mediaType.type() != null && mediaType.type() == "text") {
            return true
        }
        if (mediaType.subtype() != null) {
            if (mediaType.subtype() == "json" || mediaType.subtype() == "xml" || mediaType.subtype() == "html" || mediaType.subtype() == "webviewhtml") return true
        }
        return false
    }

    private fun bodyToString(request: Request): String {
        return try {
            val copy = request.newBuilder()
                .build()
            val buffer = Buffer()
            copy.body()?.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            "something error when show requestBody."
        }

    }

    companion object {
        const val TAG = "OkHttpUtils"
    }
}
