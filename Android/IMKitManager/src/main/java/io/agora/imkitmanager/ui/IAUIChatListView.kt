package io.agora.imkitmanager.ui

import io.agora.imkitmanager.ui.listener.AUIChatListItemClickListener

interface IAUIChatListView {
    /**
     * 聊天的列表项点击事件
     *
     * @param listener
     */
    fun setChatListItemClickListener(listener: AUIChatListItemClickListener?) {}

    /**
     * 刷新列表
     *
     * @param msgList
     */
    fun refresh(msgList: List<AUIChatInfo>) {}

    /**
     * 刷新列表并移动到最后一条
     *
     * @param msgList
     */
    fun refreshSelectLast(msgList: List<AUIChatInfo>?) {}
}