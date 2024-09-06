package io.agora.rtmsyncmanager.service.rtm

import android.text.TextUtils
import android.util.Log
import io.agora.rtm.LinkStateEvent
import io.agora.rtm.LockDetail
import io.agora.rtm.LockEvent
import io.agora.rtm.MessageEvent
import io.agora.rtm.Metadata
import io.agora.rtm.PresenceEvent
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmEventListener
import io.agora.rtm.StorageEvent
import io.agora.rtm.TopicEvent
import io.agora.rtmsyncmanager.utils.AUILogger

/**
 * Interface for handling RTM error responses.
 */
interface AUIRtmErrorRespObserver {

    /**
     * Called when the token is about to expire.
     * @param channelName The name of the channel.
     */
    fun onTokenPrivilegeWillExpire(channelName: String?)

    /**
     * Called when the link state changes.
     * @param event The link state event.
     */
    fun onLinkStateEvent(event: LinkStateEvent?) {}

    /**
     * Called when the network state changes.
     * @param channelName The name of the channel.
     * @param state The new state.
     * @param reason The reason for the state change.
     */
    fun onConnectionStateChanged(channelName: String?, state: Int, reason: Int) {}

    /**
     * Called when the received message is empty.
     * @param channelName The name of the channel.
     */
    fun onMsgReceiveEmpty(channelName: String) {}

    /**
     * Called when the system timestamps update.
     * @param timestamp The new timestamp.
     */
    fun onTimeStampsDidUpdate(timestamp: Long) {}
}

/**
 * Interface for handling RTM attribute responses.
 */
interface AUIRtmAttributeRespObserver {
    fun onAttributeChanged(channelName: String, key: String, value: Any)
}

/**
 * Interface for handling RTM message responses.
 */
interface AUIRtmMessageRespObserver {
    fun onMessageReceive(channelName: String, publisherId: String, message: String)
}

enum class AUIConnectionState(val value: Int) {
    CONNECTED(0),
    DISCONNECTED(1)
}

/**
 * Enum for defining the reasons a user leaves.
 */
enum class AUIRtmUserLeaveReason(val value: Int) {
    NORMAL(0),
    TIMEOUT(1)
}

/**
 * Interface for handling RTM user responses.
 */
interface AUIRtmUserRespObserver {
    fun onUserSnapshotRecv(channelName: String, userId: String, userList: List<Map<String, Any>>)
    fun onUserDidJoined(channelName: String, userId: String, userInfo: Map<String, Any>)
    fun onUserDidLeaved(channelName: String, userId: String, userInfo: Map<String, Any>, reason: AUIRtmUserLeaveReason)
    fun onUserDidUpdated(channelName: String, userId: String, userInfo: Map<String, Any>)
}

/**
 * Interface for handling RTM lock responses.
 */
interface AUIRtmLockRespObserver {
    fun onReceiveLock(channelName: String, lockName: String, lockOwner: String)
    fun onReleaseLock(channelName: String, lockName: String, lockOwner: String, isExpire: Boolean)
}

/**
 * Class for handling RTM messages.
 * It includes methods for registering and unregistering observers, cleaning cache, and handling various RTM events.
 */
class AUIRtmMsgProxy : RtmEventListener {
    private val tag = "AUIRtmMsgProxy"
    var originEventListeners: RtmEventListener? = null
    private val attributeRespObservers: MutableMap<String, ArrayList<AUIRtmAttributeRespObserver>> =
        mutableMapOf()
    private val msgCacheAttr: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
    private val userRespObservers: MutableList<AUIRtmUserRespObserver> = mutableListOf()
    private val errorRespObservers: MutableList<AUIRtmErrorRespObserver> = mutableListOf()
    private val lockRespObservers: MutableList<AUIRtmLockRespObserver> = mutableListOf()
    private val messageRespObservers: MutableList<AUIRtmMessageRespObserver> = mutableListOf()
    private val lockDetailCaches = mutableMapOf<String, MutableList<LockDetail>>()
    var isMetaEmpty = false

    fun cleanCache(channelName: String) {
        msgCacheAttr.remove(channelName)
        AUILogger.logger().d(tag, "cleanCache channelName$channelName, msgCacheAttr=$msgCacheAttr")
    }

    fun unRegisterAllObservers() {
        attributeRespObservers.clear()
        userRespObservers.clear()
        errorRespObservers.clear()
        lockRespObservers.clear()
        messageRespObservers.clear()
    }

    fun registerAttributeRespObserver(
        channelName: String,
        itemKey: String,
        observer: AUIRtmAttributeRespObserver
    ) {
        val key = "${channelName}__${itemKey}"
        AUILogger.logger().d(tag, "registerAttributeRespObserver: $key")
        val observers = attributeRespObservers[key] ?: ArrayList()
        observers.add(observer)
        attributeRespObservers[key] = observers
    }

    fun unRegisterAttributeRespObserver(
        channelName: String,
        itemKey: String,
        observer: AUIRtmAttributeRespObserver
    ) {
        val key = "${channelName}__${itemKey}"
        AUILogger.logger().d(tag, "unRegisterAttributeRespObserver: $key")
        val observers = attributeRespObservers[key] ?: return
        observers.remove(observer)
    }

    fun registerUserRespObserver(observer: AUIRtmUserRespObserver) {
        if (userRespObservers.contains(observer)) {
            return
        }
        userRespObservers.add(observer)
    }

    fun unRegisterUserRespObserver(observer: AUIRtmUserRespObserver) {
        userRespObservers.remove(observer)
    }

    fun registerErrorRespObserver(observer: AUIRtmErrorRespObserver) {
        if (errorRespObservers.contains(observer)) {
            return
        }
        errorRespObservers.add(observer)
    }

    fun unRegisterErrorRespObserver(observer: AUIRtmErrorRespObserver) {
        errorRespObservers.remove(observer)
    }

    fun registerMessageRespObserver(observer: AUIRtmMessageRespObserver) {
        if (messageRespObservers.contains(observer)) {
            return
        }
        messageRespObservers.add(observer)
    }

    fun unRegisterMessageRespObserver(observer: AUIRtmMessageRespObserver) {
        messageRespObservers.remove(observer)
    }

    fun registerLockRespObserver(
        channelName: String,
        lockName: String,
        observer: AUIRtmLockRespObserver
    ) {
        if (lockRespObservers.contains(observer)) {
            return
        }
        lockRespObservers.add(observer)
        lockDetailCaches[channelName]?.find { it.lockName == lockName }?.let {
            observer.onReceiveLock(channelName, it.lockName, it.lockOwner)
        }
    }

    fun unRegisterLockRespObserver(observer: AUIRtmLockRespObserver) {
        lockRespObservers.remove(observer)
    }

    override fun onStorageEvent(event: StorageEvent?) {
        //AUILogger.logger().d("rtm_event", "onStorageEvent update: $event")
        originEventListeners?.onStorageEvent(event)
        event ?: return
        processTimeStampsDidUpdate(event.timestamp)
        val channelName = event.target
        processMetaData(channelName, event.data)
    }

    fun processMetaData(channelName: String, metadata: Metadata?) {
        metadata ?: return
        val items = metadata.items
        if (items.isEmpty()) {
            if (isMetaEmpty) {
                return
            }
            isMetaEmpty = true
            errorRespObservers.forEach { handler ->
                handler.onMsgReceiveEmpty(channelName)
            }
            return
        }
        isMetaEmpty = false

        val cache = msgCacheAttr[channelName] ?: mutableMapOf()
        val existKeys = mutableListOf<String>()
        items.forEach { item ->
            existKeys.add(item.key)
            if (cache[item.key] == item.value) {
                return@forEach
            }
            cache[item.key] = item.value
            val handlerKey = "${channelName}__${item.key}"
            Log.d("rtm_event", "onStorageEvent: key event:  ${item.key} \n value: ${item.value}")
            attributeRespObservers[handlerKey]?.forEach { handler ->
                handler.onAttributeChanged(channelName, item.key, item.value)
            }
        }

        val cacheKeys = cache.keys
        cacheKeys.forEach { key ->
            if (existKeys.contains(key)) return@forEach
            // If the remote no longer has the corresponding key, all delegates need to be notified
            cache.remove(key)
            val delegateKey = "${channelName}__${key}"
            attributeRespObservers[delegateKey]?.forEach { handler ->
                handler.onAttributeChanged(channelName, key, emptyMap<String, String>())
            }
        }

        msgCacheAttr[channelName] = cache
        return
    }

    private fun processTimeStampsDidUpdate(timeStamp: Long) {
        errorRespObservers.forEach { handler ->
            handler.onTimeStampsDidUpdate(timeStamp)
        }
    }

    override fun onPresenceEvent(event: PresenceEvent?) {
        originEventListeners?.onPresenceEvent(event)
        Log.d(
            "rtm_presence_event",
            "onPresenceEvent Type: ${event?.eventType} Publisher: ${event?.publisherId}"
        )
        event ?: return
        processTimeStampsDidUpdate(event.timestamp)
        val map = mutableMapOf<String, String>()
        event.stateItems.forEach { item ->
            map[item.key] = item.value
        }
        Log.d("rtm_presence_event", "onPresenceEvent Map: $map")
        when (event.eventType) {
            RtmConstants.RtmPresenceEventType.REMOTE_JOIN ->
                userRespObservers.forEach { handler ->
                    handler.onUserDidJoined(event.channelName, event.publisherId ?: "", map)
                }

            RtmConstants.RtmPresenceEventType.REMOTE_LEAVE ->
                userRespObservers.forEach { handler ->
                    handler.onUserDidLeaved(event.channelName, event.publisherId ?: "", map, AUIRtmUserLeaveReason.NORMAL)
                }

            RtmConstants.RtmPresenceEventType.REMOTE_TIMEOUT ->
                userRespObservers.forEach { handler ->
                    handler.onUserDidLeaved(event.channelName, event.publisherId ?: "", map, AUIRtmUserLeaveReason.TIMEOUT)
                }

            RtmConstants.RtmPresenceEventType.REMOTE_STATE_CHANGED ->
                userRespObservers.forEach { handler ->
                    handler.onUserDidUpdated(event.channelName, event.publisherId ?: "", map)
                }

            RtmConstants.RtmPresenceEventType.SNAPSHOT -> {
                val userList = arrayListOf<Map<String, String>>()
                Log.d(
                    "rtm_presence_event",
                    "event.snapshot.userStateList: ${event.snapshot.userStateList}"
                )
                event.snapshot.userStateList.forEach { user ->
                    Log.d("rtm_presence_event", "----------SNAPSHOT User Start--------")
                    Log.d("rtm_presence_event", "user.states: ${user.states}")
                    Log.d("rtm_presence_event", "user.userId: ${user.userId}")
                    Log.d("rtm_presence_event", "----------SNAPSHOT User End--------")
                    val userMap = mutableMapOf<String, String>()
                    userMap["userId"] = user.userId

                    if (user.states.isNotEmpty()) {
                        user.states.forEach { item ->
                            userMap[item.key] = item.value
                        }
                    }
                    userList.add(userMap)
                }
                Log.d("rtm_presence_event", "onPresenceEvent SNAPSHOT: $userList")
                Log.d("room_enter_flow", "onPresenceEvent SNAPSHOT: $userList")
                userRespObservers.forEach { handler ->
                    handler.onUserSnapshotRecv(event.channelName, event.publisherId ?: "", userList)
                }
            }

            else -> {
                // do nothing
            }
        }
    }


    override fun onMessageEvent(event: MessageEvent?) {
        originEventListeners?.onMessageEvent(event)
        event ?: return
        Log.d("rtm_event", "onMessageEvent event: $event")
        processTimeStampsDidUpdate(event.timestamp)
        val message = event.message?.data?.let {
            if (it is ByteArray) {
                String(it)
            } else if (it is String) {
                it
            } else {
                ""
            }
        } ?: ""
        messageRespObservers.forEach {
            it.onMessageReceive(event.channelName, event.publisherId, message)
        }
    }


    override fun onTopicEvent(event: TopicEvent?) {
        originEventListeners?.onTopicEvent(event)
    }

    override fun onLockEvent(event: LockEvent?) {
        AUILogger.logger().d("rtm_lock_event", "onLockEvent event: $event")
        originEventListeners?.onLockEvent(event)
        event ?: return
        processTimeStampsDidUpdate(event.timestamp)
        val addLockDetails = mutableListOf<LockDetail>()
        val removeLockDetails = mutableListOf<LockDetail>()
        when (event.eventType) {
            RtmConstants.RtmLockEventType.SNAPSHOT, RtmConstants.RtmLockEventType.ACQUIRED -> {
                val snapshotList = lockDetailCaches[event.channelName] ?: mutableListOf()
                snapshotList.addAll(event.lockDetailList)
                lockDetailCaches[event.channelName] = snapshotList
                addLockDetails.addAll(event.lockDetailList)
            }

            RtmConstants.RtmLockEventType.EXPIRED, RtmConstants.RtmLockEventType.REMOVED, RtmConstants.RtmLockEventType.RELEASED -> {
                lockDetailCaches[event.channelName]?.let { snapshotList ->
                    event.lockDetailList.forEach { lockDetail ->
                        snapshotList.removeIf { it.lockName == lockDetail.lockName && it.lockOwner == lockDetail.lockOwner }
                    }
                }
                removeLockDetails.addAll(event.lockDetailList)
            }

            else -> {}
        }

        addLockDetails.forEach { lockDetail ->
            val lockOwner = lockDetail.lockOwner
            if (!TextUtils.isEmpty(lockOwner)) {
                lockRespObservers.forEach { observer ->
                    observer.onReceiveLock(event.channelName, lockDetail.lockName, lockOwner)
                }
            }
        }
        removeLockDetails.forEach { lockDetail ->
            lockRespObservers.forEach { observer ->
                observer.onReleaseLock(event.channelName, lockDetail.lockName, lockDetail.lockOwner, event.eventType == RtmConstants.RtmLockEventType.EXPIRED)
            }
        }
    }

    override fun onLinkStateEvent(event: LinkStateEvent?) {
        super.onLinkStateEvent(event)
        AUILogger.logger().d(tag, "rtm -- link state event: currentState=${event?.currentState}, event: $event")

        errorRespObservers.forEach {
            it.onLinkStateEvent(event)
        }
    }

    override fun onConnectionStateChanged(
        channelName: String?,
        state: RtmConstants.RtmConnectionState?,
        reason: RtmConstants.RtmConnectionChangeReason?
    ) {
        super.onConnectionStateChanged(channelName, state, reason)
        AUILogger.logger().d(tag, "rtm -- connect state change: $state, reason: $reason")

        errorRespObservers.forEach {
            it.onConnectionStateChanged(
                channelName,
                RtmConstants.RtmConnectionState.getValue(state),
                RtmConstants.RtmConnectionChangeReason.getValue(reason)
            )
        }
    }

    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        originEventListeners?.onTokenPrivilegeWillExpire(channelName)
        if (channelName?.isNotEmpty() == true) {
            errorRespObservers.forEach {
                it.onTokenPrivilegeWillExpire(channelName)
            }
        }
    }
}