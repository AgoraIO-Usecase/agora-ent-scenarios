package io.agora.scene.aichat.service.interceptor.logging

import android.util.Log
import io.agora.scene.aichat.ext.ZipHelper
import io.agora.scene.aichat.ext.hasUrlEncoded
import io.agora.scene.aichat.ext.jsonFormat
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.Locale
import java.util.concurrent.TimeUnit

class LogInterceptor : Interceptor {
    private val mPrinter: FormatPrinter = DefaultFormatPrinter()

    private val printLevel = Level.RESPONSE

    private fun shouldLog(printLevel: Level, targetLevel: Level): Boolean {
        return printLevel == Level.ALL || (printLevel != Level.NONE && printLevel == targetLevel)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val shouldLogRequest = shouldLog(printLevel, Level.REQUEST)
        val shouldLogResponse = shouldLog(printLevel, Level.RESPONSE)

        if (shouldLogRequest) {
            //打印请求信息
            if (request.body != null && isParseable(request.body!!.contentType())) {
                mPrinter.printJsonRequest(request, parseParams(request))
            } else {
                mPrinter.printFileRequest(request)
            }
        }
        val startTime = if (shouldLogResponse) System.nanoTime() else 0

        val originalResponse: Response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Log.d("Http Error", e.message ?: "Unknown error")
            throw e
        }
        val endTime = if (shouldLogResponse) System.nanoTime() else 0
        val responseBody = originalResponse.body

        //打印响应结果
        var bodyString: String? = null
        if (responseBody != null && isParseable(responseBody.contentType())) {
            bodyString = printResult(request, originalResponse, shouldLogResponse)
        }
        if (shouldLogResponse) {
            val segmentList = request.url.encodedPathSegments
            val headers = originalResponse.headers.toString()
            val code = originalResponse.code
            val isSuccessful = originalResponse.isSuccessful
            val message = originalResponse.message
            val url = originalResponse.request.url.toString()
            if (responseBody != null && isParseable(responseBody.contentType())) {
                mPrinter.printJsonResponse(
                    TimeUnit.NANOSECONDS.toMillis(endTime - startTime), isSuccessful,
                    code, headers, responseBody.contentType(), bodyString, segmentList, message, url
                )
            } else {
                mPrinter.printFileResponse(
                    TimeUnit.NANOSECONDS.toMillis(endTime - startTime),
                    isSuccessful, code, headers, segmentList, message, url
                )
            }
        }
        return originalResponse
    }

    /**
     * 打印响应结果
     *
     * @param request     [Request]
     * @param response    [Response]
     * @param logResponse 是否打印响应结果
     * @return 解析后的响应结果
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun printResult(
        request: Request,
        response: Response,
        logResponse: Boolean
    ): String? {
        return try {
            //读取服务器返回的结果
            val responseBody = response.newBuilder().build().body
            val source = responseBody!!.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()

            //获取content的压缩类型
            val encoding = response.headers["Content-Encoding"]
            val clone = buffer.clone()

            //解析response content
            parseContent(responseBody, encoding, clone)
        } catch (e: IOException) {
            e.printStackTrace()
            "{\"error\": \"" + e.message + "\"}"
        }
    }

    /**
     * 解析服务器响应的内容
     *
     * @param responseBody [ResponseBody]
     * @param encoding     编码类型
     * @param clone        克隆后的服务器响应内容
     * @return 解析后的响应结果
     */
    private fun parseContent(
        responseBody: ResponseBody?,
        encoding: String?,
        clone: Buffer
    ): String? {
        var charset = Charset.forName("UTF-8")
        val contentType = responseBody!!.contentType()
        if (contentType != null) {
            charset = contentType.charset(charset)
        }
        //content 使用 gzip 压缩
        return if ("gzip".equals(encoding, ignoreCase = true)) {
            //解压
            ZipHelper.decompressForGzip(
                clone.readByteArray(),
                convertCharset(charset)
            )
        } else if ("zlib".equals(encoding, ignoreCase = true)) {
            //content 使用 zlib 压缩
            ZipHelper.decompressToStringForZlib(
                clone.readByteArray(),
                convertCharset(charset)
            )
        } else {
            //content 没有被压缩, 或者使用其他未知压缩方式
            clone.readString(charset)
        }
    }

    enum class Level {
        /**
         * 不打印log
         */
        NONE,

        /**
         * 只打印请求信息
         */
        REQUEST,

        /**
         * 只打印响应信息
         */
        RESPONSE,

        /**
         * 所有数据全部打印
         */
        ALL
    }

    companion object {
        /**
         * 解析请求服务器的请求参数
         *
         * @param request [Request]
         * @return 解析后的请求信息
         * @throws UnsupportedEncodingException
         */
        @Throws(UnsupportedEncodingException::class)
        fun parseParams(request: Request): String {
            return try {
                val body = request.newBuilder().build().body ?: return ""
                val requestbuffer = Buffer()
                body.writeTo(requestbuffer)
                var charset = Charset.forName("UTF-8")
                val contentType = body.contentType()
                if (contentType != null) {
                    charset = contentType.charset(charset)
                }
                var json = requestbuffer.readString(charset)
                if (json.hasUrlEncoded()) {
                    json = URLDecoder.decode(
                        json,
                        convertCharset(charset)
                    )
                }
                json.jsonFormat()
            } catch (e: IOException) {
                e.printStackTrace()
                "{\"error\": \"" + e.message + "\"}"
            }
        }

        /**
         * 是否可以解析
         *
         * @param mediaType [MediaType]
         * @return `true` 为可以解析
         */
        fun isParseable(mediaType: MediaType?): Boolean {
            return if (mediaType?.type == null) {
                false
            } else isText(mediaType) || isPlain(
                mediaType
            )
                    || isJson(mediaType) || isForm(
                mediaType
            )
                    || isHtml(mediaType) || isXml(
                mediaType
            )
        }

        fun isText(mediaType: MediaType?): Boolean {
            return if (mediaType?.type == null) {
                false
            } else "text" == mediaType.type
        }

        fun isPlain(mediaType: MediaType?): Boolean {
            return if (mediaType?.subtype == null) {
                false
            } else mediaType.subtype
                .lowercase(Locale.ROOT).contains("plain")
        }

        @JvmStatic
        fun isJson(mediaType: MediaType?): Boolean {
            return if (mediaType?.subtype == null) {
                false
            } else mediaType.subtype.lowercase(Locale.getDefault()).contains("json")
        }

        @JvmStatic
        fun isXml(mediaType: MediaType?): Boolean {
            return if (mediaType?.subtype == null) {
                false
            } else mediaType.subtype.lowercase(Locale.getDefault()).contains("xml")
        }

        fun isHtml(mediaType: MediaType?): Boolean {
            return if (mediaType?.subtype == null) {
                false
            } else mediaType.subtype.lowercase(Locale.getDefault()).contains("html")
        }

        fun isForm(mediaType: MediaType?): Boolean {
            return if (mediaType?.subtype == null) {
                false
            } else mediaType.subtype.lowercase(Locale.getDefault())
                .contains("x-www-form-urlencoded")
        }

        fun convertCharset(charset: Charset?): String {
            val s = charset.toString()
            val i = s.indexOf("[")
            return if (i == -1) {
                s
            } else s.substring(i + 1, s.length - 1)
        }
    }
}