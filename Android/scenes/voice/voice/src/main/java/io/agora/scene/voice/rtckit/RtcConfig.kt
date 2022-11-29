package io.agora.scene.voice.rtckit

import io.agora.voice.buddy.config.ConfigConstants

/**当前频道数据*/
data class RtcChannelTemp constructor(
    var broadcaster: Boolean = true, // 是否是主播模式
    var isLocalAudioMute: Boolean = false,//本地audio 是否mute
    var firstActiveBot: Boolean = true,//第一次启动机器，播放
    var firstSwitchAnis: Boolean = true,// 第一次切换ai 降噪
    var anisMode: Int = ConfigConstants.AINSMode.AINS_Medium,// 降噪模式
) {
    fun reset() {
        broadcaster = true
        isLocalAudioMute = true
        firstActiveBot = true
        firstSwitchAnis = true
        anisMode = ConfigConstants.AINSMode.AINS_Medium
    }
}