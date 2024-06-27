package io.agora.scene.playzone.live

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.imkitmanager.model.AUIChatEntity
import io.agora.imkitmanager.service.IAUIIMManagerService
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

    // rtc 引擎
    private var mRtcEngine: RtcEngineEx? = null

    private val mPlayServiceProtocol by lazy { PlayZoneServiceProtocol.serviceProtocol }

    private val mChatRoomService by lazy {
        PlayChatRoomService.chatRoomService
    }


    // 网络状态
    val networkStatusLiveData = MutableLiveData<NetWorkEvent>()

    // 房间销毁
    val roomDestroyLiveData = MutableLiveData<Boolean>()

    // 房间超时
    val roomExpireLiveData = MutableLiveData<Boolean>()

    // 房间人数
    val userCountLiveData = MutableLiveData<Int>()

    // 机器人
    val mRobotListLiveData = MutableLiveData<List<PlayRobotInfo>>()

    // 房间存活时间
    val mRoomTimeLiveData = MutableLiveData<String>()

    // 聊天消息
    val mRoomChatListLiveData = MutableLiveData<List<AUIChatEntity>>()

    // 是否房主
    val isRoomOwner: Boolean get() = mRoomInfo.roomOwner?.userId == PlayCenter.mUser.id.toString()

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

    // 初始化 chat
    fun initChatRoom() {
        mChatRoomService.imManagerService.registerRespObserver(auiIMManagerRespObserver)
        if (isRoomOwner) {
            mChatRoomService.imManagerService.createChatRoom(
                roomName = mRoomInfo.roomName,
                description = "welcome",
                completion = { response, error ->
                    error?.message?.let {
                        ToastUtils.showToast(it)
                    }
                })
        } else {
            val chatRoomId = mRoomInfo.customPayload[PlayZoneParameters.CHAT_ID] as? String ?: return
            mChatRoomService.imManagerService.joinChatRoom(chatRoomId, completion = { error ->
                error?.message?.let {
                    ToastUtils.showToast(it)
                }
            })
        }
        mChatRoomService.chatManager.saveWelcomeMsg(context().getString(R.string.play_zone_room_welcome))
    }

    // 初始化
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
                // 网络状态回调, 本地user uid = 0
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
        config.mChannelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        try {
            mRtcEngine = RtcEngine.create(config) as RtcEngineEx
        } catch (e: Exception) {
            e.printStackTrace()
            PlayLogger.e(TAG, "RtcEngine.create() called error: $e")
        }
        // ------------------ 加入频道 ------------------
        mRtcEngine?.apply {
            enableAudio()
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

        // ------------------ 开启语音鉴定服务 ------------------
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
        mChatRoomService.imManagerService.unRegisterRespObserver(auiIMManagerRespObserver)
        if (isRoomOwner){
            mChatRoomService.imManagerService.userDestroyedChatroom()
        } else{
            mChatRoomService.imManagerService.userQuitRoom {  }
        }
    }

    // 退出房间
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

    private val auiIMManagerRespObserver = object : IAUIIMManagerService.AUIIMManagerRespObserver {
        override fun messageDidReceive(chatRoomId: String, message: IAUIIMManagerService.AgoraChatTextMessage) {
            mRoomChatListLiveData.postValue(mChatRoomService.chatManager.getMsgList())
        }

        override fun onUserDidJoinRoom(chatRoomId: String, message: IAUIIMManagerService.AgoraChatTextMessage) {
            mRoomChatListLiveData.postValue(mChatRoomService.chatManager.getMsgList())
        }
    }

    // 发送消息
    fun sendMessage(message: String) {
        mChatRoomService.imManagerService.sendMessage(message, completion = { chatMessage,error->
            if (error==null){
                mRoomChatListLiveData.postValue(mChatRoomService.chatManager.getMsgList())
            }
        })
    }

    // 插入本地消息
    fun insertLocalMessage(message: String) {
        mChatRoomService.chatManager.insertLocalMsg(message)
        mRoomChatListLiveData.postValue(mChatRoomService.chatManager.getMsgList())
    }
}