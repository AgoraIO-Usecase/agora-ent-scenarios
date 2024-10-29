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
import io.agora.scene.aichat.service.interceptor.ConflictToSuccessInterceptor
import io.agora.scene.aichat.service.interceptor.CustomHeadInterceptor
import io.agora.scene.base.ServerConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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

    private val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }

        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }
    }
    )

    private val okHttpClient by lazy {

        // 设定 SSL 上下文来忽略证书验证
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false) // 禁用连接失败重试
            .followRedirects(false) // 禁用重定向
            .addInterceptor(CustomHeadInterceptor())
//            .addInterceptor(LogInterceptor())
            .addInterceptor(ConflictToSuccessInterceptor(listOf("chat/users")))
            .addInterceptor(CurlInterceptor(object : Logger {
                override fun log(message: String) {
                    try {
                        Log.d("CurlInterceptor", message)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }))
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { hostname, session -> true }
        builder.build()
    }

    private var innerRetrofit: Retrofit? = null

    private val mRetrofit: Retrofit
        get() {
            if (innerRetrofit == null) {
                val builder = Retrofit.Builder()
                    .baseUrl(ServerConfig.aiChatUrl + "/$version/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                innerRetrofit = builder.build()
            }
            return innerRetrofit!!
        }


    fun <T> getApi(serviceClass: Class<T>): T {
        return mRetrofit.create(serviceClass)
    }
}
