package io.agora.scene.voice.spatial.ui.dialog.common

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogCenterFragmentContentBinding
import io.agora.scene.voice.spatial.ui.BaseFragmentDialog

/**
 * 中间弹框，确认/取消按钮
 */
class CommonFragmentContentDialog constructor() : BaseFragmentDialog<VoiceSpatialDialogCenterFragmentContentBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceSpatialDialogCenterFragmentContentBinding {
        return VoiceSpatialDialogCenterFragmentContentBinding.inflate(inflater, container, false)
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
         * 点击确定按钮事件
         */
        fun onConfirmClick()
    }
}