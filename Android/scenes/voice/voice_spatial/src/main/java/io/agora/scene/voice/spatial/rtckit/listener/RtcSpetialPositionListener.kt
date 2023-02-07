package io.agora.scene.voice.spatial.rtckit.listener

import io.agora.scene.voice.spatial.model.SeatPositionInfo

/**
 * @author create by hezhengqing
 *
 * 远端空间位置变化监听
 */
abstract class RtcSpatialPositionListener {

    /**
     * 远端空间位置变化
     */
    abstract fun onRemoteSpatialChanged(position: SeatPositionInfo)

}