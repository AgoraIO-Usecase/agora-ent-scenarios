package io.agora.scene.voice.rtckit

import io.agora.scene.voice.global.ConfigConstants

/** Current channel data */
data class RtcChannelTemp constructor(
    var broadcaster: Boolean = true, // Whether in broadcaster mode
    var firstActiveBot: Boolean = true, // First time activating robot, play
    var firstSwitchAnis: Boolean = true, // First time switching AI noise reduction
    var AINSMode: Int = ConfigConstants.AINSMode.AINS_Tradition_Weakness, // Noise reduction mode
    var AINSMusicMode: Int = ConfigConstants.AINSMode.AINS_Off, // Music protection
    var AINSMicMode: Int = ConfigConstants.AINSMode.AINS_Off, // Voice protection
    var isAIAECOn: Boolean = false, // Echo cancellation
    var isAIAGCOn: Boolean = false, // Voice enhancement
) {
    fun reset() {
        broadcaster = true
        firstActiveBot = true
        firstSwitchAnis = true
        AINSMode = ConfigConstants.AINSMode.AINS_Tradition_Weakness
        AINSMusicMode = ConfigConstants.AINSMode.AINS_Off
        AINSMicMode = ConfigConstants.AINSMode.AINS_Off
        isAIAECOn = false
        isAIAGCOn = false
    }
}