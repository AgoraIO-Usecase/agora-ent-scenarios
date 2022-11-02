package io.agora.voice.rtckit.internal.base

import io.agora.voice.rtckit.constants.RtcKitConstant
import io.agora.voice.rtckit.internal.IRtcClientListener

/**
 * @author create by zhangwei03
 *
 * base engine eg,audioEngine,audio
 */
internal abstract class RtcBaseEngine<T> {

    companion object{
        const val TAG = RtcKitConstant.RTC_TAG
    }

    protected var engine: T? = null
    protected var listener: IRtcClientListener? = null

    fun attach(client: T?, listener: IRtcClientListener?) {
        this.engine = client
        this.listener = listener
    }

    open fun detach() {
        this.engine = null
        this.listener = null
    }
}