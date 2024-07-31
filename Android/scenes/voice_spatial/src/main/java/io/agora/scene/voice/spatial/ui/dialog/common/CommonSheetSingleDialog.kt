package io.agora.scene.voice.spatial.ui.dialog.common

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogBottomSheetSingleBinding
import io.agora.scene.voice.spatial.ui.BaseSheetDialog

/**
 * 单按钮
 */
class CommonSheetSingleDialog constructor(): BaseSheetDialog<VoiceSpatialDialogBottomSheetSingleBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceSpatialDialogBottomSheetSingleBinding {
        return VoiceSpatialDialogBottomSheetSingleBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        binding?.apply {
            setOnApplyWindowInsets(root)
            if (!TextUtils.isEmpty(singleText)){
                mbCancel.text = singleText
            }
            mbCancel.setOnClickListener {
                clickListener?.onSingleClick()
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

    private var singleText: String = ""
    private var clickListener: OnClickBottomListener? = null

    fun singleText(singleText: String) = apply {
        this.singleText = singleText
    }

    fun setOnClickListener(clickListener: OnClickBottomListener) = apply {
        this.clickListener = clickListener
    }

    interface OnClickBottomListener {

        fun onSingleClick()
    }
}