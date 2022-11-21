package io.agora.scene.voice.general.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import io.agora.voice.network.tools.bean.VRMicListBean
import io.agora.scene.voice.general.net.ChatroomHttpManager
import io.agora.voice.baseui.general.callback.ResultCallBack
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.network.tools.VRValueCallBack
import io.agora.voice.network.tools.bean.VRoomUserBean
import io.agora.voice.network.tools.bean.VRGiftBean

class VoiceUserListRepository : BaseRepository() {

    /**
     * 举手列表
     */
    fun getRaisedList(
        context: Context, roomId: String?, pageSize: Int, cursor: String?
    ): LiveData<Resource<VRMicListBean>> {
        val resource = object : NetworkOnlyResource<VRMicListBean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VRMicListBean>>) {
                ChatroomHttpManager.getInstance(context)
                    .getApplyMicList(roomId, pageSize, cursor, object : VRValueCallBack<VRMicListBean> {
                        override fun onSuccess(var1: VRMicListBean) {
                            callBack.onSuccess(createLiveData(var1))
                        }

                        override fun onError(code: Int, desc: String) {
                            callBack.onError(code, desc)
                        }
                    })
            }
        }
        return resource.asLiveData()
    }

    /**
     * 邀请列表
     */
    fun getInvitedList(
        context: Context, roomId: String?, pageSize: Int, cursor: String?
    ): LiveData<Resource<VRoomUserBean>> {
        val resource = object : NetworkOnlyResource<VRoomUserBean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VRoomUserBean>>) {
                ChatroomHttpManager.getInstance(context)
                    .getRoomMembers(roomId, pageSize, cursor, object : VRValueCallBack<VRoomUserBean> {
                        override fun onSuccess(var1: VRoomUserBean) {
                            callBack.onSuccess(createLiveData(var1))
                        }

                        override fun onError(code: Int, desc: String) {
                            callBack.onError(code, desc)
                        }
                    })
            }
        }
        return resource.asLiveData()
    }

    /**
     * 礼物榜单
     */
    fun getGifts(context: Context, roomId: String?): LiveData<Resource<VRGiftBean>> {
        val resource = object : NetworkOnlyResource<VRGiftBean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VRGiftBean>>) {
                ChatroomHttpManager.getInstance(context).getGiftList(roomId, object : VRValueCallBack<VRGiftBean> {
                    override fun onSuccess(var1: VRGiftBean) {
                        callBack.onSuccess(createLiveData(var1))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }
}