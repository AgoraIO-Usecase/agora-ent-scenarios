package com.agora.entfulldemo.home.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.agora.entfulldemo.databinding.AppDialogCallPhoneBinding
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.component.BaseDialog

class CallPhoneDialog: BaseBottomSheetDialogFragment<AppDialogCallPhoneBinding>() {

    companion object {
        const val KEY_PHONE = "phoneNum"
    }

    private val phone by lazy {
        arguments?.getString(KEY_PHONE)
    }

    var onClickCallPhone: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tvCallPhone.text = phone
        mBinding.tvCallPhone.setOnClickListener {
            onClickCallPhone?.invoke()
            dismiss()
        }
        mBinding.tvCancel.setOnClickListener {
            dismiss()
        }
    }
}