package io.agora.scene.voice.ui.dialog.common

import android.os.Bundle
import android.view.View
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.voice.databinding.VoiceDialogChatroomAiaecBinding

class RoomAIAGCSheetDialog: BaseBottomSheetDialogFragment<VoiceDialogChatroomAiaecBinding>() {

    public var onClickCheckBox: ((isOn: Boolean) -> UInt)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding?.accbAEC?.setOnClickListener {
            onClickCheckBox?.invoke(true)
        }
    }
}
