package io.agora.scene.joy.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.api.model.User
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.joy.RtcEngineInstance
import io.agora.scene.joy.network.JoyApiManager
import io.agora.scene.joy.network.JoyApiResult
import io.agora.scene.joy.network.JoyApiService
import io.agora.scene.joy.network.JoyGameEntity
import io.agora.scene.joy.network.JoyGameResult
import io.agora.scene.joy.network.JoyRtcConfig
import io.agora.scene.joy.network.SingleLiveEvent
import io.agora.scene.joy.service.JoyRoomInfo
import io.agora.scene.joy.utils.JoyLogger
import kotlinx.coroutines.launch
import java.util.UUID

class JoyViewModel : ViewModel() {

    private val TAG = "Joy_JoyViewModel"

    private val mJoyApiService: JoyApiService by lazy {
        JoyApiManager.create(JoyApiService::class.java)
    }

    private val mUser: User
        get() = UserManager.getInstance().user

    private val mAppId: String
        get() = BuildConfig.AGORA_APP_ID

    private val mBasicAuth: String
        get() = String.format("agora token=%s", RtcEngineInstance.generalToken())

    var mCurrentGame:JoyGameEntity?=null

    val mGameEntityList: MutableLiveData<List<JoyGameEntity>> = SingleLiveEvent()
    val mStartGame: MutableLiveData<Boolean> = SingleLiveEvent()
    val mStopGame: MutableLiveData<Boolean> = SingleLiveEvent()

    fun getGames() {
        viewModelScope.launch {
            try {
                val entity = JoyGameEntity(
                    appId = mAppId,
                    basicAuth = mBasicAuth,
                    traceId = UUID.randomUUID().toString()
                )
                val res: JoyApiResult<JoyGameResult> = mJoyApiService.getGames(entity)

                if (res.isSucceed) {
                    mGameEntityList.value = res.data?.list ?: emptyList()
                } else {
                    ToastUtils.showToast("获取房间列表失败")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getGames Exception ${e.message}")
            }
        }
    }

    fun startGame(roomInfo: JoyRoomInfo, gameEntity: JoyGameEntity) {
        viewModelScope.launch {
            try {
                val assistantToken = TokenGenerator.fetchToken(
                    roomInfo.roomId,
                    roomInfo.assistantUid.toString(),
                    TokenGenerator.TokenGeneratorType.token007,
                    TokenGenerator.AgoraTokenType.rtc
                )
                JoyLogger.e(TAG, "assistantToken：${assistantToken}")
                val requestEntity = JoyGameEntity(
                    appId = mAppId,
                    basicAuth = mBasicAuth,
                    gameId = gameEntity.gameId,
                    openId = mUser.id.toString(),
                    avatar = mUser.headUrl,
                    nickname = mUser.name,
                    rtcConfig = JoyRtcConfig(
                        broadcastUid = mUser.id.toInt(),
                        uid = roomInfo.assistantUid,
                        token = assistantToken,
                        channelName = roomInfo.roomId
                    )
                )
                val res: JoyApiResult<JoyGameResult> = mJoyApiService.startGame(requestEntity)
               if (res.isSucceed){
                   mCurrentGame = gameEntity
               }
                mStartGame.value = res.isSucceed
            } catch (e: Exception) {
                mStartGame.value = false
                Log.e(TAG, "startGame Exception ${e.message}")
            }
        }
    }

    fun stopGame(roomInfo: JoyRoomInfo, gameId: String, assistantToken: String) {
        viewModelScope.launch {
            try {
                val gameEntity = JoyGameEntity(
                    appId = mAppId,
                    basicAuth = mBasicAuth,
                    gameId = gameId,
                    openId = mUser.id.toString(),
                    avatar = mUser.headUrl,
                    nickname = mUser.name,
                    rtcConfig = JoyRtcConfig(
                        broadcastUid = mUser.id.toInt(),
                        uid = roomInfo.assistantUid,
                        token = assistantToken,
                        channelName = roomInfo.roomId
                    )
                )
                val res: JoyApiResult<JoyGameResult> = mJoyApiService.startGame(gameEntity)
                if (res.isSucceed) {
                    mStartGame.value = true
                    mCurrentGame = null
                } else {
                    ToastUtils.showToast("启动游戏失败")
                }
            } catch (e: Exception) {
                Log.e(TAG, "startGame Exception ${e.message}")
            }
        }
    }
}