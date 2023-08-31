package io.agora.scene.showTo1v1.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils

// 房间存活时间，单位ms
const val ROOM_AVAILABLE_DURATION: Long = 60 * 20 * 1000// 20min

enum class ShowTo1v1ServiceNetworkStatus {
    Connecting, // 连接中
    Open,       //已打开
    Fail,       //失败
    Closed,     //已关闭
}

interface ShowTo1v1ServiceListenerProtocol {

    // 网络变化回调
    fun onNetworkStatusChanged(status: ShowTo1v1ServiceNetworkStatus)

    // 用户变化
    fun onUserListDidChanged(userList: ShowTo1v1UserInfo)

    // 房间销毁
    fun onRoomDidDestroy(roomInfo: ShowTo1v1RoomInfo)

    // 房间体验时间到
    fun onRoomTimeUp()
}

interface ShowTo1v1ServiceProtocol {

    companion object {

        private val instance by lazy {
            ShowTo1v1ServiceImpl(AgoraApplication.the()) {
                if (it.message != "action error") {
                    ToastUtils.showToast(it.message)
                }
            }
        }

        fun getImplInstance(): ShowTo1v1ServiceProtocol {
            return instance
        }
    }

    // 创建房间
    fun createRoom(roomName: String, completion: (error: Exception?, roomInfo: ShowTo1v1RoomInfo?) -> Unit)

    // 加入房间
    fun joinRoom(roomInfo: ShowTo1v1RoomInfo, completion: (error: Exception?) -> Unit)

    // 离开房间
    fun leaveRoom(roomInfo: ShowTo1v1RoomInfo, completion: (error: Exception?) -> Unit)

    // 获取房间列表
    fun getRoomList(completion: (error: Exception?, roomList: List<ShowTo1v1RoomInfo>) -> Unit)

    // 订阅回调
    fun subscribeListener(listener: ShowTo1v1ServiceListenerProtocol)


}