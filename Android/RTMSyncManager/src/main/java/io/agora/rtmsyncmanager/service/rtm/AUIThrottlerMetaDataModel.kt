package io.agora.rtmsyncmanager.service.rtm

class AUIThrottlerUpdateMetaDataModel {

    val throttler = AUIThrottler()

    private val _metaData = mutableMapOf<String, String>()
    val metadata : Map<String, String>
        get() = LinkedHashMap(_metaData)

    private val _callbacks = mutableListOf<(AUIRtmException?) -> Unit>()
    val callbacks : List<(AUIRtmException?) -> Unit>
        get() = ArrayList(_callbacks)

    fun appendMetaDataInfo(metadata: Map<String, String>, callback: (AUIRtmException?) -> Unit) {
        metadata.forEach { (key, value) ->
            _metaData[key] = value
        }
        _callbacks.add(callback)
    }

    fun reset() {
        _metaData.clear()
        _callbacks.clear()
        throttler.clean()
    }
}

class AUIThrottlerRemoveMetaDataModel {

    val throttler = AUIThrottler()

    private val _keys= mutableListOf<String>()
    val keys : List<String>
        get() = ArrayList(_keys)

    private val _callbacks = mutableListOf<(AUIRtmException?) -> Unit>()
    val callbacks : List<(AUIRtmException?) -> Unit>
        get() = ArrayList(_callbacks)

    fun appendMetaDataInfo(keys: List<String>, callback: (AUIRtmException?) -> Unit) {
        _keys.addAll(keys)
        _callbacks.add(callback)
    }

    fun reset() {
        _keys.clear()
        _callbacks.clear()
        throttler.clean()
    }
}