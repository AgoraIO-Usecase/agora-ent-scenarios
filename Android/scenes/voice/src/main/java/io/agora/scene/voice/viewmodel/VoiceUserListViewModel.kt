package io.agora.scene.voice.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.scene.voice.viewmodel.repositories.VoiceUserListRepository
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.model.VoiceRankUserModel
import io.agora.scene.voice.netkit.Resource

/**
 * @author create by zhangwei03
 */
class VoiceUserListViewModel : ViewModel() {

    private val mRepository: VoiceUserListRepository by lazy { VoiceUserListRepository() }
    private val _applicantsListObservable: SingleSourceLiveData<Resource<List<VoiceMemberModel>>> =
        SingleSourceLiveData()
    private val _inviteListObservable: SingleSourceLiveData<Resource<List<VoiceMemberModel>>> =
        SingleSourceLiveData()
    private val _contributeListObservable: SingleSourceLiveData<Resource<List<VoiceRankUserModel>>> =
        SingleSourceLiveData()
    private val _startMicSeatInvitationObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()
    private val _acceptMicSeatApplyObservable: SingleSourceLiveData<Resource<VoiceMicInfoModel>> =
        SingleSourceLiveData()
    private val _roomMemberObservable: SingleSourceLiveData<Resource<List<VoiceMemberModel>>> =
        SingleSourceLiveData()
    private val _kickMemberObservable: SingleSourceLiveData<Resource<Int>> =
        SingleSourceLiveData()

    /** Application list */
    fun applicantsListObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _applicantsListObservable

    /** Invitation list */
    fun inviteListObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _inviteListObservable

    /** Member list */
    fun memberListObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _roomMemberObservable

    /** Ranking list */
    fun contributeListObservable(): LiveData<Resource<List<VoiceRankUserModel>>> = _contributeListObservable

    /** Invite user to take mic */
    fun startMicSeatInvitationObservable(): LiveData<Resource<Boolean>> = _startMicSeatInvitationObservable

    /** Accept mic application */
    fun acceptMicSeatApplyObservable(): LiveData<Resource<VoiceMicInfoModel>> = _acceptMicSeatApplyObservable

    /** Kick out user */
    fun kickOffObservable():LiveData<Resource<Int>> = _kickMemberObservable

    /** Application list */
    fun fetchApplicantsList() {
        _applicantsListObservable.setSource(mRepository.fetchApplicantsList())
    }

    /** Invitation list */
    fun fetchInviteList() {
        _inviteListObservable.setSource(mRepository.fetchInvitedList())
    }

    /** Contribution ranking */
    fun fetchGiftContribute() {
        _contributeListObservable.setSource(mRepository.fetchGiftContribute())
    }

    /** Member list */
    fun fetchMemberList(){
        _roomMemberObservable.setSource(mRepository.fetchRoomMembers())
    }

    // Invite user to take mic
    fun startMicSeatInvitation(chatUid: String, micIndex: Int?) {
        _startMicSeatInvitationObservable.setSource(mRepository.startMicSeatInvitation(chatUid, micIndex))
    }

    // Accept mic application
    fun acceptMicSeatApply(micIndex: Int?,chatUid: String) {
        _acceptMicSeatApplyObservable.setSource(mRepository.acceptMicSeatApply(micIndex,chatUid))
    }

    // Kick members out of the room
    fun kickMembersOutOfTheRoom(chatUid:String,index:Int){
        val userList = mutableListOf<String>()
        userList.add(chatUid)
        _kickMemberObservable.setSource(mRepository.kickRoomMember(userList,index))
    }
}