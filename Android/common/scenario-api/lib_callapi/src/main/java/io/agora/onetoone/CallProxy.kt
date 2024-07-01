package io.agora.onetoone

import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler

class CallLocalFirstFrameProxy(
    private val handler : IRtcEngineEventHandler? = null
): IRtcEngineEventHandler() {

    override fun onFirstLocalVideoFrame(
        source: Constants.VideoSourceType?,
        width: Int,
        height: Int,
        elapsed: Int
    ) {
        super.onFirstLocalVideoFrame(source, width, height, elapsed)
        handler?.onFirstLocalVideoFrame(source, width, height, elapsed)
    }
}