package io.agora.scene.show.service.rtmsync

import io.agora.rtmsyncmanager.RoomExpirationPolicy
import io.agora.rtmsyncmanager.RoomService
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.service.http.HttpManager
import io.agora.rtmsyncmanager.service.room.AUIRoomManager
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools

private val tag = "SyncExtensions"

private val mInteractionServiceMap = mutableMapOf<String, InteractionService>()

private val mApplyServiceMap = mutableMapOf<String, ApplyService>()

private val mInvitationServiceMap = mutableMapOf<String, InvitationService>()

private val mPKServiceMap = mutableMapOf<String, PKService>()

private val mMessageRetainerMap = mutableMapOf<String, MutableMap<String, MessageRetainer>>()

private val mRoomManager = AUIRoomManager()

private var mRoomService: RoomService? = null

private var mRoomPresenceService: RoomPresenceService? = null

fun SyncManager.getExInteractionService(channelName: String): InteractionService {
    return mInteractionServiceMap.getOrPut(channelName) {
        InteractionService(channelName, this, getExRoomPresenceService())
    }
}

fun SyncManager.getExApplyService(channelName: String): ApplyService {
    return mApplyServiceMap.getOrPut(channelName) {
        ApplyService(
            channelName,
            this,
            getExInteractionService(channelName)
        )
    }
}

fun SyncManager.getExInvitationService(channelName: String): InvitationService {
    return mInvitationServiceMap.getOrPut(channelName) {
        InvitationService(
            channelName,
            this,
            getExInteractionService(channelName)
        )
    }
}

fun SyncManager.getExPKService(channelName: String): PKService {
    return mPKServiceMap.getOrPut(channelName) {
        PKService(
            channelName,
            this,
            getExRoomPresenceService(),
            getExInteractionService(channelName)
        )
    }
}

fun SyncManager.getExMessageRetainer(channelName: String, key: String): MessageRetainer {
    return mMessageRetainerMap.getOrPut(channelName) {
        mutableMapOf()
    }.getOrPut(key) {
        MessageRetainer(rtmManager, channelName, key)
    }
}

fun SyncManager.cleanExServices(channelName: String) {
    mInteractionServiceMap.remove(channelName)?.release()
    mApplyServiceMap.remove(channelName)?.release()
    mInvitationServiceMap.remove(channelName)?.release()
    mPKServiceMap.remove(channelName)?.release()
    mMessageRetainerMap.remove(channelName)?.values?.forEach { it.release() }
}

fun SyncManager.getExRoomManager(): AUIRoomManager {
    return mRoomManager
}

fun SyncManager.getExRoomService(): RoomService {
    return mRoomService ?: throw IllegalStateException("Please setupExtensions first")
}

fun SyncManager.getExRoomPresenceService(): RoomPresenceService {
    return mRoomPresenceService ?: throw IllegalStateException("Please setupExtensions first")
}

fun SyncManager.isExRoomOwner(channelName: String): Boolean{
    return getExRoomService().isRoomOwner(channelName)
}

fun SyncManager.setupExtensions(
    roomPresenceChannelName: String,
    roomExpirationPolicy: RoomExpirationPolicy,
    roomHostUrl: String? = null,
    loggerConfig: AUILogger.Config? = null
) {
    roomHostUrl?.let {
        HttpManager.setBaseURL(it)
    }
    loggerConfig?.let {
        AUILogger.initLogger(it)
    }
    AUILogger.logger().d(tag, "setupExtensions")
    mRoomService = RoomService(roomExpirationPolicy, getExRoomManager(), this)
    mRoomPresenceService = RoomPresenceService(rtmManager, roomPresenceChannelName)
}

fun SyncManager.destroyExtensions() {
    AUILogger.logger().d(tag, "destroyExtensions")
    mInteractionServiceMap.values.forEach { it.release() }
    mApplyServiceMap.values.forEach { it.release() }
    mInvitationServiceMap.values.forEach { it.release() }
    mPKServiceMap.values.forEach { it.release() }
    mMessageRetainerMap.values.forEach { map -> map.values.forEach { it.release() } }
    mInteractionServiceMap.clear()
    mApplyServiceMap.clear()
    mInvitationServiceMap.clear()
    mPKServiceMap.clear()
    mMessageRetainerMap.clear()
    mRoomService = null
    mRoomPresenceService = null
}

internal fun <T> GsonTools.toBeanSafely(data: Any?, clazz: Class<T>): T? {
    if(data == null){
        return null
    }
    if (data is Map<*, *> && data.isEmpty()) {
        return null
    }
    return toBean(beanToString(data), clazz)
}

