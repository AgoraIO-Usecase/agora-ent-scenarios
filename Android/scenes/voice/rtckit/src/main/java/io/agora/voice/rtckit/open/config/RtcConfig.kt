package io.agora.voice.rtckit.open.config

import io.agora.voice.rtckit.annotation.SoundSelection

/**初始化*/
data class RtcInitConfig constructor(var appId: String? = null)

/**频道配置*/
data class RtcChannelConfig constructor(
    var appToken: String,
    var roomId: String = "",
    var userId: Int = 0,
    var audioEnabled: Boolean = true,
    var broadcaster: Boolean = true,
    @SoundSelection var soundType: Int = SoundSelection.SocialChat,

    )