package io.agora.scene.voice.spatial.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.global.VSpatialCenter
import io.agora.scene.voice.spatial.model.*
import io.agora.scene.voice.spatial.net.NetworkOnlyResource
import io.agora.scene.voice.spatial.net.Resource
import io.agora.scene.voice.spatial.net.SingleSourceLiveData
import io.agora.scene.voice.spatial.net.callback.ResultCallBack
import io.agora.scene.voice.spatial.net.callback.VRValueCallBack
import io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.spatial.viewmodel.repositories.VoiceRoomLivingRepository

/**
 * Voice room
 *
 * @author create by zhangwei03
 */
class VoiceRoomLivingViewModel : ViewModel() {

    companion object {
        private val TAG = VoiceRoomLivingViewModel::class.java.simpleName
    }

    private val mRepository by lazy { VoiceRoomLivingRepository() }

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
    private val _openBlueBotAirAbsorbObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _closeBlueBotAirAbsorbObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _openRedBotAirAbsorbObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _closeRedBotAirAbsorbObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _openBlueBotBlurObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _openRedBotBlurObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _blueRobotAttenuationObservable: SingleSourceLiveData<Resource<Pair<Double, Boolean>>> =
        SingleSourceLiveData()
    private val _redRobotAttenuationObservable: SingleSourceLiveData<Resource<Pair<Double, Boolean>>> =
        SingleSourceLiveData()

    private val _muteMicObservable: SingleSourceLiveData<Resource<VoiceMemberModel>> =
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

    /**Room details*/
    fun roomDetailsObservable(): LiveData<Resource<VoiceRoomInfo>> = _roomDetailsObservable

    /**Join im room && rtc channel*/
    fun joinObservable(): LiveData<Resource<Boolean>> = _joinObservable

    /**Update announcement*/
    fun roomNoticeObservable(): LiveData<Resource<Pair<String, Boolean>>> = _roomNoticeObservable

    /**Open robot*/
    fun openBotObservable(): LiveData<Resource<Boolean>> = _openBotObservable

    /**Close robot*/
    fun closeBotObservable(): LiveData<Resource<Boolean>> = _closeBotObservable

    /**Change robot volume*/
    fun robotVolumeObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _robotVolumeObservable

    /**Open blue robot air absorb*/
    fun openBlueBotAirAbsorbObservable(): LiveData<Resource<Boolean>> = _openBlueBotAirAbsorbObservable

    /**Open red robot air absorb*/
    fun openRedBotAirAbsorbObservable(): LiveData<Resource<Boolean>> = _openRedBotAirAbsorbObservable

    /**Open blue robot blur*/
    fun openBlueBotBlurObservable(): LiveData<Resource<Boolean>> = _openBlueBotBlurObservable

    /**Open red robot blur*/
    fun openRedBotBlurObservable(): LiveData<Resource<Boolean>> = _openRedBotBlurObservable

    /**Blue robot attenuation coefficient*/
    fun blueRobotAttenuationObservable(): LiveData<Resource<Pair<Double, Boolean>>> = _blueRobotAttenuationObservable

    /**Red robot attenuation coefficient*/
    fun redRobotAttenuationObservable(): LiveData<Resource<Pair<Double, Boolean>>> = _redRobotAttenuationObservable

    /**Mute mic*/
    fun muteMicObservable(): LiveData<Resource<VoiceMemberModel>> = _muteMicObservable

    /**Leave mic*/
    fun leaveMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _leaveMicObservable

    /**Forbid mic*/
    fun forbidMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _forbidMicObservable

    /**Cancel forbid mic*/
    fun cancelForbidMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _cancelForbidMicObservable

    /**Kick user off mic*/
    fun kickMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _kickMicObservable

    /**User reject mic invitation*/
    fun rejectMicInvitationObservable(): LiveData<Resource<Boolean>> = _rejectMicInvitationObservable

    /**Lock mic*/
    fun lockMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _lockMicObservable

    /**Cancel lock mic*/
    fun cancelLockMicObservable(): LiveData<Resource<VoiceMicInfoModel>> = _cancelLockMicObservable

    /**Apply for mic seat*/
    fun startMicSeatApplyObservable(): LiveData<Resource<Boolean>> = _startMicSeatApplyObservable

    /**Cancel apply*/
    fun cancelMicSeatApplyObservable(): LiveData<Resource<Boolean>> = _cancelMicSeatApplyObservable

    /**Change mic*/
    fun changeMicObservable(): LiveData<Resource<Map<Int, VoiceMicInfoModel>>> = _changeMicObservable

    /**Accept invitation*/
    fun acceptMicSeatInvitationObservable(): LiveData<Resource<VoiceMicInfoModel>> = _acceptMicSeatInvitationObservable

    /**Leave syncManager room*/
    fun leaveSyncRoomObservable(): LiveData<Resource<Boolean>> = _leaveSyncRoomObservable

    /**Update member list*/
    fun updateRoomMemberObservable(): LiveData<Resource<Boolean>> = _updateRoomMemberObservable

    /**Get details*/
    fun fetchRoomDetail(voiceRoomModel: VoiceRoomModel) {
        _roomDetailsObservable.setSource(mRepository.fetchRoomDetail(voiceRoomModel))
    }

    fun initSdkJoin(roomKitBean: RoomKitBean) {
        AgoraRtcEngineController.get().joinChannel(
            AgoraApplication.the(),
            roomKitBean.channelId,
            VSpatialCenter.rtcUid,
            roomKitBean.soundEffect, roomKitBean.isOwner,
            object : VRValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean) {
                    VoiceSpatialLogger.d(TAG, "rtc  joinChannel onSuccess channelId:${roomKitBean.channelId}")
                    ThreadManager.getInstance().runOnMainThread {
                        _joinObservable.setSource(object : NetworkOnlyResource<Boolean>() {
                            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                                callBack.onSuccess(MutableLiveData(true))
                            }
                        }.asLiveData())
                    }
                }

                override fun onError(error: Int, errorMsg: String) {
                    VoiceSpatialLogger.e(
                        TAG,
                        "rtc  joinChannel onError channelId:${roomKitBean.channelId}, error:$error  $errorMsg"
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
    }

    // Enable/disable robot
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

    fun enableBlueRobotAirAbsorb(active: Boolean) {
        if (active) {
            _openBlueBotAirAbsorbObservable.setSource(mRepository.enableBlueRobotAirAbsorb(true))
        } else {
            _openBlueBotAirAbsorbObservable.setSource(mRepository.enableBlueRobotAirAbsorb(false))
        }
    }

    fun enableRedRobotAirAbsorb(active: Boolean) {
        if (active) {
            _openRedBotAirAbsorbObservable.setSource(mRepository.enableRedRobotAirAbsorb(true))
        } else {
            _openRedBotAirAbsorbObservable.setSource(mRepository.enableRedRobotAirAbsorb(false))
        }
    }

    fun enableBlueRobotBlur(active: Boolean) {
        if (active) {
            _openBlueBotBlurObservable.setSource(mRepository.enableBlueRobotBlur(true))
        } else {
            _openBlueBotBlurObservable.setSource(mRepository.enableBlueRobotBlur(false))
        }
    }

    fun enableRedRobotBlur(active: Boolean) {
        if (active) {
            _openRedBotBlurObservable.setSource(mRepository.enableRedRobotBlur(true))
        } else {
            _openRedBotBlurObservable.setSource(mRepository.enableRedRobotBlur(false))
        }
    }

    // Update robot volume
    fun updateBlueRoBotAttenuation(attenuation: Double) {
        _blueRobotAttenuationObservable.setSource(mRepository.updateBlueRoBotAttenuation(attenuation))
    }

    // Update robot volume
    fun updateRedRoBotAttenuation(attenuation: Double) {
        _redRobotAttenuationObservable.setSource(mRepository.updateRedRoBotAttenuation(attenuation))
    }

    // Update announcement
    fun updateAnnouncement(notice: String) {
        _roomNoticeObservable.setSource(mRepository.updateAnnouncement(notice))
    }

    // Mute mic
    fun muteLocal(mute: Boolean) {
        _muteMicObservable.setSource(mRepository.muteLocal(mute))
    }

    // Leave mic
    fun leaveMic(micIndex: Int) {
        _leaveMicObservable.setSource(mRepository.leaveMic(micIndex))
    }

    // Forbid mic
    fun forbidMic(micIndex: Int) {
        _forbidMicObservable.setSource(mRepository.forbidMic(micIndex))
    }

    // Cancel forbid mic
    fun cancelMuteMic(micIndex: Int) {
        _cancelForbidMicObservable.setSource(mRepository.unForbidMic(micIndex))
    }

    // Kick user off mic
    fun kickOff(micIndex: Int) {
        _kickMicObservable.setSource(mRepository.kickOff(micIndex))
    }

    // Accept invitation
    fun acceptMicSeatInvitation() {
        _acceptMicSeatInvitationObservable.setSource(mRepository.acceptMicSeatInvitation())
    }

    // User reject mic invitation
    fun refuseInvite() {
        _rejectMicInvitationObservable.setSource(mRepository.refuseInvite())
    }

    // Lock mic
    fun lockMic(micIndex: Int) {
        _lockMicObservable.setSource(mRepository.lockMic(micIndex))
    }

    // Cancel lock mic
    fun unLockMic(micIndex: Int) {
        _cancelLockMicObservable.setSource(mRepository.unLockMic(micIndex))
    }

    // Apply for mic seat
    fun startMicSeatApply(micIndex: Int?) {
        _startMicSeatApplyObservable.setSource(mRepository.startMicSeatApply(micIndex))
    }

    // Cancel apply
    fun cancelMicSeatApply(userId: String) {
        _cancelMicSeatApplyObservable.setSource(mRepository.cancelMicSeatApply(userId))
    }

    // Change mic
    fun changeMic(oldIndex: Int, newIndex: Int) {
        _changeMicObservable.setSource(mRepository.changeMic(oldIndex, newIndex))
    }

    fun leaveSyncManagerRoom(roomId: String, isRoomOwnerLeave: Boolean) {
        _leaveSyncRoomObservable.setSource(mRepository.leaveSyncManagerRoom(roomId, isRoomOwnerLeave))
    }
}