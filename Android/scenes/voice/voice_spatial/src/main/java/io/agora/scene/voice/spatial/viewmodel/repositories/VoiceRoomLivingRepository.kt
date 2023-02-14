package io.agora.scene.voice.spatial.viewmodel.repositories

import androidx.lifecycle.LiveData
import io.agora.scene.voice.spatial.model.RobotSpatialAudioModel
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.model.VoiceRoomInfo
import io.agora.scene.voice.spatial.model.VoiceRoomModel
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol
import io.agora.voice.common.net.Resource
import io.agora.voice.common.net.callback.ResultCallBack
import io.agora.voice.common.viewmodel.NetworkOnlyResource

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
     * 获取详情
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
     * 开启/关闭机器人
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

    fun updateBlueRoBotAttenuation(attenuation: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        voiceRobotInfo.blueRobotAttenuation = (attenuation / 100).toFloat()
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
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

    fun updateRedRoBotAttenuation(attenuation: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        voiceRobotInfo.redRobotAttenuation = (attenuation / 100).toFloat()
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
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
     * 更新房间公告
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
     * 更新机器人音量
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
     * 提交上麦申请
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
     * 撤销上麦申请
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
     * 本地禁麦
     */
    fun muteLocal(micIndex: Int): LiveData<Resource<VoiceMicInfoModel>> {
        val resource = object : NetworkOnlyResource<VoiceMicInfoModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMicInfoModel>>) {
                voiceServiceProtocol.muteLocal(micIndex, completion = { error, result ->
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
     * 取消本地禁麦
     */
    fun unMuteLocal(micIndex: Int): LiveData<Resource<VoiceMicInfoModel>> {
        val resource = object : NetworkOnlyResource<VoiceMicInfoModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMicInfoModel>>) {
                voiceServiceProtocol.unMuteLocal(micIndex, completion = { error, result ->
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

    // 下麦
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

    // 禁言指定麦位
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

    // 取消指定麦位禁言
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

    // 踢用户下麦
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

    // 用户拒绝上麦申请
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

    // 锁麦
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

    // 取消锁麦
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

    // 换麦
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

    // 接受邀请
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
     * 离开syncManager 房间
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