package io.agora.scene.show.service.rtmsync

import android.util.Log
import androidx.annotation.IntDef
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.service.collection.AUIAttributesModel
import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserRespObserver
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper

class InteractionService(
    private val channelName: String,
    private val syncManager: SyncManager,
    private val roomPresenceService: RoomPresenceService
) {

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
                val info = if (value.getMap()?.isEmpty() != false) null else GsonTools.toBean(
                    GsonTools.beanToString(value.getMap()),
                    InteractionInfo::class.java
                )
                if(info?.type == InteractionType.LINKING){
                    roomPresenceService.updateRoomPresenceInfo(
                        channelName,
                        RoomPresenceStatus.INTERACTING_LINKING,
                        info.userId,
                        info.userName
                    )
                }
                if (info == null) {
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
            userInfo: Map<String, Any>
        ) {
            if (interactionInfo?.userId == userId) {
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
        syncManager.getScene(channelName)?.getCollection(key, mapCollectionCreator)
            ?.subscribeAttributesDidChanged(delegate)

        if (AUIRoomContext.shared().isRoomOwner(channelName)) {
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
        if (interactionInfo != null) {
            failure?.invoke(RuntimeException("interaction already started"))
            return
        }
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            failure?.invoke(RuntimeException("scene not found"))
            return
        }

        val info = InteractionInfo(
            userId = userId,
            userName = userName,
            roomId = roomId,
            type = InteractionType.PK
        )
        scene.getCollection(key, mapCollectionCreator).addMetaData(
            "startPKInteraction",
            GsonTools.beanToMap(info),
        ) {
            if (it != null) {
                failure?.invoke(RuntimeException(it))
                return@addMetaData
            }
            success?.invoke(info)
        }
    }

    fun startLinkingInteraction(
        userId: String,
        success: ((InteractionInfo) -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        if (interactionInfo != null) {
            failure?.invoke(RuntimeException("interaction already started"))
            return
        }
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            failure?.invoke(RuntimeException("scene not found"))
            return
        }
        val userInfo = scene.userService.getUserInfo(userId)
        if (userInfo == null) {
            failure?.invoke(RuntimeException("user not found"))
            return
        }

        val info =
            InteractionInfo(userId = userId, userName = userInfo.userName, roomId = channelName)
        scene.getCollection(key, mapCollectionCreator).addMetaData(
            "startLinkingInteraction",
            GsonTools.beanToMap(info),
        ) {
            if (it != null) {
                failure?.invoke(RuntimeException(it))
                return@addMetaData
            }
            success?.invoke(info)
        }
    }


    fun stopInteraction(
        success: (() -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            failure?.invoke(RuntimeException("scene not found"))
            return
        }
        scene.getCollection(key, mapCollectionCreator).cleanMetaData {
            Log.d("ShowInteraction", "stopInteraction: $it")
            if (it != null) {
                failure?.invoke(RuntimeException(it))
                return@cleanMetaData
            }
            success?.invoke()
        }
    }

    fun getInteractionInfo(): InteractionInfo? = interactionInfo

    fun subscribeInteractionEvent(onUpdate: (InteractionInfo?) -> Unit) {
        observableHelper.subscribeEvent(onUpdate)
    }

    fun unSubscribeInteractionEvent(onUpdate: (InteractionInfo?) -> Unit) {
        observableHelper.unSubscribeEvent(onUpdate)
    }

    fun release() {
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

@IntDef(InteractionType.LINKING, InteractionType.PK)
@Retention(AnnotationRetention.RUNTIME)
annotation class InteractionType {
    companion object {
        const val LINKING = 1
        const val PK = 2
    }
}

data class InteractionInfo constructor(
    @InteractionType val type: Int = InteractionType.LINKING,
    val userId: String, // 互动者ID
    val userName: String, // 互动者用户名
    val roomId: String, // 互动者所在房间ID
)