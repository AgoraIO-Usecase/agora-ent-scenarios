package io.agora.scene.voice.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogAudioSamplingSettingBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.ui.dialog.BaseFragmentDialog

class AudioSamplingSettingDialog: DialogFragment() {

    lateinit var binding: VoiceDialogAudioSamplingSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = VoiceDialogAudioSamplingSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        val key = AgoraRtcEngineController.kAudioSamplingMode
        val oboe = SPUtil.getBoolean(key, true)
        if (oboe) {
            binding.rgSamplingMode.check(R.id.rbOboe)
        } else {
            binding.rgSamplingMode.check(R.id.rbJava)
        }
        binding.rgSamplingMode.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbOboe -> {
                    SPUtil.putBoolean(key, true)
                    dismiss()
                }
                R.id.rbJava -> {
                    SPUtil.putBoolean(key, false)
                    dismiss()
                }
            }
        }
    }
}
