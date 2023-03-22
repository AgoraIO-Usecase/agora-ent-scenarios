package io.agora.scene.voice.spatial.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.scene.voice.spatial.model.VoiceMemberModel
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.model.VoiceRankUserModel
import io.agora.scene.voice.spatial.viewmodel.repositories.VoiceUserListRepository
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

    /**申请列表*/
    fun applicantsListObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _applicantsListObservable

    /**邀请列表*/
    fun inviteListObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _inviteListObservable

    /** 榜单列表 */
    fun contributeListObservable(): LiveData<Resource<List<VoiceRankUserModel>>> = _contributeListObservable

    /**邀请用户上麦*/
    fun startMicSeatInvitationObservable(): LiveData<Resource<Boolean>> = _startMicSeatInvitationObservable

    /**同意上麦申请*/
    fun acceptMicSeatApplyObservable(): LiveData<Resource<VoiceMicInfoModel>> = _acceptMicSeatApplyObservable

    /** 申请列表*/
    fun fetchApplicantsList() {
        _applicantsListObservable.setSource(mRepository.fetchApplicantsList())
    }

    /**邀请列表*/
    fun fetchInviteList() {
        _inviteListObservable.setSource(mRepository.fetchInvitedList())
    }

    // 邀请用户上麦
    fun startMicSeatInvitation(userId: String, micIndex: Int?) {
        _startMicSeatInvitationObservable.setSource(mRepository.startMicSeatInvitation(userId, micIndex))
    }

    // 同意上麦申请
    fun acceptMicSeatApply(userId: String) {
        _acceptMicSeatApplyObservable.setSource(mRepository.acceptMicSeatApply(userId))
    }
}