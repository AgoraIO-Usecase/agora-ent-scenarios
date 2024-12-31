package io.agora.scene.voice.spatial.viewmodel.repositories

import androidx.lifecycle.LiveData
import io.agora.scene.voice.spatial.model.*
import io.agora.scene.voice.spatial.net.NetworkOnlyResource
import io.agora.scene.voice.spatial.net.Resource
import io.agora.scene.voice.spatial.net.callback.ResultCallBack
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol

/**
 * @author create by zhangwei03
 */
class VoiceRoomLivingRepository : BaseRepository() {

    /**
     * voice chat protocol
     */
    private val voiceServiceProtocol = VoiceServiceProtocol.getImplInstance()
    private val voiceRobotInfo: RobotSpatialAudioModel = RobotSpatialAudioModel()

    /**
     * Get room details
     */
    fun fetchRoomDetail(voiceRoomModel: VoiceRoomModel): LiveData<Resource<VoiceRoomInfo>> {
        val resource = object : NetworkOnlyResource<VoiceRoomInfo>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceRoomInfo>>) {
                voiceServiceProtocol.fetchRoomDetail(voiceRoomModel, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    /**
     * Enable/disable robot
     */
    fun enableRobot(useRobot: Boolean): LiveData<Resource<Boolean>> {
        voiceRobotInfo.useRobot = useRobot
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.updateRobotInfo(voiceRobotInfo, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    fun enableBlueRobotAirAbsorb(active: Boolean): LiveData<Resource<Boolean>> {
        voiceRobotInfo.blueRobotAbsorb = active
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.updateRobotInfo(voiceRobotInfo, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    fun enableRedRobotAirAbsorb(active: Boolean): LiveData<Resource<Boolean>> {
        voiceRobotInfo.redRobotAbsorb = active
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.updateRobotInfo(voiceRobotInfo, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    fun enableBlueRobotBlur(active: Boolean): LiveData<Resource<Boolean>> {
        voiceRobotInfo.blueRobotBlur = active
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.updateRobotInfo(voiceRobotInfo, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    fun enableRedRobotBlur(active: Boolean): LiveData<Resource<Boolean>> {
        voiceRobotInfo.redRobotBlur = active
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.updateRobotInfo(voiceRobotInfo, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    fun updateBlueRoBotAttenuation(attenuation: Double): LiveData<Resource<Pair<Double, Boolean>>> {
        voiceRobotInfo.blueRobotAttenuation = attenuation
        val resource = object : NetworkOnlyResource<Pair<Double, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Double, Boolean>>>) {
                voiceServiceProtocol.updateRobotInfo(voiceRobotInfo, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(Pair(attenuation, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    fun updateRedRoBotAttenuation(attenuation: Double): LiveData<Resource<Pair<Double, Boolean>>> {
        voiceRobotInfo.redRobotAttenuation = attenuation
        val resource = object : NetworkOnlyResource<Pair<Double, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Double, Boolean>>>) {
                voiceServiceProtocol.updateRobotInfo(voiceRobotInfo, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(Pair(attenuation, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    /**
     * Update room announcement
     */
    fun updateAnnouncement(content: String): LiveData<Resource<Pair<String, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<String, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<String, Boolean>>>) {
                voiceServiceProtocol.updateAnnouncement(content, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(Pair(content, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    /**
     * Update robot volume
     */
    fun updateRobotVolume(value: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        voiceRobotInfo.robotVolume = value
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.updateRobotInfo(voiceRobotInfo, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(Pair(value, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    /**
     * Submit mic seat application
     */
    fun startMicSeatApply(micIndex: Int?): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.startMicSeatApply(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    /**
     * Cancel mic seat application
     */
    fun cancelMicSeatApply(userId: String): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.cancelMicSeatApply(userId, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    /**
     * Mute local on / off
     */
    fun muteLocal(mute: Boolean): LiveData<Resource<VoiceMemberModel>> {
        val resource = object : NetworkOnlyResource<VoiceMemberModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMemberModel>>) {
                voiceServiceProtocol.muteLocal(mute) { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                }
            }
        }
        return resource.asLiveData()
    }

    // Leave mic
    fun leaveMic(micIndex: Int): LiveData<Resource<VoiceMicInfoModel>> {
        val resource = object : NetworkOnlyResource<VoiceMicInfoModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMicInfoModel>>) {
                voiceServiceProtocol.leaveMic(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // Mute mic
    fun forbidMic(micIndex: Int): LiveData<Resource<VoiceMicInfoModel>> {
        val resource = object : NetworkOnlyResource<VoiceMicInfoModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMicInfoModel>>) {
                voiceServiceProtocol.forbidMic(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // Unmute mic
    fun unForbidMic(micIndex: Int): LiveData<Resource<VoiceMicInfoModel>> {
        val resource = object : NetworkOnlyResource<VoiceMicInfoModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMicInfoModel>>) {
                voiceServiceProtocol.unForbidMic(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // Kick user off mic
    fun kickOff(micIndex: Int): LiveData<Resource<VoiceMicInfoModel>> {
        val resource = object : NetworkOnlyResource<VoiceMicInfoModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMicInfoModel>>) {
                voiceServiceProtocol.kickOff(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // User refuses mic seat application
    fun refuseInvite(): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.refuseInvite(completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // Lock mic
    fun lockMic(micIndex: Int): LiveData<Resource<VoiceMicInfoModel>> {
        val resource = object : NetworkOnlyResource<VoiceMicInfoModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMicInfoModel>>) {
                voiceServiceProtocol.lockMic(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // Unlock mic
    fun unLockMic(micIndex: Int): LiveData<Resource<VoiceMicInfoModel>> {
        val resource = object : NetworkOnlyResource<VoiceMicInfoModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMicInfoModel>>) {
                voiceServiceProtocol.unLockMic(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // Change mic
    fun changeMic(oldIndex: Int, newIndex: Int): LiveData<Resource<Map<Int, VoiceMicInfoModel>>> {
        val resource = object : NetworkOnlyResource<Map<Int, VoiceMicInfoModel>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Map<Int, VoiceMicInfoModel>>>) {
                voiceServiceProtocol.changeMic(oldIndex, newIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // Accept invitation
    fun acceptMicSeatInvitation(): LiveData<Resource<VoiceMicInfoModel>> {
        val resource = object : NetworkOnlyResource<VoiceMicInfoModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMicInfoModel>>) {
                voiceServiceProtocol.acceptMicSeatInvitation(completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    /**
     * Leave syncManager room
     */
    fun leaveSyncManagerRoom(roomId: String, isRoomOwnerLeave: Boolean): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.leaveRoom(roomId, isRoomOwnerLeave, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }
}