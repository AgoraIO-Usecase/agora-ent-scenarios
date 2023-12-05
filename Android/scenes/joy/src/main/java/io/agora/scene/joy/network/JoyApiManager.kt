package io.agora.scene.joy.network

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.google.gson.GsonBuilder
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.api.ApiManager
import io.agora.scene.base.api.common.NetConstants
import io.agora.scene.base.manager.UserManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


object JoyApiManager {

    private const val baseUrl = BuildConfig.TOOLBOX_SERVER_HOST

    private var token:String = ""

    internal class AuthorizationInterceptor : Interceptor {
        private var apiHost: String? = Uri.parse(baseUrl).host

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest: Request = chain.request()
            if (originalRequest.url.host != apiHost) {
                return chain.proceed(originalRequest)
            }
            val builder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
            builder.addHeader(NetConstants.HEADER_PROJECT_NAME, "agora_ent_demo") // "appProject" "agora_ent_demo"
            builder.addHeader(NetConstants.HEADER_APP_OS, "android") // "appOs" "android"
            builder.addHeader(NetConstants.HEADER_VERSION_NAME, BuildConfig.APP_VERSION_NAME) // "versionName" "4.10.0"
            builder.addHeader(NetConstants.HEADER_VERSION_CODE, BuildConfig.APP_VERSION_CODE) // "versionCode" "5"

            if (!TextUtils.isEmpty(token)) {
                builder.addHeader(NetConstants.AUTHORIZATION,token)
            } else {
                if (UserManager.getInstance().user != null) {
                    token = UserManager.getInstance().user.token
                }
                if (!TextUtils.isEmpty(ApiManager.token)) {
                    builder.addHeader(NetConstants.AUTHORIZATION, ApiManager.token)
                }
            }

            val authRequest = builder.method(originalRequest.method, originalRequest.body)
                .build()
            return chain.proceed(authRequest)
        }
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