package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import io.agora.scene.voice.databinding.VoiceDialog3dSpatialMicTutorialBinding

class Room3DSpatialMicTutorialSheetDialog constructor(): BaseSheetDialog<VoiceDialog3dSpatialMicTutorialBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialog3dSpatialMicTutorialBinding {
        return VoiceDialog3dSpatialMicTutorialBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            setOnApplyWindowInsets(root)
        }
    }
}