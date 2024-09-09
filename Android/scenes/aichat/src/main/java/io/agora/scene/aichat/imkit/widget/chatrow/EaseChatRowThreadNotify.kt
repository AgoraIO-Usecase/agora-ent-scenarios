package io.agora.scene.aichat.imkit.widget.chatrow

import android.content.Context
import android.util.AttributeSet
import io.agora.scene.aichat.R

class EaseChatRowThreadNotify @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean = false
): EaseChatRow(context, attrs, defStyleAttr, isSender)  {

    override fun onInflateView() {
        inflater.inflate(R.layout.ease_row_thread_notify, this)
    }

    override fun onSetUpView() {
        message?.run {
        }
    }
}