package io.agora.scene.playzone.service

import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.component.AgoraApplication

/**
 * Play zone service listener protocol
 *
 * @constructor Create empty Play zone service listener protocol
 */
interface PlayZoneServiceListenerProtocol {
    /**
     * Room expired
     *
     */
    fun onRoomExpire() {}

    /**
     * Room destroyed
     *
     */
    fun onRoomDestroy() {}

    /**
     * Robot list
     *
     * @param robotMap
     */
    fun onRobotMapSnapshot(robotMap: Map<String, PlayRobotInfo>) {}

    /**
     * Room user count update
     *
     * @param userCount
     */
    fun onUserCountUpdate(userCount: Int) {}
}

/**
 * Play zone service protocol
 *
 * @constructor Create empty Play zone service protocol
 */
interface PlayZoneServiceProtocol {

    companion object {
        private var innerProtocol: PlayZoneServiceProtocol? = null

        val serviceProtocol: PlayZoneServiceProtocol
            get() {
                if (innerProtocol == null) {
                    innerProtocol = PlaySyncManagerServiceImp(AgoraApplication.the())
                }
                return innerProtocol!!
            }

        @Synchronized
        fun destroy() {
            (innerProtocol as? PlaySyncManagerServiceImp)?.destroy()
            innerProtocol = null
        }
    }

    /**
     * Get room remaining time
     *
     * @param roomId
     * @return
     */
    fun getCurrentDuration(roomId: String): Long

    /**
     * Get room current timestamp
     *
     * @param roomId
     * @return
     */
    fun getCurrentTs(roomId: String): Long

    /**
     * Get room list
     *
     * @param completion
     * @receiver
     */
    fun getRoomList(completion: (error: Exception?, roomList: List<AUIRoomInfo>?) -> Unit)

    /**
     * Create room
     *
     * @param inputModel
     * @param completion
     * @receiver
     */
    fun createRoom(inputModel: PlayCreateRoomModel, completion: (error: Exception?, result: AUIRoomInfo?) -> Unit)

    /**
     * Join room
     *
     * @param roomId
     * @param completion
     * @receiver
     */
    fun joinRoom(roomId: String, password: String?, completion: (error: Exception?) -> Unit)

    /**
     * Leave room
     *
     * @param completion
     * @receiver
     */
    fun leaveRoom(completion: (error: Exception?) -> Unit)

    /**
     * Subscribe listener
     *
     * @param listener
     */
    fun subscribeListener(listener: PlayZoneServiceListenerProtocol)

    /**
     * Unsubscribe listener
     *
     * @param listener
     */
    fun unsubscribeListener(listener: PlayZoneServiceListenerProtocol)
}