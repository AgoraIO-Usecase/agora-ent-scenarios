package io.agora.scene.base

import android.os.Build
import io.agora.scene.base.api.SecureOkHttpClient
import io.agora.scene.base.utils.UUIDUtil
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Event tracking API
 */
object ReportApi {
    private const val TAG = "ReportApi"
    private const val REPORT_URL = "https://report-ad.apprtc.cn/v1/report"
    private const val SOURCE = "agora_ent_demo"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val okHttpClient by lazy {
        SecureOkHttpClient.create()
            .build()
    }

    @JvmStatic
    fun reportEnter(
        sceneName: String,
        success: ((Boolean) -> Unit),
        failure: ((Exception) -> Unit)? = null
    ) {
        report("entryScene", sceneName, success, failure)
    }

    private fun report(
        eventName: String,
        sceneName: String,
        success: ((Boolean) -> Unit)? = null,
        failure: ((Exception) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                success?.invoke(fetchReport(eventName, sceneName))
            } catch (e: Exception) {
                CommonBaseLogger.e(TAG, "Report failed: ${e.message}")
                failure?.invoke(e)
            }
        }
    }

    suspend fun reportAsync(
        eventName: String,
        sceneName: String
    ): Result<Boolean> = withContext(Dispatchers.Main) {
        try {
            Result.success(fetchReport(eventName, sceneName))
        } catch (e: Exception) {
            CommonBaseLogger.e(TAG, "Report failed: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun fetchReport(
        eventName: String,
        sceneName: String
    ): Boolean = withContext(Dispatchers.IO) {
        val request = buildReportRequest(eventName, sceneName)
        executeRequest(request)
    }

    private fun buildReportRequest(eventName: String, sceneName: String): Request {
        val timestamp = System.currentTimeMillis()
        val sign = UUIDUtil.uuid("src=$SOURCE&ts=$timestamp").lowercase()
        
        val postBody = JSONObject().apply {
            put("pts", buildReportContent(eventName, sceneName))
            put("src", SOURCE)
            put("ts", timestamp)
            put("sign", sign)
        }

        return Request.Builder()
            .url(REPORT_URL)
            .addHeader("Content-Type", "application/json")
            .post(postBody.toString().toRequestBody())
            .build()
    }

    private fun buildReportContent(eventName: String, sceneName: String): JSONArray {
        val logContent = JSONObject().apply {
            put("m", "event")
            put("ls", JSONObject().apply {
                put("name", eventName)
                put("project", sceneName)
                put("version", BuildConfig.APP_VERSION_NAME)
                put("platform", "Android")
                put("model", Build.MODEL)
            })
            put("vs", JSONObject().apply {
                put("count", 1)
            })
        }
        
        return JSONArray().apply {
            put(logContent)
        }
    }

    private fun executeRequest(request: Request): Boolean {
        val response = okHttpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw RuntimeException("Report error: httpCode=${response.code}, httpMsg=${response.message}")
        }

        val bodyString = response.body.string()
        val bodyJson = JSONObject(bodyString)
        
        if (bodyJson.optInt("code", -1) != 0) {
            throw RuntimeException(
                "Report error: httpCode=${response.code}, " +
                "httpMsg=${response.message}, " +
                "reqCode=${bodyJson.opt("code")}, " +
                "reqMsg=${bodyJson.opt("message")}"
            )
        }

        val data = bodyJson.getJSONObject("data")
        CommonBaseLogger.d(TAG, "Report response: $data")
        return data.getBoolean("ok")
    }
}