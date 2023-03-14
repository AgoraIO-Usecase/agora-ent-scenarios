package io.agora.scene.voice.spatial.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialog3dSpatialMicTutorialBinding
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class Room3DSpatialMicTutorialSheetDialog constructor(): BaseSheetDialog<VoiceSpatialDialog3dSpatialMicTutorialBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceSpatialDialog3dSpatialMicTutorialBinding {
        return VoiceSpatialDialog3dSpatialMicTutorialBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            setOnApplyWindowInsets(root)
        }
    }
}