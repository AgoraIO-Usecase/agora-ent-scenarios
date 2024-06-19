package io.agora.scene.playzone.service

import io.agora.scene.base.component.AgoraApplication

/**
 * Play zone service listener protocol
 *
 * @constructor Create empty Play zone service listener protocol
 */
interface PlayZoneServiceListenerProtocol {
    /**
     * On room expire
     *
     */
    fun onRoomExpire() {}

    /**
     * On room destroy
     *
     */
    fun onRoomDestroy() {}

}

/**
 * Play zone service protocol
 *
 * @constructor Create empty Play zone service protocol
 */
interface PlayZoneServiceProtocol {

    companion object {
        // time limit
        val ROOM_AVAILABLE_DURATION: Long = 10 * 60 * 1000 // 10min

        private var innnerProtocol: PlayZoneServiceProtocol? = null

        val serviceProtocol: PlayZoneServiceProtocol
            get() {
                if (innnerProtocol == null) {
                    innnerProtocol = PlayZoneSyncManagerServiceImp(AgoraApplication.the())
                }
                return innnerProtocol!!
            }

        fun reset() {
            innnerProtocol = null
        }
    }

}