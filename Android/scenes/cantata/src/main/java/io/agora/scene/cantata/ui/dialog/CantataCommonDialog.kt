package io.agora.scene.cantata.ui.dialog

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import io.agora.scene.base.component.BaseDialog
import io.agora.scene.cantata.databinding.CantataDialogCommonBinding
import io.agora.scene.cantata.ui.widget.OnClickJackingListener

class CantataCommonDialog constructor(context: Context) : BaseDialog<CantataDialogCommonBinding>(context) {
    override fun getViewBinding(inflater: LayoutInflater): CantataDialogCommonBinding {
        return CantataDialogCommonBinding.inflate(inflater)
    }

    override fun initView() {
        binding?.apply {
            btnLeft.setOnClickListener(object :OnClickJackingListener{
                override fun onClickJacking(view: View) {
                    getOnButtonClickListener().onLeftButtonClick()
                    dismiss()
                }
            })
            btnRight.setOnClickListener(object :OnClickJackingListener{
                override fun onClickJacking(view: View) {
                    getOnButtonClickListener().onRightButtonClick()
                    dismiss()
                }
            })
        }

    }

    fun setDescText(desc: String?) {
        binding?.apply {
            tvDesc.text = desc
            tvDesc.visibility = View.VISIBLE
        }
    }

    fun setDialogBtnText(leftText: String?, rightText: String?) {
        binding?.apply {
            btnRight.text = rightText
            if (TextUtils.isEmpty(leftText)) {
                btnLeft.visibility = View.GONE
            } else {
                btnLeft.text = leftText
            }
        }
    }

    override fun setGravity() {
        window?.attributes?.gravity = Gravity.CENTER
    }
}