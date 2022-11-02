package io.agora.scene.voice.bean

import io.agora.voice.buddy.config.ConfigConstants

data class AINSModeBean constructor(
    val anisName: String = "",
    var anisMode: Int = ConfigConstants.AINSMode.AINS_Medium // 默认
) : io.agora.scene.voice.bean.BaseRoomBean

data class AINSSoundsBean constructor(
    val soundType: Int = ConfigConstants.AINSSoundType.AINS_TVSound,
    val soundName: String = "",
    val soundSubName: String = "",
    var soundMode: Int = ConfigConstants.AINSMode.AINS_Unknown
) : io.agora.scene.voice.bean.BaseRoomBean