package io.agora.scene.joy.service.api

import android.net.Uri
import android.text.TextUtils
import com.google.gson.GsonBuilder
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.api.ApiManager
import io.agora.scene.base.api.HttpLogger
import io.agora.scene.base.api.SecureOkHttpClient
import io.agora.scene.base.manager.UserManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


object JoyApiManager {

    private var token: String = ""

    internal class AuthorizationInterceptor : Interceptor {
        private var apiHost: String? = Uri.parse(ServerConfig.toolBoxUrl).host

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest: Request = chain.request()
            if (originalRequest.url.host != apiHost) {
                return chain.proceed(originalRequest)
            }
            val builder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
            builder.addHeader("appProject", "agora_ent_demo") // "appProject" "agora_ent_demo"
            builder.addHeader("appOs", "android") // "appOs" "android"
            builder.addHeader("versionName", BuildConfig.APP_VERSION_NAME) // "versionName" "4.10.0"
            builder.addHeader("versionCode", BuildConfig.APP_VERSION_CODE) // "versionCode" "5"

            if (!TextUtils.isEmpty(token)) {
                builder.addHeader("Authorization", token)
            } else {
                if (UserManager.getInstance().user != null) {
                    token = UserManager.getInstance().user.token
                }
                if (!TextUtils.isEmpty(ApiManager.token)) {
                    builder.addHeader("Authorization", ApiManager.token)
                }
            }

            val authRequest = builder.method(originalRequest.method, originalRequest.body)
                .build()
            return chain.proceed(authRequest)
        }
    }

    private val mOkHttpClient by lazy {
        SecureOkHttpClient.create()
            .addInterceptor(HttpLogger())
            .addInterceptor(AuthorizationInterceptor())
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private var innerRetrofit: Retrofit? = null

    private val mRetrofit: Retrofit
        get() {
            if (innerRetrofit == null) {
                val factory =
                    DynamicModelRuntimeTypeAdapterFactory.of(DynamicModel::class.java)

                val builder = Retrofit.Builder()
                    .baseUrl("${ServerConfig.toolBoxUrl}/")
                    .addConverterFactory(
                        GsonConverterFactory.create(
                            GsonBuilder()
                                .registerTypeAdapterFactory(factory)
                                .create()
                        )
                    )
                    .client(mOkHttpClient)
                innerRetrofit = builder.build()
            }
            return innerRetrofit!!
        }

    fun reset() {
        innerRetrofit = null
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