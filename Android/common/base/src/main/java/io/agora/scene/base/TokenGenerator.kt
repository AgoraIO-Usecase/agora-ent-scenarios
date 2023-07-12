package io.agora.scene.base

import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject


object TokenGenerator {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(CurlInterceptor(object :Logger{
                    override fun log(message: String) {
                        Log.d("CurlInterceptor",message)
                    }
                }))
        }
        builder.build()
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
        success: (Map<AgoraTokenType, String>) -> Unit,
        failure: ((Exception?) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                val out = mutableMapOf<AgoraTokenType, String>()
                tokenTypes.forEach {
                    out[it] = fetchToken(channelName, uid, genType, it)
                }
                success.invoke(out)
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
        failure: ((Exception?) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                success.invoke(fetchToken(channelName, uid, genType, tokenType))
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    private suspend fun fetchToken(
        channelName: String, uid: String, genType: TokenGeneratorType, tokenType: AgoraTokenType
    ) = withContext(Dispatchers.IO) {

        val postBody = JSONObject()
        postBody.put("appId", BuildConfig.AGORA_APP_ID)
        postBody.put("appCertificate", BuildConfig.AGORA_APP_CERTIFICATE)
        postBody.put("channelName", channelName)
        postBody.put("expire", if(expireSecond > 0) expireSecond else 1500)
        postBody.put("src", "Android")
        postBody.put("ts", System.currentTimeMillis().toString() + "")
        postBody.put("type", tokenType.value)
        postBody.put("uid", uid + "")

        val request = Request.Builder().url(
            if (genType == TokenGeneratorType.token006) "${BuildConfig.TOOLBOX_SERVER_HOST}/v2/token006/generate"
            else "${BuildConfig.TOOLBOX_SERVER_HOST}/v2/token/generate"
        ).addHeader("Content-Type", "application/json").post(postBody.toString().toRequestBody()).build()
        val execute = okHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJobj = JSONObject(body.string())
            if (bodyJobj["code"] != 0) {
                throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}, reqMsg=${bodyJobj["message"]},")
            } else {
                (bodyJobj["data"] as JSONObject)["token"] as String
            }
        } else {
            throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }
}