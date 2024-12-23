package io.agora.scene.base.api

import android.util.Log
import io.agora.scene.base.CommonBaseLogger
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class HttpLogger : Interceptor {
    companion object {
        private val SENSITIVE_HEADERS = setOf(
            "authorization",
            "token",
            "access_token",
            "refresh_token",
            "x-token",
            "x-auth-token",
            "appCertificate"
        )

        private val SENSITIVE_PARAMS = setOf(
            "token",
            "accessToken",
            "refreshToken",
            "password",
            "secret",
            "appCertificate"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body

        // 记录请求信息（完整版和模糊版）
        val fullCurl = StringBuilder("curl -X ${request.method}")
        val maskedCurl = StringBuilder("curl -X ${request.method}")

        // 记录 header
        request.headers.forEach { (name, value) ->
            fullCurl.append(" -H '$name: $value'")
            val safeValue = if (name.lowercase() in SENSITIVE_HEADERS) "***" else value
            maskedCurl.append(" -H '$name: $safeValue'")
        }

        // 记录请求体
        requestBody?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset = body.contentType()?.charset() ?: Charset.defaultCharset()
            val bodyString = buffer.readString(charset)

            fullCurl.append(" -d '$bodyString'")

            var maskedBodyString = bodyString
            SENSITIVE_PARAMS.forEach { param ->
                maskedBodyString = maskedBodyString.replace(
                    Regex(""""$param"\s*:\s*"[^"]*""""),
                    """"$param":"***""""
                )
            }
            maskedCurl.append(" -d '$maskedBodyString'")
        }

        // 处理 URL
        val urlBuilder = StringBuilder()
        val url = request.url
        urlBuilder.append(url.scheme).append("://")
            .append(url.host)
        if (url.port != 80 && url.port != 443) {
            urlBuilder.append(":").append(url.port)
        }
        urlBuilder.append(url.encodedPath)

        // 处理查询参数
        if (url.queryParameterNames.isNotEmpty()) {
            fullCurl.append("?")
            maskedCurl.append("?")

            url.queryParameterNames.forEachIndexed { index, name ->
                if (index > 0) {
                    fullCurl.append("&")
                    maskedCurl.append("&")
                }
                val value = url.queryParameter(name)
                fullCurl.append("$name=$value")

                // 对敏感参数进行模糊处理
                val safeValue = if (name.lowercase() in SENSITIVE_PARAMS) "***" else value
                maskedCurl.append("$name=$safeValue")
            }
        }
        fullCurl.append(" '${urlBuilder}'")
        maskedCurl.append(" '${urlBuilder}'")

        // 打印完整请求到控制台
        Log.d("HTTP-Request", "$fullCurl")
        // 记录模糊请求到日志
        CommonBaseLogger.d("HTTP-Request", maskedCurl.toString())

        // 记录响应信息
        val startNs = System.nanoTime()
        val response = chain.proceed(request)
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body ?: return response
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"

        // 构建完整响应日志
        val fullResponseLog = buildString {
            append("${response.code} ${response.message} for ${urlBuilder}")
            append(" (${tookMs}ms")
            if (response.networkResponse != null && response.networkResponse != response) {
                append(", $bodySize body")
            }
            append(")")

            response.headers.forEach { (name, value) ->
                append("\n$name: $value")
            }

            responseBody.let { body ->
                val contentType = body.contentType()
                if (contentType?.type == "application" &&
                    (contentType.subtype.contains("json") || contentType.subtype.contains("xml"))
                ) {
                    val source = body.source()
                    source.request(Long.MAX_VALUE)
                    val buffer = source.buffer
                    val charset = contentType.charset() ?: Charset.defaultCharset()
                    if (contentLength != 0L) {
                        append("\n\n")
                        append(buffer.clone().readString(charset))
                    }
                }
            }
        }

        // 构建模糊响应日志
        val maskedResponseLog = buildString {
            append("${response.code} ${response.message} for ${urlBuilder}")
            append(" (${tookMs}ms")
            if (response.networkResponse != null && response.networkResponse != response) {
                append(", $bodySize body")
            }
            append(")")

            response.headers.forEach { (name, value) ->
                val safeValue = if (name.lowercase() in SENSITIVE_HEADERS) "***" else value
                append("\n$name: $safeValue")
            }

            responseBody.let { body ->
                val contentType = body.contentType()
                if (contentType?.type == "application" &&
                    (contentType.subtype.contains("json") || contentType.subtype.contains("xml"))
                ) {
                    val source = body.source()
                    source.request(Long.MAX_VALUE)
                    val buffer = source.buffer
                    val charset = contentType.charset() ?: Charset.defaultCharset()
                    if (contentLength != 0L) {
                        append("\n\n")
                        var bodyString = buffer.clone().readString(charset)
                        SENSITIVE_PARAMS.forEach { param ->
                            bodyString = bodyString.replace(
                                Regex(""""$param"\s*:\s*"[^"]*""""),
                                """"$param":"***""""
                            )
                        }
                        append(bodyString)
                    }
                }
            }
        }

        // 打印完整响应到控制台
        Log.d("HTTP-Response", fullResponseLog)
        // 记录模糊响应到日志
        CommonBaseLogger.d("HTTP-Response", maskedResponseLog)

        return response
    }
} 