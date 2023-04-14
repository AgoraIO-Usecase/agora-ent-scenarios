package io.agora.scene.show.service

import android.os.CountDownTimer
import android.util.Base64
import io.agora.scene.base.BuildConfig
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject


class CloudPlayerService {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val baseUrl = "https://test-toolbox.bj2.agoralab.co/v1/"
    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        builder.build()
    }

    private val heartBeatTimerMap = mutableMapOf<String, CountDownTimer>()

    fun startCloudPlayer(
        channelName: String,
        uid: String,
        robotUid: Int,
        streamUrl: String,
        streamRegion: String, // cn, ap, na, eu
        success: () -> Unit,
        failure: (Exception) -> Unit
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    reqStartCloudPlayer(channelName, uid, robotUid, streamUrl, streamRegion, channelName)
                }
                success.invoke()
            } catch (ex: Exception) {
                failure.invoke(ex)
            }
        }
    }

    fun startHeartBeat(
        channelName: String,
        uid: String,
        failure: ((Exception) -> Unit)? = null
    ) {
        if (heartBeatTimerMap[channelName] != null) {
            return
        }
        reqHeatBeatAsync(channelName, uid, failure)
        heartBeatTimerMap[channelName] = object : CountDownTimer(Long.MAX_VALUE, 30 * 1000) {
            override fun onTick(millisUntilFinished: Long) {
                reqHeatBeatAsync(channelName, uid, failure)
            }

            override fun onFinish() {
                // do nothing
            }
        }.start()
    }

    fun stopHeartBeat(channelName: String) {
        val countDownTimer = heartBeatTimerMap.remove(channelName) ?: return
        countDownTimer.cancel()
    }

    private fun reqHeatBeatAsync(
        channelName: String,
        uid: String,
        failure: ((Exception) -> Unit)?
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    reqHeatBeat(channelName, uid, channelName)
                }
            } catch (ex: Exception) {
                failure?.invoke(ex)
            }
        }
    }


    private fun reqStartCloudPlayer(
        channelName: String,
        uid: String,
        robotUid: Int,
        streamUrl: String,
        streamRegion: String, // cn, ap, na, eu
        traceId: String
    ) {
        post(
            baseUrl + "cloud-player/start",
            JSONObject()
                .put("appId", BuildConfig.AGORA_APP_ID)
                .put("appCert", BuildConfig.AGORA_APP_CERTIFICATE)
                .put("basicAuth", Base64.encodeToString("${BuildConfig.CLOUD_PLAYER_KEY}:${BuildConfig.CLOUD_PLAYER_SECRET}".toByteArray(Charsets.UTF_8), Base64.DEFAULT))
                .put("channelName", channelName)
                .put("uid", uid)
                .put("robotUid", robotUid)
                .put("region", streamRegion)
                .put("streamUrl", streamUrl)
                .put("traceId", traceId)
                .put("src", "Android")
                .toString()
                .toRequestBody()
        )
    }

    private fun reqHeatBeat(
        channelName: String,
        uid: String,
        traceId: String
    ) {
        post(
            baseUrl + "heartbeat",
            JSONObject()
                .put("appId", BuildConfig.AGORA_APP_ID)
                .put("channelName", channelName)
                .put("uid", uid)
                .put("traceId", traceId)
                .put("src", "Android")
                .toString()
                .toRequestBody()
        )
    }

    private fun post(url: String, body: RequestBody) {
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(body)
            .build()
        val execute = okHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val _body = execute.body
                ?: throw RuntimeException("$url error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJson = JSONObject(_body.string())
            if (bodyJson["code"] != 0) {
                throw RuntimeException("$url error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJson["code"]}, reqMsg=${bodyJson["message"]},")
            }
        } else {
            throw RuntimeException("$url error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }
}