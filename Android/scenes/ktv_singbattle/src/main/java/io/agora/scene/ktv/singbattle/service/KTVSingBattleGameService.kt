package io.agora.scene.ktv.singbattle.service

import android.util.Log
import io.agora.scene.base.BuildConfig
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */
object KTVSingBattleGameService {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        builder.build()
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

        val request = Request.Builder().url("${BuildConfig.TOOLBOX_SERVER_HOST}/v1/ktv/song/grab").
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
        val url = "${BuildConfig.TOOLBOX_SERVER_HOST}/v1/ktv/song/grab/query?appId=$appId&sceneId=$sceneId&roomId=$roomId&songCode=$songCode&src=postman"
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