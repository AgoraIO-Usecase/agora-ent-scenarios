package io.agora.scene.voice.spatial.model

import androidx.annotation.DrawableRes
import io.agora.scene.voice.spatial.global.ConfigConstants
import io.agora.scene.voice.spatial.model.annotation.MicClickAction
import java.io.Serializable

interface BaseRoomBean : IKeepProguard, Serializable {
}

/**
 * Room initialization properties, will not change
 */
data class RoomKitBean constructor(
    var roomId: String = "",
    var channelId: String = "",
    var chatroomId: String = "",
    var ownerId: String = "",
    var ownerChatUid: String = "",
    var isOwner: Boolean = false,
    var soundEffect: Int = ConfigConstants.SoundSelection.Social_Chat
) : Serializable

/**
 * Robot
 */
data class BotMicInfoBean constructor(
    var blueBot: VoiceMicInfoModel,
    var redBot: VoiceMicInfoModel
) : BaseRoomBean

/**
 * Seat management
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
    var enable: Boolean = true, // Whether to click
    var roomType: Int = 0,
    var botOpen: Boolean = false,
    var botVolume: Int = ConfigConstants.RotDefaultVolume,
    var soundSelection: Int = ConfigConstants.SoundSelection.Social_Chat,
    var AINSMode: Int = ConfigConstants.AINSMode.AINS_Medium,
    var spatialOpen: Boolean = false,
    var isAIAECOn: Boolean = false,
    var isAIAGCOn: Boolean = false,
    var voiceChangerMode: Int = 0,
) : BaseRoomBean

/**
 * Noise reduction mode
 */
data class AINSModeBean constructor(
    val anisName: String = "",
    var anisMode: Int = ConfigConstants.AINSMode.AINS_Medium // Default
) : BaseRoomBean

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
    val speakerType: Int, // Audio effect playback type
    var soundId: Int,
    var audioUrl: String, // Voice chat URL
    var audioUrlHigh: String = "", // Voice chat URL high noise reduction
    var audioUrlMedium: String = "", // Voice chat URL medium noise reduction
) : BaseRoomBean

/**
 * Best audio effect introduction
 */
data class SoundSelectionBean constructor(
    val soundSelectionType: Int = ConfigConstants.SoundSelection.Social_Chat,
    val index: Int = 0,
    val soundName: String = "",
    val soundIntroduce: String = "",
    var isCurrentUsing: Boolean = false,
    val customer: List<CustomerUsageBean>? = null
) :BaseRoomBean

data class CustomerUsageBean constructor(
    val name: String? = "",
    @DrawableRes val avatar: Int = 0
) : BaseRoomBean