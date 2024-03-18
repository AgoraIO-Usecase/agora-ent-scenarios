package io.agora.rtmsyncmanager.service.arbiter

import io.agora.rtmsyncmanager.service.rtm.AUIRtmException

interface AUIArbiterCallback {
    fun onArbiterDidChange(channelName: String, arbiterId: String)
    fun onError(channelName: String, error: AUIRtmException)
}