package io.agora.scene.show.service.rtmsync

import androidx.annotation.IntDef
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserRespObserver
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper
import io.agora.rtmsyncmanager.utils.ThreadManager

class RoomPresenceService(
    private val rtmManager: AUIRtmManager,
    private val channelName: String
) {

    private val observerHelper = ObservableHelper<RoomPresenceSubscriber>()

    private val roomPresenceInfoList = mutableListOf<RoomPresenceInfo>()

    private val userRespObserver = object : AUIRtmUserRespObserver {

        override fun onUserSnapshotRecv(
            channelName: String,
            userId: String,
            userList: List<Map<String, Any>>
        ) {
            if (channelName != this@RoomPresenceService.channelName) {
                return
            }
            userList.forEach {
                val info = GsonTools.toBeanSafely(it, RoomPresenceInfo::class.java) ?: return@forEach
                val index = roomPresenceInfoList.indexOfFirst { it.roomId == info.roomId }
                if (index == -1) {
                    roomPresenceInfoList.add(info)
                } else {
                    roomPresenceInfoList[index] = info
                }
            }
            observerHelper.notifyEventHandlers {
                it.onSnapshot?.invoke(ArrayList(roomPresenceInfoList))
            }
        }

        override fun onUserDidJoined(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>
        ) {
            if (channelName != this@RoomPresenceService.channelName) {
                return
            }
            val info = GsonTools.toBeanSafely(userInfo, RoomPresenceInfo::class.java) ?: return
            val index = roomPresenceInfoList.indexOfFirst { it.roomId == info.roomId }
            if (index == -1) {
                roomPresenceInfoList.add(info)
            } else {
                roomPresenceInfoList[index] = info
            }
            observerHelper.notifyEventHandlers {
                it.onUpdate?.invoke(info)
            }
        }

        override fun onUserDidLeaved(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>
        ) {
            if (channelName != this@RoomPresenceService.channelName) {
                return
            }
            val info = roomPresenceInfoList.findLast { it.ownerId == userId }
            if (info != null) {
                roomPresenceInfoList.remove(info)
                observerHelper.notifyEventHandlers {
                    it.onDelete?.invoke(info)
                }
            }
        }

        override fun onUserDidUpdated(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>
        ) {
            if (channelName != this@RoomPresenceService.channelName) {
                return
            }
            val info = GsonTools.toBeanSafely(userInfo, RoomPresenceInfo::class.java) ?: return
            val index = roomPresenceInfoList.indexOfFirst { it.roomId == info.roomId }
            if (index == -1) {
                roomPresenceInfoList.add(info)
            } else {
                roomPresenceInfoList[index] = info
            }
            observerHelper.notifyEventHandlers {
                it.onUpdate?.invoke(info)
            }
        }

    }

    fun login(complete: () -> Unit) {
        rtmManager.subscribeUser(userRespObserver)
        rtmManager.subscribe(channelName) { ex ->
            if (ex != null) {
                observerHelper.notifyEventHandlers {
                    it.onError?.invoke(RuntimeException(ex))
                }
            } else {
                complete.invoke()
            }
        }
    }

    fun logout() {
        observerHelper.unSubscribeAll()
        rtmManager.unSubscribe(channelName)
        rtmManager.unsubscribeUser(userRespObserver)
    }

    fun setup(
        info: RoomPresenceInfo,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ) {
        setRoomPresenceInfo(info, success, error)
    }

    fun subscribe(subscriber: RoomPresenceSubscriber) {
        observerHelper.subscribeEvent(subscriber)
    }

    fun unSubscribe(subscriber: RoomPresenceSubscriber) {
        observerHelper.unSubscribeEvent(subscriber)
    }

    private fun setRoomPresenceInfo(
        info: RoomPresenceInfo,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ) {
        rtmManager.setPresenceState(
            channelName,
            attr = GsonTools.beanToMap(info)
        ) { ex ->
            if (ex != null) {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(ex))
                }
                return@setPresenceState
            }
            ThreadManager.getInstance().runOnMainThread {
                val index = roomPresenceInfoList.indexOfFirst { it.roomId == info.roomId }
                if (index == -1) {
                    roomPresenceInfoList.add(info)
                } else {
                    roomPresenceInfoList[index] = info
                }
                observerHelper.notifyEventHandlers {
                    it.onUpdate?.invoke(info)
                }
                success?.invoke()
            }
        }
    }

    fun updateRoomPresenceInfo(
        roomId: String,
        @RoomPresenceStatus status: Int,
        interactorId: String = "",
        interactorName: String = "",
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ) {
        val interactionInfo = getRoomPresenceInfo(roomId)
        if (interactionInfo == null) {
            error?.invoke(RuntimeException("RoomInteractionInfo not found"))
            return
        }
        setRoomPresenceInfo(
            interactionInfo.copy(
                status = status,
                interactorId = interactorId,
                interactorName = interactorName
            ),
            success,
            error
        )
    }

    fun getAllRoomPresenceInfo(
        success: (List<RoomPresenceInfo>) -> Unit,
        error: ((Exception) -> Unit)? = null
    ) {
        rtmManager.whoNow(channelName) { ex, userList ->
            if (ex != null) {
                error?.invoke(RuntimeException(ex))
                return@whoNow
            }
            val list = mutableListOf<RoomPresenceInfo>()
            userList?.forEach {
                val info = GsonTools.toBeanSafely(it, RoomPresenceInfo::class.java) ?: return@forEach
                list.add(info)
            }
            ThreadManager.getInstance().runOnMainThread {
                success.invoke(list)
            }
        }
    }

    fun getRoomPresenceInfo(roomId: String): RoomPresenceInfo? {
        return roomPresenceInfoList.firstOrNull { it.roomId == roomId }
    }
}

@IntDef(
    RoomPresenceStatus.IDLE,
    RoomPresenceStatus.INTERACTING_LINKING,
    RoomPresenceStatus.INTERACTING_PK
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RoomPresenceStatus {
    companion object {
        const val IDLE = 0
        const val INTERACTING_LINKING = 1
        const val INTERACTING_PK = 2
    }
}

data class RoomPresenceInfo constructor(
    val roomId: String, // 唯一房间ID
    val roomName: String, // 房间名
    val ownerId: String, // 房主用户ID
    val ownerName: String, // 房主名
    val ownerAvatar: String, // 房主头像
    @RoomPresenceStatus val status: Int = RoomPresenceStatus.IDLE,
    val interactorId: String = "", // 互动者ID
    val interactorName: String = ""// 互动者名
)

data class RoomPresenceSubscriber(
    val onSnapshot: ((List<RoomPresenceInfo>) -> Unit)? = null,
    val onUpdate: ((RoomPresenceInfo) -> Unit)? = null,
    val onDelete: ((RoomPresenceInfo) -> Unit)? = null,
    val onError: ((Exception) -> Unit)? = null
)