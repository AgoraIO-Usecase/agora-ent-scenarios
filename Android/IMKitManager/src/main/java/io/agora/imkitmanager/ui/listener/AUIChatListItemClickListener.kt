package io.agora.imkitmanager.ui.listener

import io.agora.imkitmanager.ui.AUIChatInfo


interface AUIChatListItemClickListener {
    fun onItemClickListener(message: AUIChatInfo?) {}
    fun onChatListViewClickListener() {}
}