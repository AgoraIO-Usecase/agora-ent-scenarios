package io.agora.scene.ktv.singrelay.service

import android.util.Log
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.api.HttpLogger
import io.agora.scene.base.api.SecureOkHttpClient
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/*
 * Service Module
 * Introduction: This module is responsible for the interaction between the frontend business module and the business server 
 * (including room list + room business data synchronization, etc.)
 * Implementation principle: The business server of this scenario is a backend service wrapped with rethinkDB for data storage. 
 * It can be considered as a DB that can be freely written by the app side. Room list data and room business data are constructed 
 * on the app and stored in this DB.
 * When data in the DB is added, deleted, or modified, each end will be notified to achieve business data synchronization.
 * TODO Note⚠️: The backend service of this scenario is only for demonstration purposes and cannot be used commercially. 
 * If you need to go online, you must deploy your own backend service or cloud storage server (such as leancloud, easemob, etc.) 
 * and re-implement this module!!!!!!!!!
 */
object KTVSingRelayGameService {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val okHttpClient by lazy {
        SecureOkHttpClient.create()
            .addInterceptor(HttpLogger())
            .build()
    }

    fun graspSong(
        sceneId: String,
        roomId: String,
        userId: String,
        userName: String,
        songCode: String,
        headUrl: String,
        success: (userId: String) -> Unit,
        failure: ((Exception?) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                success.invoke(graspSong(sceneId, roomId, userId, userName, songCode, headUrl))
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    fun getWinnerInfo(
        sceneId: String,
        roomId: String,
        songCode: String,
        success: (userId: String, userName: String) -> Unit,
        failure: ((Exception?) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                val pair = fetchWinnerInfo(sceneId, roomId, songCode)
                success.invoke(pair.first, pair.second)
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }


    private suspend fun graspSong(
        sceneId: String,
        roomId: String,
        userId: String,
        userName: String,
        songCode: String,
        headUrl: String,
    ) = withContext(Dispatchers.IO) {

        val postBody = JSONObject()
        postBody.put("appId", BuildConfig.AGORA_APP_ID)
        postBody.put("sceneId", sceneId)
        postBody.put("roomId", roomId)
        postBody.put("userName", userName)
        postBody.put("userId", userId)
        postBody.put("songCode", songCode)
        postBody.put("headUrl", headUrl)
        postBody.put("src", "postman")
        postBody.put("traceId", "test-trace")

        val request = Request.Builder().url("${ServerConfig.toolBoxUrl}/v1/ktv/song/grab").
        addHeader("Content-Type", "application/json").post(postBody.toString().toRequestBody()).build()
        val execute = okHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("graspSong error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJobj = JSONObject(body.string())
            Log.d("hugo", "graspSong: $bodyJobj")
            if (bodyJobj["code"] != 0) {
                throw RuntimeException("graspSong error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}, reqMsg=${bodyJobj["msg"]},")
            } else {
                (bodyJobj["data"] as JSONObject)["userId"] as String
            }
        } else {
            throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }

    private suspend fun fetchWinnerInfo(
        sceneId: String,
        roomId: String,
        songCode: String,
    ): Pair<String, String> = withContext(Dispatchers.IO) {

        val postBody = JSONObject()
        postBody.put("appId", BuildConfig.AGORA_APP_ID)
        postBody.put("sceneId", sceneId)
        postBody.put("roomId", roomId)
        postBody.put("songCode", songCode)
        postBody.put("src", "postman")
        postBody.put("traceId", "test-trace")

        val appId = BuildConfig.AGORA_APP_ID
        val url = "${ServerConfig.toolBoxUrl}/v1/ktv/song/grab/query?appId=$appId&sceneId=$sceneId&roomId=$roomId" +
                "&songCode=$songCode&src=postman"
        Log.d("hugo", "fetchWinnerInfo: $url")
        val request = Request.Builder().url(url).get().build()
        val execute = okHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("graspSong error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJobj = JSONObject(body.string())
            Log.d("hugo", "fetchWinnerInfo: $bodyJobj")
            if (bodyJobj["code"] != 0) {
                throw RuntimeException(bodyJobj["code"].toString())
            } else {
                Pair((bodyJobj["data"] as JSONObject)["userId"] as String, (bodyJobj["data"] as JSONObject)["userName"] as String)
            }
        } else {
            Log.d("hugo", "Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}")
            throw RuntimeException("Fetch token error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }
}