package io.agora.scene.show.widget

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowWidgetToastLayoutBinding

class ToastDialog : Dialog {

    private val mBinding by lazy {
        ShowWidgetToastLayoutBinding.inflate(LayoutInflater.from(context))
    }
    private val mDismissRun = Runnable { dismiss() }

    constructor(context: Context) : this(context, R.style.show_toast_dialog)
    constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        setContentView(mBinding.root)
    }

    override fun onStop() {
        super.onStop()
        mBinding.root.removeCallbacks(mDismissRun)
    }

    fun dismissDelayShort(){
        dismissDelay(1000)
    }

    fun dismissDelayLong(){
        dismissDelay(4000)
    }

    private fun dismissDelay(duration: Long){
        mBinding.root.removeCallbacks(mDismissRun)
        mBinding.root.postDelayed(mDismissRun, duration)
    }

    fun showTip(text: String) {
        mBinding.tvMessage.text = text
        setIcon(R.mipmap.show_toast_ic_tip)
        show()
    }

    fun showError(text: String){
        mBinding.tvMessage.text = text
        setIcon(R.mipmap.show_toast_ic_error)
        show()
    }

    fun showMessage(text: String){
        mBinding.tvMessage.text = text
        show()
    }

    private fun setIcon(@DrawableRes icon: Int) {
        if(icon != View.NO_ID){
            mBinding.ivIcon.isVisible = true
            mBinding.ivIcon.setImageResource(icon)
        }else{
            mBinding.ivIcon.isVisible = false
        }

    }

}