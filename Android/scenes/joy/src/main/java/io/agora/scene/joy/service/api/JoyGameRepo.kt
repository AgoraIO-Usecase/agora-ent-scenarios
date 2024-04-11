package io.agora.scene.joy.service.api

import io.agora.scene.base.BuildConfig
import io.agora.scene.base.api.model.User
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.joy.JoyServiceManager
import io.agora.scene.joy.service.base.BaseRepository
import io.agora.scene.joy.service.base.JoyJsonModel
import io.agora.scene.joy.service.base.StateLiveData
import io.agora.scene.joy.widget.getRandomString
import java.util.UUID

class JoyGameRepo constructor(private val service: JoyApiService) : BaseRepository() {

    companion object{
        const val ERROR_CODE_ERROR = 1300
        const val CODE_NO_CLOUD_HOST = 2002
    }

    private val mUser: User
        get() = UserManager.getInstance().user

    private val mAppId: String
        get() = BuildConfig.AGORA_APP_ID

    private val mBasicAuth: String
        get() = String.format("agora token=%s", JoyServiceManager.mTokenConfig.rtcToken)

    private val mSrc: String
        get() = "Android"

    private val mTraceId: String
        get() = UUID.randomUUID().toString().replace("-", "")

    suspend fun getGames(stateLiveData: StateLiveData<JoyGameResult>) {
        val entity = JoyGameEntity(
            appId = mAppId,
            src = mSrc,
            traceId = mTraceId,
        )
        executeResp({ service.getGames(entity = entity) }, stateLiveData)
    }

    suspend fun getGameDetail(gameId: String, stateLiveData: StateLiveData<JoyGameDetailResult>) {
        val entity = JoyGameEntity(
            appId = mAppId,
            gameId = gameId,
            src = mSrc,
            traceId = mTraceId,
        )
        executeResp({ service.gameDetails(entity = entity) }, stateLiveData)
    }

    suspend fun startGame(
        gameId: String, roomId: String, assistantUid: Int, assistantToken: String,
        stateLiveData: StateLiveData<JoyGameResult>
    ) {
        val requestEntity = JoyGameEntity(
            appId = mAppId,
            src = mSrc,
            traceId = mTraceId,
            gameId = gameId,
            roomId = roomId,
            openId = mUser.id.toString(),
            avatar = mUser.headUrl,
            nickname = mUser.name,
            rtcConfig = JoyRtcConfig(
                broadcastUid = mUser.id.toInt(),
                uid = assistantUid,
                token = assistantToken,
                channelName = roomId
            )
        )
        executeResp({ service.startGame(entity = requestEntity) }, stateLiveData)
    }

    suspend fun stopGame(roomId: String,gameId: String, taskId: String, stateLiveData: StateLiveData<JoyJsonModel
    .JoyEmpty>
    ) {
        val requestEntity = JoyGameEntity(
            appId = mAppId,
            src = mSrc,
            traceId = mTraceId,
            roomId = roomId,
            openId = mUser.id.toString(),
            gameId = gameId,
            taskId = taskId
        )
        executeResp({ service.stopGame(entity = requestEntity) }, stateLiveData)
    }

    suspend fun sendGift(
        gameId: String, roomId: String, giftId: String, giftNum: Int, giftValue: Int,
        stateLiveData: StateLiveData<JoyJsonModel.JoyEmpty>
    ) {
        val giftMsg = JoyMessageEntity(
            msgId = 32.getRandomString,
            openId = mUser.id.toString(),
            avatar = mUser.headUrl,
            nickname = mUser.name,
            giftId = giftId,
            giftNum = giftNum,
            giftValue = giftValue,
            timestamp = TimeUtils.currentTimeMillis()
        )
        val requestEntity = JoySendMessage(
            appId = mAppId,
            src = mSrc,
            traceId = mTraceId,
            gameId = gameId,
            roomId = roomId,
            payload = mutableListOf(giftMsg)
        )
        executeResp({ service.sendGift(entity = requestEntity) }, stateLiveData)
    }

    suspend fun sendComment(
        gameId: String, roomId: String, content: String,
        stateLiveData: StateLiveData<JoyJsonModel.JoyEmpty>
    ) {
        val commentMsg = JoyMessageEntity(
            msgId = 32.getRandomString,
            openId = mUser.id.toString(),
            avatar = mUser.headUrl,
            nickname = mUser.name,
            content = content,
            timestamp = TimeUtils.currentTimeMillis()
        )
        val requestEntity = JoySendMessage(
            appId = mAppId,
            src = mSrc,
            traceId = mTraceId,
            gameId = gameId,
            roomId = roomId,
            payload = mutableListOf(commentMsg)
        )
        executeResp({ service.sendComment(entity = requestEntity) }, stateLiveData)
    }

    suspend fun sendLike(
        gameId: String, roomId: String, likeNum: Int, stateLiveData: StateLiveData<JoyJsonModel.JoyEmpty>
    ) {
        val likeMsg = JoyMessageEntity(
            msgId = 32.getRandomString,
            openId = mUser.id.toString(),
            avatar = mUser.headUrl,
            nickname = mUser.name,
            likeNum = likeNum,
            timestamp = TimeUtils.currentTimeMillis()
        )
        val requestEntity = JoySendMessage(
            appId = mAppId,
            src = mSrc,
            traceId = mTraceId,
            gameId = gameId,
            roomId = roomId,
            payload = mutableListOf(likeMsg)
        )
        executeResp({ service.sendLike(entity = requestEntity) }, stateLiveData)
    }

    suspend fun gameState(gameId: String, taskId: String, stateLiveData: StateLiveData<JoyGameResult>) {
        val requestEntity = JoyGameEntity(
            appId = mAppId,
            src = mSrc,
            traceId = mTraceId,
            gameId = gameId,
            taskId = taskId,
        )
        executeResp({ service.gameState(entity = requestEntity) }, stateLiveData)
    }

    suspend fun gameRenewToken(
        gameId: String, roomId: String, taskId: String, rtcUid: String, rtcToken: String,
        stateLiveData: StateLiveData<JoyJsonModel.JoyEmpty>
    ) {
        val requestEntity = JoyGameEntity(
            appId = mAppId,
            src = mSrc,
            traceId = mTraceId,
            gameId = gameId,
            openId = mUser.id.toString(),
            roomId = roomId,
            taskId = taskId,
            rtcUid = rtcUid,
            rtcToken = rtcToken,
        )
        executeResp({ service.renewToken(entity = requestEntity) }, stateLiveData)
    }

    suspend fun getGameConfig(stateLiveData: StateLiveData<JoyGameResult>) {

        executeResp({ service.gameConfig("game") }, stateLiveData)
    }
}