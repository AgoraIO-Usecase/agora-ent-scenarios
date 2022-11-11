package io.agora.scene.voice.bean

/**
 * @author create by zhangwei03
 *
 * 语聊脚本
 */
data class SoundAudioBean constructor(
    val speakerType: Int, // 音效播放类型，
    var soundId: Int,
    var audioUrl: String, // 语聊url
    var audioUrlHigh: String = "", // 语聊url高降噪
    var audioUrlMedium: String = "", // 语聊url中降噪
) : io.agora.scene.voice.bean.BaseRoomBean
