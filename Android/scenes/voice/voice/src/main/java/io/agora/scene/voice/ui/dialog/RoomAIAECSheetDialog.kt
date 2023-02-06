package io.agora.scene.voice.ui.dialog

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogChatroomAiaecBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import java.io.File

class RoomAIAECSheetDialog: BaseSheetDialog<VoiceDialogChatroomAiaecBinding>() {

    companion object {
        const val KEY_IS_ON = "isOn"
    }

    private val beforeSoundId = 201001
    private val afterSoundId = 201002

    private val isOn by lazy {
        arguments?.getBoolean(KEY_IS_ON, true) ?: true
    }

    var onClickCheckBox: ((isOn: Boolean) -> Unit)? = null

    private val beforeAnim by lazy {
        binding?.ivBefore?.background as AnimationDrawable
    }

    private val afterAnim by lazy {
        binding?.ivAfter?.background as AnimationDrawable
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomAiaecBinding? {
        return VoiceDialogChatroomAiaecBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation

        setupOnClickPlayButton()
        binding?.accbAEC?.isChecked = isOn
        binding?.accbAEC?.setOnCheckedChangeListener { _, isChecked ->
            onClickCheckBox?.invoke(isChecked)
        }
    }

    fun setupOnClickPlayButton() {
        binding?.btnBefore?.setOnClickListener {
            tst()
            if (it.isSelected) { // stop play
                AgoraRtcEngineController.get().resetMediaPlayer()
                beforeAnim.stop()
                afterAnim.stop()
                it.isSelected = false
            } else { // start play
                val file = "file:///android_asset/sounds/voice_sample_aec_before.m4a"
                AgoraRtcEngineController.get().playMusic(beforeSoundId, file, 0)
                it.isSelected = true
                binding?.btnAfter?.isSelected = false
                beforeAnim.start()
                afterAnim.stop()
            }
        }
        binding?.btnAfter?.setOnClickListener {
            if (it.isSelected) { // stop play
                AgoraRtcEngineController.get().resetMediaPlayer()
                beforeAnim.stop()
                afterAnim.stop()
                it.isSelected = false
            } else { // start play
                val file = "file:///android_asset/sounds/voice_sample_aec_before.m4a"
                AgoraRtcEngineController.get().playMusic(afterSoundId, file, 0)
                it.isSelected = true
                binding?.btnBefore?.isSelected = false
                afterAnim.start()
                beforeAnim.stop()
            }
        }
    }

    fun tst() {
        // SD 卡路径
        val sdCardPath = System.getenv("EXTERNAL_STORAGE")
        val path = activity?.applicationContext?.filesDir?.absolutePath
        Log.e("Dir", path!!)
        val assets = activity?.applicationContext?.assets
        val files = assets?.list("sounds")
        if (files?.count() == 0) {
//            File.separator
        } else {

        }
        Log.e("AssetsLog", "ASSADSADAS------------------")
        for (str in  files!!) {
            Log.e("AssetsLog", str)
        }
    }
}
