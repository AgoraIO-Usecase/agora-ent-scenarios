package io.agora.scene.voice.ui.dialog.common

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.databinding.VoiceDialogCenterFragmentAlertBinding
import io.agora.scene.base.component.BaseFragmentDialog

/**
 * Center dialog with confirm/cancel buttons
 */
class CommonFragmentAlertDialog constructor() : BaseFragmentDialog<VoiceDialogCenterFragmentAlertBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogCenterFragmentAlertBinding {
        return VoiceDialogCenterFragmentAlertBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        mBinding?.apply {
//            addMargin(view)
            setDialogSize(view)
            if (!TextUtils.isEmpty(titleText)) {
                mtTitle.text = titleText
            } else {
                mtTitle.isVisible = false
                // Update spacing
                val layoutParams: ConstraintLayout.LayoutParams = mbLeft.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.setMargins(layoutParams.marginStart, 34.dp.toInt(), layoutParams.marginEnd, layoutParams
                    .bottomMargin)
                mbLeft.layoutParams = layoutParams
            }
            if (!TextUtils.isEmpty(contentText)) {
                mtContent.text = contentText
            }
            if (!TextUtils.isEmpty(leftText)) {
                mbLeft.text = leftText
            }
            if (!TextUtils.isEmpty(rightText)) {
                mbRight.text = rightText
            }
            mbLeft.setOnClickListener {
                clickListener?.onCancelClick()
                dismiss()
            }
            mbRight.setOnClickListener {
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

    private var titleText: String = ""
    private var contentText: String = ""
    private var leftText: String = ""
    private var rightText: String = ""
    private var clickListener: OnClickBottomListener? = null

    fun titleText(titleText: String) = apply {
        this.titleText = titleText
    }

    fun contentText(contentText: String) = apply {
        this.contentText = contentText
    }

    fun leftText(leftText: String) = apply {
        this.leftText = leftText
    }

    fun rightText(rightText: String) = apply {
        this.rightText = rightText
    }

    fun setOnClickListener(clickListener: OnClickBottomListener) = apply {
        this.clickListener = clickListener
    }

    interface OnClickBottomListener {
        /**
         * Click confirm button event
         */
        fun onConfirmClick()

        /**
         * Click cancel button event
         */
        fun onCancelClick() {}
    }
}