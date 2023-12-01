package io.agora.scene.joy.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.joy.utils.JoyLogger
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
     * 房间销毁
     */
    fun onRoomDidDestroy(roomInfo: JoyRoomInfo)

    /**
     * 房间超时
     */
    fun onRoomTimeUp()
}

interface JoyServiceProtocol {


    enum class JoySubscribe {
        JoySubscribeCreated,      //创建
        JoySubscribeDeleted,      //删除
        JoySubscribeUpdated,      //更新
    }

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

    fun reset()

    // ============== 房间相关 ==============

    /**
     * 获取房间列表
     */
    fun getRoomList(completion: (list: List<JoyRoomInfo>) -> Unit)

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
     * 订阅回调变化
     */
    fun subscribeListener(listener: JoyServiceListenerProtocol)


}