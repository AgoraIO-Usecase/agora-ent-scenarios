package io.agora.scene.show.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */
interface ShowServiceProtocol {

    enum class ShowSubscribeStatus {
        deleted,
        updated
    }

    companion object {
        // 房间存活时间，单位ms
        var ROOM_AVAILABLE_DURATION: Long = 1200 * 1000
        // PK单局时间，单位ms
        var PK_AVAILABLE_DURATION: Long = 120 * 1000

        private val instance by lazy {
            ShowSyncManagerServiceImpl(AgoraApplication.the()){
                if (it.message != "action error") {
                    ToastUtils.showToast(it.message)
                }
            }
        }

        fun getImplInstance(): ShowServiceProtocol = instance
    }

    fun destroy()

    // 获取房间列表
    fun getRoomList(
        success: (List<ShowRoomDetailModel>) -> Unit,
        error: ((Exception) -> Unit)? = null
    )

    // 创建房间
    fun createRoom(
        roomId: String,
        roomName: String,
        thumbnailId: String,
        success: (ShowRoomDetailModel) -> Unit,
        error: ((Exception) -> Unit)? = null
    )

    // 加入房间
    fun joinRoom(
        roomId: String,
        success: (ShowRoomDetailModel) -> Unit,
        error: ((Exception) -> Unit)? = null
    )

    // 离开房间
    fun leaveRoom(roomId: String)

    // 订阅当前加入的房间的更新删除事件
    fun subscribeCurrRoomEvent(roomId: String, onUpdate: (status: ShowSubscribeStatus, roomInfo: ShowRoomDetailModel?) -> Unit)

    // 获取当前房间所有用户
    fun getAllUserList(roomId: String, success: (List<ShowUser>) -> Unit, error: ((Exception) -> Unit)? = null)

    // 监听用户变化
    fun subscribeUser(roomId: String, onUserChange: (ShowSubscribeStatus, ShowUser?) -> Unit)

    // 发送聊天消息
    fun sendChatMessage(
        roomId: String,
        message: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 订阅聊天消息变化
    fun subscribeMessage(
        roomId: String,
        onMessageChange: (ShowSubscribeStatus, ShowMessage) -> Unit
    )


    // 获取上麦申请列表
    fun getAllMicSeatApplyList(
        roomId: String,
        success: (List<ShowMicSeatApply>) -> Unit,
        error: ((Exception) -> Unit)? = null
    )

    // 主播订阅连麦申请变化
    fun subscribeMicSeatApply(roomId: String, onMicSeatChange: (ShowSubscribeStatus, ShowMicSeatApply?) -> Unit)

    // 观众申请连麦
    fun createMicSeatApply(roomId: String, success: (() -> Unit)? = null, error: ((Exception) -> Unit)? = null)

    // 观众取消连麦申请
    fun cancelMicSeatApply(roomId: String, success: (() -> Unit)? = null, error: ((Exception) -> Unit)? = null)

    // 主播接受连麦申请
    fun acceptMicSeatApply(
        roomId: String,
        apply: ShowMicSeatApply,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 主播拒绝连麦申请
    fun rejectMicSeatApply(
        roomId: String,
        apply: ShowMicSeatApply,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 获取上麦邀请列表
    fun getAllMicSeatInvitationList(
        roomId: String,
        success: ((List<ShowMicSeatInvitation>) -> Unit),
        error: ((Exception) -> Unit)? = null
    )

    // 观众订阅连麦邀请
    fun subscribeMicSeatInvitation(roomId: String,onMicSeatInvitationChange: (ShowSubscribeStatus, ShowMicSeatInvitation?) -> Unit)

    // 主播创建连麦邀请
    fun createMicSeatInvitation(
        roomId: String,
        user: ShowUser,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 主播取消连麦邀请
    fun cancelMicSeatInvitation(
        roomId: String,
        userId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 观众同意连麦
    fun acceptMicSeatInvitation(
        roomId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 观众拒绝连麦
    fun rejectMicSeatInvitation(
        roomId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    /// 获取可PK对象列表(目前等价getRoomList)
    fun getAllPKUserList(
        success: ((List<ShowRoomDetailModel>) -> Unit),
        error: ((Exception) -> Unit)? = null
    )

    // 获取PK邀请列表
    fun getAllPKInvitationList(
        roomId: String,
        isFromUser: Boolean,
        success: ((List<ShowPKInvitation>) -> Unit),
        error: ((Exception) -> Unit)? = null
    )

    // 观众订阅连麦邀请
    fun subscribePKInvitationChanged(roomId: String, onPKInvitationChanged: (ShowSubscribeStatus, ShowPKInvitation?) -> Unit)

    // 创建PK邀请
    fun createPKInvitation(
        roomId: String,
        room: ShowRoomDetailModel,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 同意PK
    fun acceptPKInvitation(
        roomId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 拒绝PK
    fun rejectPKInvitation(
        roomId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 获取互动列表
    fun getAllInterationList(
        roomId: String,
        success: ((List<ShowInteractionInfo>) -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 订阅互动邀请
    fun subscribeInteractionChanged(roomId: String, onInteractionChanged: (ShowSubscribeStatus, ShowInteractionInfo?) -> Unit)

    // 停止互动
    fun stopInteraction(
        roomId: String,
        interaction: ShowInteractionInfo,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 静音设置
    fun muteAudio(
        roomId: String,
        mute: Boolean,
        userId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 订阅重连事件
    fun subscribeReConnectEvent(roomId: String, onReconnect: () -> Unit)

    // 启动机器人
    fun startCloudPlayer()

}