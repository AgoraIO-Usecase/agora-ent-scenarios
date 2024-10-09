package io.agora.scene.aichat.imkit.widget.chatrow

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.agora.scene.aichat.R
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageStatus
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.callback.OnItemBubbleClickListener
import io.agora.scene.aichat.imkit.callback.OnMessageListItemClickListener
import io.agora.scene.aichat.imkit.extensions.getDateFormat
import io.agora.scene.aichat.imkit.extensions.getMsgSendUser
import io.agora.scene.aichat.imkit.extensions.isFail
import io.agora.scene.aichat.imkit.extensions.isSuccess
import io.agora.scene.aichat.imkit.helper.DateFormatHelper
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.model.getBotIds
import io.agora.scene.aichat.imkit.model.isChat
import io.agora.scene.aichat.imkit.model.isGroup
import io.agora.scene.aichat.imkit.provider.getSyncUser

abstract class EaseChatRow @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyle: Int = 0,
    val isSender: Boolean
) : FrameLayout(context, attrs, defStyle) {

    val inflater by lazy { LayoutInflater.from(context) }

    val timeStampView: TextView? by lazy { findViewById(R.id.timestamp) }
    val userAvatarView: ImageView? by lazy { findViewById(R.id.iv_userhead) }
    val bubbleLayout: ViewGroup? by lazy { findViewById(R.id.bubble) }
    val usernickView: TextView? by lazy { findViewById(R.id.tv_username) }
    val progressBar: ProgressBar? by lazy { findViewById(R.id.progress_bar) }
    val statusView: ImageView? by lazy { findViewById(R.id.msg_status) }
    protected val topBubbleLayout: ConstraintLayout? by lazy { findViewById(R.id.ll_top_bubble) }
    protected val bottomBubbleLayout: ConstraintLayout? by lazy { findViewById(R.id.ll_bubble_bottom) }

    var message: ChatMessage? = null
    var position: Int = -1
    var count: Int = 0

    var itemClickListener: OnMessageListItemClickListener? = null
    protected var itemBubbleClickListener: OnItemBubbleClickListener? = null

    var audioStatus = EaseChatAudioStatus.UNKNOWN

    init {
        onInflateView()
    }

    /**
     * Bind data to view.
     * The method is called by ViewHolder.
     */
    fun setUpView(message: ChatMessage?, position: Int) {
        this.message = message
        this.position = position
        setUpBaseView()
        onSetUpView()
    }

    fun updateView() {
        updateMessageByStatus()
    }

    /**
     * Calls by ViewHolder.
     */
    fun setTimestamp(preMessage: ChatMessage?) {
        if (position == 0) {
            preMessage?.run {
                timeStampView?.text = getDateFormat(true)
                timeStampView?.visibility = VISIBLE
            }
        } else {
            setOtherTimestamp(preMessage)
        }
    }

    private fun setUpBaseView() {
        setTimestamp(message)
        setAvatarAndNickname()
        updateMessageStatus()
        initView()
        initListener()
    }

    private fun initView() {

    }

    private fun updateMessageStatus() {
        updateMessageByStatus()
        updateSendMessageStatus()
        updateAudioStatus()
    }

    /// Update audio status
    protected open fun updateAudioStatus() {}

    private fun updateMessageByStatus() {
        message?.run {
            when (status()) {
                ChatMessageStatus.CREATE -> {
                    // When get local messages and check it's status, change the create status to fail.
                }

                ChatMessageStatus.INPROGRESS -> {
                    onMessageInProgress()
                }

                ChatMessageStatus.SUCCESS -> {
                    onMessageSuccess()
                }

                ChatMessageStatus.FAIL -> {
                    onMessageError()
                }
            }
        }
    }

    private fun updateSendMessageStatus() {
        message?.run {
            if (isSend()) {
                if (isSuccess()) {
                    showSuccessStatus()
                }
                // update error status
                setSendMessageFailStatus()
            }
        }
    }

    private fun updateMessageErrorStatus() {
        showErrorStatus()
    }

    fun setSendMessageFailStatus() {
        message?.run {
            if (isSend() && isFail()) {
                statusView?.visibility = View.VISIBLE
            }
        }
    }

    /**
     * All user info is from message ext.
     */
    private fun setAvatarAndNickname() {
        message?.let {
            if (isSender) {
                userAvatarView?.isVisible = false
                usernickView?.isVisible = false
            } else {
                val fromId = it.from
                val fromUser: EaseProfile =
                    EaseIM.getUserProvider().getSyncUser(fromId) ?: EaseProfile(fromId)
                val isSingleGroup = fromUser.isGroup() && fromUser.getBotIds().size == 1
                if (fromUser.isChat() || isSingleGroup) {
                    userAvatarView?.isVisible = false
                    usernickView?.isVisible = false
                }else{
                    val userInfo = it.getMsgSendUser()
                    userAvatarView?.isVisible = true
                    usernickView?.isVisible = true
                    userAvatarView?.loadCircleImage(userInfo.avatar ?: "")
                    usernickView?.setText(userInfo.getNotEmptyName())
                }
            }
        }
    }

    open fun setOtherTimestamp(preMessage: ChatMessage?) {
        if (ChatClient.getInstance().options.isSortMessageByServerTime) {
            message?.let {
                if (preMessage != null && DateFormatHelper.isCloseEnough(it.msgTime, preMessage.msgTime)) {
                    timeStampView?.visibility = GONE
                    return
                }
            }
        } else {
            message?.let {
                if (preMessage != null && DateFormatHelper.isCloseEnough(
                        it.localTime(), preMessage.localTime()
                    )
                ) {
                    timeStampView?.visibility = GONE
                    return
                }
            }
        }

        message?.run {
            timeStampView?.text = getDateFormat(true)
            timeStampView?.visibility = VISIBLE
        }
    }

    private fun initListener() {
        bubbleLayout?.let {
            it.setOnClickListener {
                if (itemClickListener?.onBubbleClick(message) == true) {
                    return@setOnClickListener
                }
                itemBubbleClickListener?.onBubbleClick(message)
            }
            it.setOnLongClickListener { view ->
                return@setOnLongClickListener itemClickListener?.onBubbleLongClick(view, message) == true
            }
        }
        statusView?.let {
            it.setOnClickListener {
                itemClickListener?.onResendClick(message)
            }
        }
        userAvatarView?.let {
            it.setOnClickListener {
                message?.run {
                    itemClickListener?.onUserAvatarClick(if (isSend()) ChatClient.getInstance().currentUser else from)
                }
            }
            it.setOnLongClickListener {
                message?.run {
                    if (itemClickListener != null) {
                        itemClickListener?.onUserAvatarLongClick(if (isSend()) ChatClient.getInstance().currentUser else from)
                        return@setOnLongClickListener true
                    }
                }
                return@setOnLongClickListener false
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    /**
     * Show success status.
     */
    protected open fun showSuccessStatus() {
        progressBar?.visibility = INVISIBLE
        statusView?.visibility = INVISIBLE
    }

    protected open fun showErrorStatus() {
        progressBar?.visibility = INVISIBLE
        statusView?.visibility = VISIBLE
    }

    protected open fun showInProgressStatus() {
        progressBar?.visibility = VISIBLE
        statusView?.visibility = INVISIBLE
    }

    protected open fun onMessageSuccess() {
        updateSendMessageStatus()
    }

    protected open fun onMessageError() {
        updateMessageErrorStatus()
    }

    protected open fun onMessageInProgress() {
        showInProgressStatus()
    }

    protected open fun isSend(): Boolean {
        return isSender
    }

    /**
     * Set message item click listeners.
     * @param listener
     */
    fun setOnMessageListItemClickListener(listener: OnMessageListItemClickListener?) {
        this.itemClickListener = listener
    }

    fun setOnItemBubbleClickListener(listener: OnItemBubbleClickListener?) {
        this.itemBubbleClickListener = listener
    }

    /**
     * Override it and inflate your view in this method.
     */
    abstract fun onInflateView()

    /**
     * Override it and set data or listener in this method.
     */
    abstract fun onSetUpView()

    companion object {
        const val TAG = "EaseChatRow"
    }

}