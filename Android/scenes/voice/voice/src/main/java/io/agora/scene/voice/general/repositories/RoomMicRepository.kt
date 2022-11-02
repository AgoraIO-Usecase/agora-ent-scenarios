package io.agora.scene.voice.general.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import io.agora.voice.baseui.general.callback.ResultCallBack
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.network.tools.VRValueCallBack
import io.agora.voice.network.tools.bean.VRMicBean
import io.agora.voice.network.tools.bean.VRMicListBean

/**
 * @author create by zhangwei03
 */
class RoomMicRepository : io.agora.scene.voice.general.repositories.BaseRepository() {

    // 获取上麦申请列表
    fun getApplyMicList(
        context: Context,
        roomId: String,
        cursor: String,
        limit: Int
    ): LiveData<Resource<VRMicListBean>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<VRMicListBean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VRMicListBean>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context)
                    .getApplyMicList(roomId, limit, cursor, object : VRValueCallBack<VRMicListBean> {
                        override fun onSuccess(data: VRMicListBean) {
                            callBack.onSuccess(createLiveData(data))
                        }

                        override fun onError(code: Int, desc: String) {
                            callBack.onError(code, desc)
                        }
                    })
            }
        }
        return resource.asLiveData()
    }

    // 撤销上麦申请
    fun cancelSubmitMic(context: Context, roomId: String): LiveData<Resource<Boolean>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).cancelSubmitMic(roomId, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(data))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 获取麦位信息
    fun getMicInfo(context: Context, roomId: String): LiveData<Resource<VRMicBean>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<VRMicBean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VRMicBean>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).getMicInfo(roomId, object : VRValueCallBack<VRMicBean> {
                    override fun onSuccess(data: VRMicBean) {
                        callBack.onSuccess(createLiveData(data))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 关麦
    fun closeMic(context: Context, roomId: String, micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).closeMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(Pair(micIndex, data)))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 取消关麦
    fun cancelCloseMic(context: Context, roomId: String, micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).cancelCloseMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(Pair(micIndex, data)))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 下麦
    fun leaveMicMic(context: Context, roomId: String, micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).leaveMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(Pair(micIndex, data)))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 禁言指定麦位
    fun muteMic(context: Context, roomId: String, micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).muteMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(Pair(micIndex, data)))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 取消指定麦位禁言
    fun cancelMuteMic(context: Context, roomId: String, micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).cancelMuteMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(Pair(micIndex, data)))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 踢用户下麦
    fun kickMic(
        context: Context,
        roomId: String,
        userId: String,
        micIndex: Int
    ): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).kickMic(roomId, userId, micIndex, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(Pair(micIndex, data)))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 用户拒绝上麦申请
    fun rejectMicInvitation(context: Context, roomId: String): LiveData<Resource<Boolean>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).rejectMicInvitation(roomId, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(data))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 锁麦
    fun lockMic(context: Context, roomId: String, micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).lockMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(Pair(micIndex, data)))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 取消锁麦
    fun cancelLockMic(context: Context, roomId: String, micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).cancelLockMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(Pair(micIndex, data)))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 邀请上麦
    fun invitationMic(context: Context, roomId: String, uid: String): LiveData<Resource<Boolean>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).invitationMic(roomId, uid, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(data))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 同意上麦申请
    fun applySubmitMic(context: Context, roomId: String, uid: String, micIndex: Int): LiveData<Resource<Boolean>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).applySubmitMic(roomId, uid, micIndex, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(data))
                    }

                    override fun onError(code: Int, desc: String) {
                        callBack.onError(code, desc)
                    }
                })
            }
        }
        return resource.asLiveData()
    }

    // 拒绝上麦申请
    fun rejectSubmitMic(context: Context, roomId: String, uid: String): LiveData<Resource<Boolean>> {
        val resource = object : io.agora.scene.voice.general.repositories.NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(context).rejectSubmitMic(roomId, uid, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean) {
                        callBack.onSuccess(createLiveData(data))
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