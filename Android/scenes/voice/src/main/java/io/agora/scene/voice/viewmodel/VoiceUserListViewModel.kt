package io.agora.scene.voice.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.scene.voice.viewmodel.repositories.VoiceUserListRepository
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.model.VoiceRankUserModel
import io.agora.voice.common.net.Resource
import io.agora.voice.common.viewmodel.SingleSourceLiveData

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

    /**申请列表*/
    fun applicantsListObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _applicantsListObservable

    /**邀请列表*/
    fun inviteListObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _inviteListObservable

    /**成员列表*/
    fun memberListObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _roomMemberObservable

    /** 榜单列表 */
    fun contributeListObservable(): LiveData<Resource<List<VoiceRankUserModel>>> = _contributeListObservable

    /**邀请用户上麦*/
    fun startMicSeatInvitationObservable(): LiveData<Resource<Boolean>> = _startMicSeatInvitationObservable

    /**同意上麦申请*/
    fun acceptMicSeatApplyObservable(): LiveData<Resource<VoiceMicInfoModel>> = _acceptMicSeatApplyObservable

    /**踢出用户*/
    fun kickOffObservable():LiveData<Resource<Int>> = _kickMemberObservable

    /** 申请列表*/
    fun fetchApplicantsList() {
        _applicantsListObservable.setSource(mRepository.fetchApplicantsList())
    }

    /**邀请列表*/
    fun fetchInviteList() {
        _inviteListObservable.setSource(mRepository.fetchInvitedList())
    }

    /**贡献排行榜*/
    fun fetchGiftContribute() {
        _contributeListObservable.setSource(mRepository.fetchGiftContribute())
    }

    /**成员列表*/
    fun fetchMemberList(){
        _roomMemberObservable.setSource(mRepository.fetchRoomMembers())
    }

    // 邀请用户上麦
    fun startMicSeatInvitation(chatUid: String, micIndex: Int?) {
        _startMicSeatInvitationObservable.setSource(mRepository.startMicSeatInvitation(chatUid, micIndex))
    }

    // 同意上麦申请
    fun acceptMicSeatApply(micIndex: Int?,chatUid: String) {
        _acceptMicSeatApplyObservable.setSource(mRepository.acceptMicSeatApply(micIndex,chatUid))
    }

    // 将成员踢出房间
    fun kickMembersOutOfTheRoom(chatUid:String,index:Int){
        val userList = mutableListOf<String>()
        userList.add(chatUid)
        _kickMemberObservable.setSource(mRepository.kickRoomMember(userList,index))
    }
}