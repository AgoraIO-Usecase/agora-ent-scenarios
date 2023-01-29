package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.voice.databinding.VoiceDialogChatroomAiagcBinding
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class RoomAIAGCSheetDialog: BaseSheetDialog<VoiceDialogChatroomAiagcBinding>() {

    companion object {
        const val KEY_IS_ON = "isOn"
    }

    private val isOn by lazy {
        arguments?.getBoolean(RoomAIAECSheetDialog.KEY_IS_ON, true) ?: true
    }

    public var onClickCheckBox: ((isOn: Boolean) -> Unit)? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomAiagcBinding? {
        return VoiceDialogChatroomAiagcBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.accbAGC?.isChecked = isOn
        binding?.accbAGC?.setOnCheckedChangeListener { _, b ->
            onClickCheckBox?.invoke(b)
        }
    }
}
