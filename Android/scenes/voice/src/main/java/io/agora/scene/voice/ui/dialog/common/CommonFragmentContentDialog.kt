package io.agora.scene.voice.ui.dialog.common

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.databinding.VoiceDialogCenterFragmentContentBinding
import io.agora.scene.base.component.BaseFragmentDialog

/**
 * Center dialog with confirm/cancel buttons
 */
class CommonFragmentContentDialog constructor() : BaseFragmentDialog<VoiceDialogCenterFragmentContentBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogCenterFragmentContentBinding {
        return VoiceDialogCenterFragmentContentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        mBinding?.apply {
            setDialogSize(view)
            if (!TextUtils.isEmpty(contentText)) {
                mtContent.text = contentText
            }
            if (!TextUtils.isEmpty(submitText)) {
                mbSubmit.text = submitText
            }
            mbSubmit.setOnClickListener {
                clickListener?.onConfirmClick()
                dismiss()
            }
        }
    }

    private fun setDialogSize(view: View) {
        val layoutParams: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = 300.dp.toInt()
        view.layoutParams = layoutParams
    }

    private var contentText: String = ""
    private var submitText: String = ""
    private var clickListener: OnClickBottomListener? = null

    fun contentText(contentText: String) = apply {
        this.contentText = contentText
    }

    fun submitText(submitText: String) = apply {
        this.submitText = submitText
    }

    fun setOnClickListener(clickListener: OnClickBottomListener) = apply {
        this.clickListener = clickListener
    }

    interface OnClickBottomListener {
        /**
         * Click confirm button event
         */
        fun onConfirmClick()
    }
}