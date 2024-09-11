package io.agora.scene.aichat.imkit.widget.chatrow

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import io.agora.scene.aichat.R
import io.agora.scene.aichat.imkit.ChatTextMessageBody

class EaseChatRowUnsent constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean = false
) : EaseChatRow(context, attrs, defStyleAttr, isSender) {
    private val contentView: TextView? by lazy { findViewById(R.id.text_content) }

    override fun onInflateView() {
        inflater.inflate(R.layout.ease_row_unsent_message, this)
    }

    override fun onSetUpView() {
        message?.run {
            contentView?.text = (body as ChatTextMessageBody).message
        }
    }
}