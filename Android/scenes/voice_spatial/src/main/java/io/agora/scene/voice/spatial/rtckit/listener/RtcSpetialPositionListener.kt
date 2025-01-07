package io.agora.scene.voice.spatial.rtckit.listener

import io.agora.scene.voice.spatial.model.SeatPositionInfo

/**
 * @author create by hezhengqing
 *
 * Remote spatial position change listener
 */
abstract class RtcSpatialPositionListener {

    /**
     * Remote spatial position change
     */
    abstract fun onRemoteSpatialChanged(position: SeatPositionInfo)

}