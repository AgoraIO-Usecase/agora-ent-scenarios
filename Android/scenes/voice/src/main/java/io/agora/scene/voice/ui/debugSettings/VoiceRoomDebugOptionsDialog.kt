package io.agora.scene.voice.ui.debugSettings

import android.os.Bundle
import android.view.View
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.voice.databinding.VoiceDialogDebugOptionsBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController

class VoiceRoomDebugOptionsDialog : BaseBottomSheetDialogFragment<VoiceDialogDebugOptionsBinding>() {

    companion object {
        fun debugMode() {
            AgoraRtcEngineController.get().setApmOn(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding?.apply {
            accbAPM.setOnCheckedChangeListener { _, b ->
                AgoraRtcEngineController.get().setApmOn(b)
            }
            etNsEnable.setText(VoiceDebugSettingModel.nsEnable.toString())
            tvSettingNsEnable.setOnClickListener {
                etNsEnable.clearFocus()
                VoiceDebugSettingModel.nsEnable = etNsEnable.text.toString().toIntOrNull() ?: 0
            }

            etAinsToLoadFlag.setText(VoiceDebugSettingModel.ainsToLoadFlag.toString())
            tvSettingAinsToLoadFlag.setOnClickListener {
                VoiceDebugSettingModel.ainsToLoadFlag = etAinsToLoadFlag.text.toString().toIntOrNull() ?: 0
            }

            etNsngAlgRoute.setText(VoiceDebugSettingModel.nsngAlgRoute.toString())
            tvSettingNsngAlgRoute.setOnClickListener {
                VoiceDebugSettingModel.nsngAlgRoute = etNsngAlgRoute.text.toString().toIntOrNull() ?: 10
            }

            etNsngPredefAgg.setText(VoiceDebugSettingModel.nsngPredefAgg.toString())
            tvSettingNsngPredefAgg.setOnClickListener {
                VoiceDebugSettingModel.nsngPredefAgg = etNsngPredefAgg.text.toString().toIntOrNull() ?: 11
            }

            etNsngMapInMaskMin.setText(VoiceDebugSettingModel.nsngMapInMaskMin.toString())
            tvSettingNsngMapInMaskMin.setOnClickListener {
                VoiceDebugSettingModel.nsngMapInMaskMin = etNsngMapInMaskMin.text.toString().toIntOrNull() ?: 80
            }

            etNsngMapOutMaskMin.setText(VoiceDebugSettingModel.nsngMapOutMaskMin.toString())
            tvSettingNsngMapOutMaskMin.setOnClickListener {
                VoiceDebugSettingModel.nsngMapOutMaskMin = etNsngMapOutMaskMin.text.toString().toIntOrNull() ?: 50
            }

            etStatNsLowerBound.setText(VoiceDebugSettingModel.statNsLowerBound.toString())
            tvSettingStatNsLowerBound.setOnClickListener {
                VoiceDebugSettingModel.statNsLowerBound = etStatNsLowerBound.text.toString().toIntOrNull() ?: 5
            }

            etNsngFinalMaskLowerBound.setText(VoiceDebugSettingModel.nsngFinalMaskLowerBound.toString())
            tvSettingNsngFinalMaskLowerBound.setOnClickListener {
                VoiceDebugSettingModel.nsngFinalMaskLowerBound =
                    etNsngFinalMaskLowerBound.text.toString().toIntOrNull() ?: 30
            }

            etStatNsEnhFactor.setText(VoiceDebugSettingModel.statNsEnhFactor.toString())
            tvSettingStatNsEnhFactor.setOnClickListener {
                VoiceDebugSettingModel.statNsEnhFactor = etStatNsEnhFactor.text.toString().toIntOrNull() ?: 200
            }

            etStatNsFastNsSpeechTrigThreshold.setText(VoiceDebugSettingModel.statNsFastNsSpeechTrigThreshold.toString())
            tvSettingStatNsFastNsSpeechTrigThreshold.setOnClickListener {
                VoiceDebugSettingModel.statNsFastNsSpeechTrigThreshold =
                    etStatNsFastNsSpeechTrigThreshold.text.toString().toIntOrNull() ?: 0
            }

            etAedEnable.setText(VoiceDebugSettingModel.aedEnable.toString())
            tvSettingAedEnable.setOnClickListener {
                VoiceDebugSettingModel.aedEnable = etAedEnable.text.toString().toIntOrNull() ?: 1
            }

            etNsngMusicProbThr.setText(VoiceDebugSettingModel.nsngMusicProbThr.toString())
            tvSettingNsngMusicProbThr.setOnClickListener {
                VoiceDebugSettingModel.nsngMusicProbThr = etNsngMusicProbThr.text.toString().toIntOrNull() ?: 85
            }

            etStatNsMusicModeBackoffDB.setText(VoiceDebugSettingModel.statNsMusicModeBackoffDB.toString())
            tvSettingStatNsMusicModeBackoffDB.setOnClickListener {
                VoiceDebugSettingModel.statNsMusicModeBackoffDB =
                    etStatNsMusicModeBackoffDB.text.toString().toIntOrNull() ?: 200
            }

            etAinsMusicModeBackoffDB.setText(VoiceDebugSettingModel.ainsMusicModeBackoffDB.toString())
            tvSettingAinsMusicModeBackoffDB.setOnClickListener {
                VoiceDebugSettingModel.ainsMusicModeBackoffDB =
                    etAinsMusicModeBackoffDB.text.toString().toIntOrNull() ?: 270
            }

            etAinsSpeechProtectThreshold.setText(VoiceDebugSettingModel.ainsSpeechProtectThreshold.toString())
            tvSettingAinsSpeechProtectThreshold.setOnClickListener {
                VoiceDebugSettingModel.ainsSpeechProtectThreshold =
                    etAinsSpeechProtectThreshold.text.toString().toIntOrNull() ?: 100
            }
        }
    }
}