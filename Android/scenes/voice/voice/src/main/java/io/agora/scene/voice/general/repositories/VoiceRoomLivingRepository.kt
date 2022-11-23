package io.agora.scene.voice.general.repositories

import androidx.lifecycle.LiveData
import io.agora.scene.voice.service.*
import io.agora.voice.baseui.general.callback.ResultCallBack
import io.agora.voice.baseui.general.net.Resource

/**
 * @author create by zhangwei03
 */
class VoiceRoomLivingRepository : BaseRepository() {

    /**
     * voice chat protocol
     */
    private val voiceServiceProtocol = VoiceServiceProtocol.getImplInstance()

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
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.enableRobot(useRobot, completion = { error, result ->
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
     * 更新房间公告
     */
    fun updateAnnouncement(content: String): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.updateAnnouncement(content, completion = { error, result ->
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
     * 更新机器人音量
     */
    fun updateRobotVolume(value: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.updateRobotVolume(value, completion = { error, result ->
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
    fun cancelMicSeatApply(chatUid: String): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.cancelMicSeatApply(chatUid, completion = { error, result ->
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
    fun muteLocal(micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.muteLocal(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(Pair(micIndex, result)))
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
    fun unMuteLocal(micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.unMuteLocal(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(Pair(micIndex, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 下麦
    fun leaveMic(micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.leaveMic(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(Pair(micIndex, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 禁言指定麦位
    fun forbidMic(micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.forbidMic(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(Pair(micIndex, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 取消指定麦位禁言
    fun unForbidMic(micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.unForbidMic(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(Pair(micIndex, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 踢用户下麦
    fun kickOff(micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.kickOff(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(Pair(micIndex, result)))
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
    fun lockMic(micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.lockMic(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(Pair(micIndex, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 取消锁麦
    fun unLockMic(micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.unLockMic(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(Pair(micIndex, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 邀请上麦
    fun startMicSeatInvitation(chatUid: String, micIndex: Int?): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.startMicSeatInvitation(chatUid, micIndex, completion = { error, result ->
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

    // 同意上麦申请
    fun acceptMicSeatApply(chatUid: String): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.acceptMicSeatApply(chatUid, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 换麦
    fun changeMic(oldIndex: Int,newIndex:Int): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.changeMic(oldIndex,newIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 接受邀请
    fun acceptMicSeatInvitation(): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                voiceServiceProtocol.acceptMicSeatInvitation( completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
//                        callBack.onSuccess(createLiveData(result))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 获取礼物榜单
    fun fetchGiftContribute(): LiveData<Resource<List<VoiceRankUserModel>>> {
        val resource = object : NetworkOnlyResource<List<VoiceRankUserModel>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<List<VoiceRankUserModel>>>) {
                voiceServiceProtocol.fetchGiftContribute( completion = { error, result ->
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

    // 获取用户列表
    fun fetchRoomMembers(): LiveData<Resource<List<VoiceMemberModel>>> {
        val resource = object : NetworkOnlyResource<List<VoiceMemberModel>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<List<VoiceMemberModel>>>) {
                voiceServiceProtocol.fetchRoomMembers( completion = { error, result ->
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