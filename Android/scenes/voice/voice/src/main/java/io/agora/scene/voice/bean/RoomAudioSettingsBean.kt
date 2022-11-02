package io.agora.scene.voice.bean

import io.agora.voice.buddy.config.ConfigConstants

data class RoomAudioSettingsBean constructor(
    var enable: Boolean = true, // 是否可以点击
    var roomType: Int = 0,
    var botOpen: Boolean = false,
    var botVolume: Int = ConfigConstants.RotDefaultVolume,
    var soundSelection: Int = ConfigConstants.SoundSelection.Social_Chat,
    var anisMode: Int = ConfigConstants.AINSMode.AINS_Medium,
    var spatialOpen: Boolean = false,
) : io.agora.scene.voice.bean.BaseRoomBean

