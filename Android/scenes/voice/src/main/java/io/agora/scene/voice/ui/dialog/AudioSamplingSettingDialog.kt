package io.agora.scene.voice.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogAudioSamplingSettingBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.ui.dialog.BaseFragmentDialog

class AudioSamplingSettingDialog: BaseFragmentDialog<VoiceDialogAudioSamplingSettingBinding>() {

    private val kAudioSamplingMode = "AUDIO_SAMPLING_MODE"

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogAudioSamplingSettingBinding? {
        return VoiceDialogAudioSamplingSettingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        val oboe = SPUtil.getBoolean(kAudioSamplingMode, true)
        if (oboe) {
            mBinding?.rgSamplingMode?.check(R.id.rbOboe)
        } else {
            mBinding?.rgSamplingMode?.check(R.id.rbJava)
        }

        mBinding?.rgSamplingMode?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbOboe -> {
                    AgoraRtcEngineController.get().setAudioSamplingOboe(true)
                    SPUtil.putBoolean(kAudioSamplingMode, true)
                    dismiss()
                }
                R.id.rbJava -> {
                    AgoraRtcEngineController.get().setAudioSamplingOboe(false)
                    SPUtil.putBoolean(kAudioSamplingMode, false)
                    dismiss()
                }
            }
        }
    }
}
