package io.agora.rtmsyncmanager

import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.utils.GsonTools

fun AUIMapCollection.initMetaData(channelName: String, metadata: Map<String, String>, completion: (AUIRtmException?) -> Unit) {
    val data = GsonTools.beanToString(metadata)
    if (data == null) {
        completion.invoke(AUIRtmException(-1, "encodeToJsonStringFail", ""))
        return
    }
    rtmManager.setBatchMetadata(
        channelName = channelName,
        lockName = "",
        metadata = mapOf(Pair(observeKey, data)),
        fetchImmediately = true,
        completion = completion)
}