package io.agora.voice.rtckit.open.status

import io.agora.voice.rtckit.annotation.RtcNetWorkQuality

/**
 * @author create by zhangwei03
 *
 * 网络状态
 */
data class RtcNetWorkStatus(
    var userId: String = "",
    @RtcNetWorkQuality
    var txQuality: Int = RtcNetWorkQuality.QualityUnknown,    //上行网络
    @RtcNetWorkQuality
    var rxQuality: Int = RtcNetWorkQuality.QualityUnknown  //下行网络
)