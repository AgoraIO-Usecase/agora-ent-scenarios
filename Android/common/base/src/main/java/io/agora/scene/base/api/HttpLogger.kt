package io.agora.scene.base.api

import android.util.Log
import io.agora.scene.base.CommonBaseLogger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.HttpUrl
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
            "appCert",
            "appCertificate"
        )

        private val SENSITIVE_PARAMS = setOf(
            "token",
            "accessToken",
            "refreshToken",
            "password",
            "secret",
            "appCert",
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

        // Skip excluded paths and content types checks
        if (shouldSkipLogging(request)) {
            return chain.proceed(request)
        }

        // Build curl command for request logging
        val (fullCurl, maskedCurl) = buildCurlCommand(request)

        // Log requests
        Log.d("HttpLogger", "HTTP-Request: $fullCurl")
        CommonBaseLogger.d("HTTP-Request", maskedCurl)

        // Execute request and log response
        val startNs = System.nanoTime()
        val response = chain.proceed(request)
        
        // Log response if needed
        logResponse(response, startNs, url)

        return response
    }

    private fun buildCurlCommand(request: Request): Pair<String, String> {
        val fullCurl = StringBuilder()
        val maskedCurl = StringBuilder()

        // Start curl command
        fullCurl.append("curl -X ${request.method}")
        maskedCurl.append("curl -X ${request.method}")

        // Collect all headers
        val headers = mutableListOf<Pair<String, String>>()
        
        // Add Content-Type header first if exists
        request.body?.contentType()?.let { contentType ->
            headers.add("Content-Type" to contentType.toString())
        }
        
        // Add other headers
        request.headers.forEach { (name, value) ->
            if (name.lowercase() != "content-type") {  // Skip Content-Type as it's already added
                headers.add(name to value)
            }
        }

        // Format all headers with a single -H
        if (headers.isNotEmpty()) {
            fullCurl.append(" -H \"")
            maskedCurl.append(" -H \"")
            
            headers.forEachIndexed { index, (name, value) ->
                if (index > 0) {
                    fullCurl.append(";")
                    maskedCurl.append(";")
                }
                fullCurl.append("$name:$value")
                val safeValue = if (name.lowercase() in SENSITIVE_HEADERS) "***" else value
                maskedCurl.append("$name:$safeValue")
            }
            
            fullCurl.append("\"")
            maskedCurl.append("\"")
        }

        // Add request body
        request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset = body.contentType()?.charset() ?: Charset.defaultCharset()
            val bodyString = buffer.readString(charset)
            
            fullCurl.append(" -d '${bodyString}'")
            
            var maskedBodyString = bodyString
            SENSITIVE_PARAMS.forEach { param ->
                maskedBodyString = maskedBodyString.replace(
                    Regex(""""$param"\s*:\s*"[^"]*""""),
                    """"$param":"***""""
                )
            }
            maskedCurl.append(" -d '${maskedBodyString}'")
        }

        // Add URL
        val urlString = buildUrlString(request.url)
        val maskedUrlString = buildMaskedUrlString(request.url)
        
        fullCurl.append(" \"$urlString\"")
        maskedCurl.append(" \"$maskedUrlString\"")

        return Pair(fullCurl.toString(), maskedCurl.toString())
    }

    private fun buildUrlString(url: HttpUrl): String {
        return buildString {
            append(url.scheme).append("://").append(url.host)
            if (url.port != 80 && url.port != 443) {
                append(":").append(url.port)
            }
            append(url.encodedPath)
            
            if (url.queryParameterNames.isNotEmpty()) {
                append("?")
                url.queryParameterNames.forEachIndexed { index, name ->
                    if (index > 0) append("&")
                    val value = url.queryParameter(name)
                    append("$name=$value")
                }
            }
        }
    }

    private fun buildMaskedUrlString(url: HttpUrl): String {
        return buildString {
            append(url.scheme).append("://").append(url.host)
            if (url.port != 80 && url.port != 443) {
                append(":").append(url.port)
            }
            append(url.encodedPath)
            
            if (url.queryParameterNames.isNotEmpty()) {
                append("?")
                url.queryParameterNames.forEachIndexed { index, name ->
                    if (index > 0) append("&")
                    val value = url.queryParameter(name)
                    val safeValue = if (name.lowercase() in SENSITIVE_PARAMS) "***" else value
                    append("$name=$safeValue")
                }
            }
        }
    }

    private fun shouldSkipLogging(request: Request): Boolean {
        // Check if path should be excluded
        if (EXCLUDE_PATHS.any { path -> request.url.encodedPath.contains(path) }) {
            return true
        }

        // Check if Content-Type should be excluded
        request.body?.contentType()?.let { contentType ->
            val contentTypeString = contentType.toString()
            if (EXCLUDE_CONTENT_TYPES.any { type ->
                    if (type.endsWith("/*")) {
                        contentTypeString.startsWith(type.removeSuffix("/*"))
                    } else {
                        contentTypeString == type
                    }
                }) {
                return true
            }
        }
        
        return false
    }

    private fun logResponse(response: Response, startNs: Long, url: HttpUrl) {
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body ?: return
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"

        // Build full response log
        val fullResponseLog = buildString {
            append("${response.code} ${response.message} for ${buildUrlString(url)}")
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
            append("${response.code} ${response.message} for ${buildMaskedUrlString(url)}")
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
    }
} 