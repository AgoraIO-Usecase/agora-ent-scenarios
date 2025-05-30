package io.agora.scene.voice.spatial.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.scene.voice.spatial.model.VoiceMemberModel
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.model.VoiceRankUserModel
import io.agora.scene.voice.spatial.net.Resource
import io.agora.scene.voice.spatial.net.SingleSourceLiveData
import io.agora.scene.voice.spatial.viewmodel.repositories.VoiceUserListRepository

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

    /**Apply list*/
    fun applicantsListObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _applicantsListObservable

    /**Invite list*/
    fun inviteListObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _inviteListObservable

    /**Rank list*/
    fun contributeListObservable(): LiveData<Resource<List<VoiceRankUserModel>>> = _contributeListObservable

    /**Invite user to mic*/
    fun startMicSeatInvitationObservable(): LiveData<Resource<Boolean>> = _startMicSeatInvitationObservable

    /**Accept mic seat apply*/
    fun acceptMicSeatApplyObservable(): LiveData<Resource<VoiceMicInfoModel>> = _acceptMicSeatApplyObservable

    /**Apply list*/
    fun fetchApplicantsList() {
        _applicantsListObservable.setSource(mRepository.fetchApplicantsList())
    }

    /**Invite list*/
    fun fetchInviteList() {
        _inviteListObservable.setSource(mRepository.fetchInvitedList())
    }

    /**Invite user to mic*/
    fun startMicSeatInvitation(userId: String, micIndex: Int?) {
        _startMicSeatInvitationObservable.setSource(mRepository.startMicSeatInvitation(userId, micIndex))
    }

    /**Accept mic seat apply*/
    fun acceptMicSeatApply(userId: String) {
        _acceptMicSeatApplyObservable.setSource(mRepository.acceptMicSeatApply(userId))
    }
}