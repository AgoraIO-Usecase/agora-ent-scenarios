package io.agora.rtmsyncmanager.service.rtm

/**
 * A class that handles the update of metadata in a throttled manner.
 * It uses an instance of AUIThrottler to ensure that updates are not too frequent.
 */
class AUIThrottlerUpdateMetaDataModel {

    // An instance of AUIThrottler to throttle the updates
    val throttler = AUIThrottler()

    // A mutable map to hold the metadata
    private val _metaData = mutableMapOf<String, String>()
    // An immutable view of the metadata
    val metadata : Map<String, String>
        get() = LinkedHashMap(_metaData)

    // A mutable list to hold the callbacks
    private val _callbacks = mutableListOf<(AUIRtmException?) -> Unit>()
    // An immutable view of the callbacks
    val callbacks : List<(AUIRtmException?) -> Unit>
        get() = ArrayList(_callbacks)

    /**
     * Appends metadata and a callback to the respective lists.
     * @param metadata The metadata to be appended.
     * @param callback The callback to be appended.
     */
    fun appendMetaDataInfo(metadata: Map<String, String>, callback: (AUIRtmException?) -> Unit) {
        metadata.forEach { (key, value) ->
            _metaData[key] = value
        }
        _callbacks.add(callback)
    }

    /**
     * Resets the metadata, callbacks, and throttler.
     */
    fun reset() {
        _metaData.clear()
        _callbacks.clear()
        throttler.clean()
    }
}

/**
 * A class that handles the removal of metadata in a throttled manner.
 * It uses an instance of AUIThrottler to ensure that removals are not too frequent.
 */
class AUIThrottlerRemoveMetaDataModel {

    // An instance of AUIThrottler to throttle the removals
    val throttler = AUIThrottler()

    // A mutable list to hold the keys of the metadata to be removed
    private val _keys= mutableListOf<String>()
    // An immutable view of the keys
    val keys : List<String>
        get() = ArrayList(_keys)

    // A mutable list to hold the callbacks
    private val _callbacks = mutableListOf<(AUIRtmException?) -> Unit>()
    // An immutable view of the callbacks
    val callbacks : List<(AUIRtmException?) -> Unit>
        get() = ArrayList(_callbacks)

    /**
     * Appends keys and a callback to the respective lists.
     * @param keys The keys to be appended.
     * @param callback The callback to be appended.
     */
    fun appendMetaDataInfo(keys: List<String>, callback: (AUIRtmException?) -> Unit) {
        _keys.addAll(keys.filter { !_keys.contains(it) })
        _callbacks.add(callback)
    }

    /**
     * Resets the keys, callbacks, and throttler.
     */
    fun reset() {
        _keys.clear()
        _callbacks.clear()
        throttler.clean()
    }
}