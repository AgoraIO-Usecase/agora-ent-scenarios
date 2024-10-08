package io.agora.scene.aichat.imkit.widget.chatrow

import android.content.Context
import android.text.Spannable
import android.text.Spanned
import android.text.style.URLSpan
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import io.agora.scene.aichat.R
import io.agora.scene.aichat.imkit.ChatTextMessageBody
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.isReceive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private var typingJob: Job? = null

fun TextView.typeWrite(newText: String, intervalMs: Long = 50L){
// 如果已有的打字机任务正在运行，并且需要追加新的文字
    val currentText = this.text.toString()
    if (typingJob != null && typingJob?.isActive == true) {
        // 追加剩余的文字到已有的任务中
        val additionalText = newText.substring(currentText.length.coerceAtMost(newText.length))
        typingJob = CoroutineScope(Dispatchers.Main).launch {
            for (i in additionalText.indices) {
                delay(intervalMs)
                if (isActive) {
                    this@typeWrite.text = this@typeWrite.text.toString() + additionalText[i]
                }
            }
        }
    } else {
        // 否则，启动新的打字机任务
        typingJob?.cancel()
        typingJob = CoroutineScope(Dispatchers.Main).launch {
            runCatching {
                for (i in currentText.length until newText.length) {
                    delay(intervalMs)
                    if (isActive) {
                        this@typeWrite.text = this@typeWrite.text.toString() + newText[i]
                    }
                }
            }.onFailure {
                if (it !is CancellationException) {
                    Log.d("typeWrite", "typeWrite onFailure $it $newText")
                }
                this@typeWrite.text = newText
            }
        }
    }
}

enum class EaseChatAudioStatus {
    START_PLAY,
    START_RECOGNITION,
    RECOGNIZING,
    PLAYING,
    UNKNOWN
}

open class EaseChatRowText @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean
) : EaseChatRow(context, attrs, defStyleAttr, isSender) {
    protected val contentView: TextView? by lazy { findViewById(R.id.tv_chatcontent) }

    protected val ivMsgStartPlay: ImageView? by lazy { findViewById(R.id.iv_msg_start_play) }

    // 开始识别/正在识别/开始播放/正在播放
    protected val tvMsgStartPlay: TextView? by lazy { findViewById(R.id.tv_msg_start_play) }
    protected val progressRecognition: ProgressBar? by lazy { findViewById(R.id.progress_recognition) }

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
                // 收到的消息显示打字机效果
                if (msg.isReceive() && position == count - 1 && msg.isUnread) {
                    // 避免重复调用 typeWrite
                    if (view.text.toString() != textBody.message) {
                        view.typeWrite(textBody.message)
                    }
                } else {
                    view.text = textBody.message
                }
                view.text = textBody.message
                view.setOnLongClickListener { v ->
                    view.setTag(R.id.action_chat_long_click, true)
                    itemClickListener?.onBubbleLongClick(v, message) ?: false
                }
            }
            bottomBubbleLayout?.let {
                it.setOnClickListener {
                    val oldAudioStatus = audioStatus
                    if (oldAudioStatus == EaseChatAudioStatus.START_RECOGNITION) {
                        isRecognizing = true

                    }
                    if (oldAudioStatus == EaseChatAudioStatus.START_PLAY) {
                        isPlaying = true
                    }
                    if (itemClickListener?.onBottomBubbleClick(message, audioStatus) == true) {
                        return@setOnClickListener
                    }
                    itemBubbleClickListener?.onBottomBubbleClick(message, audioStatus)
                }
            }
        }
    }

    // audio 正在播放
    private var isPlaying = false

    // audio 正在识别
    private var isRecognizing = false

    fun setAudioPlaying(isPlaying: Boolean) {
        if (isPlaying) {
            this.isRecognizing = false
        }
        this.isPlaying = isPlaying
        updateAudioStatus()
    }

    fun setAudioRecognizing(isRecognizing: Boolean) {
        if (isRecognizing) {
            this.isPlaying = false
        }
        this.isRecognizing = isRecognizing
        updateAudioStatus()
    }

    /**
     * 更新语音播放状态
     *
     */
    override fun updateAudioStatus() {
        if (isSender) return
        val msg = message ?: return
        val conversationId = msg.from ?: return
        val audioPath = EaseIM.getCache().getAudiPath(conversationId, msg.msgId)
        val audioState = if (audioPath.isNullOrEmpty()) {
            if (isRecognizing) {
                EaseChatAudioStatus.RECOGNIZING
            } else {
                EaseChatAudioStatus.START_RECOGNITION
            }
        } else {
            if (isPlaying) {
                EaseChatAudioStatus.PLAYING
            } else {
                EaseChatAudioStatus.START_PLAY
            }
        }
        setStartPlayBtn(audioState)
    }

    private fun setStartPlayBtn(audioState: EaseChatAudioStatus) {
        this.audioStatus = audioState
        when (audioState) {
            EaseChatAudioStatus.START_PLAY -> {
                ivMsgStartPlay?.isInvisible = false
                progressRecognition?.isVisible = false
                ivMsgStartPlay?.setImageResource(R.drawable.aichat_icon_start_play)
                tvMsgStartPlay?.setText(R.string.aichat_click_to_play)
            }

            EaseChatAudioStatus.PLAYING -> {
                ivMsgStartPlay?.isInvisible = false
                progressRecognition?.isVisible = false
                ivMsgStartPlay?.setImageResource(R.drawable.aichat_icon_stop_play)
                tvMsgStartPlay?.setText(R.string.aichat_playing)
            }

            EaseChatAudioStatus.RECOGNIZING -> {
                ivMsgStartPlay?.isInvisible = true
                progressRecognition?.isVisible = true
                tvMsgStartPlay?.setText(R.string.aichat_recognizing)
            }

            else -> {
                ivMsgStartPlay?.isInvisible = false
                progressRecognition?.isVisible = false
                ivMsgStartPlay?.setImageResource(R.drawable.aichat_icon_start_play)
                tvMsgStartPlay?.setText(R.string.aichat_click_to_recognition)
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
}