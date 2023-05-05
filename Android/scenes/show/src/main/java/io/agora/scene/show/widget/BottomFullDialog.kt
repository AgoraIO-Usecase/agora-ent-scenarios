package io.agora.scene.show.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.scene.show.R
import io.agora.scene.widget.utils.StatusBarUtil

open class BottomFullDialog : BottomSheetDialog {
    private val mParentContext: Context

    constructor(context: Context) : this(context, R.style.show_bottom_full_dialog)
    constructor(context: Context, theme: Int) : super(context, theme) {
        mParentContext = context
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val container = findViewById<View>(com.google.android.material.R.id.container)
        bottomSheet?.let {
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        behavior.isDraggable = false
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        container?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPaddingRelative(inset.left, 0, inset.right, inset.bottom)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        StatusBarUtil.hideStatusBar(window, true)
    }

    fun getParentContext() = mParentContext

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        StatusBarUtil.hideStatusBar(window, false)
    }
}