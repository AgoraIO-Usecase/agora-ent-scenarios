package io.agora.scene.aichat.imkit.widget

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.EaseWidgetChatPrimaryMenuBinding
import io.agora.scene.aichat.ext.showSoftKeyboard

enum class EaseInputMenuStyle {

    Single,


    Group,
}

interface IChatPrimaryMenu {
    /**
     * Set menu display type
     * @param style
     */
    fun setMenuShowType(style: EaseInputMenuStyle)

    /**
     * Show EditText but hide soft keyboard.
     */
    fun showNormalStatus()

    /**
     * Show EditText and soft keyboard.
     */
    fun showTextStatus()

    /**
     * Show voice style and hide other status.
     */
    fun showVoiceStatus()

    /**
     * Show voice style and hide other status.
     */
    fun showCallingStatus()

    /**
     * Hide soft keyboard.
     */
    fun hideSoftKeyboard()

    /**
     * Insert text
     * @param text
     */
    fun onTextInsert(text: CharSequence?)

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
     * toggle on/off text button
     */
    fun onToggleTextBtnClicked()

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

    private var inputMenuStyle: EaseInputMenuStyle? = EaseInputMenuStyle.Single

    private val inputManager: InputMethodManager by lazy { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    init {
        binding.etSendmessage.requestFocus()
        binding.etSendmessage.run {
            setHorizontallyScrolling(false)
            setMaxLines(4)
        }
        binding.btnSetModeSend.setOnClickListener {
            listener?.run {
                val s = binding.etSendmessage.text.toString()
                binding.etSendmessage.setText("")
                hideSoftKeyboard()
                setInputMenuType()
                this.onSendBtnClicked(s)
            }
        }
        binding.btnSetModeVoice.setOnClickListener {
            showVoiceStatus()
        }
        binding.btnSetModeCall.setOnClickListener {
            showCallingStatus()
        }
        binding.etSendmessage.setOnClickListener {
            showTextStatus()
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
        showNormalStatus()
    }

    private fun setInputMenuType() {
        if (inputMenuStyle == EaseInputMenuStyle.Single) {
            binding.btnSetModeCall.visibility = VISIBLE
            binding.btnSetModeVoice.visibility = VISIBLE
            binding.btnSetModeSend.visibility = VISIBLE
        } else if (inputMenuStyle == EaseInputMenuStyle.Group) {
            binding.btnSetModeCall.visibility = GONE
            binding.btnSetModeVoice.visibility = VISIBLE
            binding.btnSetModeSend.visibility = VISIBLE
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
        setInputMenuType()
    }

    override fun showNormalStatus() {
        hideSoftKeyboard()
        setInputMenuType()
        checkSendButton()
    }

    override fun showTextStatus() {
        setInputMenuType()
        binding.btnSetModeVoice.visibility = GONE
        binding.btnSetModeCall.visibility = GONE
        checkSendButton()
        showSoftKeyboard(editText)
        listener?.onToggleTextBtnClicked()
    }

    override fun showVoiceStatus() {
        hideSoftKeyboard()
        setInputMenuType()
        listener?.onToggleVoiceBtnClicked()
    }

    override fun showCallingStatus() {
        hideSoftKeyboard()
        setInputMenuType()
        listener?.onCallBtnClicked()
    }

    override fun hideSoftKeyboard() {
        if (context !is Activity) {
            return
        }
        binding.etSendmessage.requestFocus()
        if (context.window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            context.currentFocus?.let {
                inputManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
        binding.etSendmessage.clearFocus()
    }

    override fun onTextInsert(text: CharSequence?) {
        val start = binding.etSendmessage.selectionStart
        val editable = binding.etSendmessage.editableText
        editable.insert(start, text)
        showTextStatus()
    }

    override val editText: EditText
        get() = binding.etSendmessage

    override fun setEaseChatPrimaryMenuListener(listener: EaseChatPrimaryMenuListener?) {
        this.listener = listener
    }

    override fun setVisible(visible: Int) {
        this.visibility = visible
    }
}