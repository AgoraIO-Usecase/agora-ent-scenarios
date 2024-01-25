package io.agora.scene.joy.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.joy.JoyLogger
import io.agora.syncmanager.rtm.Sync

interface JoyServiceListenerProtocol {
    /**
     *  网络状况变化
     */
    fun onNetworkStatusChanged(status: Sync.ConnectionState)

    /**
     * 用户变化
     */
    fun onUserListDidChanged(userList: List<JoyUserInfo>)

    /**
     * 接收到新消息
     */
    fun onMessageDidAdded(message: JoyMessage)

    /**
     * 房间进行的游戏变化
     */
    fun onStartGameInfoDidChanged(startGameInfo: JoyStartGameInfo)

    /**
     * 房间信息变化
     */
    fun onRoomDidChanged(roomInfo: JoyRoomInfo)

    /**
     * 房间销毁
     */
    fun onRoomDidDestroy(roomInfo: JoyRoomInfo, abnormal: Boolean = false)
}

interface JoyServiceProtocol {

    companion object {
        // time limit
        val ROOM_AVAILABLE_DURATION: Long = 10 * 60 * 1000 // 10min

        private val instance by lazy {
            // JOyServiceImp()
            JoySyncManagerServiceImp(AgoraApplication.the()) { error ->
                error?.message?.let { JoyLogger.e("SyncManager", it) }
            }
        }

        fun getImplInstance(): JoyServiceProtocol = instance
    }

    // ============== 房间相关 ==============

    /**
     * 获取房间列表
     */
    fun getRoomList(completion: (list: List<JoyRoomInfo>) -> Unit)

    /**
     * 修改房间信息
     */
    fun updateRoom(roomInfo: JoyRoomInfo, completion: (error: Exception?) -> Unit)

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
    fun createRoom(roomName: String, completion: (error: Exception?, out: JoyRoomInfo?) -> Unit)

    /**
     * 加入房间
     */
    fun joinRoom(roomInfo: JoyRoomInfo, completion: (error: Exception?) -> Unit)

    /**
     * 离开房间
     */
    fun leaveRoom(roomInfo: JoyRoomInfo, completion: (error: Exception?) -> Unit)


    /**
     * 发送消息
     */
    fun sendChatMessage(roomId: String, message: String, completion: (error: Exception?) -> Unit)

    /**
     * 订阅回调变化
     */
    fun subscribeListener(listener: JoyServiceListenerProtocol)


}