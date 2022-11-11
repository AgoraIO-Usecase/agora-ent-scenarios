package io.agora.voice.rtckit.internal.impl

import io.agora.rtc2.RtcEngineEx
import io.agora.voice.rtckit.internal.base.RtcBaseDeNoiseEngine

/**
 * @author create by zhangwei03
 *
 * AI 降噪管理
 */
internal class AgoraRtcDeNoiseEngine : RtcBaseDeNoiseEngine<RtcEngineEx>() {

    override fun closeDeNoise(): Boolean {
        engine?.apply {
            setParameters("{\"che.audio.ains_mode\":0}")
            setParameters("{\"che.audio.nsng.lowerBound\":80}")
            setParameters("{\"che.audio.nsng.lowerMask\":50}")
            setParameters("{\"che.audio.nsng.statisticalbound\":5}")
            setParameters("{\"che.audio.nsng.finallowermask\":30}")
            setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
        }
        return true
    }

    override fun openMediumDeNoise(): Boolean {
        engine?.apply {
            setParameters("{\"che.audio.ains_mode\":2}")
            setParameters("{\"che.audio.nsng.lowerBound\":80}")
            setParameters("{\"che.audio.nsng.lowerMask\":50}")
            setParameters("{\"che.audio.nsng.statisticalbound\":5}")
            setParameters("{\"che.audio.nsng.finallowermask\":30}")
            setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
        }
        return true
    }

    override fun openHeightDeNoise(): Boolean {
        engine?.apply {
            setParameters("{\"che.audio.ains_mode\":2}")
            setParameters("{\"che.audio.nsng.lowerBound\":10}")
            setParameters("{\"che.audio.nsng.lowerMask\":10}")
            setParameters("{\"che.audio.nsng.statisticalbound\":0}")
            setParameters("{\"che.audio.nsng.finallowermask\":8}")
            setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
        }
        return true
    }
}