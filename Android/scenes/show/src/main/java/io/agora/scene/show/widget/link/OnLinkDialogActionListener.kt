package io.agora.scene.show.widget.link

import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.show.service.ShowUser

interface OnLinkDialogActionListener {
    /**
     * 连麦-当下拉刷新时回调
     */
    fun onRequestMessageRefreshing(dialog: LiveLinkDialog)

    /**
     * 连麦-item"同意上麦"按钮点击时回调
     */
    fun onAcceptMicSeatApplyChosen(dialog: LiveLinkDialog, seatApply: ShowMicSeatApply)

    /**
     * 连麦-当下拉刷新时回调
     */
    fun onOnlineAudienceRefreshing(dialog: LiveLinkDialog)

    /**
     * 连麦-item"邀请上麦"按钮点击时回调
     */
    fun onOnlineAudienceInvitation(dialog: LiveLinkDialog, userItem: ShowUser)

    /**
     * 连麦-item"邀请上麦"按钮点击时回调
     */
    fun onStopLinkingChosen(dialog: LiveLinkDialog)

    /**
     * 连麦-item"取消申请上麦"按钮点击时回调
     */
    fun onStopApplyingChosen(dialog: LiveLinkDialog)
}