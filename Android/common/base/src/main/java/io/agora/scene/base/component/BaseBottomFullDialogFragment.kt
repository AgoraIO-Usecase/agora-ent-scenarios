package io.agora.scene.base.component

import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.agora.scene.base.utils.UiUtil

abstract class BaseBottomFullDialogFragment<B : ViewBinding?> : BottomSheetDialogFragment() {

    var mBinding: B? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = getViewBinding(inflater, container)
        return if (mBinding != null) mBinding!!.root else null
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater?, container: ViewGroup?): B?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPaddingRelative(inset.left, 0, inset.right, 0)
            WindowInsetsCompat.CONSUMED
        }
        requireDialog().setOnShowListener { dialog: DialogInterface? ->
            (view.parent as ViewGroup).setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun onStart() {
        super.onStart()
        val viewRoot: FrameLayout? = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        viewRoot?.apply {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        val bottomSheetBehavior = BottomSheetBehavior.from(view?.parent as View)
        bottomSheetBehavior.isDraggable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    private fun getViewBindingByReflect(inflater: LayoutInflater, container: ViewGroup?): B? {
        try {
            val c = UiUtil.getGenericClass<B>(javaClass, 0)
            return UiUtil.getViewBinding(c, inflater, container)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    protected fun hideKeyboard(editText: EditText) {
        editText.clearFocus()
        val context = activity ?: return
        // 隐藏软键盘
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    protected fun showKeyboard(editText: EditText?) {
        val context = activity ?: return
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0)
    }
}