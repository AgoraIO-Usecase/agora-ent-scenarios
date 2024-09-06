package io.agora.rtmsyncmanager.service.arbiter

import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmLockRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.ObservableHelper

/**
 * AUIArbiter is a class that manages the arbitration process in a channel.
 * It handles the creation, destruction, acquisition, and release of locks in a channel.
 * It also notifies the subscribed handlers about the changes in the arbitration process.
 */
class AUIArbiter(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val currentUserId: String
) {

    private val tag = "AUIArbiter"

    private var lockOwnerId = ""
        set(value) {
            field = value
            notifyArbiterDidChange()
        }
    private var arbiterHandlers = ObservableHelper<AUIArbiterCallback>()

    private val rtmLockRespObserver = object : AUIRtmLockRespObserver {
        override fun onReceiveLock(channelName: String, lockName: String, lockOwner: String) {
            AUILogger.logger().d(tag, "onReceiveLock channelName: $channelName lockName: $lockName lockOwner: $lockOwner")
            if (channelName != this@AUIArbiter.channelName) return
            /*
             * The following two scenarios require refreshing the metadata to the latest:
             * 1. If the lockOwnerId is the current user and the arbiter has switched (not the first acquisition, otherwise the first onReceiveLock obtained in roomService is empty),
             *    it is necessary to refresh before notifying the external lock transfer.
             * 2. If the lockOwnerId is not the current user, and the previous lockOwnerId was the current user, it means that the current user has switched from the arbiter to a non-arbiter,
             *    and it needs to be refreshed after notifying the external (because the collection will not use remote data when it thinks it is the lock owner),
             *    it is possible that the local data of the current user is not up to date.
             */
            val gotLockFromOthers = lockOwner == currentUserId
            val lossLockToOthers = lockOwnerId == currentUserId && lockOwner != currentUserId
            if (gotLockFromOthers) {
                rtmManager.fetchMetaDataSnapshot(channelName) {
                    //TODO: error handler, retry?
                    lockOwnerId = lockOwner
                }
            } else if (lossLockToOthers) {
                lockOwnerId = lockOwner
                rtmManager.fetchMetaDataSnapshot(channelName) {
                    //TODO: error handler, retry?
                }
            } else {
                lockOwnerId = lockOwner
            }
        }

        override fun onReleaseLock(channelName: String, lockName: String, lockOwner: String, isExpire: Boolean) {
            AUILogger.logger().d(tag, "onReleaseLock channelName: $channelName lockName: $lockName lockOwner: $lockOwner")
            if (channelName == this@AUIArbiter.channelName) {
                acquire()
                if (isExpire && lockOwnerId != currentUserId) return
                lockOwnerId = ""
            }
        }
    }

    init {
        rtmManager.subscribeLock(channelName, observer = rtmLockRespObserver)
    }

    fun deInit(){
        rtmManager.unsubscribeLock(rtmLockRespObserver)
    }

    // Subscribes a handler to the arbiter events
    fun subscribeEvent(handler: AUIArbiterCallback) {
        arbiterHandlers.subscribeEvent(handler)
    }

    // Unsubscribes a handler from the arbiter events
    fun unSubscribeEvent(handler: AUIArbiterCallback) {
        arbiterHandlers.unSubscribeEvent(handler)
    }

    // Creates a lock in the channel
    fun create(completion: ((AUIRtmException?) -> Unit)? = null) {
        AUILogger.logger().d(tag, "setLock start: $channelName")
        rtmManager.setLock(channelName) { error ->
            AUILogger.logger().d(tag, "setLock error: $error")
            notifyError(error)
            completion?.invoke(error)
        }
    }

    // Destroys the lock in the channel
    fun destroy(completion: ((AUIRtmException?) -> Unit)? = null) {
        AUILogger.logger().d(tag, "removeLock channelName: $channelName")
        rtmManager.removeLock(channelName) { error ->
            AUILogger.logger().d(tag, "removeLock error: $error")
            notifyError(error)
            completion?.invoke(error)
        }
    }

    // Acquires the lock in the channel
    fun acquire(completion: ((AUIRtmException?) -> Unit)? = null) {
        AUILogger.logger().d(tag, "acquireLock channelName: $channelName")
        rtmManager.acquireLock(channelName) { error ->
            AUILogger.logger().d(tag, "acquireLock error: $error")
            notifyError(error)
            completion?.invoke(error)
        }
    }

    // Releases the lock in the channel
    fun release(completion: ((AUIRtmException?) -> Unit)? = null) {
        AUILogger.logger().d(tag, "releaseLock channelName: $channelName")
        rtmManager.releaseLock(channelName){ error ->
            AUILogger.logger().d(tag, "releaseLock error: $error")
            notifyError(error)
            completion?.invoke(error)
        }
    }

    // Checks if the current user is the arbiter
    fun isArbiter() = lockOwnerId == currentUserId

    // Returns the ID of the user who currently owns the lock
    fun lockOwnerId() = lockOwnerId

    // Notifies the handlers about an error
    private fun notifyError(error: AUIRtmException?) {
        if (error == null) {
            return
        }
        arbiterHandlers.notifyEventHandlers { handler ->
            handler.onError(channelName, error)
        }
    }

    // Notifies the handlers when the arbiter changes
    private fun notifyArbiterDidChange() {
        arbiterHandlers.notifyEventHandlers { handler ->
            handler.onArbiterDidChange(channelName, lockOwnerId)
        }
    }
}