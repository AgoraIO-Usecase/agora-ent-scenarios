package io.agora.scene.base.api.intercepter

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
            "/ping"       // Ping API
        )

        // Excluded Content-Types
        private val EXCLUDE_CONTENT_TYPES = setOf(
            "multipart/form-data",    // File upload
            "application/octet-stream", // Binary stream
            "image/*",
            "file",
            "audio/*",                // Audio files
            "video/*"                 // Video files
        )

        // Paths containing these keywords will also be checked for content type exclusion
        private val SENSITIVE_PATH_KEYWORDS = setOf(
            "upload",
            "file",
            "media"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val requestId = java.util.UUID.randomUUID().toString().substring(0, 8)

        // Check if should completely skip logging or only log results
        val shouldSkipCompletely = shouldSkipLoggingCompletely(request)
        val logResultOnly = shouldLogResultOnly(request)

        // If not completely skipped and not only logging results, log the request
        if (!shouldSkipCompletely && !logResultOnly) {
            // Build curl command and log request
            val (fullCurl, maskedCurl) = buildCurlCommand(request)
            CommonBaseLogger.d("[$requestId] HTTP-Request", maskedCurl)
        } else if (logResultOnly) {
            // Only log simplified request info in debug mode
            CommonBaseLogger.d("[$requestId] HTTP-Request", "Large file upload request: ${request.method} ${request.url}")
        }

        // Execute request
        val startNs = System.nanoTime()
        val response = chain.proceed(request)

        // If not completely skipping logging, log the response
        if (!shouldSkipCompletely) {
            logResponse(response, startNs, url, requestId)
        }

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

    // Determine if logging should be completely skipped
    private fun shouldSkipLoggingCompletely(request: Request): Boolean {
        // Check if the path should be excluded
        return EXCLUDE_PATHS.any { path -> request.url.encodedPath.contains(path) }
    }

    // Determine if only results should be logged without request body
    private fun shouldLogResultOnly(request: Request): Boolean {
        // Check if path contains sensitive keywords
        val path = request.url.encodedPath.lowercase()
        if (SENSITIVE_PATH_KEYWORDS.any { keyword -> path.contains(keyword) }) {
            // For paths containing sensitive keywords, apply stricter content type checking
            request.body?.let { body ->
                // If body size exceeds 1MB, only log result
                if (body.contentLength() > 1024 * 1024) {
                    return true
                }
            }
        }

        // Check if Content-Type is in the exclusion list
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

    private fun logResponse(response: Response, startNs: Long, url: HttpUrl, requestId: String) {
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
        // Log masked response
        CommonBaseLogger.d("[$requestId] HTTP-Response", maskedResponseLog)
    }
} 