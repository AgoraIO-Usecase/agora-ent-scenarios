package io.agora.scene.joy.service

import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.scene.base.component.AgoraApplication

interface JoyServiceListenerProtocol {

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

    /**
     * User changes
     */
    fun onUserListDidChanged(userList: List<AUIUserInfo>)

    /**
     * Received new message
     */
    fun onMessageDidAdded(message: JoyMessage)

    /**
     * Game changes in the room
     */
    fun onStartGameInfoDidChanged(startGameInfo: JoyStartGameInfo)
}

interface JoyServiceProtocol {

    companion object {
        // time limit
        var ROOM_AVAILABLE_DURATION: Long = 10 * 60 * 1000 // 10min

        private var innerProtocol: JoyServiceProtocol? = null

        val serviceProtocol: JoyServiceProtocol
            get() {
                if (innerProtocol == null) {
                    innerProtocol = JoySyncManagerServiceImp(AgoraApplication.the())
                }
                return innerProtocol!!
            }

        @Synchronized
        fun destroy() {
            (innerProtocol as? JoySyncManagerServiceImp)?.destroy()
            innerProtocol = null
        }
    }

    /**
     * Get room list
     */
    fun getRoomList(completion: (error: Exception?, roomList: List<AUIRoomInfo>?) -> Unit)

    /**
     * Get remaining room time
     */
    fun getCurrentRoomDuration(roomId: String): Long

    /**
     * Get information about the game in progress
     */
    fun getStartGame(roomId: String, completion: (error: Exception?, out: JoyStartGameInfo?) -> Unit)

    /**
     * Update information about the game in progress
     */
    fun updateStartGame(roomId: String, gameInfo: JoyStartGameInfo, completion: (error: Exception?) -> Unit)

    /**
     * Create room
     */
    fun createRoom(roomName: String, completion: (error: Exception?, roomInfo: AUIRoomInfo?) -> Unit)

    /**
     * Join room
     */
    fun joinRoom(roomId: String, completion: (error: Exception?) -> Unit)

    /**
     * Leave room
     */
    fun leaveRoom(completion: (error: Exception?) -> Unit)

    /**
     * Send message
     */
    fun sendChatMessage(roomId: String, message: String, completion: (error: Exception?) -> Unit)

    /**
     * Get current ts
     *
     * @param channelName
     * @return
     */
    fun getCurrentTs(channelName: String): Long

    /**
     * Subscribe to callback changes
     */
    fun subscribeListener(listener: JoyServiceListenerProtocol)
}