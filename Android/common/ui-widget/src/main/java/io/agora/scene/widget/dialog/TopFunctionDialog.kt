package io.agora.scene.widget.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import io.agora.scene.base.component.BaseDialog
import io.agora.scene.widget.databinding.DialogTopFunctionBinding
import io.agora.scene.widget.utils.StatusBarUtil
import io.agora.scene.widget.R
import io.agora.scene.widget.toast.CustomToast

/**
 * @author create by zhangwei03
 */
class TopFunctionDialog constructor(context: Context, val showReportUser: Boolean = false) :
    BaseDialog<DialogTopFunctionBinding>(context) {
    override fun getViewBinding(inflater: LayoutInflater): DialogTopFunctionBinding {
        return DialogTopFunctionBinding.inflate(inflater)
    }

    /**
     * report content
     */
    var reportContentCallback: (() -> Unit)? = null

    /**
     * report user
     */
    var reportUserCallback: (() -> Unit)? = null

    override fun setContentView(view: View) {
        super.setContentView(view)
        window?.let { window ->
            // fix 小米部分机型不能占用状态栏
            StatusBarUtil.hideStatusBar(window, 0xF2151325.toInt(), true)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(0f)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
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
        binding.layoutReportUser.visibility = if (showReportUser) View.VISIBLE else View.GONE
        binding.layoutReportContent.setOnClickListener {
            CustomToast.show(R.string.common_report_content_tips)
            reportContentCallback?.invoke()
            dismiss()
        }

        binding.layoutReportUser.setOnClickListener {
            CustomToast.show(R.string.common_report_user_tips)
            reportUserCallback?.invoke()
            dismiss()
        }
    }

    override fun setGravity() {
        window?.attributes?.gravity = Gravity.TOP
    }
}