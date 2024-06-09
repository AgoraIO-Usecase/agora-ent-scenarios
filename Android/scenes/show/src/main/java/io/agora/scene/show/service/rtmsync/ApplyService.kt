package io.agora.scene.show.service.rtmsync

import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.service.collection.AUIAttributesModel
import io.agora.rtmsyncmanager.service.collection.AUIListCollection
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserRespObserver
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper

/**
 * 申请服务
 *
 */
class ApplyService(
    private val channelName: String,
    private val syncManager: SyncManager,
    private val interactionService: InteractionService
) {
    private val tag = "ApplyService($channelName)"

    private val key = "apply"

    private val listCollectionCreator =
        { channelName: String, sceneKey: String, rtmManager: AUIRtmManager ->
            AUIListCollection(channelName, sceneKey, rtmManager)
        }

    private val observableHelper = ObservableHelper<(List<ApplyInfo>) -> Unit>()

    private val delegate: (channelName: String, observeKey: String, value: AUIAttributesModel) -> Unit =
        { cn: String, observeKey: String, value: AUIAttributesModel ->
            if (observeKey == key) {

                val list = mutableListOf<ApplyInfo>()
                value.getList()?.forEach {
                    val item = GsonTools.toBeanSafely(it, ApplyInfo::class.java)?: return@forEach
                    list.add(item)
                }
                AUILogger.logger().d(tag, "onApplyList Updated: $list")
                observableHelper.notifyEventHandlers {
                    it.invoke(ArrayList(list))
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
            AUILogger.logger().d(tag, "onUserDidLeaved userId:$userId")
            removeUserApply(userId)
        }

        override fun onUserDidUpdated(
            channelName: String,
            userId: String,
            userInfo: Map<String, Any>
        ) {

        }

    }

    init {
        syncManager.getScene(channelName)?.getCollection(key, listCollectionCreator)
            ?.subscribeAttributesDidChanged(delegate)
        if (AUIRoomContext.shared().isRoomOwner(channelName)) {
            syncManager.rtmManager.subscribeUser(userObserver)
        }
    }

    fun addApply(
        userId: String,
        success: ((ApplyInfo) -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        AUILogger.logger().d(tag, "addApply userId:$userId")
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            AUILogger.logger().d(tag, "addApply scene not found")
            failure?.invoke(RuntimeException("scene not found"))
            return
        }
        val userInfo = scene.userService.getUserInfo(userId)
        if (userInfo == null) {
            AUILogger.logger().d(tag, "addApply user not found")
            failure?.invoke(RuntimeException("user not found"))
            return
        }

        val applyInfo = ApplyInfo(
            userId = userId,
            userName = userInfo.userName,
            userAvatar = userInfo.userAvatar
        )
        scene.getCollection(key, listCollectionCreator).addMetaData(
            "createApply",
            GsonTools.beanToMap(applyInfo),
            listOf(
                mapOf(
                    "userId" to userId
                )
            )
        ) {
            AUILogger.logger().d(tag, "addApply result:$it")
            if (it != null) {
                failure?.invoke(RuntimeException(it))
                return@addMetaData
            }
            success?.invoke(applyInfo)
        }
    }

    fun acceptApply(
        userId: String,
        success: (() -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        AUILogger.logger().d(tag, "acceptApply userId:$userId")
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            AUILogger.logger().d(tag, "acceptApply scene not found")
            failure?.invoke(RuntimeException("scene not found"))
            return
        }

        val list = mutableListOf<ApplyInfo>()
        scene.getCollection(key, listCollectionCreator).getLocalMetaData().getList()?.forEach {
            val item = GsonTools.toBeanSafely(it, ApplyInfo::class.java)?: return@forEach
            list.add(item)
        }
        val applyInfo = list.find {
                it.userId == userId
            }

        if (applyInfo == null) {
            AUILogger.logger().d(tag, "acceptApply apply not found")
            failure?.invoke(RuntimeException("apply not found"))
            return
        }
        removeApply(userId)
        interactionService.startLinkingInteraction(
            applyInfo.userId,
            success = {
                success?.invoke()
            },
            failure = failure
        )
    }

    fun cancelApply(
        userId: String,
        success: (() -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        AUILogger.logger().d(tag, "cancelApply userId:$userId")
        removeApply(userId, success, failure)
    }

    private fun removeApply(
        userId: String,
        success: (() -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        AUILogger.logger().d(tag, "removeApply userId:$userId")
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            AUILogger.logger().d(tag, "removeApply scene not found")
            failure?.invoke(RuntimeException("scene not found"))
            return
        }

        scene.getCollection(key, listCollectionCreator).removeMetaData(
            "removeApply",
            listOf(
                mapOf(
                    "userId" to userId
                )
            )
        ) {
            AUILogger.logger().d(tag, "removeApply result:$it")
            if (it != null) {
                failure?.invoke(RuntimeException(it))
                return@removeMetaData
            }
            success?.invoke()
        }
    }


    fun getApplyList(
        success: ((List<ApplyInfo>) -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            failure?.invoke(RuntimeException("scene not found"))
            return
        }
        scene.getCollection(key, listCollectionCreator).getMetaData { error, value ->
            if (error != null) {
                failure?.invoke(RuntimeException(error))
                return@getMetaData
            }
            val list = (value as? List<*>) ?: emptyList<Any>()
            val ret = mutableListOf<ApplyInfo>()
            list.forEach {
                val item = GsonTools.toBeanSafely(it, ApplyInfo::class.java)?: return@forEach
                ret.add(item)
            }
            success?.invoke(ret)
        }
    }

    fun subscribeApplyEvent(
        onUpdate: (List<ApplyInfo>) -> Unit,
        failure: ((Throwable) -> Unit)? = null
    ) {
        AUILogger.logger().d(tag, "subscribeApplyEvent onUpdate:$onUpdate")
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            failure?.invoke(RuntimeException("scene not found"))
            return
        }

        observableHelper.subscribeEvent(onUpdate)
    }

    fun unSubscribeApplyEvent(onUpdate: (List<ApplyInfo>) -> Unit) {
        AUILogger.logger().d(tag, "unSubscribeApplyEvent onUpdate:$onUpdate")
        observableHelper.unSubscribeEvent(onUpdate)
    }

    fun release() {
        AUILogger.logger().d(tag, "release")
        observableHelper.unSubscribeAll()
        syncManager.getScene(channelName)?.getCollection(key, listCollectionCreator)
            ?.subscribeAttributesDidChanged(null)

        if (AUIRoomContext.shared().isRoomOwner(channelName)) {
            syncManager.rtmManager.unsubscribeUser(userObserver)
            syncManager.getScene(channelName)?.getCollection(key, listCollectionCreator)
                ?.cleanMetaData { }
        }
    }


    private fun removeUserApply(
        userId: String,
        success: (() -> Unit)? = null,
        failure: ((Throwable) -> Unit)? = null
    ) {
        AUILogger.logger().d(tag, "removeUserApply userId:$userId")
        val scene = syncManager.getScene(channelName)
        if (scene == null) {
            AUILogger.logger().d(tag, "removeUserApply scene not found")
            failure?.invoke(RuntimeException("scene not found"))
            return
        }

        scene.getCollection(key, listCollectionCreator).removeMetaData(
            "removeUserApply",
            listOf(
                mapOf(
                    "userId" to userId
                )
            )
        ) {
            AUILogger.logger().d(tag, "removeUserApply result:$it")
            if (it != null) {
                failure?.invoke(RuntimeException(it))
                return@removeMetaData
            }
            success?.invoke()
        }
    }
}

data class ApplyInfo constructor(
    val userId: String,
    val userName: String,
    val userAvatar: String
)