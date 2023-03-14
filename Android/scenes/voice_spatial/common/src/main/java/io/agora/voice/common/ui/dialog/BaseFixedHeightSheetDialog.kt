package io.agora.voice.common.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * @author create by zhangwei03
 */
abstract class BaseFixedHeightSheetDialog<B : ViewBinding?> : BaseSheetDialog<B>() {

    private val heightRadio = 0.7

    override fun onStart() {
        super.onStart()
        dialog?.window?.setDimAmount(0f) //设置布局
//        val h = (heightRadio * resources.displayMetrics.heightPixels).toInt()
//        val viewRoot: FrameLayout? = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
//        viewRoot?.apply {
//            layoutParams.width = -1
//            layoutParams.height = h
//        }

        var bottomSheetBehavior = BottomSheetBehavior.from(view?.parent as View) //dialog的高度
//        bottomSheetBehavior.isHideable = false
//        bottomSheetBehavior.peekHeight = h
    }
}

internal class FixedHeightSheetDialog constructor(
    context: Context,
    @StyleRes theme: Int,
    private val fixedHeight: Int,
    private val dpiRatio: Float,
) : BottomSheetDialog(context, theme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPeekHeight()
        setMaxHeight(fixedHeight)
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setPeekHeight() {
        if (dpiRatio > 1.78) return
        getBottomSheetBehavior()?.peekHeight = fixedHeight
    }

    private fun setMaxHeight(maxHeight: Int) {
        if (maxHeight <= 0) return
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, maxHeight)
            setGravity(Gravity.BOTTOM)
        }
    }

    private fun getBottomSheetBehavior(): BottomSheetBehavior<View>? {
        val view: View? = window?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        return view?.let { BottomSheetBehavior.from(view) }
    }
}