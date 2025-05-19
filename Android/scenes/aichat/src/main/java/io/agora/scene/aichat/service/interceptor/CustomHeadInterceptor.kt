package io.agora.scene.aichat.service.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Ai head interceptor
 *
 * @constructor Create empty Ai head interceptor
 */
class CustomHeadInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // 仅在 POST 请求并且没有设置 Content-Type 的情况下添加
        if (request.method == "POST" && request.header("Content-Type") == null) {
            val modifiedRequest = request.newBuilder()
                .addHeader("Content-Type", "application/json")
                .build()
            return chain.proceed(modifiedRequest)
        }
        return chain.proceed(request)
    }
}