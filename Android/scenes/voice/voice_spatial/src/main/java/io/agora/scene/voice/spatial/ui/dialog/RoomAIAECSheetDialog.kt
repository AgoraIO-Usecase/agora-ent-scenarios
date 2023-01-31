package io.agora.scene.voice.spatial.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogChatroomAiaecBinding
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class RoomAIAECSheetDialog: BaseSheetDialog<VoiceSpatialDialogChatroomAiaecBinding>() {

    companion object {
        const val KEY_IS_ON = "isOn"
    }

    private val isOn by lazy {
        arguments?.getBoolean(KEY_IS_ON, true) ?: true
    }

    public var onClickCheckBox: ((isOn: Boolean) -> Unit)? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceSpatialDialogChatroomAiaecBinding? {
        return VoiceSpatialDialogChatroomAiaecBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation

        binding?.accbAEC?.isChecked = isOn
        binding?.accbAEC?.setOnCheckedChangeListener { _, isChecked ->
            onClickCheckBox?.invoke(isChecked)
        }
    }
}
