package io.agora.scene.show.widget

import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowWidgetTextInputDialogBinding

class TextInputDialog : BottomSheetDialog {
    private val TAG = "TextInputDialog"
    private val mBinding by lazy {
        ShowWidgetTextInputDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }
    private var onSentClickListener: ((DialogInterface, String) -> Unit)? = null
    private var onInsertHeightChangedListener: ((Int) -> Unit)? = null
    private var visibleBottomMax = 0
    private var isKeyboardShowed = false

    constructor(context: Context) : this(context, R.style.show_text_input_dialog)

    constructor(context: Context, theme: Int) : super(context, theme) {
        init()
    }

    private fun init() {
        setContentView(mBinding.root)
        mBinding.clTextInput.isVisible = false
        window?.decorView?.fitsSystemWindows = false
        window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                    or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        )
        ViewCompat.setOnApplyWindowInsetsListener(window?.decorView!!){ view: View, insets: WindowInsetsCompat ->

            val imeInset = insets.getInsets(WindowInsetsCompat.Type.ime())
            Log.d(TAG, "imeInset: $imeInset")

            if (imeInset.bottom == 0) {
                ViewCompat.setOnApplyWindowInsetsListener(window?.decorView!!, null)
                window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                            or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                )
                window?.decorView?.let { decorView ->
                    decorView.viewTreeObserver.addOnGlobalLayoutListener {
                        val rect = Rect()
                        decorView.getWindowVisibleDisplayFrame(rect)
                        if(rect.bottom > visibleBottomMax){
                            visibleBottomMax = rect.bottom
                        }

                        Log.d(TAG, "imeInset global layout rect=$rect, rootHeight=${decorView.height}")
                        val imeHeight = visibleBottomMax - rect.bottom
                        onIMEHeightChanged(imeHeight)
                    }
                }
            } else {
                onIMEHeightChanged(imeInset.bottom)
            }

            insets
        }

        mBinding.editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                onSentClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        mBinding.btnSent.setOnClickListener {
            onSentClick()
        }

        mBinding.editText.requestFocus()
    }

    private fun onSentClick() {
        val text = mBinding.editText.text.toString()
        if(text.isEmpty()){
            Toast.makeText(context, R.string.show_text_input_empty, Toast.LENGTH_SHORT).show()
        }else{
            onSentClickListener?.invoke(this, text)
        }
    }

    fun setMaxInput(max: Int): TextInputDialog{
        mBinding.editText.filters = arrayOf(
            LengthFilter(max)
        )
        return this
    }

    fun setOnInsertHeightChangeListener(onInsertHeightChanged: (Int) -> Unit): TextInputDialog {
        onInsertHeightChangedListener = onInsertHeightChanged
        return this
    }

    fun setOnSentClickListener(onSentClick: (DialogInterface, String) -> Unit): TextInputDialog {
        onSentClickListener = onSentClick
        return this
    }

    override fun onStop() {
        super.onStop()
        onInsertHeightChangedListener?.invoke(0)
    }

    private fun onIMEHeightChanged(height: Int) {
        onInsertHeightChangedListener?.invoke(height)
        if (height == 0) {
            if (isKeyboardShowed) {
                dismiss()
            }
        } else {
            (mBinding.root.layoutParams as MarginLayoutParams).let { params ->
                if(params.bottomMargin != height){
                    params.bottomMargin = height
                    mBinding.root.layoutParams = params
                    mBinding.clTextInput.isVisible = true
                }
            }

            isKeyboardShowed = true
        }
    }


}