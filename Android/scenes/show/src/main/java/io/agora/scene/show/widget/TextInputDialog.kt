package io.agora.scene.show.widget

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    constructor(context: Context) : this(context, R.style.show_text_input_dialog)

    constructor(context: Context, theme: Int) : super(context, theme) {
        init()
    }

    private fun init() {
        setContentView(mBinding.root)
        window?.decorView?.fitsSystemWindows = false
        ViewCompat.setOnApplyWindowInsetsListener(window?.decorView!!){ view: View, insets: WindowInsetsCompat ->

            val imeInset = insets.getInsets(WindowInsetsCompat.Type.ime())
            Log.d(TAG, "imeInset: $imeInset")
            onIMEHeightChanged(imeInset.bottom)

            if(imeInset.bottom == 0){
                window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                            or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                )
            }else{
                window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                            or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                )
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
        onInsertHeightChangedListener?.invoke(height + mBinding.clTextInput.height)
        if (height == 0) {
            if ((mBinding.root.layoutParams as MarginLayoutParams).bottomMargin > 0) {
                dismiss()
            }
        } else {
            mBinding.clTextInput.layoutParams =
                (mBinding.root.layoutParams as MarginLayoutParams).apply {
                    bottomMargin = height
                }
        }
    }


}