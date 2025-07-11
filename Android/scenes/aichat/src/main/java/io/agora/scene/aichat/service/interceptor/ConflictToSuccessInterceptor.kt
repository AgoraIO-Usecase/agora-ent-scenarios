package io.agora.scene.aichat.service.interceptor

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class ConflictToSuccessInterceptor constructor(private val targetKeywords: List<String>) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        // 检查请求路径是否在目标接口数组中
        if (request.method == "POST" && targetKeywords.any { keyword -> request.url.encodedPath.contains(keyword) }) {
            val response: Response = chain.proceed(request)
            if (response.code == 409) {
                try {
                    val responseBody = response.peekBody(1024)
                    val errorJson = responseBody.string()
                    // 假设服务端返回的是一个 JSON 对象
                    val jsonObject = Gson().fromJson(errorJson, JsonObject::class.java)
                    // 构造一个新的响应体来替代原来的 errorBody
                    val modifiedBody = jsonObject.toString()
                        .toResponseBody("application/json".toMediaTypeOrNull())
                    // 返回修改后的响应，替换状态码和响应体
                    val newResponse = response.newBuilder()
                        .code(200)
                        .body(modifiedBody)
                        .build()
                    response.close()
                    return newResponse
                } catch (e: Exception) {
                    // 如果解析失败，返回原始的响应
                    response.close()
                    return response
                }
            }
            return response // 只返回一次请求的响应
        }

        // 如果不满足条件，继续链式调用
        return chain.proceed(request)
    }
}