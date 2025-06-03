package io.agora.scene.voice.spatial.ui.dialog.common

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogBottomSheetAlertBinding

/**
 * Confirm/cancel
 */
class CommonSheetAlertDialog constructor(): BaseBottomSheetDialogFragment<VoiceSpatialDialogBottomSheetAlertBinding>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        mBinding?.apply {
            if (!TextUtils.isEmpty(contentText)){
                mtContent.text = contentText
            }
            if (!TextUtils.isEmpty(leftText)){
                mbLeft.text = leftText
            }
            if (!TextUtils.isEmpty(rightText)){
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

    private fun addMargin(view: View) {
        val layoutParams: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
        val marginHorizontal = 15.dp.toInt()
        val marginVertical = 24.dp.toInt()
        layoutParams.setMargins(marginHorizontal, 0, marginHorizontal, marginVertical)
        view.layoutParams = layoutParams
    }

    private var contentText: String = ""
    private var leftText: String = ""
    private var rightText: String = ""
    private var clickListener: OnClickBottomListener? = null

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
         * Click the confirm button event
         */
        fun onConfirmClick()

        /**
         * Click the cancel button event
         */
        fun onCancelClick(){}
    }
}