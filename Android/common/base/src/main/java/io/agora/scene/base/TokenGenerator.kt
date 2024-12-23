package io.agora.scene.base

import android.util.Log
import io.agora.scene.base.api.HttpLogger
import io.agora.scene.base.api.SecureOkHttpClient
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject


object TokenGenerator {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val okHttpClient by lazy {
        SecureOkHttpClient.create()
            .addInterceptor(HttpLogger())
            .build()
    }

    var expireSecond: Long = -1

    enum class TokenGeneratorType {
        token006, token007;
    }

    enum class AgoraTokenType(val value: Int) {
        rtc(1), rtm(2), chat(3);
    }

    fun generateTokens(
        channelName: String,
        uid: String,
        genType: TokenGeneratorType,
        tokenTypes: Array<AgoraTokenType>,
        success: (String) -> Unit,
        failure: ((Exception?) -> Unit)? = null,
        specialAppId: String? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                success.invoke(fetchToken(channelName, uid, genType, tokenTypes, specialAppId))
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    fun generateToken(
        channelName: String,
        uid: String,
        genType: TokenGeneratorType,
        tokenType: AgoraTokenType,
        success: (String) -> Unit,
        failure: ((Exception?) -> Unit)? = null,
        specialAppId: String? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                success.invoke(fetchToken(channelName, uid, genType, arrayOf(tokenType), specialAppId))
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    suspend fun fetchToken(
        channelName: String,
        uid: String,
        genType: TokenGeneratorType,
        tokenTypes: Array<AgoraTokenType>,
        specialAppId: String? = null
    ) = withContext(Dispatchers.IO) {

        val postBody = JSONObject()
        if (specialAppId == null || specialAppId == "") {
            postBody.put("appId", BuildConfig.AGORA_APP_ID)
            postBody.put("appCertificate", BuildConfig.AGORA_APP_CERTIFICATE)
        } else {
            postBody.put("appId", specialAppId)
            postBody.put("appCertificate", "")
        }
        postBody.put("channelName", channelName)
        postBody.put("expire", if (expireSecond > 0) expireSecond else 60 * 60 * 24)
        postBody.put("src", "Android")
        postBody.put("ts", System.currentTimeMillis().toString() + "")
        if (tokenTypes.size == 1) {
            postBody.put("type", tokenTypes[0].value)
        } else if (tokenTypes.size > 1) {
            val types = tokenTypes.map { it.value }.toIntArray()
            val jsonArray = JSONArray(types)
            postBody.put("types", jsonArray)
        }
        postBody.put("uid", uid + "")

        val request = Request.Builder().url(
            if (genType == TokenGeneratorType.token006) "${ServerConfig.toolBoxUrl}/v2/token006/generate"
            else "${ServerConfig.toolBoxUrl}/v2/token/generate"
        ).addHeader("Content-Type", "application/json").post(postBody.toString().toRequestBody()).build()
        Log.d("hugo", "fetchToken: ${request.body}")
        val execute = okHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJObj = JSONObject(body.string())
            if (bodyJObj["code"] != 0) {
                throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJObj["code"]}, reqMsg=${bodyJObj["message"]},")
            } else {
                (bodyJObj["data"] as JSONObject)["token"] as String
            }
        } else {
            throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }
}