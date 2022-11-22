package io.agora.scene.voice.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.scene.voice.general.livedatas.SingleSourceLiveData
import io.agora.scene.voice.general.repositories.VoiceUserListRepository
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.network.tools.bean.VRGiftBean
import io.agora.voice.network.tools.bean.VRMicListBean
import io.agora.voice.network.tools.bean.VRoomUserBean

/**
 * @author create by zhangwei03
 */
class VoiceUserListViewModel : ViewModel() {

    private val mRepository: VoiceUserListRepository by lazy { VoiceUserListRepository() }
    private val _inviteObservable: SingleSourceLiveData<Resource<VRoomUserBean>> = SingleSourceLiveData()
    private val _raisedObservable: SingleSourceLiveData<Resource<VRMicListBean>> = SingleSourceLiveData()
    private val _giftsObservable: SingleSourceLiveData<Resource<VRGiftBean>> = SingleSourceLiveData()
    private val _membersObservable: SingleSourceLiveData<Resource<VRoomUserBean>> = SingleSourceLiveData()

    /**邀请列表*/
    fun getInviteObservable(): LiveData<Resource<VRoomUserBean>>  = _inviteObservable
    /**举手列表*/
    fun getRaisedObservable(): LiveData<Resource<VRMicListBean>>  = _raisedObservable
    /** 获取榜单*/
    fun giftsObservable(): LiveData<Resource<VRGiftBean>> = _giftsObservable
    /** 获取用户列表*/
    fun membersObservable(): LiveData<Resource<VRoomUserBean>> = _membersObservable

    fun getInviteList(context: Context, roomId: String?, pageSize: Int, cursor: String?) {
        _inviteObservable.setSource(mRepository.getInvitedList(context, roomId, pageSize, cursor))
    }

    fun getRaisedList(context: Context, roomId: String?, pageSize: Int, cursor: String?) {
        _raisedObservable.setSource(mRepository.getRaisedList(context, roomId, pageSize, cursor))
    }

    fun getGifts(context: Context, roomId: String) {
        _giftsObservable.setSource(mRepository.getGifts(context, roomId))
    }

    fun getMembers(context: Context, roomId: String, pageSize: Int, cursor: String) {
        _membersObservable.setSource(mRepository.getInvitedList(context, roomId, pageSize, cursor))
    }
}