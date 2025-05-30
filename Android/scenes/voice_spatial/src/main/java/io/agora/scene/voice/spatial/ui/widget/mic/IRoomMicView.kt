package io.agora.scene.voice.spatial.ui.widget.mic

import android.graphics.PointF
import io.agora.scene.voice.spatial.model.SeatPositionInfo
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel

/**
 * @author create by zhangwei03
 */
interface IRoomMicView {

    /**Initialize mic data*/
    fun onInitMic(micInfoList: List<VoiceMicInfoModel>, isBotActive: Boolean, complete: (() -> Unit)?)

    /**Turn on/off robot*/
    fun activeBot(active: Boolean, each: ((Int, Pair<PointF, PointF>) -> Unit)?)

    /**Volume indicator*/
    fun updateVolume(index: Int, volume: Int)

    /**Robot volume indicator
     * @return Robot spatial position update
     */
    fun updateBotVolume(speakerType: Int, volume: Int)

    /**Update multiple mic data*/
    fun onSeatUpdated(newMicMap: Map<Int, VoiceMicInfoModel>, complete: (() -> Unit)?)

    /**Whether on the mic, -1 not on*/
    fun findMicByUid(uid: String): Int

    fun myRtcUid(): Int

    /** Update spatial audio mic position UI */
    fun updateSpatialPosition(info: SeatPositionInfo)
}