package io.agora.scene.voice.model

import androidx.annotation.DrawableRes
import io.agora.scene.voice.model.annotation.MicClickAction
import io.agora.voice.common.constant.ConfigConstants
import java.io.Serializable

interface BaseRoomBean : IKeepProguard, Serializable {
}

/**
 * 机器人
 */
data class BotMicInfoBean constructor(
    var blueBot: VoiceMicInfoModel,
    var redBot: VoiceMicInfoModel
) : BaseRoomBean

/**
 * 麦位管理
 */
data class MicManagerBean constructor(
    val name: String,
    var enable: Boolean = true,
    @MicClickAction var micClickAction: Int = MicClickAction.Invite
) : BaseRoomBean

/**
 * 音效设置
 */
data class RoomAudioSettingsBean constructor(
    var enable: Boolean = true, // 是否可以点击
    var roomType: Int = 0,
    var botOpen: Boolean = false,
    var botVolume: Int = ConfigConstants.RotDefaultVolume,
    var soundSelection: Int = ConfigConstants.SoundSelection.Social_Chat,
    var AINSMode: Int = ConfigConstants.AINSMode.AINS_Tradition_Weakness, // 降噪
    var AINSMusicMode: Int = ConfigConstants.AINSMode.AINS_Off, // 音乐保护
    var AINSMicMode: Int = ConfigConstants.AINSMode.AINS_Off, // 人声保护
    var spatialOpen: Boolean = false,
    var isAIAECOn: Boolean = false,
    var isAIAGCOn: Boolean = false,
    var isEarBckOn: Boolean = false,
    var voiceChangerMode: Int = 0,
) : BaseRoomBean

enum class AINSType {
    AINS_Default, // 降噪
    AINS_Music, // 音乐保护
    AINS_Mic, // 人声保护
}

/**
 * 降噪模式
 */
data class AINSModeBean constructor(
    val type: AINSType = AINSType.AINS_Default,
    val anisName: String = "",
    var anisMode: Int = ConfigConstants.AINSMode.AINS_Medium // 默认
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
 * 降噪选择
 */
data class AINSSoundsBean constructor(
    val soundType: Int = ConfigConstants.AINSSoundType.AINS_TVSound,
    val soundName: String = "",
    val soundSubName: String = "",
    var soundMode: Int = ConfigConstants.AINSMode.AINS_Unknown
) : BaseRoomBean

/**
 * 语聊脚本
 */
data class SoundAudioBean constructor(
    val speakerType: Int, // 音效播放类型，
    var soundId: Int,
    var audioUrl: String, // 语聊url
    var audioUrlHigh: String = "", // 语聊url高降噪
    var audioUrlMedium: String = "", // 语聊url中降噪
) : BaseRoomBean

/**
 * 最佳音效介绍
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