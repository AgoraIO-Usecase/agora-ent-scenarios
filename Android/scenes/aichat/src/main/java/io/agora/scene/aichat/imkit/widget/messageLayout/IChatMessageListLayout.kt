package io.agora.scene.aichat.imkit.widget.messageLayout

import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.callback.OnChatErrorListener
import io.agora.scene.aichat.imkit.callback.OnMessageAckSendCallback
import io.agora.scene.aichat.imkit.callback.OnMessageAudioStatusCallback
import io.agora.scene.aichat.imkit.callback.OnMessageChatThreadClickListener
import io.agora.scene.aichat.imkit.callback.OnMessageListItemClickListener

interface IChatMessageListLayout {

    val refreshLayout: SmartRefreshLayout?

    val messageListLayout: RecyclerView?

    /**
     * Get conversation
     * @return
     */
    val currentConversation: ChatConversation?

    /**
     * Whether the list can scroll to the bottom automatically.
     * If true, it means that when list view's height changes or new message comes, the list will scroll to the bottom automatically.
     */
    var isCanAutoScrollToBottom: Boolean

    /**
     * Set custom message list viewModel.
     */
    fun setViewModel(viewModel: IChatMessageListRequest?)

    /**
     * Set custom adapter.
     */
    fun setMessagesAdapter(adapter: EaseMessagesAdapter?)

    /**
     * Get chat message adapter.
     */
    fun getMessagesAdapter(): EaseMessagesAdapter?

    /**
     * Set message list item click listener.
     */
    fun setOnMessageListItemClickListener(listener: OnMessageListItemClickListener?)

    /**
     * Set message thread view click listener.
     */
    fun setOnMessageThreadViewClickListener(listener: OnMessageChatThreadClickListener?)

    /**
     * Set on message audio click listener
     *
     * @param listener
     */
    fun setOnMessageAudioStatusCallback(listener: OnMessageAudioStatusCallback?)

    /**
     * Set message ack send callback.
     */
    fun setOnMessageAckSendCallback(callback: OnMessageAckSendCallback?)

    /**
     * Set error listener.
     */
    fun setOnChatErrorListener(listener: OnChatErrorListener?)

    /**
     * Whether to use the default refresh method.
     * @param useDefaultRefresh True means use default refresh method, false means use custom refresh method.
     */
    fun useDefaultRefresh(useDefaultRefresh: Boolean)

    /**
     * Get cache messages and refresh.
     */
    fun refreshMessages()

    /**
     * Scroll to bottom
     *
     * @param isRefresh
     */
    fun scrollToBottom(isRefresh: Boolean= true)

    /**
     * Refresh the target message.
     * @param messageId The message Id.
     */
    fun refreshMessage(messageId: String?)

    /**
     * Refresh the target message.
     * @param message The message object.
     */
    fun refreshMessage(message: ChatMessage?)

    /**
     * Remove the target message.
     * @param message The message object.
     */
    fun removeMessage(message: ChatMessage?)

    fun addMessageToLast(message: ChatMessage?)

    /**
     * Move to the target position.
     * @param position The target position.
     */
    fun moveToTarget(position: Int)

    /**
     * Move to the target position.
     * @param position The target position.
     */
    fun moveToTarget(message: ChatMessage?)

    /**
     * Whether to show default refresh animator.
     * @param refreshing True means show, false means hide.
     */
    fun setRefreshing(refreshing: Boolean)

    /**
     * Whether to scroll to the bottom when the RecyclerView's height changes
     * @param isNeedToScrollBottom
     */
    fun isNeedScrollToBottomWhenViewChange(isNeedToScrollBottom: Boolean)

    /**
     * Remove adapter.
     * @param adapter
     */
    fun removeAdapter(adapter: RecyclerView.Adapter<*>?)
}