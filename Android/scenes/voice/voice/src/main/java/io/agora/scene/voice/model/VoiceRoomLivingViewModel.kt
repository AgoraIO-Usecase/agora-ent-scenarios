package io.agora.scene.voice.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.ValueCallBack
import io.agora.chat.ChatClient
import io.agora.chat.ChatRoom
import io.agora.scene.voice.bean.RoomKitBean
import io.agora.scene.voice.general.livedatas.SingleSourceLiveData
import io.agora.scene.voice.general.repositories.NetworkOnlyResource
import io.agora.scene.voice.general.repositories.VoiceRoomLivingRepository
import io.agora.scene.voice.rtckit.RtcChannelTemp
import io.agora.scene.voice.rtckit.RtcRoomController
import io.agora.scene.voice.service.VoiceBuddyFactory
import io.agora.voice.baseui.general.callback.ResultCallBack
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.voice.network.tools.VRDefaultValueCallBack
import io.agora.voice.network.tools.bean.VRMicBean
import io.agora.voice.network.tools.bean.VRMicListBean
import io.agora.voice.network.tools.bean.VRoomInfoBean
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 语聊房
 *
 * @author create by zhangwei03
 */
class VoiceRoomLivingViewModel : ViewModel() {

    private val mRepository by lazy { VoiceRoomLivingRepository() }

    private val joinRtcChannel = AtomicBoolean(false)
    private val joinImRoom = AtomicBoolean(false)

    private val _roomDetailsObservable: SingleSourceLiveData<Resource<VRoomInfoBean>> = SingleSourceLiveData()
    private val _joinObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _leaveObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _roomNoticeObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _openBotObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _closeBotObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _robotVolumeObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _applyMicListObservable: SingleSourceLiveData<Resource<VRMicListBean>> = SingleSourceLiveData()
    private val _micInfoObservable: SingleSourceLiveData<Resource<VRMicBean>> = SingleSourceLiveData()
    private val _closeMicObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _cancelCloseMicObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _leaveMicObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _muteMicObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _cancelMuteMicObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _kickMicObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _rejectMicInvitationObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _lockMicObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _cancelLockMicObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _invitationMicObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _applySubmitMicObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _rejectSubmitMicObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()

    /**房间详情*/
    fun roomDetailsObservable(): LiveData<Resource<VRoomInfoBean>> = _roomDetailsObservable

    /**加入im房间&&rtc 频道*/
    fun joinObservable(): LiveData<Resource<Boolean>> = _joinObservable

    /**来开im 房间&& rtc 频道*/
    fun leaveObservable(): LiveData<Resource<Boolean>> = _leaveObservable

    /**更新公告*/
    fun roomNoticeObservable(): LiveData<Resource<Boolean>> = _roomNoticeObservable

    /**打开机器人*/
    fun openBotObservable(): LiveData<Resource<Boolean>> = _openBotObservable

    /**关闭机器人*/
    fun closeBotObservable(): LiveData<Resource<Boolean>> = _closeBotObservable

    /**改变机器人音量*/
    fun robotVolumeObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _robotVolumeObservable

    /**获取申请列表*/
    fun applyMicListObservable(): LiveData<Resource<VRMicListBean>> = _applyMicListObservable

    /**麦位信息*/
    fun micInfoObservable(): LiveData<Resource<VRMicBean>> = _micInfoObservable

    /**关麦*/
    fun closeMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _closeMicObservable

    /**取消关麦*/
    fun cancelCloseMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _cancelCloseMicObservable

    /**下麦*/
    fun leaveMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _leaveMicObservable

    /**禁言指定麦位*/
    fun muteMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _muteMicObservable

    /**取消禁言指定麦位*/
    fun cancelMuteMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _cancelMuteMicObservable

    /** 踢用户下麦*/
    fun kickMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _kickMicObservable

    /**用户拒绝上麦申请*/
    fun rejectMicInvitationObservable(): LiveData<Resource<Boolean>> = _rejectMicInvitationObservable

    /**锁麦*/
    fun lockMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _lockMicObservable

    /**取消锁麦*/
    fun cancelLockMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _cancelLockMicObservable

    /**邀请上麦*/
    fun invitationMicObservable(): LiveData<Resource<Boolean>> = _invitationMicObservable

    /**同意上麦申请*/
    fun applySubmitMicObservable(): LiveData<Resource<Boolean>> = _applySubmitMicObservable

    /**拒绝上麦申请*/
    fun rejectSubmitMicObservable(): LiveData<Resource<Boolean>> = _rejectSubmitMicObservable

    fun getDetails(context: Context?, roomId: String?) {
        _roomDetailsObservable.setSource(mRepository.getRoomInfo(context, roomId))
    }

    fun initSdkJoin(roomKitBean: RoomKitBean, password: String?) {
        joinRtcChannel.set(false)
        joinImRoom.set(false)
        RtcRoomController.get().joinChannel(VoiceBuddyFactory.get().getVoiceBuddy().application(),
            roomKitBean.channelId,
            VoiceBuddyFactory.get().getVoiceBuddy().rtcUid(), roomKitBean.isOwner,
            object : VRDefaultValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean) {
                    "rtc  joinChannel onSuccess ".logE()
                    joinRtcChannel.set(true)
                    joinRoom(roomKitBean.roomId, password)
                }

                override fun onError(error: Int, errorMsg: String) {
                    "rtc  joinChannel onError $error  $errorMsg".logE()
                    ThreadManager.getInstance().runOnMainThread {
                        _joinObservable.setSource(object : NetworkOnlyResource<Boolean>() {
                            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                                callBack.onError(error, errorMsg)
                            }
                        }.asLiveData())
                    }

                }
            }
        )
        ChatClient.getInstance().chatroomManager()
            .joinChatRoom(roomKitBean.chatroomId, object : ValueCallBack<ChatRoom?> {
                override fun onSuccess(value: ChatRoom?) {
                    "im  joinChatRoom onSuccess ".logE()
                    joinImRoom.set(true)
                    joinRoom(roomKitBean.roomId, password)
                }

                override fun onError(error: Int, errorMsg: String) {
                    ThreadManager.getInstance().runOnMainThread {
                        _joinObservable.setSource(object : NetworkOnlyResource<Boolean>() {
                            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                                callBack.onError(error, errorMsg)
                            }
                        }.asLiveData())
                    }
                    "im  joinChatRoom onError $error  $errorMsg".logE()
                }
            })
    }

    fun joinRoom(roomId: String?, password: String?) {
        if (joinRtcChannel.get() && joinImRoom.get()) {
            ThreadManager.getInstance().runOnMainThreadDelay({
                _joinObservable.setSource(
                    mRepository.joinRoom(
                        VoiceBuddyFactory.get().getVoiceBuddy().application(),
                        roomId, password
                    )
                )
            }, 200)
        }
    }

    fun leaveRoom(context: Context, roomId: String?) {
        _leaveObservable.setSource(mRepository.leaveRoom(context, roomId))
    }


    fun activeBot(context: Context, roomId: String, active: Boolean) {
        if (active) {
            _openBotObservable.setSource(mRepository.activeBot(context, roomId, true))
        } else {
            _closeBotObservable.setSource(mRepository.activeBot(context, roomId, false))
        }
    }

    fun updateBotVolume(context: Context, roomId: String, robotVolume: Int) {
        _robotVolumeObservable.setSource(mRepository.changeRobotVolume(context, roomId, robotVolume))
    }

    fun updateRoomNotice(context: Context, roomId: String, notice: String) {
        _roomNoticeObservable.setSource(mRepository.updateRoomNotice(context, roomId, notice))
    }

    // 获取上麦申请列表
    fun getApplyMicList(context: Context, roomId: String, cursor: String, micIndex: Int) {
        _applyMicListObservable.setSource(mRepository.getApplyMicList(context, roomId, cursor, micIndex))
    }

    // 获取麦位信息
    fun getMicInfo(context: Context, roomId: String) {
        _micInfoObservable.setSource(mRepository.getMicInfo(context, roomId))
    }

    // 关麦
    fun closeMic(context: Context, roomId: String, micIndex: Int) {
        _closeMicObservable.setSource(mRepository.closeMic(context, roomId, micIndex))
    }

    // 取消关麦
    fun cancelCloseMic(context: Context, roomId: String, micIndex: Int) {
        _cancelCloseMicObservable.setSource(mRepository.cancelCloseMic(context, roomId, micIndex))
    }

    // 下麦
    fun leaveMicMic(context: Context, roomId: String, micIndex: Int) {
        _leaveMicObservable.setSource(mRepository.leaveMicMic(context, roomId, micIndex))
    }

    // 禁言指定麦位
    fun muteMic(context: Context, roomId: String, micIndex: Int) {
        _muteMicObservable.setSource(mRepository.muteMic(context, roomId, micIndex))
    }

    // 取消指定麦位禁言
    fun cancelMuteMic(context: Context, roomId: String, micIndex: Int) {
        _cancelMuteMicObservable.setSource(mRepository.cancelMuteMic(context, roomId, micIndex))
    }

    // 踢用户下麦
    fun kickMic(context: Context, roomId: String, userId: String, micIndex: Int) {
        _kickMicObservable.setSource(mRepository.kickMic(context, roomId, userId, micIndex))
    }

    // 用户拒绝上麦申请
    fun rejectMicInvitation(context: Context, roomId: String) {
        _rejectMicInvitationObservable.setSource(mRepository.rejectMicInvitation(context, roomId))
    }

    // 锁麦
    fun lockMic(context: Context, roomId: String, micIndex: Int) {
        _lockMicObservable.setSource(mRepository.lockMic(context, roomId, micIndex))
    }

    // 取消锁麦
    fun cancelLockMic(context: Context, roomId: String, micIndex: Int) {
        _cancelLockMicObservable.setSource(mRepository.cancelLockMic(context, roomId, micIndex))
    }

    //  邀请上麦
    fun invitationMic(context: Context, roomId: String, userId: String) {
        _invitationMicObservable.setSource(mRepository.invitationMic(context, roomId, userId))
    }

    // 同意上麦申请
    fun applySubmitMic(context: Context, roomId: String, userId: String, micIndex: Int) {
        _applySubmitMicObservable.setSource(mRepository.applySubmitMic(context, roomId, userId, micIndex))
    }

    // 拒绝上麦申请
    fun rejectSubmitMic(context: Context, roomId: String, userId: String) {
        _rejectSubmitMicObservable.setSource(mRepository.rejectSubmitMic(context, roomId, userId))
    }
}