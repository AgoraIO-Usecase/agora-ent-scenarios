package io.agora.scene.aichat.service.interceptor

import com.google.gson.Gson
import io.agora.scene.aichat.service.api.AIBaseResponse
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * Ai token expire interceptor
 *
 * @constructor Create empty A i token expire interceptor
 */
class TokenExpireInterceptor : Interceptor {

    val gson: Gson by lazy { Gson() }

    @kotlin.jvm.Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        return if (response.body != null && response.body!!.contentType() != null) {
            val mediaType = response.body!!.contentType()
            val string = response.body!!.string()
            val responseBody = string.toResponseBody(mediaType)
            val apiResponse = gson.fromJson(string, AIBaseResponse::class.java)
            // TODO: token
//            if (apiResponse.code == 401) {
//
//            }
            response.newBuilder().body(responseBody).build()
        } else {
            response
        }
    }
}
