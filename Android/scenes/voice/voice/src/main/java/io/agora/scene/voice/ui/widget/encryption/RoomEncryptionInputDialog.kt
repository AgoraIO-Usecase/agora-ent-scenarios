package io.agora.scene.voice.ui.widget.encryption

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.voice.baseui.dialog.BaseFragmentDialog
import io.agora.scene.voice.databinding.VoiceDialogEncryptionBinding

/**
 * 输入密码 dialog
 */
class RoomEncryptionInputDialog constructor() : BaseFragmentDialog<VoiceDialogEncryptionBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogEncryptionBinding? {
        return VoiceDialogEncryptionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)

        mBinding?.apply {
            if (!TextUtils.isEmpty(titleText)) {
                title.text = titleText
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
                clickListener?.onConfirmClick(mtContent.text.toString())
                dismiss()
            }
        }
    }

    private var clickListener: OnClickBottomListener? = null
    private var leftText: String = ""
    private var rightText: String = ""
    private var titleText: String = ""

    fun setOnClickListener(clickListener: OnClickBottomListener) = apply {
        this.clickListener = clickListener
    }

    fun leftText(leftText: String) = apply {
        this.leftText = leftText
    }

    fun rightText(rightText: String) = apply {
        this.rightText = rightText
    }

    fun titleText(titleText: String) = apply {
        this.titleText = titleText
    }


    interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        fun onConfirmClick(password: String)

        /**
         * 点击取消按钮事件
         */
        fun onCancelClick() {}
    }
}