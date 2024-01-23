package io.agora.scene.showTo1v1.service

import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.showTo1v1.ShowTo1v1Manger

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */

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
    fun onUserListDidChanged(userList: List<ShowTo1v1UserInfo>)

    // 房间销毁
    fun onRoomDidDestroy(roomInfo: ShowTo1v1RoomInfo)

    // 房间体验时间到
    fun onRoomTimeUp()
}

interface ShowTo1v1ServiceProtocol {

    companion object {

        private val instance by lazy {
            ShowTo1v1ServiceImpl(ShowTo1v1Manger.getImpl().mCurrentUser) {
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

    // 释放资源
    fun reset()
}