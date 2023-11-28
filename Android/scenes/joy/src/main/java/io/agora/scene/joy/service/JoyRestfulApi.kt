package io.agora.scene.joy.service

import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.codec.binary.Base64
import org.json.JSONObject


object JoyRestfulApi {

    const val BadJSON = 1000
    const val FieldMissing = 1001
    const val InvalidField = 1002
    const val RemoteAPIDown = 1003
    const val DBError = 1004
    const val DBNotFound = 1005
    const val GameScheduled = 1101

    private const val BASE_URL = BuildConfig.TOOLBOX_SERVER_HOST
    private const val REPLACE_GAME_ID = ":gameid"

    private const val gameListUrl = "/v1/apps/:${BuildConfig.AGORA_APP_ID}/cloud-bullet-game/games"
    private const val gameDetailUrl = "/v1/apps/:${BuildConfig.AGORA_APP_ID}/cloud-bullet-game/gameid/:game_id"
    private const val gameGiftUrl = "/v1/apps/:${BuildConfig.AGORA_APP_ID}/cloud-bullet-game/gameid/:gameId/gift"
    private const val gameCommentUrl = "/v1/apps/:${BuildConfig.AGORA_APP_ID}/cloud-bullet-game/gameid/:gameId/comment"
    private const val gameLikeUrl = "/v1/apps/:${BuildConfig.AGORA_APP_ID}/cloud-bullet-game/gameid/:gameId/like"
    private const val gameStartUrl = "/v1/apps/:${BuildConfig.AGORA_APP_ID}/cloud-bullet-game/gameid/:game_id/start"
    private const val gameStopUrl = "/v1/apps/:${BuildConfig.AGORA_APP_ID}/cloud-bullet-game/gameid/:game_id/stop"
    private const val gameStatusUrl = "/v1/apps/:${BuildConfig.AGORA_APP_ID}/cloud-bullet-game/gameid/:game_id/status"
    private const val gameRenewTokenUrl =
        "/v1/apps/:${BuildConfig.AGORA_APP_ID}/cloud-bullet-game/gameid/:game_id/renew-token"

    private fun base64Encoding(): String {
        // 客户 ID
        val customerKey = BuildConfig.AGORA_APP_ID
        // 客户密钥
        val customerSecret = BuildConfig.AGORA_APP_CERTIFICATE

        // 拼接客户 ID 和客户密钥并使用 base64 编码
        val plainCredentials = "$customerKey:$customerSecret"
        val base64Credentials = String(Base64.encodeBase64(plainCredentials.toByteArray()))
        // 创建 authorization header
        return "Basic $base64Credentials"
    }

    private val mMainScope = CoroutineScope(Job() + Dispatchers.Main)
    private val mOkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(CurlInterceptor(object : Logger {
                    override fun log(message: String) {
                        Log.d("CurlInterceptor", message)
                    }
                }))
        }
        builder.build()
    }

    fun requestGameList(success: ((String) -> Unit)? = null, failure: ((Exception?) -> Unit)? = null) {
        mMainScope.launch(Dispatchers.Main) {
            try {
                val result = getGameList()
                success?.invoke(result)
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    fun requestGameDetail(
        gameId: String,
        success: ((String) -> Unit)? = null,
        failure: ((Exception?) -> Unit)? = null
    ) {
        mMainScope.launch(Dispatchers.Main) {
            try {
                val result = getGameDetail(gameId)
                success?.invoke(result)
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    fun sendGameGift(
        gameId: String,
        roomId: String,
        success: ((String) -> Unit)? = null,
        failure: ((Exception?) -> Unit)? = null
    ) {
        mMainScope.launch(Dispatchers.Main) {
            try {
                val result = sendGameGift("", gameId, roomId)
                success?.invoke(result)
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    fun sendGameComment(
        gameId: String,
        roomId: String,
        content: String,
        success: ((String) -> Unit)? = null,
        failure: ((Exception?) -> Unit)? = null
    ) {
        mMainScope.launch(Dispatchers.Main) {
            try {
                val result = sendGameComment("", gameId, roomId, content)
                success?.invoke(result)
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    fun sendGameLike(
        gameId: String,
        roomId: String,
        numLike: Int,
        success: ((String) -> Unit)? = null,
        failure: ((Exception?) -> Unit)? = null
    ) {
        mMainScope.launch(Dispatchers.Main) {
            try {
                val result = sendGameLike("", gameId, roomId, numLike)
                success?.invoke(result)
            } catch (e: Exception) {
                failure?.invoke(e)
            }
        }
    }

    private suspend fun getGameList(pageSize: Int = 1, pageNum: Int = 10) = withContext(Dispatchers.IO) {

        val requestUrl = BASE_URL + gameListUrl
        val urlBuilder: HttpUrl.Builder = requestUrl.toHttpUrlOrNull()!!.newBuilder()
        urlBuilder.addQueryParameter("page_size", pageSize.toString())
        urlBuilder.addQueryParameter("page_num", pageNum.toString())

        val request: Request = Request.Builder()
            .url(urlBuilder.build().toString())
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", base64Encoding())
            .get()
            .build()

        val execute = mOkHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("getGameList error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJobj = JSONObject(body.string())
            if (bodyJobj["code"] != 0) {
                throw RuntimeException("getGameList error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
            } else {
                bodyJobj["msg"] as String
            }
        } else {
            throw RuntimeException("getGameList error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }

    private suspend fun getGameDetail(gameId: String) = withContext(Dispatchers.IO) {
        val requestUrl = BASE_URL + gameDetailUrl.replace(REPLACE_GAME_ID, gameId)
        val urlBuilder: HttpUrl.Builder = requestUrl.toHttpUrlOrNull()!!.newBuilder()

        val request: Request = Request.Builder()
            .url(urlBuilder.build().toString())
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", base64Encoding())
            .get()
            .build()

        val execute = mOkHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("getGameDetail error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJobj = JSONObject(body.string())
            if (bodyJobj["code"] != 0) {
                throw RuntimeException("getGameDetail error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
            } else {
                bodyJobj["msg"] as String
            }
        } else {
            throw RuntimeException("getGameDetail error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }


    private suspend fun sendGameGift(vid: String, gameId: String, roomId: String) = withContext(Dispatchers.IO) {
        val postBody = JSONObject()
        postBody.put("vid", vid)
        postBody.put("room_id", roomId)

        val payload = JSONObject()
        payload.put("msg_id", "")
        payload.put("open_id", "")
        payload.put("avatar", UserManager.getInstance().user.headUrl)
        payload.put("nickname", UserManager.getInstance().user.name)
        payload.put("gift_id", "")
        payload.put("gift_num", "")
        payload.put("gift_value", "")
        payload.put("timestamp", TimeUtils.currentTimeMillis())
        postBody.put("payload", payload.toString())

        val requestUrl = BASE_URL + gameGiftUrl.replace(REPLACE_GAME_ID, gameId)
        val request = Request.Builder().url(requestUrl)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", base64Encoding())
            .post(postBody.toString().toRequestBody()).build()
        val execute = mOkHttpClient.newCall(request).execute()
        if (execute.isSuccessful) {
            val body = execute.body
                ?: throw RuntimeException("sendGameGift error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
            val bodyJobj = JSONObject(body.string())
            if (bodyJobj["code"] != 0) {
                throw RuntimeException("sendGameGift error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
            } else {
                bodyJobj["msg"] as String
            }
        } else {
            throw RuntimeException("sendGameGift error: httpCode=${execute.code}, httpMsg=${execute.message}")
        }
    }

    private suspend fun sendGameComment(vid: String, gameId: String, roomId: String, content: String) =
        withContext(Dispatchers.IO) {
            val postBody = JSONObject()
            postBody.put("vid", vid)
            postBody.put("room_id", roomId)

            val payload = JSONObject()
            payload.put("msg_id", "")
            payload.put("open_id", "")
            payload.put("avatar", UserManager.getInstance().user.headUrl)
            payload.put("nickname", UserManager.getInstance().user.name)
            payload.put("content", content)
            payload.put("timestamp", TimeUtils.currentTimeMillis())
            postBody.put("payload", payload.toString())

            val requestUrl = BASE_URL + gameCommentUrl.replace(REPLACE_GAME_ID, gameId)
            val request = Request.Builder().url(requestUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", base64Encoding())
                .post(postBody.toString().toRequestBody()).build()
            val execute = mOkHttpClient.newCall(request).execute()
            if (execute.isSuccessful) {
                val body = execute.body
                    ?: throw RuntimeException("sendGameComment error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
                val bodyJobj = JSONObject(body.string())
                if (bodyJobj["code"] != 0) {
                    throw RuntimeException("sendGameComment error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
                } else {
                    bodyJobj["msg"] as String
                }
            } else {
                throw RuntimeException("sendGameComment error: httpCode=${execute.code}, httpMsg=${execute.message}")
            }
        }

    private suspend fun sendGameLike(vid: String, gameId: String, roomId: String, likeNum: Int) =
        withContext(Dispatchers.IO) {
            val postBody = JSONObject()
            postBody.put("vid", vid)
            postBody.put("room_id", roomId)

            val payload = JSONObject()
            payload.put("msg_id", "")
            payload.put("open_id", "")
            payload.put("avatar", UserManager.getInstance().user.headUrl)
            payload.put("nickname", UserManager.getInstance().user.name)
            payload.put("like_num", likeNum)
            payload.put("timestamp", TimeUtils.currentTimeMillis())
            postBody.put("payload", payload.toString())

            val requestUrl = BASE_URL + gameLikeUrl.replace(REPLACE_GAME_ID, gameId)
            val request = Request.Builder().url(requestUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", base64Encoding())
                .post(postBody.toString().toRequestBody()).build()
            val execute = mOkHttpClient.newCall(request).execute()
            if (execute.isSuccessful) {
                val body = execute.body
                    ?: throw RuntimeException("sendGameLike error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
                val bodyJobj = JSONObject(body.string())
                if (bodyJobj["code"] != 0) {
                    throw RuntimeException("sendGameLike error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
                } else {
                    bodyJobj["msg"] as String
                }
            } else {
                throw RuntimeException("sendGameLike error: httpCode=${execute.code}, httpMsg=${execute.message}")
            }
        }

    private suspend fun startGame(vid: String, gameId: String, roomId: String, broadcastUid: Int) =
        withContext(Dispatchers.IO) {
            val postBody = JSONObject()
            postBody.put("vid", vid)
            postBody.put("room_id", roomId)
            postBody.put("open_id", "")
            postBody.put("avatar", UserManager.getInstance().user.headUrl)
            postBody.put("nickname", UserManager.getInstance().user.name)


            val rtcConfig = JSONObject()
            rtcConfig.put("broadcast_uid", broadcastUid)
            rtcConfig.put("token", "")
            rtcConfig.put("channel_name", "")
            rtcConfig.put("timestamp", TimeUtils.currentTimeMillis())

            val encryption = JSONObject()
            encryption.put("mode", 0)
            encryption.put("secret", "")
            encryption.put("salt", "")
            rtcConfig.put("encryption", encryption)

            postBody.put("rtc_config", encryption)

            val requestUrl = BASE_URL + gameStartUrl.replace(REPLACE_GAME_ID, gameId)
            val request = Request.Builder().url(requestUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", base64Encoding())
                .post(postBody.toString().toRequestBody()).build()
            val execute = mOkHttpClient.newCall(request).execute()
            if (execute.isSuccessful) {
                val body = execute.body
                    ?: throw RuntimeException("startGame error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
                val bodyJobj = JSONObject(body.string())
                if (bodyJobj["code"] != 0) {
                    throw RuntimeException("startGame error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
                } else {
                    bodyJobj["msg"] as String
                }
            } else {
                throw RuntimeException("startGame error: httpCode=${execute.code}, httpMsg=${execute.message}")
            }
        }

    private suspend fun stopGame(vid: String, gameId: String, roomId: String, taskId: String) =
        withContext(Dispatchers.IO) {
            val postBody = JSONObject()
            postBody.put("vid", vid)
            postBody.put("room_id", roomId)
            postBody.put("open_id", "")
            postBody.put("task_id", taskId)

            val requestUrl = BASE_URL + gameStartUrl.replace(REPLACE_GAME_ID, gameId)
            val request = Request.Builder().url(requestUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", base64Encoding())
                .post(postBody.toString().toRequestBody()).build()
            val execute = mOkHttpClient.newCall(request).execute()
            if (execute.isSuccessful) {
                val body = execute.body
                    ?: throw RuntimeException("stopGame error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
                val bodyJobj = JSONObject(body.string())
                if (bodyJobj["code"] != 0) {
                    throw RuntimeException("stopGame error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
                } else {
                    bodyJobj["msg"] as String
                }
            } else {
                throw RuntimeException("stopGame error: httpCode=${execute.code}, httpMsg=${execute.message}")
            }
        }

    private suspend fun getGameStatus(gameId: String, taskId: String) =
        withContext(Dispatchers.IO) {
            val requestUrl = BASE_URL + gameStatusUrl.replace(REPLACE_GAME_ID, gameId)
            val urlBuilder: HttpUrl.Builder = requestUrl.toHttpUrlOrNull()!!.newBuilder()
            urlBuilder.addQueryParameter("task_id", taskId)

            val request: Request = Request.Builder()
                .url(urlBuilder.build().toString())
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", base64Encoding())
                .get().build()
            val execute = mOkHttpClient.newCall(request).execute()
            if (execute.isSuccessful) {
                val body = execute.body
                    ?: throw RuntimeException("getGameStatus error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
                val bodyJobj = JSONObject(body.string())
                if (bodyJobj["code"] != 0) {
                    throw RuntimeException("getGameStatus error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
                } else {
                    bodyJobj["msg"] as String
                }
            } else {
                throw RuntimeException("getGameStatus error: httpCode=${execute.code}, httpMsg=${execute.message}")
            }
        }

    private suspend fun renewToken(vid: String, gameId: String, roomId: String, taskId: String, rtcToken: String) =
        withContext(Dispatchers.IO) {
            val postBody = JSONObject()
            postBody.put("vid", vid)
            postBody.put("room_id", roomId)
            postBody.put("open_id", "")
            postBody.put("task_id", taskId)
            postBody.put("rtc_uid", UserManager.getInstance().user.id)
            postBody.put("rtc_token", rtcToken)

            val requestUrl = BASE_URL + gameRenewTokenUrl.replace(REPLACE_GAME_ID, gameId)
            val request = Request.Builder().url(requestUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", base64Encoding())
                .post(postBody.toString().toRequestBody()).build()
            val execute = mOkHttpClient.newCall(request).execute()
            if (execute.isSuccessful) {
                val body = execute.body
                    ?: throw RuntimeException("renewToken error: httpCode=${execute.code}, httpMsg=${execute.message}, body is null")
                val bodyJobj = JSONObject(body.string())
                if (bodyJobj["code"] != 0) {
                    throw RuntimeException("renewToken error: httpCode=${execute.code}, httpMsg=${execute.message}, reqCode=${bodyJobj["code"]}")
                } else {
                    bodyJobj["msg"] as String
                }
            } else {
                throw RuntimeException("renewToken error: httpCode=${execute.code}, httpMsg=${execute.message}")
            }
        }

}