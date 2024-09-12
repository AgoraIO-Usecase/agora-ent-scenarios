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
        if (request.method == "POST") {
            val builder = request.newBuilder().addHeader("Content-Type", "application/json")
            return chain.proceed(builder.build())
        } else return chain.proceed(request)
    }
}