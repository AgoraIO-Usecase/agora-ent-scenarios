package io.agora.scene.show.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.scene.show.R
import io.agora.scene.widget.utils.StatusBarUtil

open class BottomFullDialog : BottomSheetDialog {
    private val mParentContext: Context
    private var changeStatusBar = true

    constructor(context: Context) : this(context, R.style.show_bottom_dialog)
    constructor(context: Context, theme: Int) : super(context, theme) {
        mParentContext = context
    }

    override fun onStart() {
        super.onStart()
        behavior.isDraggable = false
        behavior.peekHeight = Int.MAX_VALUE
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    onDialogStatusChanged(mParentContext, false)
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    onDialogStatusChanged(mParentContext, true)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        StatusBarUtil.hideNavigation(window)
        onDialogStatusChanged(mParentContext, true)
    }

    override fun onStop() {
        super.onStop()
        onDialogStatusChanged(mParentContext, false)
    }

    fun getParentContext() = mParentContext

    fun setChangeStatusBar(change: Boolean) {
        changeStatusBar = change
    }

    open fun onDialogStatusChanged(parentContext: Context, visibleAndIdle: Boolean) {
        if (changeStatusBar) {
            if (visibleAndIdle) {
                (parentContext as? Activity)?.apply {
                    StatusBarUtil.hideStatusBar(window, Color.WHITE, true)
                }
            } else {
                (parentContext as? Activity)?.apply {
                    StatusBarUtil.hideStatusBar(window, false)
                }
            }
        }
    }

    override fun setContentView(view: View) {
        view.fitsSystemWindows = true
        super.setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(
            window?.decorView!!
        ) { v, insets ->
            view.layoutParams = (view.layoutParams)?.apply {
                height = window?.decorView?.height ?: 0
                if(height == 0){
                    height = view.resources.displayMetrics.heightPixels
                }
            }
            val inset =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(inset.left, 0, inset.right, 0)
            WindowInsetsCompat.CONSUMED
        }
    }
}