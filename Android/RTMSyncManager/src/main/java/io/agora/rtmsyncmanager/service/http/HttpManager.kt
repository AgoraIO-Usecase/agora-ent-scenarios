package io.agora.rtmsyncmanager.service.http

import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HttpManager {

    private var baseUrl = ""
    private const val version = "v2"
    private var retrofit: Retrofit? = null

    fun <T> getService(clazz: Class<T>): T {
        return retrofit!!.create(clazz)
    }

    fun setBaseURL(url: String) {
        if (baseUrl == url) {
            return
        }
        baseUrl = url
        retrofit = Retrofit.Builder()
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .addInterceptor(CurlInterceptor(object : Logger {
                        override fun log(message: String) {
                            Log.v("Ok2Curl", message)
                        }
                    }))
                    .build()
            )
            .baseUrl(url + "/$version/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}