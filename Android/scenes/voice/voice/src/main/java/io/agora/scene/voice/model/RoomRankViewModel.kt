package io.agora.scene.voice.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.network.tools.bean.VRGiftBean
import io.agora.voice.network.tools.bean.VRoomUserBean

/**
 * @author create by zhangwei03
 */
class RoomRankViewModel constructor() : ViewModel() {

    private val mRepository by lazy { io.agora.scene.voice.general.repositories.ChatroomHandsRepository() }

    private val _giftsObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<VRGiftBean>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _membersObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<VRoomUserBean>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()

    fun giftsObservable(): LiveData<Resource<VRGiftBean>> = _giftsObservable
    fun membersObservable(): LiveData<Resource<VRoomUserBean>> = _membersObservable

    /**
     * 获取榜单
     */
    fun getGifts(context: Context, roomId: String) {
        _giftsObservable.setSource(mRepository.getGifts(context, roomId))
    }

    /**
     * 获取用户列表
     */
    fun getMembers(context: Context, roomId: String, pageSize: Int, cursor: String) {
        _membersObservable.setSource(mRepository.getInvitedList(context, roomId, pageSize, cursor))
    }
}