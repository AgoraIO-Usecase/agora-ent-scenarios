package io.agora.scene.widget.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import io.agora.scene.base.component.BaseDialog
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.widget.databinding.DialogTopFunctionBinding

/**
 * @author create by zhangwei03
 */
class TopFunctionDialog constructor(context: Context) : BaseDialog<DialogTopFunctionBinding>(context) {
    override fun getViewBinding(inflater: LayoutInflater): DialogTopFunctionBinding {
        return DialogTopFunctionBinding.inflate(inflater)
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
            window?.let { window ->
                window.setBackgroundDrawableResource(android.R.color.transparent)
                window.setDimAmount(0f)
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                window.attributes.apply {
                    val lp = WindowManager.LayoutParams()
                    lp.copyFrom(window.attributes)
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT
                    window.attributes = lp
                }
            }
        setCanceledOnTouchOutside(true)
    }

    override fun initView() {
        binding.layoutReport.setOnClickListener {
            ToastUtils.showToast("举报成功")
            dismiss()
        }
    }

    override fun setGravity() {
        window?.attributes?.gravity = Gravity.TOP
    }
}