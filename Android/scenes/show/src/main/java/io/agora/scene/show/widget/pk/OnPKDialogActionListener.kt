package io.agora.scene.show.widget.pk

import android.view.View

interface OnPKDialogActionListener {
    /**
     * PK - Callback when pull-to-refresh
     */
    fun onRequestMessageRefreshing(dialog: LivePKDialog)

    /**
     * PK - Callback when "Invite" button clicked in item
     */
    fun onInviteButtonChosen(dialog: LivePKDialog, view: View, roomItem: LiveRoomConfig)

    /**
     * PK - Callback when "Stop PK" button clicked
     */
    fun onStopPKingChosen(dialog: LivePKDialog)
}