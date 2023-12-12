package io.agora.scene.joy.ui

import androidx.lifecycle.viewModelScope
import io.agora.scene.base.TokenGenerator
import io.agora.scene.joy.base.BaseViewModel
import io.agora.scene.joy.base.JoyJsonModel
import io.agora.scene.joy.base.StateLiveData
import io.agora.scene.joy.network.JoyApiManager
import io.agora.scene.joy.network.JoyApiService
import io.agora.scene.joy.network.JoyGameDetailResult
import io.agora.scene.joy.network.JoyGameRepo
import io.agora.scene.joy.network.JoyGameResult
import kotlinx.coroutines.launch

class JoyViewModel : BaseViewModel() {

    private val mJoyApiService: JoyApiService by lazy {
        JoyApiManager.create(JoyApiService::class.java)
    }

    private val mJoyGameRepo: JoyGameRepo by lazy {
        JoyGameRepo(mJoyApiService)
    }

    val mGameConfigLiveData = StateLiveData<JoyGameResult>()
    val mGameListLiveData = StateLiveData<JoyGameResult>()
    val mGameDetailLiveData = StateLiveData<JoyGameDetailResult>()
    val mStartGameLiveData = StateLiveData<JoyGameResult>()
    val mStopGameLiveData = StateLiveData<JoyJsonModel.JoyEmpty>()
    val mSendGiftLiveData = StateLiveData<JoyJsonModel.JoyEmpty>()
    val mSendCommentLiveData = StateLiveData<JoyJsonModel.JoyEmpty>()
    val mSendLikeLiveData = StateLiveData<JoyJsonModel.JoyEmpty>()
    val mGameStatusLiveData = StateLiveData<JoyGameResult>()
    val mGameRenewTokenLiveData = StateLiveData<JoyJsonModel.JoyEmpty>()

    val mGameDetail: JoyGameDetailResult?
        get() = mGameDetailLiveData.value?.data

    val mGamId: String
        get() = mGameDetail?.gameId ?: ""

    fun getGameConfig() {
        viewModelScope.launch {
            mJoyGameRepo.getGameConfig(mGameConfigLiveData)
        }
    }

    fun getGames() {
        viewModelScope.launch {
            mJoyGameRepo.getGames(mGameListLiveData)
        }
    }

    fun getGameDetail(gameId: String) {
        viewModelScope.launch {
            mJoyGameRepo.getGameDetail(
                gameId = gameId,
                mGameDetailLiveData
            )
        }
    }

    fun startGame(roomId: String, gameId: String, assistantUid: Int) {
        viewModelScope.launch {
            val assistantToken = TokenGenerator.fetchToken(
                roomId,
                assistantUid.toString(),
                TokenGenerator.TokenGeneratorType.token007,
                TokenGenerator.AgoraTokenType.rtc
            )
            mJoyGameRepo.startGame(
                gameId = gameId,
                roomId = roomId,
                assistantUid = assistantUid,
                assistantToken = assistantToken,
                mStartGameLiveData
            )
        }
    }

    fun stopGame(roomId: String, gameId: String, taskId: String) {
        viewModelScope.launch {
            mJoyGameRepo.stopGame(
                roomId = roomId,
                gameId = gameId,
                taskId = taskId,
                mStopGameLiveData
            )
        }
    }

    fun sendGift(gameId: String, roomId: String, giftId: String, giftNum: Int, giftValue: Int) {
        viewModelScope.launch {
            mJoyGameRepo.sendGift(
                gameId = gameId,
                roomId = roomId,
                giftId = giftId,
                giftNum = giftNum,
                giftValue = giftValue,
                mSendGiftLiveData
            )
        }
    }

    fun sendComment(gameId: String, roomId: String, content: String) {
        viewModelScope.launch {
            mJoyGameRepo.sendComment(
                gameId = gameId,
                roomId = roomId,
                content = content,
                mSendCommentLiveData
            )
        }
    }

    fun sendLike(gameId: String, roomId: String, likeNum: Int) {
        viewModelScope.launch {
            mJoyGameRepo.sendLike(
                gameId = gameId,
                roomId = roomId,
                likeNum = likeNum,
                mSendLikeLiveData
            )
        }
    }

    fun gameState(gameId: String, taskId: String) {
        viewModelScope.launch {
            mJoyGameRepo.gameState(
                gameId = gameId,
                taskId = taskId,
                mGameStatusLiveData
            )
        }
    }

    fun gameRenewToken(gameId: String, taskId: String, roomId: String, assistantUid: String) {
        viewModelScope.launch {
            val assistantToken = TokenGenerator.fetchToken(
                roomId,
                assistantUid,
                TokenGenerator.TokenGeneratorType.token007,
                TokenGenerator.AgoraTokenType.rtc
            )
            mJoyGameRepo.gameRenewToken(
                gameId = gameId,
                roomId = roomId,
                taskId = taskId,
                rtcUid = assistantUid,
                rtcToken = assistantToken,
                mGameRenewTokenLiveData
            )
        }
    }

}