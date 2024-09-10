package io.agora.scene.aichat.imkit.widget.messageLayout

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.callback.OnItemBubbleClickListener
import io.agora.scene.aichat.imkit.callback.OnMessageAckSendCallback
import io.agora.scene.aichat.imkit.extensions.isSend
import io.agora.scene.aichat.imkit.widget.chatrow.EaseChatRow

open class EaseChatRowViewHolder constructor(itemView: View) :
    EaseBaseRecyclerViewAdapter.ViewHolder<ChatMessage>(itemView),
    OnItemBubbleClickListener {
    private var messageAckSendCallback: OnMessageAckSendCallback? = null
    private val TAG = EaseChatRowViewHolder::class.java.simpleName
    protected var mContext: Context = itemView.context
    private var chatRow: EaseChatRow? = null
    private var message: ChatMessage? = null

    init {
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        itemView.layoutParams = params
    }

    override fun initView(itemView: View?) {
        chatRow = itemView as EaseChatRow
        chatRow?.setOnItemBubbleClickListener(this)
    }

    override fun setData(item: ChatMessage?, position: Int) {
        message = item
        chatRow?.setUpView(item, position)
        handleMessage()
    }

    override fun setDataList(data: List<ChatMessage>?, position: Int) {
        super.setDataList(data, position)
        data?.let {
            chatRow?.count = data.size
            if (position < data.size) {
                chatRow?.setTimestamp(if (position == 0) null else data[position - 1])
            }
        }
    }

    override fun onBubbleClick(message: ChatMessage?) {

    }

    open fun onDetachedFromWindow() {}

    open fun handleMessage() {
        message?.run {
            if (isSend()) {
                handleSendMessage(message)
            } else {
                handleReceiveMessage(message)
            }
        }
    }

    /**
     * send message
     * @param message
     */
    protected open fun handleSendMessage(message: ChatMessage?) {
        // Update the view according to the message current status.
        //getChatRow().updateView(message)
    }

    protected open fun handleReceiveMessage(message: ChatMessage?) {

    }


    open fun getContext(): Context {
        return mContext
    }

    open fun getChatRow(): EaseChatRow? {
        return chatRow
    }

    /**
     * Set message ack send callback.
     */
    fun setOnMessageAckSendCallback(callback: OnMessageAckSendCallback?) {
        this.messageAckSendCallback = callback
    }
}