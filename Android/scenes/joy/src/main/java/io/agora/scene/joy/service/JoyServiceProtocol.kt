package io.agora.scene.joy.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.joy.JoyLogger

interface JoyServiceProtocol {

    enum class JoySubscribe {
        JoySubscribeCreated,      //创建
        JoySubscribeDeleted,      //删除
        JoySubscribeUpdated,      //更新
    }

    companion object {
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
    fun getRoomList(completion: (error: Exception?, list: List<RoomListModel>?) -> Unit)

    /**
     * 创建房间
     */
    fun createRoom(
        inputModel: CreateRoomInputModel,
        completion: (error: Exception?, out: CreateRoomOutputModel?) -> Unit
    )

    /**
     * 加入房间
     */
    fun joinRoom(
        inputModel: JoinRoomInputModel,
        completion: (error: Exception?, out: JoinRoomOutputModel?) -> Unit
    )

    /**
     * 离开房间
     */
    fun leaveRoom(completion: (error: Exception?) -> Unit)

    /**
     * room status did changed
     */
    fun subscribeRoomStatus(changedBlock: (JoySubscribe, RoomListModel?) -> Unit)

    /**
     * user count did changed
     */
    fun subscribeUserListCount(changedBlock: (count: Int) -> Unit)

    /**
     * 房间超时
     */
    fun subscribeRoomTimeUp(onRoomTimeUp: () -> Unit)


    // =================== 断网重连相关 =========================

    /**
     * 订阅重连事件
     */
    fun subscribeReConnectEvent(onReconnect: () -> Unit)
}