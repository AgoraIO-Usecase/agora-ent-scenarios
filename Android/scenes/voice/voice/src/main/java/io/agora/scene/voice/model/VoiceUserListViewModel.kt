package io.agora.scene.voice.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.scene.voice.general.livedatas.SingleSourceLiveData
import io.agora.scene.voice.general.repositories.VoiceUserListRepository
import io.agora.scene.voice.service.VoiceMemberModel
import io.agora.scene.voice.service.VoiceRankUserModel
import io.agora.voice.baseui.general.net.Resource

/**
 * @author create by zhangwei03
 */
class VoiceUserListViewModel : ViewModel() {

    private val mRepository: VoiceUserListRepository by lazy { VoiceUserListRepository() }
    private val _inviteObservable: SingleSourceLiveData<Resource<List<VoiceMemberModel>>> = SingleSourceLiveData()
    private val _raisedObservable: SingleSourceLiveData<Resource<List<VoiceMemberModel>>> = SingleSourceLiveData()
    private val _giftsObservable: SingleSourceLiveData<Resource<List<VoiceRankUserModel>>> = SingleSourceLiveData()
    private val _membersObservable: SingleSourceLiveData<Resource<List<VoiceMemberModel>>> = SingleSourceLiveData()
    private val _kickOffObservable: SingleSourceLiveData<Resource<Pair<Int, Boolean>>> = SingleSourceLiveData()
    private val _startMicSeatInvitationObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()
    private val _acceptMicSeatApplyObservable: SingleSourceLiveData<Resource<Boolean>> = SingleSourceLiveData()

    /**邀请列表*/
    fun getInviteObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _inviteObservable

    /**举手列表*/
    fun getRaisedObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _raisedObservable

    /** 获取榜单*/
    fun giftsObservable(): LiveData<Resource<List<VoiceRankUserModel>>> = _giftsObservable

    /** 获取用户列表*/
    fun membersObservable(): LiveData<Resource<List<VoiceMemberModel>>> = _membersObservable

    /** 踢用户下麦*/
    fun kickOffObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _kickOffObservable

    /**邀请用户上麦*/
    fun startMicSeatInvitationObservable(): LiveData<Resource<Boolean>> = _startMicSeatInvitationObservable

    /**同意上麦申请*/
    fun acceptMicSeatApplyObservable(): LiveData<Resource<Boolean>> = _acceptMicSeatApplyObservable

    /**邀请列表*/
    fun getInviteList() {
        _inviteObservable.setSource(mRepository.getInvitedList())
    }

    /** 举手列表*/
    fun getRaisedList() {
        _raisedObservable.setSource(mRepository.getRaisedList())
    }

    /**贡献排行榜*/
    fun fetchGiftContribute() {
        _giftsObservable.setSource(mRepository.fetchGiftContribute())
    }

    /**获取用户列表*/
    fun fetchRoomMembers() {
        _membersObservable.setSource(mRepository.fetchRoomMembers())
    }

    // 踢用户下麦
    fun kickOff(micIndex: Int) {
        _kickOffObservable.setSource(mRepository.kickOff(micIndex))
    }

    // 邀请用户上麦
    fun startMicSeatInvitation(chatUid: String, micIndex: Int?) {
        _startMicSeatInvitationObservable.setSource(mRepository.startMicSeatInvitation(chatUid, micIndex))
    }

    // 同意上麦申请
    fun acceptMicSeatApply(chatUid: String) {
        _acceptMicSeatApplyObservable.setSource(mRepository.acceptMicSeatApply(chatUid))
    }
}