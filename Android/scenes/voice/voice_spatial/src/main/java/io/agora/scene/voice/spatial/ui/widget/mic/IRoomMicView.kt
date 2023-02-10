package io.agora.scene.voice.spatial.ui.widget.mic

import android.graphics.PointF
import io.agora.scene.voice.spatial.model.SeatPositionInfo
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel

/**
 * @author create by zhangwei03
 */
interface IRoomMicView {

    /**初始化麦位数据*/
    fun onInitMic(micInfoList: List<VoiceMicInfoModel>, isBotActive: Boolean)

    /**开关机器人*/
    fun activeBot(active: Boolean)

    /**音量指示*/
    fun updateVolume(index: Int, volume: Int)

    /**机器人音量指示
     * @return 机器人空间位置更新
     */
    fun updateBotVolume(speakerType: Int, volume: Int): Pair<PointF, PointF>?

    /**多麦位更新*/
    fun onSeatUpdated(newMicMap: Map<Int, VoiceMicInfoModel>, each: ((VoiceMicInfoModel) -> Unit)?)

    /**是否在麦位上,-1 不在*/
    fun findMicByUid(uid: String): Int

    fun myRtcUid(): Int

    /** 更新空间音频麦位位置*/
    fun updateSpatialPosition(info: SeatPositionInfo)
}