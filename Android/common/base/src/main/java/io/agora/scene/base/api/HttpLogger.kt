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

        // Excluded API paths
        private val EXCLUDE_PATHS = setOf(
            "/heartbeat",  // Heartbeat API
            "/ping",       // Ping API
        )


        // Excluded Content-Types
        private val EXCLUDE_CONTENT_TYPES = setOf(
            "multipart/form-data",    // File upload
            "application/octet-stream", // Binary stream
            "image/*"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url

        // Check if path should be excluded
        if (EXCLUDE_PATHS.any { path -> url.encodedPath.contains(path) }) {
            return chain.proceed(request)
        }
        // Check if Content-Type should be excluded
        request.body?.contentType()?.let { contentType ->
            val contentTypeString = contentType.toString()
            if (EXCLUDE_CONTENT_TYPES.any { type ->
                    if (type.endsWith("/*")) {
                        // Handle wildcard matching, e.g. "image/*"
                        contentTypeString.startsWith(type.removeSuffix("/*"))
                    } else {
                        contentTypeString == type
                    }
                }) {
                return chain.proceed(request)
            }
        }

        val requestBody = request.body

        // Record request info (full and masked versions)
        val fullCurl = StringBuilder("curl -X ${request.method}")
        val maskedCurl = StringBuilder("curl -X ${request.method}")

        // Record headers
        request.headers.forEach { (name, value) ->
            fullCurl.append(" -H '$name: $value'")
            val safeValue = if (name.lowercase() in SENSITIVE_HEADERS) "***" else value
            maskedCurl.append(" -H '$name: $safeValue'")
        }

        // Record request body
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

        // Handle URL
        val fullUrlBuilder = StringBuilder()
        val maskedUrlBuilder = StringBuilder()

        // Handle base URL
        fullUrlBuilder.append(url.scheme).append("://").append(url.host)
        maskedUrlBuilder.append(url.scheme).append("://").append(url.host)
        
        if (url.port != 80 && url.port != 443) {
            fullUrlBuilder.append(":").append(url.port)
            maskedUrlBuilder.append(":").append(url.port)
        }
        
        fullUrlBuilder.append(url.encodedPath)
        maskedUrlBuilder.append(url.encodedPath)

        // Handle query parameters
        if (url.queryParameterNames.isNotEmpty()) {
            fullUrlBuilder.append("?")
            maskedUrlBuilder.append("?")
            
            url.queryParameterNames.forEachIndexed { index, name ->
                if (index > 0) {
                    fullUrlBuilder.append("&")
                    maskedUrlBuilder.append("&")
                }
                val value = url.queryParameter(name)
                fullUrlBuilder.append("$name=$value")
                
                // Mask sensitive parameters
                val safeValue = if (name.lowercase() in SENSITIVE_PARAMS) "***" else value
                maskedUrlBuilder.append("$name=$safeValue")
            }
        }

        fullCurl.append(" '${fullUrlBuilder}'")
        maskedCurl.append(" '${maskedUrlBuilder}'")

        // Print full request to console
        Log.d("HttpLogger","HTTP-Request: $fullCurl")
        // Log masked request
        CommonBaseLogger.d("HTTP-Request", maskedCurl.toString())

        // Record response info
        val startNs = System.nanoTime()
        val response = chain.proceed(request)
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body ?: return response
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"

        // Build full response log
        val fullResponseLog = buildString {
            append("${response.code} ${response.message} for ${fullUrlBuilder}")
            append(" (${tookMs}ms")
            if (response.networkResponse != null && response.networkResponse != response) {
                append(", ${bodySize} body")
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

        // Build masked response log
        val maskedResponseLog = buildString {
            append("${response.code} ${response.message} for ${maskedUrlBuilder}")
            append(" (${tookMs}ms")
            if (response.networkResponse != null && response.networkResponse != response) {
                append(", ${bodySize} body")
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

        // Print full response to console
        Log.d("HttpLogger","HTTP-Response: $fullResponseLog")
        // Log masked response
        CommonBaseLogger.d("HTTP-Response", maskedResponseLog)

        return response
    }
} 