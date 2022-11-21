package io.agora.scene.voice.rtckit

import io.agora.voice.buddy.config.ConfigConstants

/**初始化*/
data class RtcInitConfig constructor(var appId: String)

/**频道配置*/
data class RtcChannelConfig constructor(
    var rtcToken: String = "",
    var channelId: String = "",
    var rtcUid: Int = 0,
    var isBroadcaster: Boolean = true,
    var soundType: Int = ConfigConstants.SoundSelection.Social_Chat
)

/**当前频道数据*/
data class RtcChannelTemp constructor(
    var broadcaster: Boolean = true, // 是否是主播模式
    var isLocalAudioMute: Boolean = false,//本地audio 是否mute
    var firstActiveBot: Boolean = true,//第一次启动机器，播放
    var firstSwitchAnis: Boolean = true,// 第一次切换ai 降噪
    var anisMode: Int = ConfigConstants.AINSMode.AINS_Medium,// 降噪模式
    var isUseBot: Boolean = false,//是否开启机器人
    var botVolume: Int = ConfigConstants.RotDefaultVolume//机器人音量
) {
    fun reset() {
        broadcaster = true
        isLocalAudioMute = true
        firstActiveBot = true
        firstSwitchAnis = true
        anisMode = ConfigConstants.AINSMode.AINS_Medium
        isUseBot = false
        botVolume = ConfigConstants.RotDefaultVolume
    }
}