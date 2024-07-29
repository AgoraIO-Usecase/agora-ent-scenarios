package io.agora.rtmsyncmanager

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.agora.rtm.LinkStateEvent
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmConstants.RtmConnectionChangeReason
import io.agora.rtm.RtmConstants.RtmConnectionState
import io.agora.rtm.RtmConstants.RtmErrorCode
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.service.IAUIUserService
import io.agora.rtmsyncmanager.service.arbiter.AUIArbiter
import io.agora.rtmsyncmanager.service.arbiter.AUIArbiterCallback
import io.agora.rtmsyncmanager.service.collection.AUIListCollection
import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.collection.IAUICollection
import io.agora.rtmsyncmanager.service.imp.AUIUserServiceImpl
import io.agora.rtmsyncmanager.service.rtm.AUIRtmErrorRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.ObservableHelper
import java.util.*

/**
 * Class representing a Scene in the Agora RTM Sync Manager.
 *
 * This class manages the state of a room, including the room's metadata, users, and collections.
 * It also handles the room's lifecycle, including creation, entry, leaving, and deletion.
 */
class Scene constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val roomExpiration: RoomExpirationPolicy,
    private val removeCompletion: () -> Unit
) {

    private val tag = "AUIScene"

    private val kRoomInfoKey = "scene_room_info"
    private val kRoomInfoRoomId = "room_id"
    private val kRoomInfoRoomOwnerId = "room_owner_id"
    private val kRoomCreateTime = "room_create_time"
    private val kRoomInfoPayloadId = "room_payload_id"

    private var collectionMap = mutableMapOf<String, IAUICollection>()

    private var arbiter: AUIArbiter = AUIArbiter(channelName, rtmManager, AUIRoomContext.shared().currentUserInfo.userId)
    private var enterCondition: AUISceneEnterCondition
    private lateinit var expireCondition: AUISceneExpiredCondition

    /**
     * The user service for this scene.
     *
     * This service is used to manage users in the room.
     */
    public val userService = AUIUserServiceImpl(channelName, rtmManager).apply {
        registerRespObserver(object: IAUIUserService.AUIUserRespObserver {
            override fun onRoomUserSnapshot(roomId: String, userList: List<AUIUserInfo>?) {
                expireCondition.userSnapshotList = userList
                val currentUser = userList?.firstOrNull { it.userId == AUIRoomContext.shared().currentUserInfo.userId }
                if (currentUser != null) {
                    AUILogger.logger().d(tag, "onRoomUserSnapshot[$roomId]")
                    if (AUIRoomContext.shared().isRoomOwner(roomId)) {
                        expireCondition.ownerHasLeftRoom = currentUser.customPayload == null
                    }
                    onUserAudioMute(userId = currentUser.userId, mute = currentUser.muteAudio)
                    onUserVideoMute(userId = currentUser.userId, mute = currentUser.muteVideo)
                }
            }
            override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {}
            override fun onRoomUserLeave(
                roomId: String,
                userInfo: AUIUserInfo,
                reason: AUIRtmUserLeaveReason
            ) {
                if (!AUIRoomContext.shared().isRoomOwner(roomId, userInfo.userId)) {
                    cleanUserInfo(userInfo.userId)
                    return
                }
                cleanUserInfo(userInfo.userId)
                respHandlers.notifyEventHandlers { handler ->
                    handler.onSceneDestroy(channelName)
                }
            }
            override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {}
            override fun onUserAudioMute(userId: String, mute: Boolean) {}
            override fun onUserVideoMute(userId: String, mute: Boolean) {}
        })
    }

    private val roomCollection: AUIMapCollection by lazy {
        getCollection(kRoomInfoKey) { channelName, sceneKey, rtmManager ->
            AUIMapCollection(channelName, sceneKey, rtmManager)
        }
    }

    private var enterRoomCompletion: ((Map<String, Any>?, AUIRtmException?)-> Unit)? = null
    private var respHandlers = ObservableHelper<ISceneResponse>()
    private var roomPayload: Map<String, Any>? = null

    private var subscribeDate: Long? = null

    init {
        AUIRoomContext.shared().roomArbiterMap[channelName] = this.arbiter
        this.enterCondition = AUISceneEnterCondition(channelName, this.arbiter)
        this.expireCondition = AUISceneExpiredCondition(channelName, roomExpiration)

        this.enterCondition.enterCompletion = {
            this.enterRoomCompletion?.invoke(this.roomPayload, null)
            this.enterRoomCompletion = null
        }

        this.expireCondition.roomDidExpired = {
            respHandlers.notifyEventHandlers {
                it.onSceneExpire(channelName)
            }

            if (AUIRoomContext.shared().isRoomOwner(channelName)) {
                cleanScene()
            }
        }
    }

    /**
     * Binds a response delegate to this scene.
     *
     * @param handler The response delegate to bind.
     */
    fun bindRespDelegate(handler: ISceneResponse) {
        respHandlers.subscribeEvent(handler)
    }

    /**
     * Unbinds a response delegate from this scene.
     *
     * @param handler The response delegate to unbind.
     */
    fun unbindRespDelegate(handler: ISceneResponse) {
        respHandlers.unSubscribeEvent(handler)
    }

    /**
     * Creates a new room in this scene.
     *
     * @param createTime The creation time of the room.
     * @param payload The payload for the room.
     * @param completion The completion handler to call when the room is created.
     */
    fun create(createTime: Long, payload: Map<String, Any>?, completion: (AUIRtmException?)->Unit) {
        if (!rtmManager.isLogin) {
            completion.invoke(AUIRtmException(-1, "create fail! not login", ""))
            return
        }
        val ownerId = AUIRoomContext.shared().currentUserInfo.userId
        val roomInfo = mutableMapOf(
            kRoomInfoRoomId to channelName,
            kRoomInfoRoomOwnerId to ownerId,
            kRoomCreateTime to createTime.toString()
        )
        if (payload != null) {
            roomInfo[kRoomInfoPayloadId] = Gson().toJson(payload)
        }

        respHandlers.notifyEventHandlers {
            val collectionDataMap = it.onWillInitSceneMetadata(channelName)
            collectionDataMap?.forEach { data ->
                val metadata = data.value as? Map<String, Any>
                if (metadata != null) {
                    val collection: AUIMapCollection = getCollection(data.key) { channelName, sceneKey, rtmManager ->
                        AUIMapCollection(channelName, sceneKey, rtmManager)
                    }
                    collection.initMetaData(channelName, metadata, false) { e -> }
                    return@forEach
                }

                val listMetadata = data.value as? List<Map<String, Any>>
                if (listMetadata != null) {
                    val collection: AUIListCollection = getCollection(data.key) { channelName, sceneKey, rtmManager ->
                        AUIListCollection(channelName, sceneKey, rtmManager)
                    }
                    collection.initMetaData(channelName, listMetadata, false) { e -> }
                    return@forEach
                }
                AUILogger.logger().e(tag, "init meta data fail, key: ${data.key} value: ${data.value}")
            }
        }

        roomCollection.initMetaData(channelName, roomInfo, true) { err ->
            if (err != null) {
                runOnUiThread { completion.invoke(err) }
                return@initMetaData
            }
            runOnUiThread { completion.invoke(null) }
        }
        userService.setUserPayload(UUID.randomUUID().toString())
        getArbiter().create()
    }

    /**
     * Enters a room in this scene.
     *
     * @param completion The completion handler to call when the room is entered.
     */
    fun enter(completion: (Map<String, Any>?, AUIRtmException?)->Unit) {
        if (!rtmManager.isLogin) {
            completion.invoke(null, AUIRtmException(-1, "create fail! not login", ""))
            return
        }
        subscribeDate = System.currentTimeMillis()
        expireCondition.joinCompletion = false
        enterRoomCompletion = { payload, err ->
            if (err != null) {
                AUILogger.logger().e(tag, "enterRoomCompletion fail: ${err.message}")
            } else {
                AUILogger.logger().d(tag, "[Benchmark]enterRoomCompletion: ${System.currentTimeMillis() - (subscribeDate ?: 0)}ms")
            }
            expireCondition.joinCompletion = true
            runOnUiThread { completion(payload, err) }
        }
        if (enterCondition.ownerId.isEmpty()) {
            roomCollection.getMetaData { err, metadata ->
                val map = metadata as? Map<String, Any> ?: run {
                    this.cleanScene()
                    this.notifyError(AUIRtmException(-1, "get room owner fatal!", ""))
                    errorRespObserver.onMsgReceiveEmpty(channelName)
                    return@getMetaData
                }
                val ownerId = map[kRoomInfoRoomOwnerId] as? String ?: run {
                    this.cleanScene()
                    this.notifyError(AUIRtmException(-1, "get room owner fatal!", ""))
                    errorRespObserver.onMsgReceiveEmpty(channelName)
                    return@getMetaData
                }
                val createTimestamp = (map[kRoomCreateTime] as? String)?.toLong() ?: run {
                    this.cleanScene()
                    this.notifyError(AUIRtmException(-1, "get room owner fatal!", ""))
                    errorRespObserver.onMsgReceiveEmpty(channelName)
                    return@getMetaData
                }

                val payloadStr = map[kRoomInfoPayloadId] as? String
                if (payloadStr != null) {
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    try {
                        roomPayload = Gson().fromJson(payloadStr, type)
                    } catch (_: Exception) { }
                }
                this.enterCondition.ownerId = ownerId
                this.expireCondition.createTimestamp = createTimestamp
            }
        }
        getArbiter().acquire {
            if (it == null) {
                //fail 走onError(channelName: String, error: NSError)，这里不处理
                enterCondition.lockOwnerAcquireSuccess = true
            }
        }
        rtmManager.subscribeError(errorRespObserver)
        getArbiter().subscribeEvent(arbiterObserver)
        rtmManager.subscribe(channelName) { error ->
            if (error != null && error.code != RtmErrorCode.getValue(RtmErrorCode.DUPLICATE_OPERATION)) {
                runOnUiThread {
                    enterRoomCompletion?.invoke(null, error)
                    enterRoomCompletion = null
                }
                return@subscribe
            }
            this.enterCondition.subscribeSuccess = true
            this.userService.setUserAttr {}
        }
    }

    /**
     * Leaves the current room in this scene.
     */
    fun leave() {
        AUILogger.logger().d(tag,"leave")
        getArbiter().release()
        cleanSDK()
        AUIRoomContext.shared().cleanRoom(channelName)
        removeCompletion.invoke()
        respHandlers.unSubscribeAll()
        collectionMap.values.forEach {
            it.release()
        }
        collectionMap.clear()
        userService.release()
    }

    /**
     * Deletes the current room in this scene.
     */
    fun delete() {
        AUILogger.logger().d(tag,"delete")
        cleanScene(true)
        getArbiter().destroy()
        cleanSDK()
        AUIRoomContext.shared().cleanRoom(channelName)
        removeCompletion.invoke()
        respHandlers.unSubscribeAll()
        collectionMap.values.forEach {
            it.release()
        }
        collectionMap.clear()
        userService.release()
    }

    /**
     * Gets a collection from this scene.
     *
     * @param key The key of the collection.
     * @param create The function to create the collection if it does not exist.
     * @return The collection.
     */
    fun <T : IAUICollection>getCollection(key: String, create: ((String, String, AUIRtmManager) -> T) ): T {
        val collection = collectionMap[key]
        if (collection != null) {
            return collection as T
        }
        val scene = create.invoke(channelName, key, rtmManager)
        collectionMap[key] = scene
        return scene
    }

    /**
     * Gets the duration of the room in this scene.
     *
     * @return The duration of the room.
     */
    fun getRoomDuration() : Long {
        return expireCondition.roomUsageDuration() ?: 0L
    }

    /**
     * Gets the current timestamp of the room in this scene.
     *
     * @return The current timestamp of the room.
     */
    fun getCurrentTs() : Long {
        return expireCondition.roomCurrentTs() ?: 0L
    }

    private fun notifyError(error: AUIRtmException) {
        AUILogger.logger().e(tag,"join fail: ${error.message}")
        if (enterRoomCompletion != null) {
            enterRoomCompletion?.invoke(null, error)
            enterRoomCompletion = null
        }
    }
    private fun getArbiter(): AUIArbiter {
        return arbiter
    }

    private fun cleanUserInfo(userId: String) {

    }

    private fun cleanScene(forceClean: Boolean = false) {
        if (!getArbiter().isArbiter() && !forceClean) {
            return
        }
        _cleanScene()
    }

    private fun _cleanScene() {
        AUILogger.logger().d(tag, "cleanScene")
        rtmManager.cleanAllMetadata(channelName = channelName, lockName = "") {
        }
        getArbiter().destroy()
    }

    private fun cleanSDK() {
        AUILogger.logger().d(tag, "cleanSDK")
        rtmManager.unSubscribe(channelName)
        rtmManager.unSubscribeError(errorRespObserver)
        getArbiter().unSubscribeEvent(arbiterObserver)
    }

    private val errorRespObserver = object: AUIRtmErrorRespObserver {
        override fun onTokenPrivilegeWillExpire(channelName: String?) {
            respHandlers.notifyEventHandlers { handler ->
                handler.onTokenPrivilegeWillExpire(channelName)
            }
        }
        override fun onMsgReceiveEmpty(channelName: String) {
            respHandlers.notifyEventHandlers { handler ->
                handler.onSceneDestroy(channelName)
            }
        }
        override fun onConnectionStateChanged(channelName: String?, state: Int, reason: Int) {
            if (channelName == null) {
                return
            }
            if (RtmConnectionChangeReason.getEnum(reason) == RtmConnectionChangeReason.REJOIN_SUCCESS) {
                getArbiter().acquire()
            }
            if (RtmConnectionState.getEnum(state) == RtmConnectionState.FAILED &&
                RtmConnectionChangeReason.getEnum(reason) == RtmConnectionChangeReason.BANNED_BY_SERVER) {
                return
            }
            respHandlers.notifyEventHandlers { handler ->
                handler.onSceneUserBeKicked(channelName, AUIRoomContext.shared().currentUserInfo.userId)
            }
        }

        override fun onTimeStampsDidUpdate(timestamp: Long) {
            if (expireCondition.lastUpdateTimestamp == null) {
                expireCondition.lastUpdateTimestamp = timestamp
            }
        }

        override fun onLinkStateEvent(event: LinkStateEvent?) {
            super.onLinkStateEvent(event)
            event ?: return
            if (event.currentState == RtmConstants.RtmLinkState.DISCONNECTED && event.previousState == RtmConstants.RtmLinkState.CONNECTED) {
                expireCondition.offlineTimestamp = event.timestamp
            } else if (event.currentState == RtmConstants.RtmLinkState.CONNECTED && event.operation == RtmConstants.RtmLinkOperation.RECONNECTED) {
                getArbiter().acquire()
                expireCondition.reconnectNow(event.timestamp)
            }

            if (event.currentState == RtmConstants.RtmLinkState.FAILED) {
                respHandlers.notifyEventHandlers { handler ->
                    // TODO onSceneFailed
                }
            }

        }
    }

    private val arbiterObserver = object: AUIArbiterCallback {
        override fun onArbiterDidChange(channelName: String, arbiterId: String) {
            if (arbiterId.isEmpty()) {return}
            enterCondition.lockOwnerRetrieved = true
        }

        override fun onError(channelName: String, error: AUIRtmException) {
            if (error.code == RtmErrorCode.getValue(RtmErrorCode.LOCK_NOT_EXIST)) {
                cleanScene()
            }
            notifyError(error)
        }
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private fun runOnUiThread(runnable: Runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            runnable.run()
        } else {
            mHandler.post(runnable)
        }
    }
}