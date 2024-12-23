package io.agora.scene.voice.spatial.ui.dialog

import android.os.Bundle
import android.view.View
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogDebugOptionsBinding
import io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController

class VoiceRoomDebugOptionsDialog: BaseBottomSheetDialogFragment<VoiceSpatialDialogDebugOptionsBinding>() {

    companion object {
        fun  debugMode() {
            AgoraRtcEngineController.get().setApmOn(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding?.accbAPM?.setOnCheckedChangeListener { _, b ->
            AgoraRtcEngineController.get().setApmOn(b)
        }
        mBinding?.cbTimeLimit?.setOnCheckedChangeListener { _, b ->
            // TODO: 打开/关闭房间时间限制
        }
    }
}
