package io.agora.scene.base

import io.agora.scene.base.api.SecureOkHttpClient
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

sealed class TokenGeneratorType {
    data object Token006 : TokenGeneratorType()
    data object Token007 : TokenGeneratorType()
}

sealed class AgoraTokenType(val value: Int) {
    data object Rtc : AgoraTokenType(1)
    data object Rtm : AgoraTokenType(2)
    data object Chat : AgoraTokenType(3)
}

object TokenGenerator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val okHttpClient by lazy {
        SecureOkHttpClient.create()
            .build()
    }

    var expireSecond: Long = -1
        private set

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
                val token = fetchToken(channelName, uid, genType, tokenTypes, specialAppId)
                success(token)
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
        generateTokens(
            channelName,
            uid,
            genType,
            arrayOf(tokenType),
            success,
            failure,
            specialAppId
        )
    }

    suspend fun generateTokensAsync(
        channelName: String,
        uid: String,
        genType: TokenGeneratorType,
        tokenTypes: Array<AgoraTokenType>,
        specialAppId: String? = null
    ): Result<String> = withContext(Dispatchers.Main) {
        try {
            Result.success(fetchToken(channelName, uid, genType, tokenTypes, specialAppId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateTokenAsync(
        channelName: String,
        uid: String,
        genType: TokenGeneratorType,
        tokenType: AgoraTokenType,
        specialAppId: String? = null
    ): Result<String> = generateTokensAsync(
        channelName,
        uid,
        genType,
        arrayOf(tokenType),
        specialAppId
    )

    private suspend fun fetchToken(
        channelName: String,
        uid: String,
        genType: TokenGeneratorType,
        tokenTypes: Array<AgoraTokenType>,
        specialAppId: String? = null
    ): String = withContext(Dispatchers.IO) {
        val postBody = buildJsonRequest(channelName, uid, tokenTypes, specialAppId)
        val request = buildHttpRequest(genType, postBody)

        executeRequest(request)
    }

    private fun buildJsonRequest(
        channelName: String,
        uid: String,
        tokenTypes: Array<AgoraTokenType>,
        specialAppId: String?
    ): JSONObject = JSONObject().apply {
        put("appId", specialAppId?.takeIf { it.isNotEmpty() } ?: BuildConfig.AGORA_APP_ID)
        put("appCertificate", if (specialAppId.isNullOrEmpty()) BuildConfig.AGORA_APP_CERTIFICATE else "")
        put("channelName", channelName)
        put("expire", if (expireSecond > 0) expireSecond else 60 * 60 * 24)
        put("src", "Android")
        put("ts", System.currentTimeMillis().toString())

        when (tokenTypes.size) {
            1 -> put("type", tokenTypes[0].value)
            else -> put("types", JSONArray(tokenTypes.map { it.value }))
        }

        put("uid", uid)
    }

    private fun buildHttpRequest(genType: TokenGeneratorType, postBody: JSONObject): Request {
        val url = when (genType) {
            is TokenGeneratorType.Token006 -> "${ServerConfig.toolBoxUrl}/v2/token006/generate"
            is TokenGeneratorType.Token007 -> "${ServerConfig.toolBoxUrl}/v2/token/generate"
        }

        return Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(postBody.toString().toRequestBody())
            .build()
    }

    private fun executeRequest(request: Request): String {
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Fetch token error: httpCode=${response.code}, httpMsg=${response.message}")
        }

        val body = response.body.string()
        val bodyJson = JSONObject(body)
        if (bodyJson.optInt("code", -1) != 0) {
            throw RuntimeException(
                "Fetch token error: httpCode=${response.code}, " +
                        "httpMsg=${response.message}, " +
                        "reqCode=${bodyJson.opt("code")}, " +
                        "reqMsg=${bodyJson.opt("message")}"
            )
        }
        return (bodyJson.getJSONObject("data")).getString("token")
    }
}