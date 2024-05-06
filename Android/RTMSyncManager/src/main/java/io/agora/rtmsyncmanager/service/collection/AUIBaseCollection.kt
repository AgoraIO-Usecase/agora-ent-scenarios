package io.agora.rtmsyncmanager.service.collection

import io.agora.rtmsyncmanager.service.rtm.AUIRtmAttributeRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmMessageRespObserver
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.GsonTools

abstract class AUIBaseCollection(
    private val channelName: String,
    private val observeKey: String,
    private val rtmManager: AUIRtmManager
) : IAUICollection {

    private val messageRespObserver = object : AUIRtmMessageRespObserver {
        override fun onMessageReceive(channelName: String, publisherId: String, message: String) {
            this@AUIBaseCollection.onMessageReceive(publisherId, message)
        }
    }

    private val attributeRespObserver = object : AUIRtmAttributeRespObserver {
        override fun onAttributeChanged(channelName: String, key: String, value: Any) {
            if (this@AUIBaseCollection.channelName != channelName || key != observeKey) {
                return
            }
            this@AUIBaseCollection.onAttributeChanged(value)
        }
    }

    protected var metadataWillAddClosure: ((
        publisherId: String, valueCmd: String?, value: Map<String, Any>
    ) -> AUICollectionException?)? = null

    protected var metadataWillUpdateClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUICollectionException?)? = null

    protected var metadataWillMergeClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUICollectionException?)? = null

    protected var metadataWillRemoveClosure: ((
        publisherId: String, valueCmd: String?, value: Map<String, Any>
    ) -> AUICollectionException?)? = null

    protected var metadataWillCalculateClosure: ((
        publisherId: String, valueCmd: String?, value: Map<String, Any>, cKey: List<String>, cValue: Int, cMin: Int, cMax: Int
    ) -> AUICollectionException?)? = null

    protected var attributesDidChangedClosure: ((
        channelName: String, observeKey: String, value: AUIAttributesModel
    ) -> Unit)? = null

    protected var attributesWillSetClosure: ((
        channelName: String, observeKey: String, valueCmd: String?, value: AUIAttributesModel
    ) -> AUIAttributesModel)? = null


    init {
        rtmManager.subscribeMessage(messageRespObserver)
        rtmManager.subscribeAttribute(channelName, observeKey, attributeRespObserver)
    }

    final override fun release() {
        rtmManager.unsubscribeMessage(messageRespObserver)
        rtmManager.unsubscribeAttribute(
            channelName, observeKey,
            attributeRespObserver
        )
    }

    final override fun subscribeWillAdd(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUICollectionException?)?) {
        metadataWillAddClosure = closure
    }

    final override fun subscribeWillUpdate(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUICollectionException?)?) {
        metadataWillUpdateClosure = closure
    }

    final override fun subscribeWillMerge(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUICollectionException?)?) {
        metadataWillMergeClosure = closure
    }

    final override fun subscribeWillRemove(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUICollectionException?)?) {
        metadataWillRemoveClosure = closure
    }

    final override fun subscribeAttributesDidChanged(closure: ((channelName: String, observeKey: String, value: AUIAttributesModel) -> Unit)?) {
        attributesDidChangedClosure = closure
    }

    final override fun subscribeAttributesWillSet(closure: ((channelName: String, observeKey: String, valueCmd: String?, value: AUIAttributesModel) -> AUIAttributesModel)?) {
        attributesWillSetClosure = closure
    }

    override fun subscribeWillCalculate(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>, cKey: List<String>, cValue: Int, cMin: Int, cMax: Int) -> AUICollectionException?)?) {
        metadataWillCalculateClosure = closure
    }

    protected fun localUid() = AUIRoomContext.shared().currentUserInfo.userId

    protected fun arbiterUid() =
        AUIRoomContext.shared().getArbiter(channelName)?.lockOwnerId() ?: ""

    protected fun isArbiter() =
        AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() ?: false

    protected fun sendReceipt(publisherId: String, uniqueId: String, error: AUICollectionException?) {
        val collectionError = AUICollectionError(error?.code ?: 0, error?.message ?: "")
        val message = AUICollectionMessage(
            channelName = channelName,
            messageType = AUICollectionMessageTypeReceipt,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                dataCmd = "",
                data = GsonTools.beanToMap(collectionError),
            )
        )
        val jsonStr = GsonTools.beanToString(message) ?: return

        rtmManager.publish(channelName, publisherId, jsonStr) {}
    }

    protected abstract fun onMessageReceive(publisherId: String, message: String)

    protected abstract fun onAttributeChanged(value: Any)
}