package io.agora.scene.base

import android.util.Log
import io.agora.scene.base.api.SecureOkHttpClient
import kotlinx.coroutines.*
import okhttp3.Request
import org.json.JSONObject

object SceneConfigManager {
    private const val TAG = "SceneConfigManager"

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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val okHttpClient by lazy {
        SecureOkHttpClient.create()
            .build()
    }

    fun fetchSceneConfig(
        success: (() -> Unit)? = null,
        failure: ((Exception) -> Unit)? = null,
    ) {
        scope.launch {
            try {
                val result = fetch()
                updateConfig(result)
                success?.invoke()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch scene config", e)
                failure?.invoke(e)
            }
        }
    }

    suspend fun fetchSceneConfigAsync(): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val result = fetch()
            updateConfig(result)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch scene config", e)
            Result.failure(e)
        }
    }

    private suspend fun fetch(): JSONObject = withContext(Dispatchers.IO) {
        val request = buildRequest()
        executeRequest(request)
    }

    private fun buildRequest(): Request =
        Request.Builder()
            .url("${ServerConfig.toolBoxUrl}/v1/configs/scene?appId=${BuildConfig.AGORA_APP_ID}")
            .addHeader("Content-Type", "application/json")
            .get()
            .build()

    private fun executeRequest(request: Request): JSONObject {
        val response = okHttpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw RuntimeException("fetchSceneConfig error: httpCode=${response.code}, httpMsg=${response.message}")
        }

        val bodyString = response.body.string()

        val bodyJson = JSONObject(bodyString)
        Log.d(TAG, "Response: $bodyJson")

        if (bodyJson.optInt("code", -1) != 0) {
            throw RuntimeException(
                "fetchSceneConfig error: reqCode=${bodyJson.opt("code")}, " +
                "reqMsg=${bodyJson.opt("message")}"
            )
        }

        return bodyJson.getJSONObject("data")
    }

    private fun updateConfig(result: JSONObject) {
        if (result.has("chat")) {
            chatExpireTime = result.getInt("chat")
        }
        if (result.has("ktv")) {
            ktvExpireTime = result.getInt("ktv")
        }
        if (result.has("show")) {
            showExpireTime = result.getInt("show")
        }
        if (result.has("showpk")) {
            showPkExpireTime = result.getInt("showpk")
        }
        if (result.has("1v1")) {
            oneOnOneExpireTime = result.getInt("1v1")
        }
        if (result.has("joy")) {
            joyExpireTime = result.getInt("joy")
        }
        if (result.has("logUpload")) {
            logUpload = result.getBoolean("logUpload")
        }
        if (result.has("cantataAppId")) {
            cantataAppId = result.getString("cantataAppId")
        }
    }
}