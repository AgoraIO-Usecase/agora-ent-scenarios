package io.agora.scene.playzone.live

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.imkitmanager.model.AUIChatRoomInfo
import io.agora.imkitmanager.ui.IAUIChatListView
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.playzone.PlayCenter
import io.agora.scene.playzone.PlayLogger
import io.agora.scene.playzone.R
import io.agora.scene.playzone.service.PlayChatRoomService
import io.agora.scene.playzone.service.PlayZoneServiceListenerProtocol
import io.agora.scene.playzone.service.PlayZoneServiceProtocol
import io.agora.scene.playzone.service.PlayRobotInfo
import io.agora.scene.playzone.service.PlayZoneParameters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class PlayGameViewModel constructor(val mRoomInfo: AUIRoomInfo) : ViewModel() {

    private val TAG = "Play_Zone_Scene_LOG"

    private val mainHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private fun runOnMainThread(runnable: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    // RTC engine
    private var mRtcEngine: RtcEngineEx? = null

    private val mPlayServiceProtocol by lazy { PlayZoneServiceProtocol.serviceProtocol }

    private val mChatRoomService by lazy {
        PlayChatRoomService.chatRoomService
    }

    // Network status
    val networkStatusLiveData = MutableLiveData<NetWorkEvent>()

    // Room destroyed
    val roomDestroyLiveData = MutableLiveData<Boolean>()

    // Room expired
    val roomExpireLiveData = MutableLiveData<Boolean>()

    // Room user count
    val userCountLiveData = MutableLiveData<Int>()

    // Robot
    val mRobotListLiveData = MutableLiveData<List<PlayRobotInfo>>()

    // Room alive time
    val mRoomTimeLiveData = MutableLiveData<String>()

    // Is room owner
    val isRoomOwner: Boolean get() = mRoomInfo.roomOwner?.userId == PlayCenter.mUser.id.toString()

    // Room owner
    val mRoomOwner: String get() = mRoomInfo.roomOwner?.userId ?: ""

    private val dataFormat = SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("GMT") }

    private val topTimerTask = object : Runnable {
        override fun run() {
            val currentDuration = mPlayServiceProtocol.getCurrentDuration(mRoomInfo.roomId)
            if (currentDuration > 0) {
                mRoomTimeLiveData.value = dataFormat.format(Date(currentDuration))
            }
            mainHandler.postDelayed(this, 1000)
        }
    }

    private fun context(): Context {
        return AgoraApplication.the().applicationContext
    }

    // Initialize chat
    fun initChatRoom(chatListView: IAUIChatListView) {
        mChatRoomService.imManagerService.setChatListView(chatListView)
        val chatRoomId = mRoomInfo.customPayload[PlayZoneParameters.CHAT_ID] as? String ?: return
        val chatRoomInfo = AUIChatRoomInfo(mRoomOwner, chatRoomId)

        mChatRoomService.imManagerService.joinChatRoom(chatRoomInfo, completion = { error ->
            if (error == null) {
                insertLocalMessage(context().getString(R.string.play_zone_room_welcome), 0)
            }
            error?.message?.let {
                ToastUtils.showToast(it)
            }
        })
    }

    // Initialize
    fun initData() {
        initRtcEngine()
        mainHandler.postDelayed(topTimerTask, 1000)
        mPlayServiceProtocol.subscribeListener(serviceListenerProtocol)
    }

    private fun initRtcEngine() {
        val rtcAppId = PlayCenter.mAppId
        if (TextUtils.isEmpty(rtcAppId)) {
            throw NullPointerException("please check \"gradle.properties\"")
        }
        if (mRtcEngine != null) return
        val config = RtcEngineConfig()
        config.mContext = AgoraApplication.the()
        config.mAppId = io.agora.scene.base.BuildConfig.AGORA_APP_ID
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
                // Network status callback, local user uid = 0
                if (uid == 0) {
                    networkStatusLiveData.postValue(NetWorkEvent(txQuality, rxQuality))
                }
            }

            override fun onContentInspectResult(result: Int) {
                super.onContentInspectResult(result)
                if (result > 1) {
                    ToastUtils.showToast(R.string.play_zone_content_inspect)
                }
            }

            override fun onError(err: Int) {
                super.onError(err)
                PlayLogger.e(TAG, "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
            }
        }
        config.mChannelProfile = Constants.CHANNEL_PROFILE_GAME
        try {
            mRtcEngine = RtcEngine.create(config) as RtcEngineEx
        } catch (e: Exception) {
            e.printStackTrace()
            PlayLogger.e(TAG, "RtcEngine.create() called error: $e")
        }
        // ------------------ Join channel ------------------
        mRtcEngine?.apply {
            enableAudio()
            setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
            val channelMediaOption = ChannelMediaOptions()
            channelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelMediaOption.autoSubscribeAudio = true
            channelMediaOption.publishMicrophoneTrack = true
            val ret =
                joinChannel(PlayCenter.mRtcToken, mRoomInfo.roomId, PlayCenter.mUser.id.toInt(), channelMediaOption)
            if (ret != Constants.ERR_OK) {
                PlayLogger.e(TAG, "joinRTC() called error: $ret")
            }
        }
        muteMic(!isRoomOwner)

        // ------------------ Enable voice moderation service ------------------
        AudioModeration.moderationAudio(mRoomInfo.roomId,
            PlayCenter.mUser.id,
            AudioModeration.AgoraChannelType.rtc,
            "play_zone",
            success = {
                PlayLogger.d(TAG, "moderationAudio success")
            },
            failure = {
                PlayLogger.e(TAG, "moderationAudio failure:$it")
            })
    }

    private val serviceListenerProtocol = object : PlayZoneServiceListenerProtocol {

        override fun onRoomDestroy() {
            innerRelease()
            roomDestroyLiveData.value = true
        }

        override fun onRoomExpire() {
            innerRelease()
            roomExpireLiveData.value = true
        }

        override fun onUserCountUpdate(userCount: Int) {
            userCountLiveData.value = userCount
        }

        override fun onRobotMapSnapshot(robotMap: Map<String, PlayRobotInfo>) {
            val robotList = mutableListOf<PlayRobotInfo>()
            robotMap.values.forEach { robotInfo ->
                robotList.add(robotInfo)
            }
            mRobotListLiveData.value = robotList
        }
    }

    private fun innerRelease() {
        mainHandler.removeCallbacks(topTimerTask)
        mPlayServiceProtocol.unsubscribeListener(serviceListenerProtocol)
        PlayLogger.d(TAG, "release called")
        mRtcEngine?.apply {
            leaveChannel()
            RtcEngine.destroy()
            mRtcEngine = null
        }
        mChatRoomService.imManagerService.leaveChatRoom { }
    }

    // Exit room
    fun exitRoom() {
        PlayLogger.d(TAG, "RoomLivingViewModel.exitRoom() called")
        mPlayServiceProtocol.leaveRoom { e: Exception? ->
            if (e == null) { // success
                PlayLogger.d(TAG, "RoomLivingViewModel.exitRoom() success")
            } else { // failure
                PlayLogger.e(TAG, "RoomLivingViewModel.exitRoom() failed: $e")
                e.message?.let { error ->
                    ToastUtils.showToast(error)
                }
            }
        }
        innerRelease()
    }

    // mute mic
    fun muteMic(mute: Boolean) {
        PlayLogger.d(TAG, "RoomLivingViewModel.mute() called mute:$mute")
        mRtcEngine?.muteLocalAudioStream(mute)
    }

    // Send message
    fun sendMessage(message: String) {
        mChatRoomService.imManagerService.sendMessage(message, completion = { chatMessage, error -> })
    }

    // Insert local message
    fun insertLocalMessage(message: String, index: Int = -1) {
        mChatRoomService.imManagerService.insertLocalMessage(message, index, completion = { chatMessage, error -> })
    }
}