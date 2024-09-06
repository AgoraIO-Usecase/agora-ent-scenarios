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
     * 用户变化
     */
    fun onUserListDidChanged(userList: List<AUIUserInfo>)

    /**
     * 接收到新消息
     */
    fun onMessageDidAdded(message: JoyMessage)

    /**
     * 房间进行的游戏变化
     */
    fun onStartGameInfoDidChanged(startGameInfo: JoyStartGameInfo)
}

interface JoyServiceProtocol {

    companion object {
        // time limit
        val ROOM_AVAILABLE_DURATION: Long = 10 * 60 * 1000 // 10min

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
     * 获取房间列表
     */
    fun getRoomList(completion: (error: Exception?, roomList: List<AUIRoomInfo>?) -> Unit)

    /**
     * 获取房间剩余时间
     */
    fun getCurrentRoomDuration(roomId: String): Long

    /**
     * 获取正在进行的游戏信息
     */
    fun getStartGame(roomId: String, completion: (error: Exception?, out: JoyStartGameInfo?) -> Unit)

    /**
     * 更新正在进行的游戏信息
     */
    fun updateStartGame(roomId: String, gameInfo: JoyStartGameInfo, completion: (error: Exception?) -> Unit)

    /**
     * 创建房间
     */
    fun createRoom(roomName: String, completion: (error: Exception?, roomInfo: AUIRoomInfo?) -> Unit)

    /**
     * 加入房间
     */
    fun joinRoom(roomId: String, completion: (error: Exception?) -> Unit)

    /**
     * 离开房间
     */
    fun leaveRoom(completion: (error: Exception?) -> Unit)

    /**
     * 发送消息
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
     * 订阅回调变化
     */
    fun subscribeListener(listener: JoyServiceListenerProtocol)
}