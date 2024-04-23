package io.agora.rtmsyncmanager.service.collection

import com.google.gson.reflect.TypeToken
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.GsonTools
import java.util.UUID

class AUIMapCollection(
    val channelName: String,
    val observeKey: String,
    val rtmManager: AUIRtmManager
) : AUIBaseCollection(channelName, observeKey, rtmManager) {

    private var currentMap: Map<String, Any> = mutableMapOf()

    private fun updateCurrentListAndNotify(map: Map<String, Any>, needNotify: Boolean) {
        if (!needNotify) return
        currentMap = map
        attributesDidChangedClosure?.invoke(channelName, observeKey, AUIAttributesModel(map))
    }

    override fun getMetaData(callback: ((error: AUICollectionException?, value: Any?) -> Unit)?) {
        rtmManager.getMetadata(
            channelName = channelName,
            completion = { error, metaData ->
                if (error != null) {
                    callback?.invoke(AUICollectionException.ErrorCode.unknown.toException(), null)
                    return@getMetadata
                }
                val data = metaData?.items?.find { it.key == observeKey }
                if (data == null) {
                    callback?.invoke(null, null)
                    return@getMetadata
                }

                val map = GsonTools.toBean<Map<String, Any>>(
                    data.value,
                    object : TypeToken<Map<String, Any>>() {}.type
                )
                if (map == null) {
                    callback?.invoke(
                        AUICollectionException.ErrorCode.encodeToJsonStringFail.toException(),
                        null
                    )
                    return@getMetadata
                }

                callback?.invoke(null, map)
            }
        )
    }

    override fun updateMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        if (isArbiter()) {
            rtmUpdateMetaData(localUid(), valueCmd, value, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                dataCmd = valueCmd,
                data = value
            )
        )
        val jsonStr = GsonTools.beanToString(data)
        if (jsonStr == null) {
            callback?.invoke(AUICollectionException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.invoke(
                    AUICollectionException.ErrorCode.recvErrorReceipt.toException()
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    override fun mergeMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        if (isArbiter()) {
            rtmMergeMetaData(localUid(), valueCmd, value, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeMerge,
                dataCmd = valueCmd,
                data = value
            )
        )
        val jsonStr = GsonTools.beanToString(data)
        if (jsonStr == null) {
            callback?.invoke(AUICollectionException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.invoke(AUICollectionException.ErrorCode.recvErrorReceipt.toException("$error"))
            } else {
                callback?.invoke(null)
            }
        }
    }

    /**
     * 添加
     *
     * @param value
     * @param callback
     */
    override fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        updateMetaData(valueCmd, value, filter, callback)
    }

    override fun removeMetaData(
        valueCmd: String?,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        callback?.invoke(AUICollectionException.ErrorCode.unsupportedAction.toException())
    }

    override fun calculateMetaData(
        valueCmd: String?,
        key: List<String>,
        value: Int,
        min: Int,
        max: Int,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        if (isArbiter()) {
            rtmCalculateMetaData(
                localUid(),
                valueCmd,
                key,
                AUICollectionCalcValue(value, min, max),
                callback
            )
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeCalculate,
                dataCmd = valueCmd,
                data = GsonTools.beanToMap(
                    AUICollectionCalcData(
                        key,
                        AUICollectionCalcValue(value, min, max)
                    )
                )
            )
        )
        val jsonStr = GsonTools.beanToString(data)
        if (jsonStr == null) {
            callback?.invoke(AUICollectionException.ErrorCode.calculateMapFail.toException())
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.invoke(
                    AUICollectionException.ErrorCode.recvErrorReceipt.toException("$error")
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    override fun cleanMetaData(callback: ((error: AUICollectionException?) -> Unit)?) {
        if (isArbiter()) {
            rtmCleanMetaData(callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeClean,
                dataCmd = null,
                data = null
            )
        )
        val jsonStr = GsonTools.beanToString(data)
        if (jsonStr == null) {
            callback?.invoke(AUICollectionException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.invoke(
                    AUICollectionException.ErrorCode.recvErrorReceipt.toException("$error")
                )
            } else {
                callback?.invoke(null)
            }
        }
    }

    private fun rtmUpdateMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        val error =
            metadataWillUpdateClosure?.invoke(publisherId, valueCmd, value, HashMap(currentMap))
        if (error != null) {
            callback?.invoke(error)
            return
        }

        val map = HashMap(currentMap)
        value.forEach { (k, v) ->
            map[k] = v
        }
        val retMap =
            attributesWillSetClosure?.invoke(
                channelName,
                observeKey,
                valueCmd,
                AUIAttributesModel(map)
            )?.getMap()
                ?: map
        val data = GsonTools.beanToString(retMap)
        if (data == null) {
            callback?.invoke(AUICollectionException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.invoke(
                    AUICollectionException.ErrorCode.unknown.toException("rtm setBatchMetadata error: $e")
                )
            } else {
                callback?.invoke(null)
            }
        }
        updateCurrentListAndNotify(map, true)
    }

    private fun rtmMergeMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        val error =
            metadataWillMergeClosure?.invoke(publisherId, valueCmd, value, HashMap(currentMap))
        if (error != null) {
            callback?.invoke(error)
            return
        }

        val map = AUICollectionUtils.mergeMap(currentMap, value)
        val retMap =
            attributesWillSetClosure?.invoke(
                channelName,
                observeKey,
                valueCmd,
                AUIAttributesModel(map)
            )?.getMap()
                ?: map
        val data = GsonTools.beanToString(retMap)
        if (data == null) {
            callback?.invoke(AUICollectionException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.invoke(
                    AUICollectionException.ErrorCode.unknown.toException("rtm setBatchMetadata error: $e")
                )
            } else {
                callback?.invoke(null)
            }
        }
        updateCurrentListAndNotify(map, true)
    }

    private fun rtmCalculateMetaData(
        publisherId: String,
        valueCmd: String?,
        key: List<String>,
        value: AUICollectionCalcValue,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        val currMap = HashMap(currentMap)
        val err = metadataWillCalculateClosure?.invoke(
            publisherId,
            valueCmd,
            currMap,
            key,
            value.value,
            value.min,
            value.max
        )
        if (err != null) {
            callback?.invoke(err)
            return
        }

        val map = AUICollectionUtils.calculateMap(
            currMap,
            key,
            value.value,
            value.min,
            value.max
        ) ?: mutableMapOf()
        val retMap =
            attributesWillSetClosure?.invoke(
                channelName,
                observeKey,
                valueCmd,
                AUIAttributesModel(map)
            )?.getMap()
                ?: map
        val data = GsonTools.beanToString(retMap)
        if (data == null) {
            callback?.invoke(AUICollectionException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.invoke(
                    AUICollectionException.ErrorCode.unknown.toException("rtm setBatchMetadata error: $e")
                )
            } else {
                callback?.invoke(null)
            }
        }
        updateCurrentListAndNotify(map, true)
    }

    private fun rtmCleanMetaData(callback: ((error: AUICollectionException?) -> Unit)?) {
        rtmManager.cleanBatchMetadata(
            channelName = channelName,
            remoteKeys = listOf(observeKey),
            fetchImmediately = false,
            completion = { error ->
                if (error != null) {
                    callback?.invoke(AUICollectionException.ErrorCode.unknown.toException("rtm rtmCleanMetaData error: $error"))
                } else {
                    callback?.invoke(null)
                }
            }
        )
    }

    override fun onAttributeChanged(value: Any) {
        val strValue = value as? String ?: return

        val map = GsonTools.toBean<Map<String, Any>>(
            strValue,
            object : TypeToken<Map<String, Any>>() {}.type
        ) ?: return

        updateCurrentListAndNotify(map, !isArbiter())
    }

    override fun onMessageReceive(publisherId: String, message: String) {
        val messageModel = GsonTools.toBean(message, AUICollectionMessage::class.java) ?: return

        val uniqueId = messageModel.uniqueId
        if (uniqueId == null
            || messageModel.channelName != channelName
            || messageModel.sceneKey != observeKey
        ) {
            return
        }

        if (messageModel.messageType == AUICollectionMessageTypeReceipt) {
            // receipt message from arbiter
            val collectionError = GsonTools.toBean(
                GsonTools.beanToString(messageModel.payload?.data),
                AUICollectionError::class.java
            )
            if (collectionError == null) {
                rtmManager.markReceiptFinished(
                    uniqueId, AUIRtmException(
                        -1, "data is not a map", "receipt message"
                    )
                )
                return
            }

            val code = collectionError.code
            val reason = collectionError.reason
            if (code == 0) {
                // success
                rtmManager.markReceiptFinished(uniqueId, null)
            } else {
                // failure
                rtmManager.markReceiptFinished(
                    uniqueId, AUIRtmException(
                        code,
                        reason,
                        "receipt message from arbiter"
                    )
                )
            }
            return
        }
        val updateType = messageModel.payload?.type
        if (updateType == null) {
            sendReceipt(publisherId, uniqueId, AUICollectionException.ErrorCode.updateTypeNotFound.toException())
            return
        }
        val valueCmd = messageModel.payload.dataCmd
        var error: AUICollectionException? = null
        when (updateType) {
            AUICollectionOperationTypeAdd, AUICollectionOperationTypeUpdate, AUICollectionOperationTypeMerge -> {
                val data = messageModel.payload.data
                if (data != null) {
                    if (updateType == AUICollectionOperationTypeMerge) {
                        rtmMergeMetaData(publisherId, valueCmd, data) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    } else {
                        rtmUpdateMetaData(publisherId, valueCmd, data) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    }

                } else {
                    error = AUICollectionException.ErrorCode.invalidPayloadType.toException()
                }
            }

            AUICollectionOperationTypeClean -> {
                rtmCleanMetaData {
                    sendReceipt(publisherId, uniqueId, it)
                }
            }

            AUICollectionOperationTypeRemove -> {
                error = AUICollectionException.ErrorCode.updateTypeNotFound.toException()
            }

            AUICollectionOperationTypeCalculate -> {
                val calcData = GsonTools.toBean(
                    GsonTools.beanToString(messageModel.payload.data),
                    AUICollectionCalcData::class.java
                )
                if (calcData != null) {
                    rtmCalculateMetaData(
                        publisherId,
                        valueCmd,
                        calcData.key,
                        calcData.value
                    ) {
                        sendReceipt(publisherId, uniqueId, it)
                    }
                } else {
                    error = AUICollectionException.ErrorCode.invalidPayloadType.toException()
                }
            }
        }

        if (error != null) {
            sendReceipt(
                publisherId,
                uniqueId,
                error
            )
        }
    }

}