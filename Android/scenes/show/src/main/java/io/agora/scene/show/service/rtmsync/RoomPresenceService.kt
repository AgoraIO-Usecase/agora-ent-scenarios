package io.agora.scene.show.service.rtmsync

import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserRespObserver
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ThreadManager
import io.agora.scene.show.service.ShowInteractionStatus
import java.util.ArrayList

internal class RoomPresenceService(
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) {

    private var onRoomPresenceSnapshot: ((List<RoomPresenceInfo>) -> Unit)? = null
    private var onRoomPresenceUpdate: ((RoomPresenceInfo) -> Unit)? = null
    private var onRoomPresenceDelete: ((RoomPresenceInfo) -> Unit)? = null

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
                val info = GsonTools.toBean(
                    GsonTools.beanToString(it),
                    RoomPresenceInfo::class.java
                )!!
                val index = roomPresenceInfoList.indexOfFirst { it.roomId == info.roomId }
                if (index == -1) {
                    roomPresenceInfoList.add(info)
                } else {
                    roomPresenceInfoList[index] = info
                }
            }
            onRoomPresenceSnapshot?.invoke(ArrayList(roomPresenceInfoList))
        }

        override fun onUserDidJoined(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>
        ) {
            if (channelName != this@RoomPresenceService.channelName) {
                return
            }
            val info = GsonTools.toBean(
                GsonTools.beanToString(userInfo),
                RoomPresenceInfo::class.java
            )!!
            val index = roomPresenceInfoList.indexOfFirst { it.roomId == info.roomId }
            if (index == -1) {
                roomPresenceInfoList.add(info)
            } else {
                roomPresenceInfoList[index] = info
            }
            onRoomPresenceUpdate?.invoke(info)
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
                onRoomPresenceDelete?.invoke(info)
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
            val info = GsonTools.toBean(
                GsonTools.beanToString(userInfo),
                RoomPresenceInfo::class.java
            )!!
            val index = roomPresenceInfoList.indexOfFirst { it.roomId == info.roomId }
            if (index == -1) {
                roomPresenceInfoList.add(info)
            } else {
                roomPresenceInfoList[index] = info
            }
            onRoomPresenceUpdate?.invoke(info)
        }
    }


    fun subscribe(
        onSnapshot: ((List<RoomPresenceInfo>) -> Unit)? = null,
        onUpdate: ((RoomPresenceInfo) -> Unit)? = null,
        onDelete: ((RoomPresenceInfo) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        rtmManager.subscribeUser(userRespObserver)
        rtmManager.subscribe(channelName) {
            if (it != null) {
                onError?.invoke(RuntimeException(it))
                return@subscribe
            }
            onRoomPresenceSnapshot = onSnapshot
            onRoomPresenceUpdate = onUpdate
            onRoomPresenceDelete = onDelete
        }
    }

    fun unsubscribe() {
        onRoomPresenceSnapshot = null
        onRoomPresenceUpdate = null
        onRoomPresenceDelete = null
        rtmManager.unSubscribe(channelName)
        rtmManager.unsubscribeUser(userRespObserver)
    }

    fun setRoomPresenceInfo(
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
                onRoomPresenceUpdate?.invoke(info)
                success?.invoke()
            }
        }
    }

    fun updateRoomPresenceInfo(
        roomId: String,
        @ShowInteractionStatus interactionStatus: Int,
        interactorId: String = "",
        interactorName: String = "",
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ){
        val interactionInfo = getRoomPresenceInfo(roomId)
        if(interactionInfo == null){
            error?.invoke(RuntimeException("RoomInteractionInfo not found"))
            return
        }
        setRoomPresenceInfo(
            interactionInfo.copy(
                interactionStatus = interactionStatus,
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
            val list = userList?.map {
                GsonTools.toBean(GsonTools.beanToString(it), RoomPresenceInfo::class.java)!!
            } ?: emptyList()
            ThreadManager.getInstance().runOnMainThread {
                success.invoke(list)
            }
        }
    }

    fun getRoomPresenceInfo(roomId: String): RoomPresenceInfo? {
        return roomPresenceInfoList.firstOrNull { it.roomId == roomId }
    }


    data class RoomPresenceInfo constructor(
        val roomId: String, // 唯一房间ID
        val roomName: String, // 房间名
        val ownerId: String, // 房主用户ID
        val ownerName: String, // 房主名
        val ownerAvatar: String, // 房主头像
        @ShowInteractionStatus val interactionStatus: Int, // 互动状态
        val interactorId: String, // 互动者ID
        val interactorName: String, // 互动者名称
    )

}