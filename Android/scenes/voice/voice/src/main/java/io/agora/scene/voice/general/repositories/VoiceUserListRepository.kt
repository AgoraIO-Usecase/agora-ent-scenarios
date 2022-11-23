package io.agora.scene.voice.general.repositories

import androidx.lifecycle.LiveData
import io.agora.scene.voice.service.VoiceMemberModel
import io.agora.scene.voice.service.VoiceRankUserModel
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.voice.baseui.general.callback.ResultCallBack
import io.agora.voice.baseui.general.net.Resource

class VoiceUserListRepository : BaseRepository() {

    /**
     * voice chat protocol
     */
    private val voiceServiceProtocol = VoiceServiceProtocol.getImplInstance()

    // 踢用户下麦
    fun kickOff(micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                voiceServiceProtocol.kickOff(micIndex, completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(Pair(micIndex, result)))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 邀请用户上麦
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
     * 举手列表
     */
    fun getRaisedList(): LiveData<Resource<List<VoiceMemberModel>>> {
        val resource = object : NetworkOnlyResource<List<VoiceMemberModel>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<List<VoiceMemberModel>>>) {
                // TODO:
            }
        }
        return resource.asLiveData()
    }

    /**
     * 邀请列表
     */
    fun getInvitedList(): LiveData<Resource<List<VoiceMemberModel>>> {
        val resource = object : NetworkOnlyResource<List<VoiceMemberModel>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<List<VoiceMemberModel>>>) {
                // TODO:
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