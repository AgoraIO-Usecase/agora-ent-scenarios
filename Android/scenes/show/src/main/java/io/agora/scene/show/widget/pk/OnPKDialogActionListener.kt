package io.agora.scene.show.widget.pk

import io.agora.scene.show.widget.UserItem
import io.agora.scene.show.widget.pk.LivePKDialog

interface OnPKDialogActionListener {
    /**
     * pk-当下拉刷新时回调
     */
    fun onRequestMessageRefreshing(dialog: LivePKDialog, index: Int)

    /**
     * 连麦-item"邀请"按钮点击时回调
     */
    fun onInviteButtonChosen(dialog: LivePKDialog, userItem: UserItem)
}