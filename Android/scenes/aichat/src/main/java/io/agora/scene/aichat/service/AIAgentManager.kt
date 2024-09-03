package io.agora.scene.aichat.service

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.ToNumberPolicy
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import io.agora.scene.aichat.service.api.AIBaseResponse
import io.agora.scene.base.BuildConfig
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Ai head interceptor
 *
 * @constructor Create empty Ai head interceptor
 */
class AIHeadInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
        return chain.proceed(builder.build())
    }
}

class AIConflictToSuccessInterceptor constructor(private val targetKeywords: List<String>) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        // 检查请求路径是否在目标接口数组中
        if (targetKeywords.any { keyword -> request.url.encodedPath.contains(keyword) }) {
            val response: Response = chain.proceed(request)
            if (response.code == 409) {
                val responseBody = response.peekBody(Long.MAX_VALUE)
                val errorJson = responseBody.string()
                // 假设服务端返回的是一个 JSON 对象
                val jsonObject = Gson().fromJson(errorJson, JsonObject::class.java)
                // 构造一个新的响应体来替代原来的 errorBody
                val modifiedBody = jsonObject.toString()
                    .toResponseBody("application/json".toMediaTypeOrNull())
                // 返回修改后的响应，替换状态码和响应体
                return response.newBuilder()
                    .code(200)
                    .body(modifiedBody)
                    .build()
            }

        }
        return chain.proceed(request)
    }
}

/**
 * Ai token expire interceptor
 *
 * @constructor Create empty A i token expire interceptor
 */
class AITokenExpireInterceptor : Interceptor {

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

object AIAgentManager {

    private const val version = "v1/projects"

    private val gson =
        GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .registerTypeAdapter(TypeToken.get(JSONObject::class.java).type, object : TypeAdapter<JSONObject>() {
                @Throws(IOException::class)
                override fun write(jsonWriter: JsonWriter, value: JSONObject) {
                    jsonWriter.jsonValue(value.toString())
                }

                @Throws(IOException::class)
                override fun read(jsonReader: JsonReader): JSONObject? {
                    return null
                }
            })
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create()

    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AIHeadInterceptor())
            .addInterceptor(AIConflictToSuccessInterceptor(listOf("chat/users")))
            .addInterceptor(CurlInterceptor(object : Logger {
                override fun log(message: String) {
                    Log.d("CurlInterceptor", message)
                }
            }))

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        builder.build()
    }

    // TODO: 切换 host
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.AI_CHAT_SERVER_HOST + "/$version/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun <T> getApi(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
