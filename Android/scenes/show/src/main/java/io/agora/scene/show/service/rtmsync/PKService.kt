package io.agora.scene.show.service.rtmsync

import androidx.annotation.IntDef
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper
import java.util.UUID

class PKService(
    private val channelName: String,
    private val syncManager: SyncManager,
    private val roomPresenceService: RoomPresenceService,
    private val interactionService: InteractionService
) {

    private val key = "pk"

    private val observerHelper = ObservableHelper<(PKInfo) -> Unit>()

    private val messageRetainer = MessageRetainer(syncManager.rtmManager, channelName, key).apply {
        subscribe { msg ->
            val pkInfo = GsonTools.toBeanSafely(
                msg.content,
                PKInfo::class.java
            )
            if (pkInfo != null) {
                observerHelper.notifyEventHandlers {
                    it.invoke(pkInfo)
                }
            }
        }
    }

    private val roomPresenceSubscriber = RoomPresenceSubscriber(
        onUpdate = { info: RoomPresenceInfo ->
            if (info.roomId != channelName) {
                val currInfo = roomPresenceService.getRoomPresenceInfo(channelName)
                if (info.status == RoomPresenceStatus.INTERACTING_PK
                    && info.interactorId == AUIRoomContext.shared().currentUserInfo.userId) {
                    if (currInfo?.status == RoomPresenceStatus.INTERACTING_PK) {
                        interactionService.startPKInteraction(
                            info.roomId,
                            info.ownerId,
                            info.ownerName
                        )
                        observerHelper.notifyEventHandlers {
                            it.invoke(
                                PKInfo(
                                    userId = info.ownerId,
                                    userName = info.ownerName,
                                    roomId = info.roomId,
                                    fromUserId = info.interactorId,
                                    fromUserName = info.interactorName,
                                    fromRoomId = info.roomId,
                                    type = PKType.ACCEPT
                                )
                            )
                        }
                    } else if(currInfo?.status == RoomPresenceStatus.IDLE){
                        roomPresenceService.updateRoomPresenceInfo(
                            channelName,
                            status = RoomPresenceStatus.INTERACTING_PK,
                            interactorId = info.ownerId,
                            interactorName = info.ownerName,
                            success = {
                                interactionService.startPKInteraction(
                                    info.roomId,
                                    info.ownerId,
                                    info.ownerName
                                )
                                observerHelper.notifyEventHandlers {
                                    it.invoke(
                                        PKInfo(
                                            userId = info.ownerId,
                                            userName = info.ownerName,
                                            roomId = info.roomId,
                                            fromUserId = info.interactorId,
                                            fromUserName = info.interactorName,
                                            fromRoomId = info.roomId,
                                            type = PKType.ACCEPT
                                        )
                                    )
                                }
                            }
                        )
                    }
                } else if (info.status == RoomPresenceStatus.IDLE
                    && currInfo?.interactorId == info.ownerId
                ) {
                    interactionService.stopInteraction()
                    roomPresenceService.updateRoomPresenceInfo(
                        channelName,
                        status = RoomPresenceStatus.IDLE,
                    )
                    observerHelper.notifyEventHandlers {
                        it.invoke(
                            PKInfo(
                                userId = currInfo.ownerId,
                                userName = currInfo.ownerName,
                                roomId = currInfo.roomId,
                                fromUserId = currInfo.interactorId,
                                fromUserName = currInfo.interactorName,
                                fromRoomId = currInfo.roomId,
                                type = PKType.END
                            )
                        )
                    }
                } else if (info.status == RoomPresenceStatus.INTERACTING_PK
                    && currInfo?.interactorId == info.ownerId
                    && info.interactorId != AUIRoomContext.shared().currentUserInfo.userId
                ) {
                    // 主播已经和其他人PK
                    roomPresenceService.updateRoomPresenceInfo(
                        channelName,
                        status = RoomPresenceStatus.IDLE,
                    )
                    observerHelper.notifyEventHandlers {
                        it.invoke(
                            PKInfo(
                                userId = currInfo.ownerId,
                                userName = currInfo.ownerName,
                                roomId = currInfo.roomId,
                                fromUserId = currInfo.interactorId,
                                fromUserName = currInfo.interactorName,
                                fromRoomId = currInfo.roomId,
                                type = PKType.END
                            )
                        )
                    }
                }
            }
        },
        onDelete = {
            val interactionInfo = interactionService.getInteractionInfo()
            if(interactionInfo?.type == InteractionType.PK && it.interactorId == interactionInfo.userId){
                interactionService.stopInteraction()
                roomPresenceService.updateRoomPresenceInfo(
                    channelName,
                    status = RoomPresenceStatus.IDLE,
                )
            }
        }
    )

    init {
        roomPresenceService.subscribe(roomPresenceSubscriber)
    }


    fun invitePK(
        roomId: String,
        success: (PKInfo) -> Unit,
        error: ((Throwable) -> Unit)? = null
    ) {
        val roomPresenceInfo = roomPresenceService.getRoomPresenceInfo(roomId)
        if (roomPresenceInfo == null) {
            error?.invoke(RuntimeException("room presence info is null"))
            return
        }
        if (roomPresenceInfo.status != RoomPresenceStatus.IDLE) {
            error?.invoke(RuntimeException("room presence status is not idle"))
            return
        }
        val currRoomPresenceInfo = roomPresenceService.getRoomPresenceInfo(channelName)
        if(currRoomPresenceInfo?.status != RoomPresenceStatus.IDLE){
            error?.invoke(RuntimeException("current room presence status is not idle"))
            return
        }
        val pkInfo = PKInfo(
            userId = roomPresenceInfo.ownerId,
            userName = roomPresenceInfo.ownerName,
            roomId = roomPresenceInfo.roomId,
            fromUserId = AUIRoomContext.shared().currentUserInfo.userId,
            fromUserName = AUIRoomContext.shared().currentUserInfo.userName,
            fromRoomId = channelName,
            type = PKType.INVITING
        )
        messageRetainer.sendMessage(
            GsonTools.beanToString(pkInfo) ?: "",
            roomPresenceInfo.ownerId,
            success = {
                success.invoke(pkInfo)
            },
            error
        )
    }

    fun acceptPK(
        pkId: String,
        success: (() -> Unit)? = null,
        error: ((Throwable) -> Unit)? = null
    ) {
        val message = messageRetainer.getMessage {
            GsonTools.toBeanSafely(
                it.content,
                PKInfo::class.java
            )?.id == pkId
        }
        val pkInfo = GsonTools.toBeanSafely(
            message?.content,
            PKInfo::class.java
        )
        if (message == null || pkInfo == null) {
            error?.invoke(RuntimeException("pk info is null"))
            return
        }
        val roomPresenceInfo = roomPresenceService.getRoomPresenceInfo(pkInfo.fromRoomId)
        if (roomPresenceInfo?.status != RoomPresenceStatus.IDLE) {
            error?.invoke(RuntimeException("room presence status is not idle"))
            return
        }

        roomPresenceService.updateRoomPresenceInfo(
            channelName,
            status = RoomPresenceStatus.INTERACTING_PK,
            interactorId = pkInfo.fromUserId,
            interactorName = pkInfo.fromUserName,
            success = {
                messageRetainer.removeMessage(message.id)
                success?.invoke()
            },
            error = {
                error?.invoke(RuntimeException(it))
            }
        )
    }

    fun rejectPK(
        pkId: String,
        success: (() -> Unit)? = null,
        error: ((Throwable) -> Unit)? = null
    ) {
        val message = messageRetainer.getMessage {
            GsonTools.toBeanSafely(
                it.content,
                PKInfo::class.java
            )?.id == pkId
        }
        var pkInfo = GsonTools.toBeanSafely(
            message?.content,
            PKInfo::class.java
        )
        if (message == null || pkInfo == null) {
            error?.invoke(RuntimeException("pk info is null"))
            return
        }

        pkInfo = pkInfo.copy(type = PKType.REJECT)
        messageRetainer.sendMessage(
            GsonTools.beanToString(pkInfo) ?: "",
            pkInfo.userId,
            success = {
                messageRetainer.removeMessage(message.id)
                success?.invoke()
            },
            error
        )
    }

    fun subscribe(onUpdate: (PKInfo) -> Unit) {
        observerHelper.subscribeEvent(onUpdate)
    }

    fun unSubscribe(onUpdate: (PKInfo) -> Unit) {
        observerHelper.unSubscribeEvent(onUpdate)
    }

    fun release() {
        observerHelper.unSubscribeAll()
        roomPresenceService.unSubscribe(roomPresenceSubscriber)
        messageRetainer.release()
    }

}

@IntDef(PKType.INVITING, PKType.ACCEPT, PKType.REJECT, PKType.END)
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class PKType {
    companion object {
        const val INVITING = 0
        const val ACCEPT = 1
        const val REJECT = 2
        const val END = 3
    }
}

data class PKInfo(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    var userName: String,
    val roomId: String,
    val fromUserId: String,
    val fromUserName: String,
    val fromRoomId: String,
    @PKType val type: Int = PKType.INVITING,
)