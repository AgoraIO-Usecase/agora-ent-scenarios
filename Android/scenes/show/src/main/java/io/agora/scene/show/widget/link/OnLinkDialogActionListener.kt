package io.agora.scene.show.widget.link

import android.view.View
import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.show.service.ShowUser

interface OnLinkDialogActionListener {
    /**
     * Linking - Callback when pull-to-refresh
     */
    fun onRequestMessageRefreshing(dialog: LiveLinkDialog)

    /**
     * Linking - Callback when "Agree to join mic" button clicked in item
     */
    fun onAcceptMicSeatApplyChosen(dialog: LiveLinkDialog, view: View, seatApply: ShowMicSeatApply)

    /**
     * Linking - Callback when pull-to-refresh
     */
    fun onOnlineAudienceRefreshing(dialog: LiveLinkDialog)

    /**
     * Linking - Callback when "Invite to join mic" button clicked in item
     */
    fun onOnlineAudienceInvitation(dialog: LiveLinkDialog, view: View, userItem: ShowUser)

    /**
     * Linking - Callback when "Invite to join mic" button clicked in item
     */
    fun onStopLinkingChosen(dialog: LiveLinkDialog, view: View)

    /**
     * Linking - Callback when "Cancel mic request" button clicked in item
     */
    fun onStopApplyingChosen(dialog: LiveLinkDialog, view: View, seatApply: ShowMicSeatApply?)
}