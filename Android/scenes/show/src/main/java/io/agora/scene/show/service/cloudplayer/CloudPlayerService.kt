package io.agora.scene.show.service.cloudplayer

import android.os.CountDownTimer
import android.util.Base64
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.api.SecureOkHttpClient
import io.agora.scene.show.ShowLogger
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

/*
 * Streaming robot interface
 * TODO: You need to contact Agora technical support to enable the rte-cloud-player permission for your appid to successfully start the robot streaming
 */
class CloudPlayerService {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val tag = "CloudPlayerService"
    private val baseUrl = "${ServerConfig.toolBoxUrl}/v1/"
    private val okHttpClient by lazy {
        SecureOkHttpClient.create()
            .build()
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
                    val traceId = UUID.randomUUID().toString().replace("-","")
                    reqStartCloudPlayer(channelName, uid, robotUid, streamUrl, streamRegion, traceId)
                }
                success.invoke()
            } catch (ex: Exception) {
                failure.invoke(ex)
                ShowLogger.e(tag, "start cloud player failure $ex")
            }
        }
    }

    fun startHeartBeat(
        channelName: String,
        uid: String,
        failure: ((Exception) -> Unit)? = null
    ) {
        if (heartBeatTimerMap[channelName] != null) return
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
        ShowLogger.d(tag, "cloud player stop heartbeat $channelName")
    }

    private fun reqHeatBeatAsync(
        channelName: String,
        uid: String,
        failure: ((Exception) -> Unit)?
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    val traceId = UUID.randomUUID().toString().replace("-","")
                    reqHeatBeat(channelName, uid, traceId)
                }
            } catch (ex: Exception) {
                failure?.invoke(ex)
                ShowLogger.e(tag, "cloud player heartbeat failure $ex")
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
            baseUrl + "rte-cloud-player/start",
            JSONObject()
                .put("appId", BuildConfig.AGORA_APP_ID)
                .put("appCert", BuildConfig.AGORA_APP_CERTIFICATE)
                .put(
                    "basicAuth",
                    Base64.encodeToString(
                        "${io.agora.scene.show.BuildConfig.RESTFUL_API_KEY}:${io.agora.scene.show.BuildConfig.RESTFUL_API_SECRET}".toByteArray(Charsets.UTF_8),
                        Base64.NO_WRAP
                    )
                )
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