package io.agora.scene.ktv.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvDialogAudioSamplingSettingBinding
import io.agora.scene.ktv.live.RoomLivingViewModel

class AudioSamplingSettingDialog: DialogFragment() {

    lateinit var binding: KtvDialogAudioSamplingSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = KtvDialogAudioSamplingSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        val key = RoomLivingViewModel.kAudioSamplingMode
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
