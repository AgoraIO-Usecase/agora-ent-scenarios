package io.agora.rtmsyncmanager.service.imp

import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.service.IAUIUserService
import io.agora.rtmsyncmanager.service.callback.AUICallback
import io.agora.rtmsyncmanager.service.callback.AUIException
import io.agora.rtmsyncmanager.service.callback.AUIUserListCallback
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserRespObserver
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper

class AUIUserServiceImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) : IAUIUserService, AUIRtmUserRespObserver {

    private val TAG = "AUiUserServiceImpl"

    private var mUserList = mutableListOf<AUIUserInfo>()

    private val observableHelper =
        ObservableHelper<IAUIUserService.AUIUserRespObserver>()

    init {
        rtmManager.subscribeUser(this)
    }

    fun release() {
        observableHelper.unSubscribeAll()
        rtmManager.unsubscribeUser(this)
    }

    override fun registerRespObserver(observer: IAUIUserService.AUIUserRespObserver?) {
        observableHelper.subscribeEvent(observer)
        if(mUserList.isNotEmpty()){
            this.observableHelper.notifyEventHandlers {
                it.onRoomUserSnapshot(channelName, mUserList)
            }
        }
    }

    override fun unRegisterRespObserver(observer: IAUIUserService.AUIUserRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun getUserInfoList(
        roomId: String,
        callback: AUIUserListCallback?
    ) {
        rtmManager.whoNow(roomId) { error, userList ->
            if (error != null) {
                callback?.onResult(
                    AUIException(
                        error.code,
                        error.message
                    ), null)
                return@whoNow
            }
            if (userList == null) {
                callback?.onResult(
                    AUIException(
                        -1,
                        ""
                    ), null)
                return@whoNow
            }
            val users = mutableListOf<AUIUserInfo>()
            userList.forEach { userMap ->
                GsonTools.toBean(GsonTools.beanToString(userMap), AUIUserInfo::class.java)?.let {
                    users.add(it)
                }
            }
            callback?.onResult(null, users)
        }
    }

    override fun muteUserAudio(isMute: Boolean, callback: AUICallback?) {
        val currentUserId = roomContext.currentUserInfo.userId
        val user = mUserList.first { it.userId == currentUserId }
        user.muteAudio = isMute
        val map = GsonTools.beanToMap(user)
        rtmManager.setPresenceState(channelName, attr = map.mapValues { it.value.toString() }) { error ->
            if (error != null) {
                callback?.onResult(
                    AUIException(
                        error.code,
                        error.message
                    )
                )
            } else {
                this.observableHelper.notifyEventHandlers {
                    it.onUserAudioMute(currentUserId, isMute)
                }
                callback?.onResult(null)
            }
        }
    }

    override fun muteUserVideo(isMute: Boolean, callback: AUICallback?) {
        val currentUserId = roomContext.currentUserInfo.userId
        val user = mUserList.firstOrNull { it.userId == currentUserId }
        if (user == null) {
            callback?.onResult(
                AUIException(
                    -1,
                    "can't find current user from users"
                )
            )
            return
        }
        user.muteVideo = isMute
        val map = GsonTools.beanToMap(user) as Map<String, String>
        rtmManager.setPresenceState(channelName, attr = map) { error ->
            if (error != null) {
                callback?.onResult(
                    AUIException(
                        error.code,
                        error.message
                    )
                )
            } else {
                callback?.onResult(null)
            }
        }
    }

    override fun getUserInfo(userId: String): AUIUserInfo? {
        return mUserList.firstOrNull { it.userId == userId }
    }

    override fun getRoomContext(): AUIRoomContext { return AUIRoomContext.shared() }

    override fun getChannelName() = channelName

    /** AUiRtmUserProxyDelegate */
    override fun onUserSnapshotRecv(
        channelName: String,
        userId: String,
        userList: List<Map<String, Any>>
    ) {
        if (this.channelName != channelName) return
        val users = mutableListOf<AUIUserInfo>()
        userList.forEach { userMap ->
            GsonTools.toBean(GsonTools.beanToString(userMap), AUIUserInfo::class.java)?.let {
                users.add(it)
            }
        }
        mUserList = users
        this.observableHelper.notifyEventHandlers {
            it.onRoomUserSnapshot(channelName, mUserList)
        }
    }

    override fun onUserDidJoined(
        channelName: String,
        userId: String,
        userInfo: Map<String, Any>
    ) {
        if (this.channelName != channelName) return
        if (userInfo.size <= 1) return
        GsonTools.toBean(GsonTools.beanToString(userInfo), AUIUserInfo::class.java)?.let { info ->
            info.userId = userId
            mUserList.add(info)
            this.observableHelper.notifyEventHandlers {
                it.onRoomUserEnter(channelName, info)
            }
        }
    }

    override fun onUserDidLeaved(
        channelName: String,
        userId: String,
        userInfo: Map<String, Any>,
        reason: AUIRtmUserLeaveReason
    ) {
        if (this.channelName != channelName) return
        val index = mUserList.indexOfFirst{ it.userId == userId }
        val info = mUserList.removeAt(index)
        this.observableHelper.notifyEventHandlers {
            it.onRoomUserLeave(channelName, info, reason)
        }
    }

    override fun onUserDidUpdated(
        channelName: String,
        userId: String,
        userInfo: Map<String, Any>
    ) {
        if (this.channelName != channelName) return
        if (userInfo.isEmpty()) {
            return
        }
        GsonTools.toBean(GsonTools.beanToString(userInfo), AUIUserInfo::class.java)?.let { info ->
            val index = mUserList.indexOfFirst{ it.userId == info.userId }
            if (index == -1) { // 不存在该用户
                mUserList.add(info)
                this.observableHelper.notifyEventHandlers {
                    it.onRoomUserEnter(channelName, info)
                }
                return
            } else {
                val oldInfo = mUserList[index]
                mUserList[index] = info
                // 单独更新语音被禁用回调
                if (oldInfo.muteAudio != info.muteAudio) {
                    this.observableHelper.notifyEventHandlers {
                        it.onUserAudioMute(info.userId, info.muteAudio)
                    }
                }
            }
            this.observableHelper.notifyEventHandlers {
                it.onRoomUserUpdate(channelName, info)
            }
        }
    }

    fun setUserPayload(payload: String) {
        val roomId = channelName
        val userId = AUIRoomContext.shared().currentUserInfo.userId
        AUILogger.logger().d(TAG, "setPayload[$channelName] : $payload")
        val userAttr = mapOf("customPayload" to payload)
        rtmManager.setPresenceState(channelName = roomId, attr = userAttr) { error ->
            if (error != null) {
                AUILogger.logger().d(TAG, "setPayload[$roomId] fail: ${error.localizedMessage}")
                //TODO: retry
                return@setPresenceState
            }

            // rtm不会返回自己更新的数据，需要手动处理
            onUserDidUpdated(channelName = roomId, userId = userId, userInfo = userAttr)
        }
    }


    fun setUserAttr(completion: (AUIRtmException?) -> Unit) {
        val roomId = channelName
        val userId = AUIRoomContext.shared().currentUserInfo.userId
        val userInfo = mUserList.firstOrNull { it.userId == userId } ?: AUIUserInfo()
        userInfo.userId = AUIRoomContext.shared().currentUserInfo.userId
        userInfo.userName = AUIRoomContext.shared().currentUserInfo.userName
        userInfo.userAvatar = AUIRoomContext.shared().currentUserInfo.userAvatar

        val userAttr = GsonTools.beanToMap(userInfo)
        AUILogger.logger().d(TAG, "setupUserAttr: $roomId : $userAttr")
        rtmManager.setPresenceState(roomId, attr = userAttr) { error ->
            if (error != null) {
                AUILogger.logger().d(TAG, "setupUserAttr: $roomId fail: ${error.reason}")
            } else {
                //rtm不会返回自己更新的数据，需要手动处理
                onUserDidUpdated(roomId, userId, userAttr)
            }
        }
    }
}