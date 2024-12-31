package io.agora.scene.voice.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.ValueCallBack
import io.agora.chat.ChatRoom
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.global.VoiceCenter
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.*
import io.agora.scene.voice.netkit.CHATROOM_CREATE_TYPE_USER
import io.agora.scene.voice.netkit.VoiceToolboxServerHttpManager
import io.agora.scene.voice.netkit.callback.VRValueCallBack
import io.agora.scene.voice.viewmodel.repositories.VoiceRoomLivingRepository
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.netkit.Resource
import io.agora.scene.voice.netkit.callback.ResultCallBack
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Voice Chat Room
 *
 * @author create by zhangwei03
 */
class VoiceRoomLivingViewModel : ViewModel() {

    companion object {
        private const val TAG = "VoiceRoomLivingViewModel"
    }

    private val mRepository by lazy { VoiceRoomLivingRepository() }

    private val joinRtcChannel = AtomicBoolean(false)
    private val joinImRoom = AtomicBoolean(false)

    private val _roomDetailsObservable: SingleSourceLiveData<Resource<VoiceRoomInfo>> =
        SingleSourceLiveData()
    private val _joinObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _roomNoticeObservable: SingleSourceLiveData<Resource<Pair<String, Boolean>>> =
        SingleSourceLiveData()
    private val _openBotObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _closeBotObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _robotVolumeObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> =
        SingleSourceLiveData()
    private val _muteMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> =
        SingleSourceLiveData()
    private val _unMuteMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> =
        SingleSourceLiveData()
    private val _leaveMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> =
        SingleSourceLiveData()
    private val _forbidMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> =
        SingleSourceLiveData()
    private val _cancelForbidMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> =
        SingleSourceLiveData()
    private val _kickMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> =
        SingleSourceLiveData()
    private val _rejectMicInvitationObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _lockMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> =
        SingleSourceLiveData()
    private val _cancelLockMicObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> =
        SingleSourceLiveData()
    private val _startMicSeatApplyObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _cancelMicSeatApplyObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _changeMicObservable: SingleSourceLiveData<Resource<Map<Int, VoiceMicInfoModel>>> =
        SingleSourceLiveData()
    private val _acceptMicSeatInvitationObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> =
        SingleSourceLiveData()
    private val _leaveSyncRoomObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _updateRoomMemberObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()

    /** Room details */
    fun roomDetailsObservable(): LiveData<Resource<VoiceRoomInfo>> = _roomDetailsObservable

    /** Join IM room & RTC channel */
    fun joinObservable(): LiveData<Resource<Boolean>> = _joinObservable

    /** Update announcement */
    fun roomNoticeObservable(): LiveData<Resource<Pair<String, Boolean>>> = _roomNoticeObservable

    /** Enable robot */
    fun openBotObservable(): LiveData<Resource<Boolean>> = _openBotObservable

    /** Disable robot */
    fun closeBotObservable(): LiveData<Resource<Boolean>> = _closeBotObservable

    /** Change robot volume */
    fun robotVolumeObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _robotVolumeObservable

    /** Local mic mute */
    fun muteMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _muteMicObservable

    /** Cancel local mic mute */
    fun unMuteMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _unMuteMicObservable

    /** Leave mic */
    fun leaveMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _leaveMicObservable

    /** Mute specific mic position */
    fun forbidMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _forbidMicObservable

    /** Unmute specific mic position */
    fun cancelForbidMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _cancelForbidMicObservable

    /** Kick user off mic */
    fun kickMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _kickMicObservable

    /** User rejects mic invitation */
    fun rejectMicInvitationObservable(): LiveData<Resource<Boolean>> = _rejectMicInvitationObservable

    /** Lock mic */
    fun lockMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _lockMicObservable

    /** Unlock mic */
    fun cancelLockMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _cancelLockMicObservable

    /** Apply for mic */
    fun startMicSeatApplyObservable(): LiveData<Resource<Boolean>> = _startMicSeatApplyObservable

    /** Cancel application */
    fun cancelMicSeatApplyObservable(): LiveData<Resource<Boolean>> = _cancelMicSeatApplyObservable

    /** Change mic position */
    fun changeMicObservable(): LiveData<Resource<Map<Int, VoiceMicInfoModel>>> = _changeMicObservable

    /** Accept invitation */
    fun acceptMicSeatInvitationObservable(): LiveData<Resource<VoiceMicInfoModel>> = _acceptMicSeatInvitationObservable

    /** Update member list */
    fun updateRoomMemberObservable(): LiveData<Resource<Boolean>> = _updateRoomMemberObservable

    /** Get details */
    fun fetchRoomDetail(voiceRoomModel: VoiceRoomModel) {
        _roomDetailsObservable.setSource(mRepository.fetchRoomDetail(voiceRoomModel))
    }

    fun initSdkJoin(context: Context, voiceRoomModel: VoiceRoomModel) {
        joinRtcChannel.set(false)
        joinImRoom.set(false)
        AgoraRtcEngineController.get().joinChannel(
            context.applicationContext,
            voiceRoomModel.roomId,
            VoiceCenter.rtcUid,
            voiceRoomModel.soundEffect, voiceRoomModel.isOwner,
            object : VRValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean) {
                    VoiceLogger.d(TAG, "rtc  joinChannel onSuccess channelId:${voiceRoomModel.roomId}")
                    joinRtcChannel.set(true)
                    checkJoinRoom()
                }

                override fun onError(error: Int, errorMsg: String) {
                    VoiceLogger.e(
                        TAG,
                        "rtc  joinChannel onError channelId:${voiceRoomModel.roomId},error:$error $errorMsg"
                    )
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
        ChatroomIMManager.getInstance()
            .joinRoom(voiceRoomModel.chatroomId, object : ValueCallBack<ChatRoom?> {
                override fun onSuccess(value: ChatRoom?) {
                    VoiceLogger.d(TAG, "im joinChatRoom onSuccess roomId:${voiceRoomModel.chatroomId}")
                    joinImRoom.set(true)
                    checkJoinRoom()
                }

                override fun onError(error: Int, errorMsg: String) {
                    _joinObservable.setSource(object : NetworkOnlyResource<Boolean>() {
                        override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                            callBack.onError(error, errorMsg)
                        }
                    }.asLiveData())
                    VoiceLogger.e(TAG, "im joinChatRoom onError roomId:${voiceRoomModel.chatroomId},$error  $errorMsg")
                }
            })
    }

    private fun checkJoinRoom() {
        ThreadManager.getInstance().runOnMainThread {
            if (joinRtcChannel.get() && joinImRoom.get()) {
                _joinObservable.setSource(object : NetworkOnlyResource<Boolean>() {
                    override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                        callBack.onSuccess(MutableLiveData(true))
                    }
                }.asLiveData())
            }
        }
    }

    // Enable/Disable robot
    fun enableRobot(active: Boolean) {
        if (active) {
            _openBotObservable.setSource(mRepository.enableRobot(true))
        } else {
            _closeBotObservable.setSource(mRepository.enableRobot(false))
        }
    }

    // Update robot volume
    fun updateBotVolume(robotVolume: Int) {
        _robotVolumeObservable.setSource(mRepository.updateRobotVolume(robotVolume))
    }

    // Update announcement
    fun updateAnnouncement(notice: String) {
        _roomNoticeObservable.setSource(mRepository.updateAnnouncement(notice))
    }

    // Local mic mute
    fun muteLocal(micIndex: Int) {
        _muteMicObservable.setSource(mRepository.muteLocal(micIndex))
    }

    // Cancel local mic mute
    fun unMuteLocal(micIndex: Int) {
        _unMuteMicObservable.setSource(mRepository.unMuteLocal(micIndex))
    }

    // Leave mic
    fun leaveMic(micIndex: Int) {
        _leaveMicObservable.setSource(mRepository.leaveMic(micIndex))
    }

    // Mute specific mic position
    fun forbidMic(micIndex: Int) {
        _forbidMicObservable.setSource(mRepository.forbidMic(micIndex))
    }

    // Unmute specific mic position
    fun cancelMuteMic(micIndex: Int) {
        _cancelForbidMicObservable.setSource(mRepository.unForbidMic(micIndex))
    }

    // Kick user off mic
    fun kickOff(micIndex: Int) {
        _kickMicObservable.setSource(mRepository.kickOff(micIndex))
    }

    // Accept invitation
    fun acceptMicSeatInvitation(micIndex: Int) {
        _acceptMicSeatInvitationObservable.setSource(mRepository.acceptMicSeatInvitation(micIndex))
    }

    // User rejects mic invitation
    fun refuseInvite() {
        _rejectMicInvitationObservable.setSource(mRepository.refuseInvite())
    }

    // Lock mic
    fun lockMic(micIndex: Int) {
        _lockMicObservable.setSource(mRepository.lockMic(micIndex))
    }

    // Unlock mic
    fun unLockMic(micIndex: Int) {
        _cancelLockMicObservable.setSource(mRepository.unLockMic(micIndex))
    }

    // Apply for mic
    fun startMicSeatApply(micIndex: Int?) {
        _startMicSeatApplyObservable.setSource(mRepository.startMicSeatApply(micIndex))
    }

    // Cancel mic application
    fun cancelMicSeatApply(chatroomId: String, chatUid: String) {
        _cancelMicSeatApplyObservable.setSource(mRepository.cancelMicSeatApply(chatroomId, chatUid))
    }

    // Change mic position
    fun changeMic(oldIndex: Int, newIndex: Int) {
        _changeMicObservable.setSource(mRepository.changeMic(oldIndex, newIndex))
    }

    fun leaveSyncManagerRoom() {
        _leaveSyncRoomObservable.setSource(mRepository.leaveSyncManagerRoom())
    }

    fun updateRoomMember() {
        _updateRoomMemberObservable.setSource(mRepository.updateRoomMember())
    }

    fun renewChatToken() {
        VoiceToolboxServerHttpManager.createImRoom(
            roomName = "",
            roomOwner = "",
            chatroomId = "",
            type = CHATROOM_CREATE_TYPE_USER
        ) { resp, error ->
            if (error == null && resp != null) {
                resp.chatToken?.let {
                    VoiceCenter.chatToken = it
                    ChatroomIMManager.getInstance().renewToken(it)
                }
            }
        }

    }
}