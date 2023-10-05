package io.agora.scene.base

import io.agora.scene.base.manager.UserManager
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

object AudioModeration {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        builder.build()
    }

    enum class AgoraChannelType(val value: Int) {
        rtc(0), broadcast(1)
    }

    fun moderationAudio(
        channelName: String,
        uid: Long,
        type: AgoraChannelType,
        sceneName: String,
        success: ((String) -> Unit)?= null,
        failure: ((Exception?) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                val result = startAudioModeration(channelName, uid, type, sceneName)
                success?.invoke(result)
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    private suspend fun startAudioModeration(
        channelName: String, uid: Long, type: AgoraChannelType, sceneName: String
    ) = withContext(Dispatchers.IO) {

        val postBody = JSONObject()
        postBody.put("appId", BuildConfig.AGORA_APP_ID)
        postBody.put("channelName", channelName)
        postBody.put("channelType", type.value)
        postBody.put("src", "Android")

        val payload = JSONObject()
        payload.put("id", uid)
        payload.put("userNo", UserManager.getInstance().user.userNo)
        payload.put("userName", UserManager.getInstance().user.name)
        payload.put("sceneName", sceneName)
        postBody.put("payload", payload.toString())

        val request = Request.Builder().url("${BuildConfig.TOOLBOX_SERVER_HOST}/v1/moderation/audio"
        ).addHeader("Content-Type", "application/json").post(postBody.toString().toRequestBody()).build()
        val execute = okHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("StartAudioModeration error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJobj = JSONObject(body.string())
            if (bodyJobj["code"] != 0) {
                throw RuntimeException("StartAudioModeration error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
            } else {
                bodyJobj["msg"] as String
            }
        } else {
            throw RuntimeException("StartAudioModeration error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }
}