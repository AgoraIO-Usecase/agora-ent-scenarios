package io.agora.scene.show.service.rtmsync

import androidx.annotation.IntDef
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper
import java.util.UUID

class PKService(
    private val channelName: String,
    private val syncManager: SyncManager,
    private val roomPresenceService: RoomPresenceService,
    private val interactionService: InteractionService
) {
    private val tag = "PKService($channelName)"
    private val key = "pk"

    private val observerHelper = ObservableHelper<(PKInfo) -> Unit>()

    private val messageRetainer = MessageRetainer(syncManager.rtmManager, channelName, key).apply {
        subscribe { msg ->
            val pkInfo = GsonTools.toBeanSafely(
                msg.content,
                PKInfo::class.java
            )
            AUILogger.logger().d(tag, "onPKUpdated from message: $pkInfo")
            if (pkInfo != null) {
                observerHelper.notifyEventHandlers {
                    it.invoke(pkInfo)
                }
            }
        }
    }

    private val roomPresenceSubscriber = RoomPresenceSubscriber(
        onUpdate = { info: RoomPresenceInfo ->
            AUILogger.logger().d(tag, "onRoomPresenceUpdated: $info")

            // 被PK方 -> 发起PK方
            if (info.roomId != channelName) {
                val currInfo = roomPresenceService.getRoomPresenceInfo(channelName)
                if (info.status == RoomPresenceStatus.INTERACTING_PK
                    && currInfo?.status == RoomPresenceStatus.IDLE
                    && info.interactorId == AUIRoomContext.shared().currentUserInfo.userId
                ) {
                    AUILogger.logger().d(tag, "after acceptPK >> updateRoomPresenceInfo : status=${RoomPresenceStatus.INTERACTING_PK}, interactorId=${info.ownerId}, interactorName=${info.ownerName}")
                    roomPresenceService.updateRoomPresenceInfo(
                        channelName,
                        status = RoomPresenceStatus.INTERACTING_PK,
                        interactorId = info.ownerId,
                        interactorName = info.ownerName,
                    )
                }
            }

            // 发起PK方 -> 被PK方
            if (info.roomId != channelName) {
                val currInfo = roomPresenceService.getRoomPresenceInfo(channelName)
                if (
                    info.status == RoomPresenceStatus.INTERACTING_PK
                    && currInfo?.status == RoomPresenceStatus.INTERACTING_PK
                    && currInfo.interactorId == info.ownerId
                    && info.interactorId == currInfo.ownerId
                ) {
                    AUILogger.logger().d(tag, "after acceptPK >> startPKInteraction roomId=${info.roomId}, ownerId=${info.ownerId}, ownerName=${info.ownerName}")
                    interactionService.startPKInteraction(
                        info.roomId,
                        info.ownerId,
                        info.ownerName,
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
            }

            // 发起PK方
            if (info.roomId == channelName) {
                val pkRoomInfo = roomPresenceService.getRoomPresenceInfoByOwnerId(info.interactorId)
                if (
                    info.status == RoomPresenceStatus.INTERACTING_PK
                    && pkRoomInfo?.status == RoomPresenceStatus.INTERACTING_PK
                    && pkRoomInfo.interactorId == info.ownerId
                    && info.interactorId == pkRoomInfo.ownerId
                ) {
                    AUILogger.logger().d(tag, "after acceptPK >> startPKInteraction roomId=${pkRoomInfo.roomId}, ownerId=${pkRoomInfo.ownerId}, ownerName=${pkRoomInfo.ownerName}")
                    interactionService.startPKInteraction(
                        pkRoomInfo.roomId,
                        pkRoomInfo.ownerId,
                        pkRoomInfo.ownerName
                    )
                    observerHelper.notifyEventHandlers {
                        it.invoke(
                            PKInfo(
                                userId = pkRoomInfo.ownerId,
                                userName = pkRoomInfo.ownerName,
                                roomId = pkRoomInfo.roomId,
                                fromUserId = pkRoomInfo.interactorId,
                                fromUserName = pkRoomInfo.interactorName,
                                fromRoomId = pkRoomInfo.roomId,
                                type = PKType.ACCEPT
                            )
                        )
                    }
                }
            }

            // 被PK方/发起PK方 有一方关闭即停止PK
            if (info.roomId != channelName) {
                val currInfo = roomPresenceService.getRoomPresenceInfo(channelName)
                if (
                    info.status == RoomPresenceStatus.IDLE
                    && currInfo?.status == RoomPresenceStatus.INTERACTING_PK
                    && currInfo.interactorId == info.ownerId
                ) {
                    AUILogger.logger().d(tag, "after stopPK >> stopInteraction")
                    interactionService.stopInteraction(
                        success = {
                            AUILogger.logger().d(tag, "after stopPK >> stopInteraction success")
                        },
                        failure = {
                            AUILogger.logger().d(tag, "after stopPK >> stopInteraction error: $it")
                        }
                    )
                    observerHelper.notifyEventHandlers {
                        it.invoke(
                            PKInfo(
                                userId = currInfo.ownerId,
                                userName = currInfo.ownerName,
                                roomId = currInfo.roomId,
                                fromUserId = info.ownerId,
                                fromUserName = info.ownerName,
                                fromRoomId = info.roomId,
                                type = PKType.END
                            )
                        )
                    }
                }
            }

            // 主播已经和其他人PK
            if (info.roomId != channelName) {
                val currInfo = roomPresenceService.getRoomPresenceInfo(channelName)
                if (info.status == RoomPresenceStatus.INTERACTING_PK
                    && currInfo?.status == RoomPresenceStatus.INTERACTING_PK
                    && currInfo.interactorId == info.ownerId
                    && info.interactorId != AUIRoomContext.shared().currentUserInfo.userId
                ) {
                    AUILogger.logger().d(tag, "after startPK >> updateRoomPresenceInfo for pk owner has started interaction with others: status=${RoomPresenceStatus.IDLE}")
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
            if (interactionInfo?.type == InteractionType.PK && it.ownerId == interactionInfo.userId) {
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
            AUILogger.logger().d(tag, "invitePK >> room presence info is null")
            error?.invoke(RuntimeException("room presence info is null"))
            return
        }
        if (roomPresenceInfo.status != RoomPresenceStatus.IDLE) {
            AUILogger.logger().d(tag, "invitePK >> room presence status is not idle")
            error?.invoke(RuntimeException("room presence status is not idle"))
            return
        }
        val currRoomPresenceInfo = roomPresenceService.getRoomPresenceInfo(channelName)
        if(currRoomPresenceInfo?.status != RoomPresenceStatus.IDLE){
            AUILogger.logger().d(tag, "invitePK >> current room presence status is not idle")
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
        AUILogger.logger().d(tag, "[${pkInfo.id}] invitePK >> sendMessage : $pkInfo")
        messageRetainer.sendMessage(
            GsonTools.beanToString(pkInfo) ?: "",
            roomPresenceInfo.ownerId,
            roomPresenceInfo.roomId,
            success = {
                AUILogger.logger().d(tag, "[${pkInfo.id}] invitePK >> sendMessage success")
                success.invoke(pkInfo)
            },
            error = {
                AUILogger.logger().d(tag, "[${pkInfo.id}] invitePK >> sendMessage error: $it")
                error?.invoke(it)
            }
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
            AUILogger.logger().d(tag, "[$pkId] acceptPK >> pk info is null")
            error?.invoke(RuntimeException("pk info is null"))
            return
        }
        val roomPresenceInfo = roomPresenceService.getRoomPresenceInfo(pkInfo.fromRoomId)
        if (roomPresenceInfo?.status != RoomPresenceStatus.IDLE) {
            AUILogger.logger().d(tag, "[$pkId] acceptPK >> room presence status is not idle")
            error?.invoke(RuntimeException("room presence status is not idle"))
            return
        }
        // 因为不管对方在不在线，更新presence总是会成功，这里被一个点对点消息用于判断对方是否在线
        messageRetainer.sendMessage(
            GsonTools.beanToString(pkInfo.copy(type = PKType.ACCEPT)) ?: "",
            pkInfo.fromUserId,
            pkInfo.fromRoomId,
            success = {
                AUILogger.logger().d(tag, "[$pkId] acceptPK >> sendMessage success")
                AUILogger.logger().d(tag, "[$pkId] acceptPK >> updateRoomPresenceInfo : interactorId=${pkInfo.fromUserId}, interactorName=${pkInfo.fromUserName}")
                roomPresenceService.updateRoomPresenceInfo(
                    channelName,
                    status = RoomPresenceStatus.INTERACTING_PK,
                    interactorId = pkInfo.fromUserId,
                    interactorName = pkInfo.fromUserName,
                    success = {
                        AUILogger.logger().d(tag, "[$pkId] acceptPK >> updateRoomPresenceInfo success")
                        messageRetainer.removeMessage(message.id)
                        success?.invoke()
                    },
                    error = {
                        AUILogger.logger().d(tag, "[$pkId] acceptPK >> updateRoomPresenceInfo error: $it")
                        error?.invoke(RuntimeException(it))
                    }
                )
            },
            error = {
                AUILogger.logger().d(tag, "[$pkId] acceptPK >> sendMessage error: $it")
                error?.invoke(it)
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
            AUILogger.logger().d(tag, "[$pkId] rejectPK >> pk info is null")
            error?.invoke(RuntimeException("pk info is null"))
            return
        }

        pkInfo = pkInfo.copy(type = PKType.REJECT)
        AUILogger.logger().d(tag, "[$pkId] rejectPK >> sendMessage : $pkInfo")
        messageRetainer.sendMessage(
            GsonTools.beanToString(pkInfo) ?: "",
            pkInfo.fromUserId,
            pkInfo.fromRoomId,
            success = {
                AUILogger.logger().d(tag, "[$pkId] rejectPK >> sendMessage success")
                messageRetainer.removeMessage(message.id)
                success?.invoke()
            },
            error = {
                AUILogger.logger().d(tag, "[$pkId] rejectPK >> sendMessage error: $it")
                error?.invoke(it)
            }
        )
    }

    fun subscribe(onUpdate: (PKInfo) -> Unit) {
        observerHelper.subscribeEvent(onUpdate)
    }

    fun unSubscribe(onUpdate: (PKInfo) -> Unit) {
        observerHelper.unSubscribeEvent(onUpdate)
    }

    fun release() {
        AUILogger.logger().d(tag, "release")
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