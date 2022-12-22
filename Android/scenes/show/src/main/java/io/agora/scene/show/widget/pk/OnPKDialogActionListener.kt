package io.agora.scene.show.widget.pk

interface OnPKDialogActionListener {
    /**
     * pk-当下拉刷新时回调
     */
    fun onRequestMessageRefreshing(dialog: LivePKDialog)

    /**
     * pk-item"邀请"按钮点击时回调
     */
    fun onInviteButtonChosen(dialog: LivePKDialog, roomItem: LiveRoomConfig)

    /**
     * pk-"停止pk"按钮点击时回调
     */
    fun onStopPKingChosen(dialog: LivePKDialog)
}