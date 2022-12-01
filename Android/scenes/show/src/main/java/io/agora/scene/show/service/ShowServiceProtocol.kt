package io.agora.scene.show.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils


interface ShowServiceProtocol {

    enum class ShowSubscribeStatus {
        deleted,
        updated
    }

    companion object {
        private val instance by lazy {
            ShowSyncManagerServiceImpl(AgoraApplication.the()){
                ToastUtils.showToast(it.message)
            }
        }

        fun getImplInstance(): ShowServiceProtocol = instance
    }

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
        roomNo: String,
        success: (ShowRoomDetailModel) -> Unit,
        error: ((Exception) -> Unit)? = null
    )

    // 离开房间
    fun leaveRoom()

    // 获取当前房间所有用户
    fun getAllUserList(success: (List<ShowUser>) -> Unit, error: ((Exception) -> Unit)? = null)

    // 监听用户变化
    fun subscribeUser(onUserChange: (ShowSubscribeStatus, ShowUser?) -> Unit)

    // 发送聊天消息
    fun sendChatMessage(
        message: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 订阅聊天消息变化
    fun subscribeMessage(
        onMessageChange: (ShowSubscribeStatus, ShowMessage) -> Unit
    )


    // 获取上麦申请列表
    fun getAllMicSeatApplyList(
        success: (List<ShowMicSeatApply>) -> Unit,
        error: ((Exception) -> Unit)? = null
    )

    // 主播订阅连麦申请变化
    fun subscribeMicSeatApply(onMicSeatChange: (ShowSubscribeStatus, ShowMicSeatApply?) -> Unit)

    // 观众申请连麦
    fun createMicSeatApply(success: (() -> Unit)? = null, error: ((Exception) -> Unit)? = null)

    // 观众取消连麦申请
    fun cancelMicSeatApply(success: (() -> Unit)? = null, error: ((Exception) -> Unit)? = null)

    // 主播接受连麦申请
    fun acceptMicSeatApply(
        apply: ShowMicSeatApply,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 主播拒绝连麦申请
    fun rejectMicSeatApply(
        apply: ShowMicSeatApply,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 获取上麦邀请列表
    fun getAllMicSeatInvitationList(
        success: ((List<ShowMicSeatInvitation>) -> Unit),
        error: ((Exception) -> Unit)? = null
    )

    // 观众订阅连麦邀请
    fun subscribeMicSeatInvitation(onMicSeatInvitationChange: (ShowSubscribeStatus, ShowMicSeatInvitation?) -> Unit)

    // 主播创建连麦邀请
    fun createMicSeatInvitation(
        user: ShowUser,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 主播取消连麦邀请
    fun cancelMicSeatInvitation(
        userId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 观众同意连麦
    fun acceptMicSeatInvitation(
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 观众拒绝连麦
    fun rejectMicSeatInvitation(
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 获取PK邀请列表
    fun getAllPKInvitationList(
        success: ((List<ShowPKInvitation>) -> Unit),
        error: ((Exception) -> Unit)? = null
    )

    // 观众订阅连麦邀请
    fun subscribePKInvitationChanged(onPKInvitationChanged: (ShowSubscribeStatus, ShowPKInvitation?) -> Unit)

    // 创建PK邀请
    fun createPKInvitation(
        room: ShowRoomListModel,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 同意PK
    fun acceptPKInvitation(
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 拒绝PK
    fun rejectPKInvitation(
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 获取互动列表
    fun getAllInterationList(
        success: ((List<ShowInteractionInfo>) -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // 订阅互动邀请
    fun subscribeInteractionChanged(onInteractionChanged: (ShowSubscribeStatus, ShowInteractionInfo?) -> Unit)

    // 停止互动
    fun stopInteraction(
        interaction: ShowInteractionInfo,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )
}