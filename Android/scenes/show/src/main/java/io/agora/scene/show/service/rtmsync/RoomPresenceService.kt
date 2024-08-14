package io.agora.scene.show.service.rtmsync

import androidx.annotation.IntDef
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserRespObserver
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper
import io.agora.rtmsyncmanager.utils.ThreadManager

class RoomPresenceService(
    private val rtmManager: AUIRtmManager,
    private val channelName: String
) {
    private val tag = "RoomPresenceService($channelName)"

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
                val info =
                    GsonTools.toBeanSafely(it, RoomPresenceInfo::class.java) ?: return@forEach
                val index = roomPresenceInfoList.indexOfFirst { it.roomId == info.roomId }
                if (index == -1) {
                    roomPresenceInfoList.add(info)
                } else {
                    roomPresenceInfoList[index] = info
                }
            }
            AUILogger.logger().d(tag, "onUserSnapshotRecv ${roomPresenceInfoList.size}")
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
            AUILogger.logger().d(tag, "onUserDidJoined $info")
            observerHelper.notifyEventHandlers {
                it.onUpdate?.invoke(info)
            }
        }

        override fun onUserDidLeaved(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>,
            reason: AUIRtmUserLeaveReason
        ) {
            if (channelName != this@RoomPresenceService.channelName) {
                return
            }
            val info = roomPresenceInfoList.findLast { it.ownerId == userId }
            AUILogger.logger().d(tag, "onUserDidLeaved $info")
            if (info != null) {
                roomPresenceInfoList.removeIf { it.ownerId == userId }
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
            AUILogger.logger().d(tag, "onUserDidUpdated $info")
            observerHelper.notifyEventHandlers {
                it.onUpdate?.invoke(info)
            }
        }

    }

    fun login(complete: () -> Unit) {
        AUILogger.logger().d(tag, "login")
        rtmManager.subscribeUser(userRespObserver)
        rtmManager.subscribe(channelName) { ex ->
            AUILogger.logger().d(tag, "login complete: $ex")
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
        AUILogger.logger().d(tag, "logout")
        observerHelper.unSubscribeAll()
        rtmManager.unSubscribe(channelName)
        rtmManager.unsubscribeUser(userRespObserver)
    }

    fun setup(
        info: RoomPresenceInfo,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ) {
        AUILogger.logger().d(tag, "setup")
        setRoomPresenceInfo(info,
            {
                AUILogger.logger().d(tag, "setup success")
                success?.invoke()
            },
            {
                AUILogger.logger().d(tag, "setup failed : $it")
                error?.invoke(it)
            })
    }

    fun subscribe(subscriber: RoomPresenceSubscriber) {
        AUILogger.logger().d(tag, "subscribe $subscriber")
        observerHelper.subscribeEvent(subscriber)
    }

    fun unSubscribe(subscriber: RoomPresenceSubscriber) {
        AUILogger.logger().d(tag, "unSubscribe $subscriber")
        observerHelper.unSubscribeEvent(subscriber)
    }

    private fun setRoomPresenceInfo(
        info: RoomPresenceInfo,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ) {
        AUILogger.logger().d(tag, "setRoomPresenceInfo $info")
        rtmManager.setPresenceState(
            channelName,
            attr = GsonTools.beanToMap(info)
        ) { ex ->
            AUILogger.logger().d(tag, "setRoomPresenceInfo complete: $ex")
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
        AUILogger.logger()
            .d(tag, "updateRoomPresenceInfo $roomId $status $interactorId $interactorName")
        val interactionInfo = getRoomPresenceInfo(roomId)
        if (interactionInfo == null) {
            AUILogger.logger().d(tag, "updateRoomPresenceInfo failed: RoomInteractionInfo not found")
            error?.invoke(RuntimeException("RoomInteractionInfo not found"))
            return
        }
        setRoomPresenceInfo(
            interactionInfo.copy(
                status = status,
                interactorId = interactorId,
                interactorName = interactorName
            ),
            {
                AUILogger.logger().d(tag, "updateRoomPresenceInfo success")
                success?.invoke()
            },
            {
                AUILogger.logger().d(tag, "updateRoomPresenceInfo failed: $it")
                error?.invoke(it)
            }
        )
    }

    fun getAllRoomPresenceInfo(
        success: (List<RoomPresenceInfo>) -> Unit,
        error: ((Exception) -> Unit)? = null
    ) {
        AUILogger.logger().d(tag, "getAllRoomPresenceInfo")
        rtmManager.whoNow(channelName) { ex, userList ->
            AUILogger.logger().d(tag, "getAllRoomPresenceInfo complete: $ex, userListSize=${userList?.size}")
            if (ex != null) {
                error?.invoke(RuntimeException(ex))
                return@whoNow
            }
            val list = mutableListOf<RoomPresenceInfo>()
            userList?.forEach {
                if (it.size <= 1) {
                    return@forEach
                }
                val info =
                    GsonTools.toBeanSafely(it, RoomPresenceInfo::class.java) ?: return@forEach
                list.add(info)
            }
            ThreadManager.getInstance().runOnMainThread {
                success.invoke(list)
            }
        }
    }

    fun getRoomPresenceInfo(roomId: String): RoomPresenceInfo? {
        val firstOrNull = roomPresenceInfoList.firstOrNull { it.roomId == roomId }
        AUILogger.logger().d(tag, "getRoomPresenceInfo $roomId $firstOrNull")
        return firstOrNull
    }

    fun getRoomPresenceInfoByOwnerId(ownerId: String): RoomPresenceInfo? {
        val firstOrNull = roomPresenceInfoList.lastOrNull { it.ownerId == ownerId }
        AUILogger.logger().d(tag, "getRoomPresenceInfoByOwnerId $ownerId $firstOrNull")
        return firstOrNull
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