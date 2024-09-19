package io.agora.scene.aichat.imkit.widget

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.agora.scene.aichat.databinding.EaseWidgetChatPrimaryMenuBinding
import io.agora.scene.aichat.ext.showSoftKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class EaseInputMenuStyle {

    Single,

    Group,
}

enum class EaseInputMenuStatus {
    Normal,
    Text,
    Voice,
    Calling,
}

interface IChatPrimaryMenu {
    /**
     * Set menu display type
     * @param style
     */
    fun setMenuShowType(style: EaseInputMenuStyle)

    /**
     * Set menu show status
     *
     * @param status
     */
    fun setMenuShowStatus(status: EaseInputMenuStatus)

    /**
     * Hide soft keyboard.
     */
    fun hideSoftKeyboard()

    /**
     * Get EditText
     * @return
     */
    val editText: EditText?

    /**
     * Set up monitoring
     * @param listener
     */
    fun setEaseChatPrimaryMenuListener(listener: EaseChatPrimaryMenuListener?)

    /**
     * Set the menu visibility
     */
    fun setVisible(visible: Int)
}

interface EaseChatPrimaryMenuListener {
    /**
     * when send button clicked
     * @param content
     */
    fun onSendBtnClicked(content: String?)

    /**
     * when call button clicked
     */
    fun onCallBtnClicked()

    /**
     * After typing on the editing text layout.
     */
    fun afterTextChanged(s: Editable?)

    /**
     * Edit text layout key events.
     */
    fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?): Boolean


    /**
     * toggle on/off voice button
     */
    fun onToggleVoiceBtnClicked()

    /**
     * if edit text has focus
     */
    fun onEditTextHasFocus(hasFocus: Boolean)
}

class EaseChatPrimaryMenu @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), IChatPrimaryMenu {

    private val binding: EaseWidgetChatPrimaryMenuBinding by lazy {
        EaseWidgetChatPrimaryMenuBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private var listener: EaseChatPrimaryMenuListener? = null

    private var inputMenuStyle: EaseInputMenuStyle = EaseInputMenuStyle.Single
    private var inputMenuStatus: EaseInputMenuStatus = EaseInputMenuStatus.Normal

    private var hideLayoutTips: Job? = null

    init {
        binding.etSendmessage.run {
            setHorizontallyScrolling(false)
            setMaxLines(4)
        }
        binding.btnSetModeSend.setOnClickListener {
            listener?.run {
                val s = binding.etSendmessage.text.toString()
                binding.etSendmessage.setText("")
                hideSoftKeyboard()
                resetInputMenuType()
                this.onSendBtnClicked(s)
            }
        }
        binding.btnSetModeVoice.setOnClickListener {
            hideLayoutTips?.cancel()
            showSpeakTipWithAnimation(binding.layoutSpeakerTips)
        }
        binding.btnSetModeVoice.setOnLongClickListener {
            setMenuShowStatus(EaseInputMenuStatus.Voice)
            true
        }
        binding.btnSetModeCall.setOnClickListener {
            setMenuShowStatus(EaseInputMenuStatus.Calling)
        }
        binding.etSendmessage.setOnClickListener {
            setMenuShowStatus(EaseInputMenuStatus.Text)
        }
        binding.etSendmessage.setOnEditTextChangeListener(object : EaseInputEditText.OnEditTextChangeListener {
            override fun onClickKeyboardSendBtn(content: String?) {
                listener?.onSendBtnClicked(content)
            }

            override fun onEditTextHasFocus(hasFocus: Boolean) {
                listener?.onEditTextHasFocus(hasFocus)
            }
        })
        binding.etSendmessage.setOnKeyListener(object : OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (listener != null) {
                    return listener?.editTextOnKeyListener(v, keyCode, event) ?: true
                }
                return false
            }
        })
        setMenuShowStatus(EaseInputMenuStatus.Normal)
    }


    private fun showSpeakTipWithAnimation(layout: FrameLayout) {
        layout.pivotX = layout.width / 3f * 2
        layout.pivotY = layout.height.toFloat()

        layout.visibility = View.VISIBLE
        // 放大动画
        ObjectAnimator.ofFloat(layout, "scaleX", 0f, 1f).apply {
            duration = 300 // 动画时长
            interpolator = DecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(layout, "scaleY", 0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }

        // 取消之前的协程任务
        hideLayoutTips?.cancel()

        // 启动新的协程来延迟 1 秒后隐藏
        hideLayoutTips = CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            hideSpeakTipWithAnimation(layout) // 隐藏并缩小 TextView
        }
    }

    private fun hideSpeakTipWithAnimation(layout: FrameLayout) {
        layout.pivotX = layout.width / 3f * 2
        layout.pivotY = layout.height.toFloat()
        // 缩小动画
        ObjectAnimator.ofFloat(layout, "scaleX", 1f, 0f).apply {
            duration = 300 // 动画时长
            interpolator = DecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(layout, "scaleY", 1f, 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }

        // 动画结束后隐藏 TextView
        CoroutineScope(Dispatchers.Main).launch {
            delay(300) // 等待动画结束
            layout.visibility = View.GONE
        }
    }

    private fun resetInputMenuType() {
        val content = binding.etSendmessage.text
        val isContentEmpty = content.isNullOrEmpty()
        binding.btnSetModeCall.isVisible = inputMenuStyle == EaseInputMenuStyle.Single && isContentEmpty
        binding.btnSetModeSend.isVisible = !isContentEmpty
        binding.btnSetModeVoice.isVisible = isContentEmpty
    }

    override fun setMenuShowStatus(status: EaseInputMenuStatus){
        this.inputMenuStatus = status
        when (status) {
            EaseInputMenuStatus.Normal -> {
                hideSoftKeyboard()
                resetInputMenuType()
                checkSendButton()
            }
            EaseInputMenuStatus.Text -> {
                resetInputMenuType()
                binding.btnSetModeVoice.visibility = GONE
                binding.btnSetModeCall.visibility = GONE
                checkSendButton()
                showSoftKeyboard(editText)
            }
            EaseInputMenuStatus.Voice -> {
                hideLayoutTips?.cancel()
                binding.layoutSpeakerTips.isVisible = false
                listener?.onToggleVoiceBtnClicked()
            }
            EaseInputMenuStatus.Calling -> {
                hideSoftKeyboard()
                resetInputMenuType()
                listener?.onCallBtnClicked()
            }
        }
    }

    private fun checkSendButton() {
        val content = binding.etSendmessage.text
        setSendButtonVisible(content)
    }

    private fun setSendButtonVisible(content: CharSequence?) {
        binding.btnSetModeSend.isActivated = !TextUtils.isEmpty(content)
    }

    /**
     * show soft keyboard
     * @param et
     */
    private fun showSoftKeyboard(et: EditText?) {
        et?.showSoftKeyboard()
        binding.btnSetModeSend.visibility = VISIBLE
    }

    private fun showSendButton(s: CharSequence?) {
        setSendButtonVisible(s)
    }

    private val onTextWatcherListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            showSendButton(s)
        }

        override fun afterTextChanged(s: Editable?) {
            listener?.afterTextChanged(s)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.etSendmessage.addTextChangedListener(onTextWatcherListener)
    }

    override fun onDetachedFromWindow() {
        binding.etSendmessage.removeTextChangedListener(onTextWatcherListener)
        super.onDetachedFromWindow()
    }

    override fun setMenuShowType(style: EaseInputMenuStyle) {
        this.inputMenuStyle = style
        resetInputMenuType()
    }

    override fun hideSoftKeyboard() {
        if (context is Activity) {
            val activity = context
            val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            // 检查当前焦点
            activity.currentFocus?.let { view ->
                inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                // 清除焦点
                view.clearFocus()
            }
        }
    }

    fun onShowKeyboardStatus() {
        resetInputMenuType()
        binding.btnSetModeVoice.visibility = GONE
        binding.btnSetModeCall.visibility = GONE
        binding.btnSetModeSend.visibility = VISIBLE
        checkSendButton()
    }

    fun onHideKeyboardStatus() {
        if (context is Activity) {
            context.currentFocus?.clearFocus()
        }
        resetInputMenuType()
        checkSendButton()
    }

    override val editText: EditText
        get() = binding.etSendmessage

    override fun setEaseChatPrimaryMenuListener(listener: EaseChatPrimaryMenuListener?) {
        this.listener = listener
    }

    override fun setVisible(visible: Int) {
        this.visibility = visible
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
    }
}