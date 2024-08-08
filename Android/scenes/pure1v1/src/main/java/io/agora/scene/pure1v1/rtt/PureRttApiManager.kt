package io.agora.scene.pure1v1.rtt

import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import io.agora.scene.base.BuildConfig
import io.agora.scene.pure1v1.Pure1v1Logger
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object PureRttApiManager {

    private val domain = "https://api.agora.io"
    private val TAG = "RttApiManager"
    private var auth = ""
    private var tokenName = ""
    private var taskId = ""

    private val session: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(CurlInterceptor(object : Logger {
                override fun log(message: String) {
                    Log.d("CurlInterceptor", message)
                }
            }))
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun setBasicAuth(token: String) {
        auth = "agora token=$token"
    }

    fun fetchCloudToken(completion: (String?) -> Unit) {
        val timeInterval = System.currentTimeMillis()
        val acquireObj = JSONObject()
        acquireObj.put("instanceId", timeInterval.toString())

        val url = getTokenUrl(domain, BuildConfig.AGORA_APP_ID)
        val request = Request.Builder()
            .url(url)
            .post(acquireObj.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", auth)
            .build()

        session.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Pure1v1Logger.e(TAG, null, "RttApiManager getToken error: ${e.message}")
                completion(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Pure1v1Logger.e(TAG, null, "RttApiManager getToken error: ${response.body?.string()}")
                    completion(null)
                    return
                }

                val responseBody = response.body?.string()
                val responseJson = JSONObject(responseBody)
                val tokenName = responseJson.optString("tokenName", null)
                Pure1v1Logger.d(TAG, "RttApiManager getToken success")
                completion(tokenName)
            }
        })
    }

    fun fetchStartRtt(
        languages: List<String>,
        sourceLanguage: String,
        targetLanguages: List<String>,
        channelName: String,
        subBotUid: String,
        subBotToken: String,
        pubBotUid: String,
        pubBotToken: String,
        completion: (Boolean) -> Unit
    ) {
        fetchCloudToken { token ->
            if (token == null) {
                Pure1v1Logger.e(TAG, null, "RttApiManager fetchStartRtt failed token is null")
                completion(false)
                return@fetchCloudToken
            } else {
                tokenName = token
            }

            val rtcConfig = JSONObject()
                .put("channelName", channelName)
                .put("subBotUid", subBotUid)
                .put("subBotToken", subBotToken)
                .put("pubBotUid", pubBotUid)
                .put("pubBotToken", pubBotToken)
                .put("subscribeAudioUids", JSONArray().put(PureRttManager.targetUid))

            val translateConfig = JSONObject()
                .put("forceTranslateInterval", 2)
                .put(
                    "languages", JSONArray().put(
                        JSONObject()
                            .put("source", sourceLanguage)
                            .put("target", JSONArray(targetLanguages))
                    )
                )

            val postBody = JSONObject()
                .put("languages", JSONArray(languages))
                .put("maxIdleTime", 50)
                .put("rtcConfig", rtcConfig)
                .put("translateConfig", translateConfig)

            val url = startTaskUrl(domain, BuildConfig.AGORA_APP_ID, tokenName)
            val request = Request.Builder()
                .url(url)
                .post(postBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", auth)
                .build()

            session.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Pure1v1Logger.e(TAG, null, "RttApiManager fetchStartRtt failed: ${e.message}")
                    completion(false)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        Pure1v1Logger.e(TAG, null, "RttApiManager fetchStartRtt failed: ${response.body?.string()}")
                        completion(false)
                        return
                    }

                    val responseBody = response.body?.string()
                    val responseJson = JSONObject(responseBody)
                    taskId = responseJson.optString("taskId", "")
                    Pure1v1Logger.d(TAG, "RttApiManager fetchStartRtt success taskId: $taskId")
                    completion(true)
                }
            })
        }
    }

    fun fetchStopRtt(completion: (Boolean) -> Unit) {
        if (taskId.isEmpty() || tokenName.isEmpty()) {
            Pure1v1Logger.e(TAG, null, "RttApiManager fetchStopRtt failed taskId || tokenName is null")
            completion(false)
            return
        }

        val url = deleteTaskUrl(domain, BuildConfig.AGORA_APP_ID, taskId, tokenName)
        val request = Request.Builder()
            .url(url)
            .delete()
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", auth)
            .build()

        session.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Pure1v1Logger.e(TAG, null, "RttApiManager fetchStopRtt: ${e.message}")
                completion(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Pure1v1Logger.e(TAG, null, "RttApiManager fetchStopRtt failed: ${response.body?.string()}")
                    completion(false)
                    return
                }

                Pure1v1Logger.d(TAG, "RttApiManager fetchStopRtt success")
                completion(true)
            }
        })
    }

    private fun getTokenUrl(domain: String, appId: String) =
        "$domain/v1/projects/$appId/rtsc/speech-to-text/builderTokens"

    private fun startTaskUrl(domain: String, appId: String, tokenName: String) =
        "$domain/v1/projects/$appId/rtsc/speech-to-text/tasks?builderToken=$tokenName"

    private fun deleteTaskUrl(domain: String, appId: String, taskId: String, tokenName: String) =
        "$domain/v1/projects/$appId/rtsc/speech-to-text/tasks/$taskId?builderToken=$tokenName"
}