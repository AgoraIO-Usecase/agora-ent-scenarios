package io.agora.scene.widget.dialog

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.agora.scene.base.component.BaseDialog
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.UiUtil
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
                window.decorView.setPaddingRelative(0, 0, 0, 0)
                window.decorView.fitsSystemWindows = true
                window.attributes.apply {
                    val lp = WindowManager.LayoutParams()
                    lp.copyFrom(window.attributes)
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT
                    window.attributes = lp
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                    window.attributes = this
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

    /**
     * 获取屏幕宽度
     */
    fun screenWidth(activity: Activity): Int {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.x
    }

    /**
     * 获取屏幕高度
     */
    fun screenHeight(activity: Activity): Int {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.y
    }
}