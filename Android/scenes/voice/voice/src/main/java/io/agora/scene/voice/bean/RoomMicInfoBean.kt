package io.agora.scene.voice.bean

import io.agora.scene.voice.service.VoiceMicInfoModel
import io.agora.secnceui.annotation.MicClickAction

data class BotMicInfoBean constructor(
    var blueBot: VoiceMicInfoModel,
    var redBot: VoiceMicInfoModel
) : BaseRoomBean

data class MicManagerBean constructor(
    val name: String,
    var enable: Boolean = true,
    @MicClickAction var micClickAction: Int = MicClickAction.Invite
) : BaseRoomBean