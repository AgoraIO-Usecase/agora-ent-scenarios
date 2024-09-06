package io.agora.scene.joy.widget.toast

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import io.agora.scene.base.component.AgoraApplication


object CustomToast {

    private val mMainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    @JvmStatic
    fun show(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        show(msg, InternalToast.COMMON, duration)
    }

    @JvmStatic
    fun showTips(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        show(msg, InternalToast.TIPS, duration)
    }

    @JvmStatic
    fun showError(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        show(msg, InternalToast.ERROR, duration)
    }

    @JvmStatic
    private fun show(msg: String, toastType: Int = InternalToast.COMMON, duration: Int = Toast.LENGTH_SHORT) {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            InternalToast.init(AgoraApplication.the())
            InternalToast.show(msg, toastType, duration)
        } else {
            mMainHandler.post {
                InternalToast.init(AgoraApplication.the())
                InternalToast.show(msg, toastType, duration)
            }
        }
    }
}