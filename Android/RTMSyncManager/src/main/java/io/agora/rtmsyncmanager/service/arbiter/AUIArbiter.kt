package io.agora.rtmsyncmanager.service.arbiter

import io.agora.rtmsyncmanager.ISceneResponse
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmLockRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.ObservableHelper
import okhttp3.internal.notify

class AUIArbiter(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val currentUserId: String
) {

    private var lockOwnerId = ""
        set(value) {
            field = value
            notifyArbiterDidChange()
        }
    private var arbiterHandlers = ObservableHelper<AUIArbiterCallback>()

    private val rtmLockRespObserver = object : AUIRtmLockRespObserver {
        override fun onReceiveLock(channelName: String, lockName: String, lockOwner: String) {
            if (lockOwnerId.isNotEmpty() && lockOwner == currentUserId) {
                rtmManager.fetchMetaDataSnapshot(channelName) {
                    //TODO: error handler, retry?
                    lockOwnerId = lockOwner
                }
            } else {
                lockOwnerId = lockOwner
            }
        }

        override fun onReleaseLock(channelName: String, lockName: String, lockOwner: String) {
            if (channelName == this@AUIArbiter.channelName) {
                acquire()
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

    fun create(){
        rtmManager.setLock(channelName){ error ->
            notifyError(error)
        }
    }

    fun destroy(){
        rtmManager.removeLock(channelName){ error ->
            notifyError(error)
        }
    }

    fun acquire() {
        rtmManager.acquireLock(channelName){ error ->
            notifyError(error)
        }
    }

    fun release() {
        rtmManager.releaseLock(channelName){ error ->
            notifyError(error)
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