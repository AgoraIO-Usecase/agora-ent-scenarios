package io.agora.rtmsyncmanager.service.arbiter

import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmLockRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.ObservableHelper

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
             下列两种情况需要刷新下 metadata 到最新
             1. 如果 lockOwnerId 是自己，并且是切换了仲裁者(非首次获取，否则第一次 roomService 里 onReceiveLock 拿到的是空)，需要在通知外部锁转移前刷新下
             2. 如果 lockOwnerId 不是自己，而之前lockOwnerId是自己，说明自己从仲裁者切换成非仲裁者了，需要通知外部后刷新下(因为 collection 认为是锁主的情况下是不会用远端数据的)，可能自己的本地数据没有到最新
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

    fun subscribeEvent(handler: AUIArbiterCallback) {
        arbiterHandlers.subscribeEvent(handler)
    }

    fun unSubscribeEvent(handler: AUIArbiterCallback) {
        arbiterHandlers.unSubscribeEvent(handler)
    }

    fun create(completion: ((AUIRtmException?) -> Unit)? = null) {
        AUILogger.logger().d(tag, "setLock error: $channelName")
        rtmManager.setLock(channelName) { error ->
            AUILogger.logger().d(tag, "setLock error: $error")
            notifyError(error)
            completion?.invoke(error)
        }
    }

    fun destroy(completion: ((AUIRtmException?) -> Unit)? = null) {
        AUILogger.logger().d(tag, "removeLock channelName: $channelName")
        rtmManager.removeLock(channelName) { error ->
            AUILogger.logger().d(tag, "removeLock error: $error")
            notifyError(error)
            completion?.invoke(error)
        }
    }

    fun acquire(completion: ((AUIRtmException?) -> Unit)? = null) {
        AUILogger.logger().d(tag, "acquireLock channelName: $channelName")
        rtmManager.acquireLock(channelName) { error ->
            AUILogger.logger().d(tag, "acquireLock error: $error")
            notifyError(error)
            completion?.invoke(error)
        }
    }

    fun release(completion: ((AUIRtmException?) -> Unit)? = null) {
        AUILogger.logger().d(tag, "releaseLock channelName: $channelName")
        rtmManager.releaseLock(channelName){ error ->
            AUILogger.logger().d(tag, "releaseLock error: $error")
            notifyError(error)
            completion?.invoke(error)
        }
    }

    fun isArbiter() = lockOwnerId == currentUserId

    fun lockOwnerId() = lockOwnerId

    private fun notifyError(error: AUIRtmException?) {
        if (error == null) {
            return
        }
        arbiterHandlers.notifyEventHandlers { handler ->
            handler.onError(channelName, error)
        }
    }

    private fun notifyArbiterDidChange() {
        arbiterHandlers.notifyEventHandlers { handler ->
            handler.onArbiterDidChange(channelName, lockOwnerId)
        }
    }
}