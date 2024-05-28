package io.agora.scene.show.widget.link

import android.view.View
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
    fun onAcceptMicSeatApplyChosen(dialog: LiveLinkDialog, view: View, seatApply: ShowMicSeatApply)

    /**
     * 连麦-当下拉刷新时回调
     */
    fun onOnlineAudienceRefreshing(dialog: LiveLinkDialog)

    /**
     * 连麦-item"邀请上麦"按钮点击时回调
     */
    fun onOnlineAudienceInvitation(dialog: LiveLinkDialog, view: View, userItem: ShowUser)

    /**
     * 连麦-item"邀请上麦"按钮点击时回调
     */
    fun onStopLinkingChosen(dialog: LiveLinkDialog, view: View)

    /**
     * 连麦-item"取消申请上麦"按钮点击时回调
     */
    fun onStopApplyingChosen(dialog: LiveLinkDialog, view: View)
}