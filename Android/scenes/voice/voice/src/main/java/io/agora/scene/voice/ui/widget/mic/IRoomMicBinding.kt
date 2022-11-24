package io.agora.scene.voice.ui.widget.mic

import io.agora.scene.voice.service.VoiceMicInfoModel

/**
 * @author create by zhangwei03
 */
interface IRoomMicBinding {
    fun binding(micInfo: VoiceMicInfoModel)
}