package io.agora.scene.voice.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.ValueCallBack
import io.agora.chat.ChatClient
import io.agora.chat.ChatRoom
import io.agora.scene.voice.bean.RoomKitBean
import io.agora.scene.voice.general.livedatas.SingleSourceLiveData
import io.agora.scene.voice.general.net.VRValueCallBack
import io.agora.scene.voice.general.repositories.NetworkOnlyResource
import io.agora.scene.voice.general.repositories.VoiceRoomLivingRepository
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.service.*
import io.agora.voice.baseui.general.callback.ResultCallBack
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.buddy.tool.ThreadManager
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

    private val _roomDetailsObservable: SingleSourceLiveData<Resource<VoiceRoomInfo>> = SingleSourceLiveData()
    private val _joinObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _roomNoticeObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _openBotObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _closeBotObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _robotVolumeObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _muteMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> = SingleSourceLiveData()
    private val _unMuteMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> = SingleSourceLiveData()
    private val _leaveMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> = SingleSourceLiveData()
    private val _forbidMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> = SingleSourceLiveData()
    private val _cancelForbidMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> = SingleSourceLiveData()
    private val _kickMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> = SingleSourceLiveData()
    private val _rejectMicInvitationObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _lockMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> = SingleSourceLiveData()
    private val _cancelLockMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> = SingleSourceLiveData()
    private val _startMicSeatApplyObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _cancelMicSeatApplyObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _changeMicObservable: SingleSourceLiveData<Resource<Map<Int,VoiceMicInfoModel>>> = SingleSourceLiveData()
    private val _acceptMicSeatInvitationObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> = SingleSourceLiveData()
    private val _giftContributeObservable: SingleSourceLiveData<Resource<List<VoiceRankUserModel>>> =
        SingleSourceLiveData()

    /**房间详情*/
    fun roomDetailsObservable(): LiveData<Resource<VoiceRoomInfo>> = _roomDetailsObservable

    /**加入im房间&&rtc 频道*/
    fun joinObservable(): LiveData<Resource<Boolean>> = _joinObservable

    /**更新公告*/
    fun roomNoticeObservable(): LiveData<Resource<Boolean>> = _roomNoticeObservable

    /**打开机器人*/
    fun openBotObservable(): LiveData<Resource<Boolean>> = _openBotObservable

    /**关闭机器人*/
    fun closeBotObservable(): LiveData<Resource<Boolean>> = _closeBotObservable

    /**改变机器人音量*/
    fun robotVolumeObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _robotVolumeObservable

    /**本地禁麦*/
    fun muteMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _muteMicObservable

    /**取消本地禁麦*/
    fun unMuteMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _unMuteMicObservable

    /**下麦*/
    fun leaveMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _leaveMicObservable

    /**禁言指定麦位*/
    fun forbidMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _forbidMicObservable

    /**取消禁言指定麦位*/
    fun cancelForbidMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _cancelForbidMicObservable

    /** 踢用户下麦*/
    fun kickMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _kickMicObservable

    /**用户拒绝上麦申请*/
    fun rejectMicInvitationObservable(): LiveData<Resource<Boolean>> = _rejectMicInvitationObservable

    /**锁麦*/
    fun lockMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _lockMicObservable

    /**取消锁麦*/
    fun cancelLockMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _cancelLockMicObservable

    /**申请上麦*/
    fun startMicSeatApplyObservable(): LiveData<Resource<Boolean>> = _startMicSeatApplyObservable

    /**取消申请*/
    fun cancelMicSeatApplyObservable(): LiveData<Resource<Boolean>> = _cancelMicSeatApplyObservable

    /**换麦*/
    fun changeMicObservable(): LiveData<Resource<Map<Int,VoiceMicInfoModel>>> = _changeMicObservable

    /**接受邀请*/
    fun acceptMicSeatInvitationObservable(): LiveData<Resource<VoiceMicInfoModel>> = _acceptMicSeatInvitationObservable

    /**获取礼物列表*/
    fun giftContributeObservable(): LiveData<Resource<List<VoiceRankUserModel>>> = _giftContributeObservable

    /**获取详情*/
    fun fetchRoomDetail(voiceRoomModel: VoiceRoomModel) {
        _roomDetailsObservable.setSource(mRepository.fetchRoomDetail(voiceRoomModel))
    }

    fun initSdkJoin(roomKitBean: RoomKitBean, password: String?) {
        joinRtcChannel.set(false)
        joinImRoom.set(false)
        AgoraRtcEngineController.get().joinChannel(VoiceBuddyFactory.get().getVoiceBuddy().application(),
            roomKitBean.channelId,
            VoiceBuddyFactory.get().getVoiceBuddy().rtcUid(),
            roomKitBean.soundEffect, roomKitBean.isOwner,
            object : VRValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean) {
                    "rtc  joinChannel onSuccess channelId:${roomKitBean.channelId}".logE()
                    joinRtcChannel.set(true)
                    checkJoinRoom()
                }

                override fun onError(error: Int, errorMsg: String) {
                    "rtc  joinChannel onError channelId:${roomKitBean.channelId},error:$error  $errorMsg".logE()
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
                    "im joinChatRoom onSuccess roomId:${roomKitBean.chatroomId}".logE()
                    joinImRoom.set(true)
                    checkJoinRoom()
                }

                override fun onError(error: Int, errorMsg: String) {
                    ThreadManager.getInstance().runOnMainThread {
                        _joinObservable.setSource(object : NetworkOnlyResource<Boolean>() {
                            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                                callBack.onError(error, errorMsg)
                            }
                        }.asLiveData())
                    }
                    "im joinChatRoom onError roomId:${roomKitBean.chatroomId},$error  $errorMsg".logE()
                }
            })
    }

    private fun checkJoinRoom() {
        if (joinRtcChannel.get() && joinImRoom.get()) {
            ThreadManager.getInstance().runOnMainThreadDelay({
                _joinObservable.setSource(object : NetworkOnlyResource<Boolean>() {
                    override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                        callBack.onSuccess(MutableLiveData(true))
                    }
                }.asLiveData())
            }, 200)
        }
    }

    // 开启/关闭机器人
    fun enableRobot(active: Boolean) {
        if (active){
            _openBotObservable.setSource(mRepository.enableRobot(true))
        }else{
            _closeBotObservable.setSource(mRepository.enableRobot(false))
        }
    }

    // 更新机器人音量
    fun updateBotVolume(robotVolume: Int) {
        _robotVolumeObservable.setSource(mRepository.updateRobotVolume(robotVolume))
    }

    // 更新公告
    fun updateAnnouncement(notice: String) {
        _roomNoticeObservable.setSource(mRepository.updateAnnouncement(notice))
    }

    // 本地禁麦
    fun muteLocal(micIndex: Int) {
        _muteMicObservable.setSource(mRepository.muteLocal(micIndex))
    }

    // 本地取消禁麦
    fun unMuteLocal(micIndex: Int) {
        _unMuteMicObservable.setSource(mRepository.unMuteLocal(micIndex))
    }

    // 下麦
    fun leaveMicMic(micIndex: Int) {
        _leaveMicObservable.setSource(mRepository.leaveMic(micIndex))
    }

    // 禁言指定麦位
    fun forbidMic(micIndex: Int) {
        _muteMicObservable.setSource(mRepository.forbidMic(micIndex))
    }

    // 取消指定麦位禁言
    fun cancelMuteMic(micIndex: Int) {
        _cancelForbidMicObservable.setSource(mRepository.unForbidMic(micIndex))
    }

    // 踢用户下麦
    fun kickOff(micIndex: Int) {
        _kickMicObservable.setSource(mRepository.kickOff(micIndex))
    }

    // 接受邀请
    fun acceptMicSeatInvitation() {
        _acceptMicSeatInvitationObservable.setSource(mRepository.acceptMicSeatInvitation())
    }

    // 用户拒绝上麦申请
    fun refuseInvite() {
        _rejectMicInvitationObservable.setSource(mRepository.refuseInvite())
    }

    // 锁麦
    fun lockMic(micIndex: Int) {
        _lockMicObservable.setSource(mRepository.lockMic(micIndex))
    }

    // 取消锁麦
    fun unLockMic(micIndex: Int) {
        _cancelLockMicObservable.setSource(mRepository.unLockMic(micIndex))
    }

    // 申请上麦
    fun startMicSeatApply(micIndex: Int?) {
        _startMicSeatApplyObservable.setSource(mRepository.startMicSeatApply(micIndex))
    }

    //取消上麦
    fun cancelMicSeatApply(chatUid: String) {
        _cancelMicSeatApplyObservable.setSource(mRepository.cancelMicSeatApply(chatUid))
    }

    // 换麦
    fun changeMic(oldIndex: Int, newIndex: Int) {
        _changeMicObservable.setSource(mRepository.changeMic(oldIndex, newIndex))
    }

    //  获取礼物列
    fun fetchGiftContribute() {
        _giftContributeObservable.setSource(mRepository.fetchGiftContribute())
    }
}