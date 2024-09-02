package io.agora.rtmsyncmanager.service.collection

import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.service.rtm.AUIRtmAttributeRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmMessageRespObserver
import io.agora.rtmsyncmanager.utils.GsonTools


/**
 * Abstract base class for collections in the Agora RTM Sync Manager.
 * This class provides common functionality for managing collections, such as subscribing to messages and attributes.
 * @param channelName The name of the channel associated with this collection.
 * @param observeKey The key used to observe changes in the collection.
 * @param rtmManager The RTM manager used to manage RTM operations.
 */
abstract class AUIBaseCollection(
    private val channelName: String,
    private val observeKey: String,
    private val rtmManager: AUIRtmManager
) : IAUICollection {

    /**
     * Observer for handling received messages.
     */
    private val messageRespObserver = object : AUIRtmMessageRespObserver {
        override fun onMessageReceive(channelName: String, publisherId: String, message: String) {
            this@AUIBaseCollection.onMessageReceive(publisherId, message)
        }
    }

    /**
     * Observer for handling attribute changes.
     */
    private val attributeRespObserver = object : AUIRtmAttributeRespObserver {
        override fun onAttributeChanged(channelName: String, key: String, value: Any) {
            if (this@AUIBaseCollection.channelName != channelName || key != observeKey) {
                return
            }
            this@AUIBaseCollection.onAttributeChanged(value)
        }
    }

    // Various closures for handling different collection operations.
    // These closures can be set by subclasses to customize the behavior of the collection.
    protected var valueWillChangeClosure: ((
        publisherId: String, valueCmd: String?, value: Map<String, Any>
    ) -> Map<String, Any>?)? = null

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

    var retryMetadata: Boolean = false

    /**
     * Initializes the collection by subscribing to messages and attributes.
     */
    init {
        rtmManager.subscribeMessage(messageRespObserver)
        rtmManager.subscribeAttribute(channelName, observeKey, attributeRespObserver)
    }

    fun setBatchMetadata(value: String, callback: (AUIRtmException?) -> Unit) {
        rtmManager.setBatchMetadata(
            channelName = channelName,
            metadata = mapOf(observeKey to value)
        ) {
            this.retryMetadata = it != null
            callback.invoke(it)
        }
    }

    /**
     * Releases the collection by unsubscribing from messages and attributes.
     */
    final override fun release() {
        rtmManager.unsubscribeMessage(messageRespObserver)
        rtmManager.unsubscribeAttribute(
            channelName, observeKey,
            attributeRespObserver
        )
    }

    // Various methods for subscribing to different collection operations.
    // These methods are final and cannot be overridden by subclasses.
    // They are used to set the closures that handle the corresponding operations.
    final override fun subscribeValueWillChange(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> Map<String, Any>?)?) {
        valueWillChangeClosure = closure
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

    override fun getLocalMetaData(): AUIAttributesModel? {
        return null
    }

    override fun syncLocalMetaData() {

    }

    override fun subscribeWillCalculate(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>, cKey: List<String>, cValue: Int, cMin: Int, cMax: Int) -> AUICollectionException?)?) {
        metadataWillCalculateClosure = closure
    }

    protected fun localUid() = AUIRoomContext.shared().currentUserInfo.userId

    protected fun arbiterUid() =
        AUIRoomContext.shared().getArbiter(channelName)?.lockOwnerId() ?: ""

    protected fun isArbiter() =
        AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() ?: false

    /**
     * Sends a receipt message to the publisher.
     * @param publisherId The ID of the publisher.
     * @param uniqueId The unique ID of the message.
     * @param error An optional error that occurred while processing the message.
     */
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

    /**
     * Called when a message is received.
     * This method should be overridden by subclasses to handle the received message.
     * @param publisherId The ID of the publisher who sent the message.
     * @param message The received message.
     */
    protected abstract fun onMessageReceive(publisherId: String, message: String)

    /**
     * Called when an attribute is changed.
     * This method should be overridden by subclasses to handle the attribute change.
     * @param value The new value of the attribute.
     */
    protected abstract fun onAttributeChanged(value: Any)
}