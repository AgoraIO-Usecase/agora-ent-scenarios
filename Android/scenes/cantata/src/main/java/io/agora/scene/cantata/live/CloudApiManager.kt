package io.agora.scene.cantata.live

import io.agora.scene.base.BuildConfig
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.base.ServerConfig
import io.agora.scene.cantata.CantataLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import io.agora.scene.base.api.SecureOkHttpClient
import android.os.Build

/**
 * Cloud transcoding request
 */
class CloudApiManager private constructor() {

    companion object {
        fun getInstance(): CloudApiManager {
            return InstanceHolder.apiManager
        }

        private const val TAG = "ApiManager"
        private const val cloudRtcUid = 20232023
    }

    internal object InstanceHolder {
        val apiManager = CloudApiManager()
    }

    private var tokenName = ""
    private var taskId = ""
    private val okHttpClient: OkHttpClient = SecureOkHttpClient.create()
        .build()

    fun fetchStartCloud(mainChannel: String, completion: (error: Exception?) -> Unit) {
        var taskId = ""
        try {
            val transcoderObj = JSONObject()
            val inputRetObj = JSONObject()
                .put("rtcUid", 0)
                .put("rtcChannel", mainChannel)
            val outputRetObj = JSONObject()
                .put("rtcUid", cloudRtcUid)
                .put("rtcChannel", mainChannel + "_ad")

            if (SceneConfigManager.cantataAppId == "") {
                transcoderObj.put("appId", BuildConfig.AGORA_APP_ID)
                transcoderObj.put("appCert", BuildConfig.AGORA_APP_CERTIFICATE)
                transcoderObj.put("basicAuth", basicAuth)
            } else {
                transcoderObj.put("appId", SceneConfigManager.cantataAppId)
                transcoderObj.put("appCert", "")
                transcoderObj.put("basicAuth", "")
            }

            transcoderObj.put("src", "Android")
            transcoderObj.put("traceId", "12345")
            transcoderObj.put("instanceId", System.currentTimeMillis().toString())
            transcoderObj.put("audioInputsRtc", inputRetObj)
            transcoderObj.put("outputsRtc", outputRetObj)

            val request: Request = Builder()
                .url(startTaskUrl())
                .addHeader("Content-Type", "application/json")
                .post(transcoderObj.toString().toRequestBody())
                .build()

            CantataLogger.d(TAG, "fetchStartCloud: ${request.url}")

            val responseStart = okHttpClient.newCall(request).execute()
            if (responseStart.isSuccessful) {
                val body = responseStart.body!!
                val bodyString = body.string()
                val jsonUid = JSONObject(bodyString).get("data") as JSONObject

                if (jsonUid.has("taskId")) {
                    taskId = jsonUid.getString("taskId")
                }
                if (jsonUid.has("builderToken")) {
                    tokenName = jsonUid.getString("builderToken")
                }
            }
        } catch (e: Exception) {
            completion.invoke(Exception())
            CantataLogger.e(TAG, "Cloud transcoding uid request error " + e.message)
        }
        if (taskId.isNotEmpty()) {
            this.taskId = taskId
        }
    }

    fun fetchStopCloud() {
        if (taskId.isEmpty() || tokenName.isEmpty()) {
            CantataLogger.e(TAG, "Cloud transcoding task stop failed taskId || tokenName is null")
            return
        }
        try {
            val transcoderObj = JSONObject()
            if (SceneConfigManager.cantataAppId == "") {
                transcoderObj.put("appId", BuildConfig.AGORA_APP_ID)
                transcoderObj.put("appCert", BuildConfig.AGORA_APP_CERTIFICATE)
                transcoderObj.put("basicAuth", basicAuth)
            } else {
                transcoderObj.put("appId", SceneConfigManager.cantataAppId)
                transcoderObj.put("appCert", "")
                transcoderObj.put("basicAuth", "")
            }
            transcoderObj.put("src", "Android")
            transcoderObj.put("traceId", "12345")
            transcoderObj.put("taskId", taskId)
            transcoderObj.put("builderToken", tokenName)

            val request: Request = Builder()
                .url(deleteTaskUrl())
                .addHeader("Content-Type", "application/json")
                .post(transcoderObj.toString().toRequestBody())
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body!!
                val bodyString = body.string()
            }
        } catch (e: Exception) {
            CantataLogger.e(TAG, "Cloud transcoding task stop failed " + e.message)
        }
    }

    private fun startTaskUrl(): String {
        val domain = ServerConfig.toolBoxUrl
        return String.format("%s/v1/cloud-transcoder/start", domain)
    }

    private fun deleteTaskUrl(): String {
        val domain = ServerConfig.toolBoxUrl
        return String.format("%s/v1/cloud-transcoder/stop", domain)
    }

    private val basicAuth: String
        private get() {
            // Concatenate customer ID and customer key and use base64 encoding
            val plainCredentials = BuildConfig.RESTFUL_API_KEY + ":" + BuildConfig.RESTFUL_API_SECRET
            val base64Credentials: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String(Base64.getEncoder().encode(plainCredentials.toByteArray()))
            }else{
                android.util.Base64.encodeToString(plainCredentials.toByteArray(), android.util.Base64.NO_WRAP)
            }
            // Create authorization header
            return "$base64Credentials"
        }
}
