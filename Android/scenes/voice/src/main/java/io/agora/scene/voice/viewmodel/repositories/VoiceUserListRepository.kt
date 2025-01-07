package io.agora.scene.voice.viewmodel.repositories

import androidx.lifecycle.LiveData
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.model.VoiceRankUserModel
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.scene.voice.netkit.callback.ResultCallBack
import io.agora.scene.voice.netkit.Resource
import io.agora.scene.voice.viewmodel.NetworkOnlyResource

class VoiceUserListRepository : BaseRepository() {

    /**
     * voice chat protocol
     */
    private val voiceServiceProtocol by lazy {
        VoiceServiceProtocol.serviceProtocol
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

    // Invite user to take mic
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

    // Accept mic seat application
    fun acceptMicSeatApply(micIndex: Int?,chatUid: String): LiveData<Resource<VoiceMicInfoModel>> {
        val resource = object : NetworkOnlyResource<VoiceMicInfoModel>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VoiceMicInfoModel>>) {
                voiceServiceProtocol.acceptMicSeatApply(micIndex, chatUid, completion = { error, result ->
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
     * Raised hands list
     */
    fun fetchApplicantsList(): LiveData<Resource<List<VoiceMemberModel>>> {
        val resource = object : NetworkOnlyResource<List<VoiceMemberModel>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<List<VoiceMemberModel>>>) {
                voiceServiceProtocol.fetchApplicantsList(completion = { error, result ->
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
     * Invitation list
     */
    fun fetchInvitedList(): LiveData<Resource<List<VoiceMemberModel>>> {
        val resource = object : NetworkOnlyResource<List<VoiceMemberModel>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<List<VoiceMemberModel>>>) {
                voiceServiceProtocol.fetchRoomInvitedMembers(completion = { error, result ->
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

    // Get gift ranking list
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

    // Get user list
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

    // Kick out user
    fun kickRoomMember(chatUidList: MutableList<String>,index:Int): LiveData<Resource<Int>> {
        val resource = object : NetworkOnlyResource<Int>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Int>>) {
                voiceServiceProtocol.kickMemberOutOfRoom(chatUidList,completion = { error, result ->
                    if (error == VoiceServiceProtocol.ERR_OK) {
                        callBack.onSuccess(createLiveData(index))
                    } else {
                        callBack.onError(error)
                    }
                })
            }
        }
        return resource.asLiveData()
    }
}