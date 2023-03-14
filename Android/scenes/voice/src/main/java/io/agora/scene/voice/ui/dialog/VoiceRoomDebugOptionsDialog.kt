package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.voice.databinding.VoiceSpatialDialogDebugOptionsBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class VoiceRoomDebugOptionsDialog: BaseSheetDialog<VoiceSpatialDialogDebugOptionsBinding>() {

    companion object {
        fun  debugMode() {
            AgoraRtcEngineController.get().setApmOn(true)
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceSpatialDialogDebugOptionsBinding? {
        return VoiceSpatialDialogDebugOptionsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.accbAPM?.setOnCheckedChangeListener { _, b ->
            AgoraRtcEngineController.get().setApmOn(b)
        }
    }
}
