package io.agora.scene.voice.ui.widget.mic

import io.agora.scene.voice.model.VoiceMicInfoModel

/**
 * @author create by zhangwei03
 */
interface IRoomMicView {

    /** Initialize mic position data */
    fun onInitMic(micInfoList: List<VoiceMicInfoModel>, isBotActive: Boolean)

    /** Enable/Disable robot */
    fun activeBot(active: Boolean)

    /** Volume indication */
    fun updateVolume(index: Int, volume: Int)

    /** Robot volume indication */
    fun updateBotVolume(speakerType: Int, volume: Int)

    /** Multiple mic positions update */
    fun onSeatUpdated(newMicMap: Map<Int, VoiceMicInfoModel>)

    /** Whether on mic position, -1 means not on mic */
    fun findMicByUid(uid: String): Int

    fun myRtcUid(): Int
}