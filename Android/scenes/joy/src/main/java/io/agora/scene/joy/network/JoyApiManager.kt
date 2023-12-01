package io.agora.scene.joy.network

import android.net.Uri
import android.util.Log
import com.google.gson.GsonBuilder
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import io.agora.rtm.RtmTokenBuilder2
import io.agora.scene.base.BuildConfig
import io.agora.scene.joy.RtcEngineInstance
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.codec.binary.Base64
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


object JoyApiManager {

    private const val baseUrl = BuildConfig.TOOLBOX_SERVER_HOST

    internal class AuthorizationInterceptor : Interceptor {
        var apiHost: String?

        init {
            apiHost = Uri.parse(baseUrl).host
        }

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest: Request = chain.request()
            if (originalRequest.url.host != apiHost) {
                return chain.proceed(originalRequest)
            }
            val builder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
//            if (RtcEngineInstance.generalToken().isNotEmpty()) {
//                builder.addHeader(
//                    "Authorization",
//                    String.format("agora token=%s", RtcEngineInstance.generalToken())
//                )
//            }
            val authRequest = builder.method(originalRequest.method, originalRequest.body)
                .build()
            return chain.proceed(authRequest)
        }
    }

    const val BadJSON = 1000
    const val FieldMissing = 1001
    const val InvalidField = 1002
    const val RemoteAPIDown = 1003
    const val DBError = 1004
    const val DBNotFound = 1005
    const val GameScheduled = 1101

    fun base64Encoding(): String {
        // 客户 ID
        val customerKey = BuildConfig.AGORA_APP_ID
        // 客户密钥
        val customerSecret = BuildConfig.AGORA_APP_CERTIFICATE

        // 拼接客户 ID 和客户密钥并使用 base64 编码
        val plainCredentials = "$customerKey:$customerSecret"
        val base64Credentials = String(Base64.encodeBase64(plainCredentials.toByteArray()))
        // 创建 authorization header
        return "Basic $base64Credentials"
    }

    private val mOkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor())
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(CurlInterceptor(object : Logger {
                    override fun log(message: String) {
                        Log.d("CurlInterceptor", message)
                    }
                }))
        }
        builder.build()
    }

    private val mRetrofit by lazy {

        val factory = DynamicModelRuntimeTypeAdapterFactory.of(DynamicModel::class.java)

        val builder = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .registerTypeAdapterFactory(factory)
                        .create()
                )
            )
            .client(mOkHttpClient)
        builder.build()
    }

    /**
     * Retrofit 结合 API泛型 ,创建接口实例并返回
     *
     * 入参: API泛型,代指各模块的 retrofit 接口API ,JoyApiService
     * 返回值: API泛型的接口实例,
     */
    fun <API> create(api: Class<API>): API {
        return mRetrofit.create(api)
    }
}