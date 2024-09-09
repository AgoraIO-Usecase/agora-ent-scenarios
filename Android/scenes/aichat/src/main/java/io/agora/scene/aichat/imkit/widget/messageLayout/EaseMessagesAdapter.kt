package io.agora.scene.aichat.imkit.widget.messageLayout

import android.view.ViewGroup
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.callback.OnEaseChatReactionErrorListener
import io.agora.scene.aichat.imkit.callback.OnMessageAckSendCallback
import io.agora.scene.aichat.imkit.callback.OnMessageChatThreadClickListener
import io.agora.scene.aichat.imkit.callback.OnMessageListItemClickListener
import io.agora.scene.aichat.imkit.widget.chatrow.EaseChatRow

open class EaseMessagesAdapter(): EaseBaseRecyclerViewAdapter<ChatMessage>() {
    private var parentId: String? = null
    private var parentMsgId: String? = null
    private var messageAckSendCallback: OnMessageAckSendCallback? = null
    private var reactionErrorListener: OnEaseChatReactionErrorListener? = null
    private var itemClickListener: OnMessageListItemClickListener? = null
    private var threadViewEventListener: OnMessageChatThreadClickListener? = null

    override fun getItemNotEmptyViewType(position: Int): Int {
        return EaseChatViewHolderFactory.getViewType(getItem(position))
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        return EaseChatViewHolderFactory.createViewHolder(parent, EaseMessageViewType.from(viewType))
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatMessage>, position: Int) {
        // Set message ack send callback.
        if (holder is EaseChatRowViewHolder) {
            holder.setOnMessageAckSendCallback(messageAckSendCallback)
        }

        super.onBindViewHolder(holder, position)

        if (holder is EaseChatRowViewHolder && holder.itemView is EaseChatRow) {
            (holder.itemView as? EaseChatRow)?.let {
                it.setOnMessageListItemClickListener(itemClickListener)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        getItem(position)?.let {
            return it.hashCode().toLong()
        }
        return super.getItemId(position)
    }

    /**
     * Set parent info for chat thread.
     * @param parentId The parent id, usually is the group that the chat thread belongs to.
     * @param parentMsgId The parent message id, usually is the group message id that created the chat thread.
     *                  It can be null if the group message was recalled.
     */
    fun setParentInfo(parentId: String?, parentMsgId: String?){
        this.parentId = parentId
        this.parentMsgId = parentMsgId
    }

    /**
     * Set message item click listener.
     */
    fun setOnMessageListItemClickListener(listener: OnMessageListItemClickListener?) {
        itemClickListener = listener
    }

    /**
     * Set message reaction error listener.
     */
    fun setOnMessageReactionErrorListener(listener: OnEaseChatReactionErrorListener?) {
        reactionErrorListener = listener
    }

    fun setOnMessageThreadEventListener(listener: OnMessageChatThreadClickListener?){
        threadViewEventListener = listener
    }

    /**
     * Set message ack send callback.
     */
    fun setOnMessageAckSendCallback(callback: OnMessageAckSendCallback?) {
        this.messageAckSendCallback = callback
    }
}