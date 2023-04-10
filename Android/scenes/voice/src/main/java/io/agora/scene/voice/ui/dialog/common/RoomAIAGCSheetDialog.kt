package io.agora.scene.voice.ui.dialog.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.voice.databinding.VoiceDialogChatroomAiaecBinding
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class RoomAIAGCSheetDialog: BaseSheetDialog<VoiceDialogChatroomAiaecBinding>() {

    public var onClickCheckBox: ((isOn: Boolean) -> UInt)? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomAiaecBinding? {
        return VoiceDialogChatroomAiaecBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.accbAEC?.setOnClickListener {
            onClickCheckBox?.invoke(true)
        }
    }
}
