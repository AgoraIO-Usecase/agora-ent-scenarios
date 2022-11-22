package io.agora.scene.voice.general.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import io.agora.scene.voice.general.net.ChatroomHttpManager
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.voice.baseui.general.callback.ResultCallBack
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.imkit.manager.ChatroomIMManager
import io.agora.voice.network.tools.VRValueCallBack
import io.agora.voice.network.tools.bean.VRMicBean
import io.agora.voice.network.tools.bean.VRMicListBean
import io.agora.voice.network.tools.bean.VRoomInfoBean

/**
 * @author create by zhangwei03
 */
class VoiceRoomLivingRepository : BaseRepository() {

    /**
     * voice chat protocol
     */
    private val voiceServiceProtocol = VoiceServiceProtocol.getImplInstance()

    fun getRoomInfo(context: Context?, roomId: String?): LiveData<Resource<VRoomInfoBean>> {
        val resource = object : NetworkOnlyResource<VRoomInfoBean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VRoomInfoBean>>) {
                ChatroomHttpManager.getInstance(context)
                    .getRoomDetails(roomId, object : VRValueCallBack<VRoomInfoBean> {
                        override fun onSuccess(data: VRoomInfoBean?) {
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

    fun joinRoom(context: Context?, roomId: String?, password: String?): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                ChatroomHttpManager.getInstance(context).joinRoom(roomId, password, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean?) {
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

    fun leaveRoom(context: Context, roomId: String?): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                ChatroomHttpManager.getInstance(context).leaveRoom(roomId, object : VRValueCallBack<Boolean> {
                    override fun onSuccess(data: Boolean?) {
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

    fun updateRoomInfo(
        context: Context, roomId: String?, name: String?, announcement: String?, isPrivate: Boolean?,
        password: String?, useRobot: Boolean?, allowedFreeJoinMic: Boolean?, robotVolume: Int?
    ): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                ChatroomHttpManager.getInstance(context).updateRoomInfo(roomId, name, announcement, isPrivate,
                    password, useRobot, allowedFreeJoinMic, robotVolume, object : VRValueCallBack<Boolean?> {
                        override fun onSuccess(var1: Boolean?) {
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

    fun activeBot(context: Context?, roomId: String?, useRobot: Boolean?): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                ChatroomHttpManager.getInstance(context).updateRoomInfo(roomId, null, null, null,
                    null, useRobot, null, null, object : VRValueCallBack<Boolean?> {
                        override fun onSuccess(var1: Boolean?) {
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

    fun changeRobotVolume(
        context: Context, roomId: String?, robotVolume: Int
    ): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                ChatroomHttpManager.getInstance(context).updateRoomInfo(roomId, null, null, null,
                    null, null, null, robotVolume, object : VRValueCallBack<Boolean> {
                        override fun onSuccess(var1: Boolean) {
                            callBack.onSuccess(createLiveData(Pair(robotVolume, var1)))
                        }

                        override fun onError(code: Int, desc: String) {
                            callBack.onError(code, desc)
                        }
                    })
            }
        }
        return resource.asLiveData()
    }

    fun updateRoomNotice(context: Context, roomId: String?, notice: String?): LiveData<Resource<Boolean>> {
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                ChatroomHttpManager.getInstance(context).updateRoomInfo(roomId, null, notice, null,
                    null, null, null, null, object : VRValueCallBack<Boolean?> {
                        override fun onSuccess(var1: Boolean?) {
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

    // 获取上麦申请列表
    fun getApplyMicList(
        context: Context,
        roomId: String,
        cursor: String,
        limit: Int
    ): LiveData<Resource<VRMicListBean>> {
        val resource = object : NetworkOnlyResource<VRMicListBean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VRMicListBean>>) {
                ChatroomHttpManager.getInstance(context)
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
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                ChatroomHttpManager.getInstance(context)
                    .cancelSubmitMic(roomId, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<VRMicBean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<VRMicBean>>) {
//                ChatroomHttpManager.getInstance(context)
//                    .getMicInfo(roomId, object : VRValueCallBack<VRMicBean> {
//                        override fun onSuccess(data: VRMicBean) {
//                            callBack.onSuccess(createLiveData(data))
//                        }
//
//                        override fun onError(code: Int, desc: String) {
//                            callBack.onError(code, desc)
//                        }
//                    })
                ChatroomIMManager.getInstance()
            }
        }
        return resource.asLiveData()
    }

    // 关麦
    fun closeMic(context: Context, roomId: String, micIndex: Int): LiveData<Resource<Pair<Int, Boolean>>> {
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                ChatroomHttpManager.getInstance(context)
                    .closeMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                ChatroomHttpManager.getInstance(context)
                    .cancelCloseMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                ChatroomHttpManager.getInstance(context)
                    .leaveMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                ChatroomHttpManager.getInstance(context)
                    .muteMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                ChatroomHttpManager.getInstance(context)
                    .cancelMuteMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                ChatroomHttpManager.getInstance(context)
                    .kickMic(roomId, userId, micIndex, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                ChatroomHttpManager.getInstance(context)
                    .rejectMicInvitation(roomId, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                ChatroomHttpManager.getInstance(context)
                    .lockMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Pair<Int, Boolean>>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Pair<Int, Boolean>>>) {
                ChatroomHttpManager.getInstance(context)
                    .cancelLockMic(roomId, micIndex, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                ChatroomHttpManager.getInstance(context)
                    .invitationMic(roomId, uid, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                ChatroomHttpManager.getInstance(context)
                    .applySubmitMic(roomId, uid, micIndex, object : VRValueCallBack<Boolean> {
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
        val resource = object : NetworkOnlyResource<Boolean>() {
            override fun createCall(callBack: ResultCallBack<LiveData<Boolean>>) {
                ChatroomHttpManager.getInstance(context)
                    .rejectSubmitMic(roomId, uid, object : VRValueCallBack<Boolean> {
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