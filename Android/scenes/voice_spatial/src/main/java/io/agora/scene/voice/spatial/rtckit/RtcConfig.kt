package io.agora.scene.voice.spatial.rtckit

import io.agora.scene.voice.spatial.global.ConfigConstants

/**Current channel data*/
data class RtcChannelTemp constructor(
    var broadcaster: Boolean = true, // Whether it is a broadcaster mode
    var firstActiveBot: Boolean = true,//First start machine, play
    var firstSwitchAnis: Boolean = true,// First switch ai noise reduction
    var AINSMode: Int = ConfigConstants.AINSMode.AINS_Medium,// Noise reduction mode
    var isAIAECOn: Boolean = false, // Echo cancellation
    var isAIAGCOn: Boolean = false, // Voice enhancement
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