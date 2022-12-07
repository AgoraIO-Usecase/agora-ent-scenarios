package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowWidgetBottomLightDialogBinding
import io.agora.scene.widget.utils.StatusBarUtil


open class BottomLightDialog : BottomSheetDialog {
    private val mBinding by lazy { ShowWidgetBottomLightDialogBinding.inflate(LayoutInflater.from(context)) }

    constructor(context: Context) : this(context, R.style.show_bottom_dialog)
    constructor(context: Context, theme: Int) : super(context, theme){
        super.setContentView(mBinding.root)
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        StatusBarUtil.hideStatusBar(window, false)
    }


    override fun setContentView(view: View) {
        throw RuntimeException("setContentView is not allow. Please use setTopView or setBottomView")
    }

    protected fun setTopView(view: View){
        mBinding.topLayout.addView(view)
    }

    protected fun setBottomView(view: View){
        mBinding.bottomLayout.addView(view)
    }



}