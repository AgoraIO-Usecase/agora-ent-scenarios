package io.agora.scene.base

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

object SceneAliveTime {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        builder.build()
    }

    fun fetchShowAliveTime(
        success: ((Int, Int) -> Unit) ?= null,
        failure: ((Exception?) -> Unit) ? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                val result = fetch()
                success?.invoke(result.first, result.second)
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    private suspend fun fetch() = withContext(Dispatchers.IO) {

        val request = Request.Builder().url("${ServerConfig.toolBoxUrl}/v1/configs/scene"
        ).addHeader("Content-Type", "application/json").get().build()
        val execute = okHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("fetchSceneAliveTime error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJobj = JSONObject(body.string())
            Log.d("hang123", "$bodyJobj")
            if (bodyJobj["code"] != 0) {
                throw RuntimeException("fetchSceneAliveTime error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
            } else {
                Pair((bodyJobj["data"] as JSONObject)["show"] as Int,  (bodyJobj["data"] as JSONObject)["showpk"] as Int)
            }
        } else {
            throw RuntimeException("fetchSceneAliveTime error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }
}