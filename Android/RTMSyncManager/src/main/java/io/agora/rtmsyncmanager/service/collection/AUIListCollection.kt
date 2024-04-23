package io.agora.rtmsyncmanager.service.collection

import android.util.Log
import com.google.gson.reflect.TypeToken
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.GsonTools
import java.util.UUID

class AUIListCollection(
    private val channelName: String,
    private val observeKey: String,
    private val rtmManager: AUIRtmManager
) : AUIBaseCollection(channelName, observeKey, rtmManager) {

    private var currentList = listOf<Map<String, Any>>()

    private fun updateCurrentListAndNotify(list: List<Map<String, Any>>, needNotify: Boolean) {
        if (!needNotify) return
        currentList = list
        attributesDidChangedClosure?.invoke(channelName, observeKey, AUIAttributesModel(list))
    }

    override fun getMetaData(callback: ((error: AUICollectionException?, value: Any?) -> Unit)?) {
        rtmManager.getMetadata(
            channelName = channelName,
            completion = { error, metaData ->
                if (error != null) {
                    callback?.invoke(AUICollectionException.ErrorCode.unknown.toException("rtm getMetadata error: $error"), null)
                    return@getMetadata
                }
                val data = metaData?.items?.find { it.key == observeKey }
                if (data == null) {
                    callback?.invoke(null, null)
                    return@getMetadata
                }

                val list = GsonTools.toBean<List<Map<String, Any>>>(
                    data.value,
                    object : TypeToken<List<Map<String, Any>>>() {}.type
                )
                if (list == null) {
                    callback?.invoke(
                        AUICollectionException.ErrorCode.encodeToJsonStringFail.toException(),
                        null
                    )
                    return@getMetadata
                }

                callback?.invoke(null, list)
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
            rtmUpdateMetaData(localUid(), valueCmd, value, filter, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                dataCmd = valueCmd,
                data = value,
                filter = filter
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
            rtmMergeMetaData(localUid(), valueCmd, value, filter, callback)
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
                data = value,
                filter = filter
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

    override fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        if (isArbiter()) {
            rtmAddMetaData(localUid(), valueCmd, value, filter, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeAdd,
                dataCmd = valueCmd,
                data = value,
                filter = filter
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

    override fun removeMetaData(
        valueCmd: String?,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        if (isArbiter()) {
            rtmRemoveMetaData(localUid(), valueCmd, filter, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeRemove,
                dataCmd = valueCmd,
                data = null,
                filter = filter
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
                filter,
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
                filter = filter,
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
                dataCmd = "",
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
                    AUICollectionException.ErrorCode.removeMetaDataFail.toException()
                )
            } else {
                callback?.invoke(null)
            }
        }
    }


    private fun rtmAddMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        //如果filter空，默认无条件写入，如果有filter，判断条件
        if (filter != null &&
            filter.isNotEmpty() &&
            AUICollectionUtils.getItemIndexes(currentList, filter)?.isNotEmpty() == true) {
            callback?.invoke(
                AUICollectionException.ErrorCode.encodeToJsonStringFail.toException()
            )
            return
        }

        val error = metadataWillAddClosure?.invoke(publisherId, valueCmd, value)
        if (error != null) {
            callback?.invoke(error)
            return
        }

        val list = ArrayList(currentList)
        list.add(value)
        val retList =
            attributesWillSetClosure?.invoke(
                channelName,
                observeKey,
                valueCmd,
                AUIAttributesModel(list)
            )?.getList()
                ?: list

        val data = GsonTools.beanToString(retList)
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
        updateCurrentListAndNotify(list, true)
    }

    private fun rtmUpdateMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        val itemIndexes = AUICollectionUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.invoke(
                AUICollectionException.ErrorCode.filterNotFound.toException()
            )
            return
        }
        val list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillUpdateClosure?.invoke(publisherId, valueCmd, value, item)
            if (error != null) {
                callback?.invoke(error)
                return
            }

            val tempItem = HashMap(item)
            value.forEach { key, value ->
                tempItem[key] = value
            }
            list[itemIdx] = tempItem
        }
        val retList =
            attributesWillSetClosure?.invoke(
                channelName,
                observeKey,
                valueCmd,
                AUIAttributesModel(list)
            )?.getList()
                ?: list

        val data = GsonTools.beanToString(retList)
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
        updateCurrentListAndNotify(list, true)
    }

    private fun rtmMergeMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        val itemIndexes = AUICollectionUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.invoke(
                AUICollectionException.ErrorCode.filterNotFound.toException()
            )
            return
        }
        val list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillMergeClosure?.invoke(publisherId, valueCmd, value, item)
            if (error != null) {
                callback?.invoke(error)
                return
            }

            val tempItem = AUICollectionUtils.mergeMap(item, value)
            list[itemIdx] = tempItem
        }
        val retList =
            attributesWillSetClosure?.invoke(
                channelName,
                observeKey,
                valueCmd,
                AUIAttributesModel(list)
            )?.getList()
                ?: list
        val data = GsonTools.beanToString(retList)
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
        updateCurrentListAndNotify(list, true)
    }

    private fun rtmRemoveMetaData(
        publisherId: String,
        valueCmd: String?,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {
        val itemIndexes = AUICollectionUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.invoke(
                AUICollectionException.ErrorCode.filterNotFound.toException()
            )
            return
        }
        var list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillRemoveClosure?.invoke(publisherId, valueCmd, item)
            if (error != null) {
                callback?.invoke(error)
                return
            }
        }


        val filterList = list.filter { !itemIndexes.contains(list.indexOf(it)) }
        list = ArrayList(filterList)
        val retList =
            attributesWillSetClosure?.invoke(
                channelName,
                observeKey,
                valueCmd,
                AUIAttributesModel(list)
            )?.getList()
                ?: list
        val data = GsonTools.beanToString(retList)
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
        updateCurrentListAndNotify(list, true)
    }

    private fun rtmCalculateMetaData(
        publisherId: String,
        valueCmd: String?,
        key: List<String>,
        value: AUICollectionCalcValue,
        filter: List<Map<String, Any>>?,
        callback: ((error: AUICollectionException?) -> Unit)?
    ) {

        val itemIndexes = AUICollectionUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.invoke(AUICollectionException.ErrorCode.filterNotFound.toException())
            return
        }

        val list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillCalculateClosure?.invoke(
                publisherId, valueCmd, item,
                key, value.value, value.min, value.max
            )
            if (error != null) {
                callback?.invoke(error)
                return
            }
            val calcItem = AUICollectionUtils.calculateMap(
                item,
                key,
                value.value,
                value.min,
                value.max
            )
            Log.d("hiut", "AUICollectionUtils.calculateMap calcItem:$calcItem")
            if (calcItem == null) {
                callback?.invoke(
                    AUICollectionException.ErrorCode.calculateMapFail.toException()
                )
                return
            }
            list[itemIdx] = calcItem
        }

        val retList =
            attributesWillSetClosure?.invoke(
                channelName,
                observeKey,
                valueCmd,
                AUIAttributesModel(list)
            )?.getList()
                ?: list
        val data = GsonTools.beanToString(retList)
        if (data == null) {
            callback?.invoke(AUICollectionException.ErrorCode.encodeToJsonStringFail.toException())
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.invoke(AUICollectionException.ErrorCode.unknown.toException("rtm setBatchMetadata error: $e"))
            } else {
                callback?.invoke(null)
            }
        }
        updateCurrentListAndNotify(list, true)
    }

    private fun rtmCleanMetaData(callback: ((error: AUICollectionException?) -> Unit)?) {
        rtmManager.cleanBatchMetadata(
            channelName = channelName,
            remoteKeys = listOf(observeKey),
            completion = { error ->
                if (error != null) {
                    callback?.invoke(AUICollectionException.ErrorCode.removeMetaDataFail.toException())
                } else {
                    callback?.invoke(null)
                }
            }
        )
    }

    override fun onAttributeChanged(value: Any) {
        val strValue = value as? String ?: return
        val list = GsonTools.toBean<List<Map<String, Any>>>(
            strValue,
            object : TypeToken<List<Map<String, Any>>>() {}.type
        ) ?: return
        //如果是仲裁者，不更新，因为本地已经修改了，否则这里收到的消息可能是老的数据，例如update1->update2->resp1->resp2，那么resp1的数据比update2要老，会造成ui上短暂的回滚
        updateCurrentListAndNotify(list, !isArbiter())
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
            Log.d("huit", "message:$message")
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
            sendReceipt(
                publisherId,
                uniqueId,
                AUICollectionException.ErrorCode.updateTypeNotFound.toException()
            )
            return
        }
        val valueCmd = messageModel.payload.dataCmd
        val filter = GsonTools.toBean<List<Map<String, Any>>>(
            GsonTools.beanToString(messageModel.payload.filter),
            object : TypeToken<List<Map<String, Any>>>() {}.type
        )
        var error: AUICollectionException? = null
        when (updateType) {
            AUICollectionOperationTypeAdd, AUICollectionOperationTypeUpdate, AUICollectionOperationTypeMerge -> {
                val data = messageModel.payload.data
                if (data != null) {
                    if (updateType == AUICollectionOperationTypeAdd) {
                        rtmAddMetaData(publisherId, valueCmd, data, filter) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    } else if (updateType == AUICollectionOperationTypeMerge) {
                        rtmMergeMetaData(publisherId, valueCmd, data, filter) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    } else {
                        rtmUpdateMetaData(publisherId, valueCmd, data, filter) {
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
                rtmRemoveMetaData(publisherId, valueCmd, filter) {
                    sendReceipt(publisherId, uniqueId, it)
                }
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
                        calcData.value,
                        filter
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