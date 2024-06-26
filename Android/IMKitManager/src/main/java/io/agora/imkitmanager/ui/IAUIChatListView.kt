package io.agora.imkitmanager.ui

import io.agora.imkitmanager.ui.impl.AUIBroadcastMessageLayout
import io.agora.imkitmanager.ui.listener.AUIChatListItemClickListener

interface IAUIChatListView {
    fun setChatListItemClickListener(listener: AUIChatListItemClickListener?) {}

    fun refresh(msgList: List<AUIChatInfo>) {}

    fun refreshSelectLast(msgList: List<AUIChatInfo>?) {}

    // broadcast view
    fun setScrollSpeed(speed: Int) {}

    fun showSubtitleView(content: String) {}

    fun setSubtitleStatusChangeListener(listener: AUIBroadcastMessageLayout.SubtitleStatusChangeListener) {}
}