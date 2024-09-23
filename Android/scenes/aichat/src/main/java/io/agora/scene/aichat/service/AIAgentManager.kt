package io.agora.scene.aichat.service

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.service.interceptor.ConflictToSuccessInterceptor
import io.agora.scene.aichat.service.interceptor.CustomHeadInterceptor
import io.agora.scene.aichat.service.interceptor.logging.LogInterceptor
import io.agora.scene.base.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

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
            .addInterceptor(CustomHeadInterceptor())
//            .addInterceptor(LogInterceptor())
            .addInterceptor(ConflictToSuccessInterceptor(listOf("chat/users")))
            .addInterceptor(CurlInterceptor(object : Logger {
                override fun log(message: String) {
                    try {
                        Log.d("CurlInterceptor", message)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }))
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
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
