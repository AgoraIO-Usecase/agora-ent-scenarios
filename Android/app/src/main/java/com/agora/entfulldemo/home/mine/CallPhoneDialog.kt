package com.agora.entfulldemo.home.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.agora.entfulldemo.databinding.AppDialogCallPhoneBinding
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.component.BaseDialog

class CallPhoneDialog constructor() : BaseBottomSheetDialogFragment<AppDialogCallPhoneBinding>() {

    companion object {
        const val KEY_PHONE = "phoneNum"
        const val KEY_TITLE = "title"
    }

    private val phone by lazy {
        arguments?.getString(KEY_PHONE)
    }

    private val title by lazy {
        arguments?.getString(KEY_TITLE)
    }


    var onClickCallPhone: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (title.isNullOrEmpty()) {
            mBinding.tvCallTitle.isVisible = false
            mBinding.divider.isVisible = false
        } else {
            mBinding.tvCallTitle.text = title
            mBinding.tvCallTitle.isVisible = true
            mBinding.divider.isVisible = true
        }
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