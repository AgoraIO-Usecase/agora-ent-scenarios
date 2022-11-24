package io.agora.scene.voice.bean

import io.agora.voice.buddy.config.ConfigConstants

/**
 * 音效设置
 */
data class RoomAudioSettingsBean constructor(
    var enable: Boolean = true, // 是否可以点击
    var roomType: Int = 0,
    var botOpen: Boolean = false,
    var botVolume: Int = ConfigConstants.RotDefaultVolume,
    var soundSelection: Int = ConfigConstants.SoundSelection.Social_Chat,
    var anisMode: Int = ConfigConstants.AINSMode.AINS_Medium,
    var spatialOpen: Boolean = false,
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

