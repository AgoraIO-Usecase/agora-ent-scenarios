package io.agora.scene.joy.service

import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.scene.base.component.AgoraApplication

data class TokenConfig constructor(
    var rtcToken: String = "",   // rtc token，需要使用万能token，token创建的时候channel name为空字符串
    var rtmToken: String = "",   // rtm token
)

interface JoyServiceListenerProtocol {
    /**
     *  网络状况变化
     */
//    fun onNetworkStatusChanged(status: Sync.ConnectionState)

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

    /**
     * 房间信息变化
     */
    fun onRoomDidChanged(roomInfo: AUIRoomInfo)

    /**
     * 房间销毁
     */
    fun onRoomDidDestroy(roomInfo: AUIRoomInfo, abnormal: Boolean = false)
}

interface JoyServiceProtocol {

    companion object {
        // time limit
        val ROOM_AVAILABLE_DURATION: Long = 10 * 60 * 1000 // 10min

        private var innnerProtocol: JoyServiceProtocol? = null

        val serviceProtocol: JoyServiceProtocol
            get() {
                if (innnerProtocol == null) {
                    innnerProtocol = JoySyncManagerServiceImp(AgoraApplication.the())
                }
                return innnerProtocol!!
            }

        fun reset() {
            innnerProtocol = null
        }
    }

    // ============== 房间相关 ==============

    /**
     * 获取房间列表
     */
    fun getRoomList(completion: (list: List<AUIRoomInfo>) -> Unit)

    /**
     * 修改房间信息
     */
    fun updateRoom(roomInfo: AUIRoomInfo, completion: (error: Exception?) -> Unit)

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
    fun createRoom(roomName: String, completion: (error: Exception?, out: AUIRoomInfo?) -> Unit)

    /**
     * 加入房间
     */
    fun joinRoom(roomInfo: AUIRoomInfo, completion: (error: Exception?) -> Unit)

    /**
     * 离开房间
     */
    fun leaveRoom(roomInfo: AUIRoomInfo, completion: (error: Exception?) -> Unit)


    /**
     * 发送消息
     */
    fun sendChatMessage(roomId: String, message: String, completion: (error: Exception?) -> Unit)

    /**
     * 订阅回调变化
     */
    fun subscribeListener(listener: JoyServiceListenerProtocol)

    fun reset()
}