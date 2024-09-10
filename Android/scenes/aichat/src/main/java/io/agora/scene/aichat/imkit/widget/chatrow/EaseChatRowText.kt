package io.agora.scene.aichat.imkit.widget.chatrow

import android.content.Context
import android.text.Spannable
import android.text.Spanned
import android.text.style.URLSpan
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import io.agora.scene.aichat.R
import io.agora.scene.aichat.imkit.ChatTextMessageBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private var typingJob: Job? = null

fun TextView.typeWrite(text: String, intervalMs: Long = 50L) {
    // 如果之前的打字任务在执行，取消它
    typingJob?.cancel()
    typingJob =  CoroutineScope(Dispatchers.Main).launch {
        runCatching {
            val preIndex = this@typeWrite.text.length
            val appendText = text.substring(preIndex)
            for (i in appendText.indices) {
                delay(intervalMs)
                if (isActive) { // 检查协程是否仍然活跃
                    this@typeWrite.text = this@typeWrite.text.toString() + appendText[i]
                }
            }
        }.onFailure {
            this@typeWrite.text = text
        }
    }
}

open class EaseChatRowText @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean
) : EaseChatRow(context, attrs, defStyleAttr, isSender) {
    protected val contentView: TextView? by lazy { findViewById(R.id.tv_chatcontent) }

    override fun onInflateView() {
        inflater.inflate(
            if (!isSender) R.layout.ease_row_received_message else R.layout.ease_row_sent_message, this
        )
    }

    override fun onSetUpView() {
        val msg = message ?: return
        if (msg.body is ChatTextMessageBody) {
            val textBody = message?.body as ChatTextMessageBody
            contentView?.let { view ->
                if (position == count - 1 && msg.msgTime > System.currentTimeMillis() - 1000 * 30) {
                    view.typeWrite(textBody.message)
                } else {
                    view.text = textBody.message
                }
                view.setOnLongClickListener { v ->
                    view.setTag(R.id.action_chat_long_click, true)
                    itemClickListener?.onBubbleLongClick(v, message) ?: false
                }
            }
        }
    }

    /**
     * Resolve long press event conflict with Relink
     * Refer to：https://www.jianshu.com/p/d3bef8449960
     */
    private fun replaceSpan() {
        (contentView?.text as? Spannable)?.let {
            val spans = it.getSpans(0, it.length, URLSpan::class.java)
            spans.forEach { item ->
                var url = item.url
                var index = it.toString().indexOf(url)
                var end = index + url.length
                if (index == -1) {
                    if (url.contains("http://")) {
                        url = url.replace("http://", "")
                    } else if (url.contains("https://")) {
                        url = url.replace("https://", "")
                    } else if (url.contains("rtsp://")) {
                        url = url.replace("rtsp://", "")
                    }
                    index = it.toString().indexOf(url)
                    end = index + url.length
                }
                if (index != -1) {
                    it.removeSpan(item)
                    it.setSpan(
                        AutolinkSpan(item.url), index, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }

            }
        }
    }

    override fun onMessageSuccess() {
        super.onMessageSuccess()
        message?.run {
            // Set ack-user list change listener.

        }
    }

    val getBubbleBottom: ConstraintLayout? = llBubbleBottom
    val getBubbleTop: ConstraintLayout? = llTopBubble
}