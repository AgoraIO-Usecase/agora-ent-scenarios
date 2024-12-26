package io.agora.scene.base

import android.util.Log
import io.agora.scene.base.api.HttpLogger
import io.agora.scene.base.api.SecureOkHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

object SceneConfigManager {
    var chatExpireTime = 1200
        private set

    var ktvExpireTime = 1200
        private set

    var showExpireTime = 1200
        private set

    var showPkExpireTime = 120
        private set

    var oneOnOneExpireTime = 1200
        private set

    var joyExpireTime = 1200
        private set

    var logUpload = false
        private set

    var cantataAppId = ""
        private set

    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val okHttpClient by lazy {
        SecureOkHttpClient.create()
            .addInterceptor(HttpLogger()).build()
    }

    fun fetchSceneConfig(
        success: (() -> Unit)? = null,
        failure: ((Exception?) -> Unit)? = null,
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                val result = fetch()
                if (result.has("chat")) {
                    chatExpireTime = result["chat"] as Int
                    chatExpireTime = 120
                }
                if (result.has("ktv")) {
                    ktvExpireTime = result["ktv"] as Int
                }
                if (result.has("show")) {
                    showExpireTime = result["show"] as Int
                }
                if (result.has("showpk")) {
                    showPkExpireTime = result["showpk"] as Int
                }
                if (result.has("1v1")) {
                    oneOnOneExpireTime = result["1v1"] as Int
                }
                if (result.has("joy")) {
                    joyExpireTime = result["joy"] as Int
                }
                if (result.has("logUpload")) {
                    logUpload = result["logUpload"] as Boolean
                }
                if (result.has("cantataAppId")) {
                    cantataAppId = result["cantataAppId"] as String
                }
                success?.invoke()
            } catch (e: Exception) {
                Log.d("SceneConfigManager", "${e.message}")
                failure?.invoke(e)
            }
        }
    }

    private suspend fun fetch() = withContext(Dispatchers.IO) {

        val request = Request.Builder().url("${ServerConfig.toolBoxUrl}/v1/configs/scene?appId=${BuildConfig.AGORA_APP_ID}"
        ).addHeader("Content-Type", "application/json").get().build()
        val execute = okHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("fetchSceneConfig error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJobj = JSONObject(body.string())
            Log.d("SceneConfigManager", "$bodyJobj")
            if (bodyJobj["code"] != 0) {
                throw RuntimeException("fetchSceneConfig error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
            } else {
                bodyJobj["data"] as JSONObject
            }
        } else {
            throw RuntimeException("fetchSceneAliveTime error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }
}