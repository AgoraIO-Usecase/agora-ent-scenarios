package io.agora.scene.voice.spatial.ui.dialog.common

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogBottomSheetContentBinding

/**
 * Content dialog, similar to room announcement
 */
class CommonSheetContentDialog constructor(): BaseBottomSheetDialogFragment<VoiceSpatialDialogBottomSheetContentBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding?.apply {
            if (!TextUtils.isEmpty(titleText)){
                mtTitle.text = titleText
            }
            if (!TextUtils.isEmpty(contentText)){
                mtContent.text = contentText
            }
        }
    }

    private var titleText: String = ""
    private var contentText: String = ""

    fun titleText(titleText: String) = apply {
        this.titleText = titleText
    }

    fun contentText(contentText: String) = apply {
        this.contentText = contentText
    }
}