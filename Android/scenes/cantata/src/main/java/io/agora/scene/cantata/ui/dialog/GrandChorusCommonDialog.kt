package io.agora.scene.cantata.ui.dialog

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import io.agora.scene.base.component.BaseDialog
import io.agora.scene.cantata.databinding.CantataDialogCommonBinding

class GrandChorusCommonDialog(context: Context) : BaseDialog<CantataDialogCommonBinding>(context) {
    override fun getViewBinding(inflater: LayoutInflater): CantataDialogCommonBinding {
        return CantataDialogCommonBinding.inflate(inflater)
    }

    override fun initView() {
        binding!!.btnLeft.setOnClickListener { view: View? ->
            getOnButtonClickListener().onLeftButtonClick()
            dismiss()
        }
        binding!!.btnRight.setOnClickListener { view: View? ->
            getOnButtonClickListener().onRightButtonClick()
            dismiss()
        }
    }

    fun setDescText(desc: String?) {
        binding!!.tvDesc.text = desc
        binding!!.tvDesc.visibility = View.VISIBLE
    }

    fun setDialogBtnText(leftText: String?, rightText: String?) {
        binding!!.btnRight.text = rightText
        if (TextUtils.isEmpty(leftText)) {
            binding!!.btnLeft.visibility = View.GONE
        } else {
            binding!!.btnLeft.text = leftText
        }
    }

    override fun setGravity() {
        window!!.attributes.gravity = Gravity.CENTER
    }
}