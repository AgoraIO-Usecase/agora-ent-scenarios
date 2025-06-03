package io.agora.imkitmanager.service.http

import io.agora.scene.base.api.SecureOkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ChatHttpManager {

    private var baseUrl = ""
    private const val version = "v1"
    private var retrofit: Retrofit? = null

    fun <T> getService(clazz: Class<T>): T {
        return retrofit!!.create(clazz)
    }

    val chatInterface: ChatInterface by lazy {
        getService(ChatInterface::class.java)
    }

    fun setBaseURL(url: String) {
        if (baseUrl == url) {
            return
        }
        baseUrl = url
        retrofit = Retrofit.Builder()
            .client(
                SecureOkHttpClient.create().build()
            )
            .baseUrl(url + "/${version}/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}