package io.agora.scene.aichat.imkit.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputEditText

class EaseInputEditText @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputEditText(
    context!!, attrs, defStyleAttr
), View.OnKeyListener, OnEditorActionListener {
    private var ctrlPress = false
    private var listener: OnEditTextChangeListener? = null

    init {
        setOnKeyListener(this)
        setOnEditorActionListener(this)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        listener?.onEditTextHasFocus(focused)
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            if (event?.action == KeyEvent.ACTION_DOWN) {
                ctrlPress = true
            } else if (event?.action == KeyEvent.ACTION_UP) {
                ctrlPress = false
            }
        }
        return false
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        return if (actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN && ctrlPress) {
            val s = text.toString()
            listener?.let {
                if (s.isNotEmpty()) {
                    setText("")
                    it.onClickKeyboardSendBtn(s)
                }
            }
            true
        } else {
            false
        }
    }

    fun setOnEditTextChangeListener(listener: OnEditTextChangeListener?) {
        this.listener = listener
    }

    interface OnEditTextChangeListener {
        /**
         * when send button clicked
         * @param content
         */
        fun onClickKeyboardSendBtn(content: String?)

        /**
         * if edit text has focus
         */
        fun onEditTextHasFocus(hasFocus: Boolean)
    }
}