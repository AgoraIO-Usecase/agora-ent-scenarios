package io.agora.scene.voice.ui.debugSettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.voice.databinding.VoiceDialogDebugOptionsBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class VoiceRoomDebugOptionsDialog constructor(val debugSettingBean: VoiceDebugSettingBean) :
    BaseSheetDialog<VoiceDialogDebugOptionsBinding>() {

    companion object {
        fun debugMode() {
            AgoraRtcEngineController.get().setApmOn(true)
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogDebugOptionsBinding {
        return VoiceDialogDebugOptionsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            setOnApplyWindowInsets(root)
            accbAPM.setOnCheckedChangeListener { _, b ->
                AgoraRtcEngineController.get().setApmOn(b)
            }
            etNsEnable.setText(debugSettingBean.sfNsEnable.toString())
            tvSettingNsEnable.setOnClickListener {
                debugSettingBean.sfNsEnable = etNsEnable.text.toString().toIntOrNull() ?: 0
            }
        }

    }


    interface OnDebugSettingCallback {
        fun onNsEnable(newValue: Int)
        fun onAinsToLoadFlag(newValue: Int)
        fun onNsngAlgRoute(newValue: Int)
        fun onNsngPredefAgg(newValue: Int)
        fun onNsngMapInMaskMin(newValue: Int)
        fun onNsngMapOutMaskMin(newValue: Int)
        fun onStatNsLowerBound(newValue: Int)
        fun onNsngFinalMaskLowerBound(newValue: Int)
        fun onStatNsEnhFactor(newValue: Int)
        fun onStatNsFastNsSpeechTrigThreshold(newValue: Int)
        fun onAedEnable(newValue: Int)
        fun onNsngMusicProbThr(newValue: Int)
        fun onStatNsMusicModeBackoffDB(newValue: Int)
        fun onAinsMusicModeBackoffDB(newValue: Int)
        fun onAinsSpeechProtectThreshold(newValue: Int)
    }

}
