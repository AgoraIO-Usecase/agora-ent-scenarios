package io.agora.scene.show.service.rtmsync

import androidx.annotation.IntDef
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.service.collection.AUIAttributesModel
import io.agora.rtmsyncmanager.service.collection.AUICollectionException
import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserRespObserver
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper
import io.agora.scene.base.utils.TimeUtils

class InteractionService(
    private val channelName: String,
    private val syncManager: SyncManager,
    private val roomPresenceService: RoomPresenceService
) {
    private val tag = "InteractionService($channelName)"

    private val key = "interaction"

    private val mapCollectionCreator =
        { channelName: String, sceneKey: String, rtmManager: AUIRtmManager ->
            AUIMapCollection(channelName, sceneKey, rtmManager)
        }

    private val observableHelper = ObservableHelper<(InteractionInfo?) -> Unit>()

    private var interactionInfo: InteractionInfo? = null

    private val delegate: (channelName: String, observeKey: String, value: AUIAttributesModel) -> Unit =
        { cn: String, observeKey: String, value: AUIAttributesModel ->
            if (channelName == cn && observeKey == key) {
                val info = GsonTools.toBeanSafely(
                    value.getMap(),
                    InteractionInfo::class.java
                )
                AUILogger.logger().d(tag, "onInteractionInfo Updated: $info")
                if (info?.type == InteractionType.LINKING) {
                    roomPresenceService.updateRoomPresenceInfo(
                        channelName,
                        RoomPresenceStatus.INTERACTING_LINKING,
                        info.userId,
                        info.userName
                    )
                }
                if (info == null || info.type == InteractionType.IDLE) {
                    roomPresenceService.updateRoomPresenceInfo(
                        channelName,
                        RoomPresenceStatus.IDLE
                    )
                }
                interactionInfo = info

                observableHelper.notifyEventHandlers {
                    it.invoke(info)
                }
            }
        }

    private val userObserver = object : AUIRtmUserRespObserver {
        override fun onUserSnapshotRecv(
            channelName: String,
            userId: String,
            userList: List<Map<String, Any>>
        ) {

        }

        override fun onUserDidJoined(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>
        ) {

        }

        override fun onUserDidLeaved(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>,
            reason: AUIRtmUserLeaveReason
        ) {
            if (interactionInfo?.userId == userId && reason == AUIRtmUserLeaveReason.NORMAL) {
                stopInteraction()
            }
        }

        override fun onUserDidUpdated(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>
        ) {

        }
    }


    init {
        val roomOwner = AUIRoomContext.shared().isRoomOwner(channelName)
        AUILogger.logger().d(tag, "init >> isRoomOwner: $roomOwner")

        val collection = syncManager.getScene(channelName)?.getCollection(key, mapCollectionCreator)
        collection?.subscribeAttributesDidChanged(delegate)

        collection?.subscribeWillAdd { publisherId, valueCmd, value ->
                val newInfo = GsonTools.toBeanSafely(
                    value,
                    InteractionInfo::class.java
                )
                val currInfo = collection.getLocalMetaData().getMap()?.let {
                    GsonTools.toBeanSafely(
                        it,
                        InteractionInfo::class.java
                    )
                }
                AUILogger.logger().d(tag, "subscribeWillAdd : publisherId=$publisherId, valueCmd=$valueCmd, value=$value, newInfo=$newInfo, currInfo=$currInfo")
                when (valueCmd) {
                    InteractionCmd.START_PK.value, InteractionCmd.START_LINKING.value -> {
                        if (currInfo == null || currInfo.type == InteractionType.IDLE) {
                            return@subscribeWillAdd null
                        }
                        return@subscribeWillAdd AUICollectionException.ErrorCode.unknown.toException(
                            -1,
                            "interaction already started"
                        )
                    }
                    InteractionCmd.STOP.value -> {
                        if(currInfo?.userId == publisherId || AUIRoomContext.shared().isRoomOwner(channelName, newInfo?.userId)){
                            return@subscribeWillAdd null
                        }
                        return@subscribeWillAdd AUICollectionException.ErrorCode.unknown.toException(
                            -1,
                            "interaction already stopped or not allowed to stop"
                        )
                    }
                }
                return@subscribeWillAdd null
            }

        if (roomOwner) {
            syncManager.rtmManager.subscribeUser(userObserver)
        }
    }

    fun startPKInteraction(
        roomId: String,
        userId: String,
        userName: String,
        success: ((InteractionInfo) -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        if (interactionInfo?.type == InteractionType.LINKING) {
            AUILogger.logger().d(tag, "startPKInteraction: linking interaction already started -- $interactionInfo")
            failure?.invoke(RuntimeException("interaction already started"))
            return
        }
        if (interactionInfo?.type == InteractionType.PK) {
            AUILogger.logger().d(tag, "startPKInteraction: pk interaction already started -- $interactionInfo")
            failure?.invoke(RuntimeException("interaction already started"))
            return
        }
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            AUILogger.logger().d(tag, "startPKInteraction: scene not found")
            failure?.invoke(RuntimeException("scene not found"))
            return
        }

        val info = InteractionInfo(
            userId = userId,
            userName = userName,
            roomId = roomId,
            type = InteractionType.PK
        )
        AUILogger.logger().d(tag, "startPKInteraction: $info")
        scene.getCollection(key, mapCollectionCreator).addMetaData(
            InteractionCmd.START_PK.value,
            GsonTools.beanToMap(info),
        ) {
            AUILogger.logger().d(tag, "startPKInteraction complete : $it")
            if (it != null) {
                failure?.invoke(RuntimeException(it))
                return@addMetaData
            }
            this.interactionInfo = info
            success?.invoke(info)
        }
    }

    fun startLinkingInteraction(
        userId: String,
        success: ((InteractionInfo) -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        if (interactionInfo?.type == InteractionType.LINKING) {
            AUILogger.logger().d(tag, "startLinkingInteraction: linking interaction already started -- $interactionInfo")
            failure?.invoke(RuntimeException("interaction already started"))
            return
        }
        if (interactionInfo?.type == InteractionType.PK) {
            AUILogger.logger().d(tag, "startLinkingInteraction: pk interaction already started -- $interactionInfo")
            failure?.invoke(RuntimeException("interaction already started"))
            return
        }
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            AUILogger.logger().d(tag, "startLinkingInteraction: scene not found")
            failure?.invoke(RuntimeException("scene not found"))
            return
        }
        val userInfo = scene.userService.getUserInfo(userId)
        if (userInfo == null) {
            AUILogger.logger().d(tag, "startLinkingInteraction: $userId user not found")
            failure?.invoke(RuntimeException("user not found"))
            return
        }

        val info =
            InteractionInfo(
                type = InteractionType.LINKING,
                userId = userId,
                userName = userInfo.userName,
                roomId = channelName
            )
        AUILogger.logger().d(tag, "startLinkingInteraction: $info")
        scene.getCollection(key, mapCollectionCreator).addMetaData(
            InteractionCmd.START_LINKING.value,
            GsonTools.beanToMap(info),
        ) {
            AUILogger.logger().d(tag, "startLinkingInteraction complete : $it")
            if (it != null) {
                failure?.invoke(RuntimeException(it))
                return@addMetaData
            }
            this.interactionInfo = info
            success?.invoke(info)
        }
    }


    fun stopInteraction(
        success: (() -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        if (interactionInfo == null) {
            AUILogger.logger().d(tag, "stopInteraction: no interaction started")
            failure?.invoke(RuntimeException("interaction already started"))
            return
        }
        if (interactionInfo?.type == InteractionType.IDLE) {
            AUILogger.logger().d(tag, "stopInteraction: the interaction type is idle -- $interactionInfo")
            failure?.invoke(RuntimeException("interaction already started"))
            return
        }

        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            AUILogger.logger().d(tag, "stopInteraction: scene not found")
            failure?.invoke(RuntimeException("scene not found"))
            return
        }
        AUILogger.logger().d(tag, "stopInteraction")
        val info = InteractionInfo(userId = AUIRoomContext.shared().currentUserInfo.userId)
        scene.getCollection(key, mapCollectionCreator).addMetaData(
            InteractionCmd.STOP.value,
            GsonTools.beanToMap(info),
        ) {
            AUILogger.logger().d(tag, "stopInteraction complete : $it")
            if (it != null) {
                failure?.invoke(RuntimeException(it))
                return@addMetaData
            }
            success?.invoke()
        }
    }

    fun getInteractionInfo(): InteractionInfo?  {
        interactionInfo = syncManager.getScene(channelName)?.getCollection(key, mapCollectionCreator)?.getLocalMetaData()?.getMap()?.let {
            GsonTools.toBeanSafely(
                it,
                InteractionInfo::class.java
            )
        }
        return interactionInfo
    }

    fun getLatestInteractionInfo(
        success: (InteractionInfo?) -> Unit,
        failure: ((Throwable) -> Unit)? = null
    ) {
        val collection = syncManager.getScene(channelName)?.getCollection(key, mapCollectionCreator)
        if (collection == null) {
            AUILogger.logger().d(tag, "getLatestInteractionInfo >> collection not found")
            failure?.invoke(RuntimeException("collection not found"))
            return
        }
        AUILogger.logger().d(tag, "getLatestInteractionInfo")
        collection.getMetaData { error, value ->
            if (error != null) {
                AUILogger.logger().d(tag, "getLatestInteractionInfo >> error: $error")
                failure?.invoke(RuntimeException(error))
                return@getMetaData
            }

            val info = GsonTools.toBeanSafely(
                value,
                InteractionInfo::class.java
            )
            AUILogger.logger().d(tag, "getLatestInteractionInfo >> info: $info")
            interactionInfo = info
            success.invoke(info)
        }
    }

    fun subscribeInteractionEvent(onUpdate: (InteractionInfo?) -> Unit) {
        AUILogger.logger().d(tag, "subscribeInteractionEvent: $onUpdate")
        observableHelper.subscribeEvent(onUpdate)
    }

    fun unSubscribeInteractionEvent(onUpdate: (InteractionInfo?) -> Unit) {
        AUILogger.logger().d(tag, "unSubscribeInteractionEvent: $onUpdate")
        observableHelper.unSubscribeEvent(onUpdate)
    }

    fun release() {
        AUILogger.logger().d(tag, "release")
        observableHelper.unSubscribeAll()
        syncManager.getScene(channelName)?.getCollection(key, mapCollectionCreator)
            ?.subscribeAttributesDidChanged(null)

        if (AUIRoomContext.shared().isRoomOwner(channelName)) {
            syncManager.rtmManager.unsubscribeUser(userObserver)
            syncManager.getScene(channelName)?.getCollection(key, mapCollectionCreator)
                ?.cleanMetaData {}
        }
    }
}

@IntDef(InteractionType.IDLE, InteractionType.LINKING, InteractionType.PK)
@Retention(AnnotationRetention.RUNTIME)
annotation class InteractionType {
    companion object {
        const val IDLE = 0
        const val LINKING = 1
        const val PK = 2
    }
}

data class InteractionInfo constructor(
    @InteractionType val type: Int = InteractionType.IDLE,
    val userId: String = "", // Interactor ID
    val userName: String = "", // Interactor username
    val roomId: String = "", // Interactor's room ID
    val createdAt: Double = TimeUtils.currentTimeMillis().toDouble()
)

enum class InteractionCmd(val value: String) {
    START_PK("startPKInteraction"),
    START_LINKING("startLinkingInteraction"),
    STOP("stopInteraction")
}