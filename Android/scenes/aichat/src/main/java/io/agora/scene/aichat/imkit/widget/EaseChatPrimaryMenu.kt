package io.agora.scene.aichat.imkit.widget

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.EaseWidgetChatPrimaryMenuBinding
import io.agora.scene.aichat.ext.hideSoftKeyboard
import io.agora.scene.aichat.ext.showSoftKeyboard
import io.agora.scene.aichat.ext.vibrate

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
     * Get EditText
     * @return
     */
    val editText: EditText?

    /**
     * Set up monitoring
     * @param listener
     */
    fun setEaseChatPrimaryMenuListener(listener: EaseChatPrimaryMenuListener?)
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

    /**
     * On recorder btn clicked
     *
     */
    fun onRecorderBtnClicked()

    /**
     * On start recording action
     *
     */
    fun onStartRecordingAction()

    /**
     * On send recording action
     *
     */
    fun onSendRecordingAction()

    /**
     * On text change to cancel
     *
     * @param cancel
     */
    fun onTextChangeToCancel(cancel: Boolean)

    /**
     * On cancel recording action
     *
     */
    fun onCancelRecordingAction()
}

class EaseChatPrimaryMenu @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), IChatPrimaryMenu, EaseRecordViewListener {

    private val binding: EaseWidgetChatPrimaryMenuBinding by lazy {
        EaseWidgetChatPrimaryMenuBinding.inflate(LayoutInflater.from(context))
    }

    private var primaryMenuListener: EaseChatPrimaryMenuListener? = null

    private var inputMenuStyle: EaseInputMenuStyle = EaseInputMenuStyle.Single
    private var inputMenuStatus: EaseInputMenuStatus = EaseInputMenuStatus.Normal

    init {
        addView(binding.root)
        binding.etSendmessage.run {
            setHorizontallyScrolling(false)
            setMaxLines(4)
        }
        binding.etSendmessage.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.etSendmessage.setHintTextColor(ContextCompat.getColor(context,io.agora.scene.widget.R.color.def_text_grey_979))
            } else {
                binding.etSendmessage.setHintTextColor(ContextCompat.getColor(context, io.agora.scene.widget.R.color.def_text_grey_303))
            }
        }
        binding.btnSetModeSend.setOnClickListener {
            primaryMenuListener?.run {
                val s = binding.etSendmessage.text.toString()
                binding.etSendmessage.setText("")
                hideSoftKeyboard()
                resetInputMenuType()
                this.onSendBtnClicked(s)
            }
        }

        setVoiceTouchEvent()
        binding.btnSetModeCall.setOnClickListener {
            setMenuShowStatus(EaseInputMenuStatus.Calling)
        }
        binding.etSendmessage.setOnClickListener {
            setMenuShowStatus(EaseInputMenuStatus.Text)
        }
        binding.etSendmessage.setOnEditTextChangeListener(object : EaseInputEditText.OnEditTextChangeListener {
            override fun onClickKeyboardSendBtn(content: String?) {
                primaryMenuListener?.onSendBtnClicked(content)
            }

            override fun onEditTextHasFocus(hasFocus: Boolean) {
                primaryMenuListener?.onEditTextHasFocus(hasFocus)
            }
        })
        binding.etSendmessage.setOnKeyListener(object : OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (primaryMenuListener != null) {
                    return primaryMenuListener?.editTextOnKeyListener(v, keyCode, event) ?: true
                }
                return false
            }
        })
        binding.recordView.setRecordViewListener(this)
        setMenuShowStatus(EaseInputMenuStatus.Normal)
    }

    private fun setVoiceTouchEvent() {
        binding.btnSetModeVoice.setOnTouchListener(object : View.OnTouchListener {
            private var isLongPress = false
            private var longPressHandler: Handler? = null

            private var x = 0f
            private var y = 0f

            private var longPressRunnable = Runnable {
                isLongPress = true

                // 将 btnSetModeVoice 的坐标转换为 recordView 内部的相对坐标
                val location = IntArray(2)
                binding.recordView.getLocationOnScreen(location)
                val recordViewX = x + binding.btnSetModeVoice.left - location[0]
                val recordViewY = y + binding.btnSetModeVoice.top - location[1]

                binding.recordView.isVisible = true
                binding.recordView.requestFocus()
                binding.recordView.isClickable = true
                binding.recordView.isFocusable = true
                setMenuShowStatus(EaseInputMenuStatus.Voice)

                Log.e("EaseChatPrimaryMenu", "onTouch $x $y")
                Log.e("EaseChatPrimaryMenu", "onTouch 11 $recordViewX $recordViewY")

                // 转发 ACTION_DOWN 事件到 recordView
                binding.recordView.dispatchTouchEvent(
                    MotionEvent.obtain(
                        0, 0, MotionEvent.ACTION_DOWN, recordViewX, recordViewY, 0
                    )
                )
            }


            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = event.rawX
                        y = event.rawY
                        isLongPress = false
                        longPressHandler = Handler(Looper.getMainLooper())
                        longPressHandler?.postDelayed(
                            longPressRunnable,
                            ViewConfiguration.getLongPressTimeout().toLong()
                        )
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        x = event.rawX
                        y = event.rawY
                        // 当长按后，转移事件给 EaseRecordView
                        if (isLongPress) {
                            val location = IntArray(2)
                            binding.recordView.getLocationOnScreen(location)
                            val recordViewX = x - location[0]
                            val recordViewY = y - location[1]
                            binding.recordView.dispatchTouchEvent(
                                MotionEvent.obtain(
                                    event.downTime,
                                    event.eventTime,
                                    MotionEvent.ACTION_MOVE,
                                    recordViewX,
                                    recordViewY,
                                    event.metaState
                                )
                            )
                            return true
                        }
                        return isLongPress
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        x = event.x
                        y = event.y
                        longPressHandler?.removeCallbacks(longPressRunnable)
                        longPressHandler = null
                        if (isLongPress) {
                            // 将 ACTION_UP 事件传递给 EaseRecordView
                            val location = IntArray(2)
                            binding.recordView.getLocationOnScreen(location)
                            val recordViewX = x - location[0]
                            val recordViewY = y - location[1]

                            binding.recordView.dispatchTouchEvent(
                                MotionEvent.obtain(
                                    event.downTime,
                                    event.eventTime,
                                    MotionEvent.ACTION_UP,
                                    recordViewX,
                                    recordViewY,
                                    event.metaState
                                )
                            )
                        } else {
                            // Handle short press event
                            primaryMenuListener?.onRecorderBtnClicked()
                        }
                        return isLongPress
                    }
                }
                return false
            }
        })
    }

    private fun resetInputMenuType() {
        val content = binding.etSendmessage.text
        val isContentEmpty = content.isNullOrEmpty()
        binding.btnSetModeCall.isVisible = inputMenuStyle == EaseInputMenuStyle.Single && isContentEmpty
        binding.btnSetModeSend.isVisible = !isContentEmpty
        binding.btnSetModeVoice.isVisible = isContentEmpty
    }

    override fun setMenuShowStatus(status: EaseInputMenuStatus) {
        this.inputMenuStatus = status
        when (status) {
            EaseInputMenuStatus.Normal -> {
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
                context?.vibrate()
                primaryMenuListener?.onToggleVoiceBtnClicked()
                binding.rlBottom.isVisible = false
            }

            EaseInputMenuStatus.Calling -> {
                resetInputMenuType()
                primaryMenuListener?.onCallBtnClicked()
            }
        }
    }

    override fun onStartRecordingAction() {
        primaryMenuListener?.onStartRecordingAction()
    }

    override fun onCancelRecordingAction() {
        binding.recordView.isVisible = false
        binding.rlBottom.isVisible = true
        primaryMenuListener?.onCancelRecordingAction()
//        binding.btnSetModeVoice.setOnTouchListener(null)
    }

    override fun onSendRecordingAction() {
        binding.recordView.isVisible = false
        binding.rlBottom.isVisible = true
        primaryMenuListener?.onSendRecordingAction()
//        binding.btnSetModeVoice.setOnTouchListener(null)
    }

    override fun onTextChangeToCancel(cancel: Boolean) {
       primaryMenuListener?.onTextChangeToCancel(cancel)
    }

    private fun checkSendButton() {
        val content = binding.etSendmessage.text
        setSendButtonActivate(content)
    }

    private fun setSendButtonActivate(content: CharSequence?) {
        binding.btnSetModeSend.isActivated = !TextUtils.isEmpty(content)
    }

    /**
     * show soft keyboard
     * @param et
     */
    private fun showSoftKeyboard(et: EditText?) {
        if (context is Activity) {
            et?.showSoftKeyboard(context)
        }
        binding.btnSetModeSend.visibility = VISIBLE
    }

    private val onTextWatcherListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setSendButtonActivate(s)
        }

        override fun afterTextChanged(s: Editable?) {
            primaryMenuListener?.afterTextChanged(s)
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

    fun hideSoftKeyboard() {
        if (context is Activity) {
            editText.hideSoftKeyboard(context)
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
        this.primaryMenuListener = listener
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
    }
}