package io.agora.scene.base

import android.os.Build
import io.agora.scene.base.utils.UUIDUtil
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject

/**
 * @author create by zhangwei03
 *
 * 打点api
 */
object ReportApi {

    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        builder.build()
    }

    // 进入场景
    @JvmStatic
    fun reportEnter(sceneName: String, success: ((Boolean) -> Unit), failure: ((Exception?) -> Unit)? = null) {
        report("entryScene", sceneName, success, failure)
    }

    private fun report(
        eventName: String, sceneName: String,
        success: ((Boolean) -> Unit)? = null, failure: ((Exception?) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                success?.invoke(fetchReport(eventName, sceneName))
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    private suspend fun fetchReport(eventName: String, sceneName: String) = withContext(Dispatchers.IO) {

        val postBody = JSONObject()
        val ptsObject = JSONObject().apply {
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
        val ptsArray = JSONArray().apply {
            put(ptsObject)
        }

        val src = "agora_ent_demo"
        val ts = System.currentTimeMillis()
        postBody.put("pts", ptsArray)
        postBody.put("src", src) // 声动互娱src
        postBody.put("ts", ts)
        postBody.put("sign", UUIDUtil.uuid("src=$src&ts=$ts").lowercase())

        val request = Request.Builder()
            .url("https://report-ad.agoralab.co/v1/report")
            .addHeader("Content-Type", "application/json")
            .post(postBody.toString().toRequestBody())
            .build()
        val execute = okHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("Fetch report error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJsonObj = JSONObject(body.string())
            if (bodyJsonObj["code"] != 0) {
                throw RuntimeException("Fetch report error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJsonObj["code"]}, reqMsg=${bodyJsonObj["message"]},")
            } else {
                CommonBaseLogger.d("ReportApi", "${bodyJsonObj["data"] as JSONObject}")
                (bodyJsonObj["data"] as JSONObject)["ok"] as Boolean
            }
        } else {
            throw RuntimeException("Fetch report error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }
}