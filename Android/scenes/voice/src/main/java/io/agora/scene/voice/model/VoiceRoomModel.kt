package io.agora.scene.voice.model

import androidx.annotation.DrawableRes
import io.agora.scene.voice.model.annotation.MicClickAction
import io.agora.scene.voice.global.ConfigConstants
import java.io.Serializable

interface BaseRoomBean : IKeepProguard, Serializable {
}

/**
 * Robot
 */
data class BotMicInfoBean constructor(
    var blueBot: VoiceMicInfoModel,
    var redBot: VoiceMicInfoModel
) : BaseRoomBean

/**
 * Mic management
 */
data class MicManagerBean constructor(
    val name: String,
    var enable: Boolean = true,
    @MicClickAction var micClickAction: Int = MicClickAction.Invite
) : BaseRoomBean

/**
 * Audio settings
 */
data class RoomAudioSettingsBean constructor(
    var enable: Boolean = true, // Whether clickable
    var roomType: Int = 0,
    var botOpen: Boolean = false,
    var botVolume: Int = ConfigConstants.RotDefaultVolume,
    var soundSelection: Int = ConfigConstants.SoundSelection.Social_Chat,
    var AINSMode: Int = ConfigConstants.AINSMode.AINS_Tradition_Weakness, // Noise reduction
    var AINSMusicMode: Int = ConfigConstants.AINSMode.AINS_Off, // Music protection
    var AINSMicMode: Int = ConfigConstants.AINSMode.AINS_Off, // Voice protection
    var spatialOpen: Boolean = false,
    var isAIAECOn: Boolean = false,
    var isAIAGCOn: Boolean = false,
    var isEarBckOn: Boolean = false,
    var voiceChangerMode: Int = 0,
) : BaseRoomBean

enum class AINSType {
    AINS_Default, // Noise reduction
    AINS_Music, // Music protection
    AINS_Mic, // Voice protection
}

/**
 * Noise reduction mode
 */
data class AINSModeBean constructor(
    val type: AINSType = AINSType.AINS_Default,
    val anisName: String = "",
    var anisMode: Int = ConfigConstants.AINSMode.AINS_Medium // Default
) : BaseRoomBean {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AINSModeBean) return false
        return type == other.type
    }

    override fun hashCode(): Int {
        var result = type.ordinal
        result = 31 * result + anisName.hashCode()
        return result
    }
}

/**
 * Noise reduction selection
 */
data class AINSSoundsBean constructor(
    val soundType: Int = ConfigConstants.AINSSoundType.AINS_TVSound,
    val soundName: String = "",
    val soundSubName: String = "",
    var soundMode: Int = ConfigConstants.AINSMode.AINS_Unknown
) : BaseRoomBean

/**
 * Voice chat script
 */
data class SoundAudioBean constructor(
    val speakerType: Int, // Sound effect playback type
    var soundId: Int,
    var audioUrl: String, // Voice chat URL
    var audioUrlHigh: String = "", // Voice chat URL high noise reduction
    var audioUrlMedium: String = "", // Voice chat URL medium noise reduction
) : BaseRoomBean

/**
 * Best sound effect introduction
 */
data class SoundSelectionBean constructor(
    val soundSelectionType: Int = ConfigConstants.SoundSelection.Social_Chat,
    val index: Int = 0,
    val soundName: String = "",
    val soundIntroduce: String = "",
    var isCurrentUsing: Boolean = false,
    val customer: List<CustomerUsageBean>? = null
) : BaseRoomBean

data class CustomerUsageBean constructor(
    val name: String? = "",
    @DrawableRes val avatar: Int = 0
) : BaseRoomBean