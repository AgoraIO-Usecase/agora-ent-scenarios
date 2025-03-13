package io.agora.scene.base

import io.agora.scene.base.api.SecureOkHttpClient
import io.agora.scene.base.manager.UserManager
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object AudioModeration {
    private const val TAG = "AudioModeration"

    sealed class AgoraChannelType(val value: Int) {
        data object Rtc : AgoraChannelType(0)
        data object Broadcast : AgoraChannelType(1)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val okHttpClient by lazy {
        SecureOkHttpClient.create()
            .build()
    }

    fun moderationAudio(
        channelName: String,
        uid: Long,
        type: AgoraChannelType,
        sceneName: String,
        success: ((String) -> Unit)? = null,
        failure: ((Exception) -> Unit)? = null
    ) {
        scope.launch {
            try {
                val result = startAudioModeration(channelName, uid, type, sceneName)
                success?.invoke(result)
            } catch (e: Exception) {
                CommonBaseLogger.e(TAG, "Audio moderation failed: ${e.message}")
                failure?.invoke(e)
            }
        }
    }

    suspend fun moderationAudioAsync(
        channelName: String,
        uid: Long,
        type: AgoraChannelType,
        sceneName: String
    ): Result<String> = withContext(Dispatchers.Main) {
        try {
            Result.success(startAudioModeration(channelName, uid, type, sceneName))
        } catch (e: Exception) {
            CommonBaseLogger.e(TAG, "Audio moderation failed: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun startAudioModeration(
        channelName: String,
        uid: Long,
        type: AgoraChannelType,
        sceneName: String
    ): String = withContext(Dispatchers.IO) {
        val request = buildModerationRequest(channelName, uid, type, sceneName)
        executeRequest(request)
    }

    private fun buildModerationRequest(
        channelName: String,
        uid: Long,
        type: AgoraChannelType,
        sceneName: String
    ): Request {
        val postBody = JSONObject().apply {
            put("appId", BuildConfig.AGORA_APP_ID)
            put("channelName", channelName)
            put("channelType", type.value)
            put("src", "Android")
            put("payload", buildPayload(uid, sceneName))
        }

        return Request.Builder()
            .url("${ServerConfig.toolBoxUrl}/v1/moderation/audio")
            .addHeader("Content-Type", "application/json")
            .post(postBody.toString().toRequestBody())
            .build()
    }

    private fun buildPayload(uid: Long, sceneName: String): String {
        val user = UserManager.getInstance().user
        return JSONObject().apply {
            put("id", uid)
            put("userNo", user.userNo)
            put("userName", user.name)
            put("sceneName", sceneName)
        }.toString()
    }

    private fun executeRequest(request: Request): String {
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException(
                "Audio moderation error: httpCode=${response.code}, httpMsg=${response.message}"
            )
        }

        val bodyString = response.body.string()
        val bodyJson = JSONObject(bodyString)

        if (bodyJson.optInt("code", -1) != 0) {
            throw RuntimeException(
                "Audio moderation error: httpCode=${response.code}, " +
                "httpMsg=${response.message}, " +
                "reqCode=${bodyJson.opt("code")}, " +
                "reqMsg=${bodyJson.opt("message")}"
            )
        }

        return bodyJson.getString("msg")
    }
}