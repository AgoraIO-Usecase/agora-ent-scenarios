package io.agora.scene.voice.model

import androidx.annotation.DrawableRes
import io.agora.scene.voice.model.annotation.MicClickAction
import io.agora.voice.common.constant.ConfigConstants
import java.io.Serializable

interface BaseRoomBean : IKeepProguard, Serializable {
}

/**
 * 房间初始化属性，不会更改
 */
data class RoomKitBean constructor(
    var roomId: String = "",
    var channelId: String = "",
    var chatroomId: String = "",
    var ownerId: String = "",
    var ownerChatUid: String = "",
    var roomType: Int = ConfigConstants.RoomType.Common_Chatroom,
    var isOwner: Boolean = false,
    var soundEffect: Int = ConfigConstants.SoundSelection.Social_Chat
) : Serializable

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
    var AINSMode: Int = ConfigConstants.AINSMode.AINS_Medium,
    var spatialOpen: Boolean = false,
    var isAIAECOn: Boolean = false,
    var isAIAGCOn: Boolean = false,
    var isEarBckOn: Boolean = false,
    var voiceChangerMode: Int = 0,
) : BaseRoomBean

/**
 * 降噪模式
 */
data class AINSModeBean constructor(
    val anisName: String = "",
    var anisMode: Int = ConfigConstants.AINSMode.AINS_Medium // 默认
) : BaseRoomBean

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
) :BaseRoomBean

data class CustomerUsageBean constructor(
    val name: String? = "",
    @DrawableRes val avatar: Int = 0
) : BaseRoomBean