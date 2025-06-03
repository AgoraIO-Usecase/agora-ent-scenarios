package io.agora.scene.voice.spatial.ui.dialog.common

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogBottomSheetSingleBinding

/**
 * Single button
 */
class CommonSheetSingleDialog constructor(): BaseBottomSheetDialogFragment<VoiceSpatialDialogBottomSheetSingleBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        mBinding?.apply {
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