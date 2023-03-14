package io.agora.scene.voice.rtckit

import io.agora.voice.common.constant.ConfigConstants

/**当前频道数据*/
data class RtcChannelTemp constructor(
    var broadcaster: Boolean = true, // 是否是主播模式
    var firstActiveBot: Boolean = true,//第一次启动机器，播放
    var firstSwitchAnis: Boolean = true,// 第一次切换ai 降噪
    var AINSMode: Int = ConfigConstants.AINSMode.AINS_Medium,// 降噪模式
    var isAIAECOn: Boolean = false, // 回声消除
    var isAIAGCOn: Boolean = false, // 人声增强
) {
    fun reset() {
        broadcaster = true
        firstActiveBot = true
        firstSwitchAnis = true
        AINSMode = ConfigConstants.AINSMode.AINS_Medium
        isAIAECOn = false
        isAIAGCOn = false
    }
}