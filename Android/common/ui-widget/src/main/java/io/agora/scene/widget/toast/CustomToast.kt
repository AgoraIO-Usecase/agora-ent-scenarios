package io.agora.scene.widget.toast

import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.dp


object CustomToast {

    private val mMainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    @JvmStatic
    fun show(@StringRes resId: Int, vararg formatArgs: String?) {
        show(AgoraApplication.the().getString(resId, *formatArgs))
    }

    @JvmStatic
    fun show(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(AgoraApplication.the().getString(resId), InternalToast.COMMON, duration)
    }

    @JvmStatic
    fun show(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        show(msg, InternalToast.COMMON, duration)
    }

    @JvmStatic
    fun showTips(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(AgoraApplication.the().getString(resId), InternalToast.TIPS, duration)
    }

    @JvmStatic
    fun showTips(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        show(msg, InternalToast.TIPS, duration)
    }

    @JvmStatic
    fun showError(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(AgoraApplication.the().getString(resId), InternalToast.ERROR, duration)
    }

    @JvmStatic
    fun showError(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        show(msg, InternalToast.ERROR, duration)
    }

    // 指定位置显示 toast
    @JvmStatic
    fun showByPosition(
        msg: String, gravity: Int = Gravity.BOTTOM, offsetY: Int = 200.dp.toInt(),
        duration: Int = Toast.LENGTH_SHORT,
    ) {
        show(msg = msg, duration = duration, gravity = gravity, offsetY = offsetY)
    }

    // 指定位置显示 toast
    @JvmStatic
    fun showByPosition(
        @StringRes resId: Int, gravity: Int = Gravity.BOTTOM, offsetY: Int = 200.dp.toInt(),
        duration: Int = Toast.LENGTH_SHORT
    ) {
        show(msg = AgoraApplication.the().getString(resId), duration = duration, gravity = gravity, offsetY = offsetY)
    }

    @JvmStatic
    private fun show(msg: String, toastType: Int = InternalToast.COMMON, duration: Int = Toast.LENGTH_SHORT) {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            InternalToast.init(AgoraApplication.the())
            InternalToast.show(msg, toastType, duration, Gravity.BOTTOM, 200.dp.toInt())
        } else {
            mMainHandler.post {
                InternalToast.init(AgoraApplication.the())
                InternalToast.show(msg, toastType, duration, Gravity.BOTTOM, 200.dp.toInt())
            }
        }
    }

    @JvmStatic
    private fun show(
        msg: String, toastType: Int = InternalToast.COMMON, duration: Int = Toast.LENGTH_SHORT,
        gravity: Int, offsetY: Int
    ) {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            InternalToast.init(AgoraApplication.the())
            InternalToast.show(msg, toastType, duration, gravity, offsetY)
        } else {
            mMainHandler.post {
                InternalToast.init(AgoraApplication.the())
                InternalToast.show(msg, toastType, duration, gravity, offsetY)
            }
        }
    }
}