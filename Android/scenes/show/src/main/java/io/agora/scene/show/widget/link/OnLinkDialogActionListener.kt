package io.agora.scene.show.widget.link

import io.agora.scene.show.widget.UserItem

interface OnLinkDialogActionListener {
    /**
     * 连麦-当下拉刷新时回调
     */
    fun onRequestMessageRefreshing(dialog: LiveLinkDialog, index: Int)

    /**
     * 连麦-item"同意上麦"按钮点击时回调
     */
    fun onAcceptMicSeatApplyChosen(dialog: LiveLinkDialog, userItem: UserItem)

    /**
     * 连麦-当下拉刷新时回调
     */
    fun onOnlineAudienceRefreshing(dialog: LiveLinkDialog, index: Int)

    /**
     * 连麦-item"邀请上麦"按钮点击时回调
     */
    fun onOnlineAudienceChosen(dialog: LiveLinkDialog, userItem: UserItem)

    /**
     * 连麦-item"邀请上麦"按钮点击时回调
     */
    fun onStopLinkingChosen(dialog: LiveLinkDialog)
}