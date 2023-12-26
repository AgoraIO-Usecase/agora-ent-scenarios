package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.isVisible
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogAudioSettingBinding
import io.agora.scene.voice.model.constructor.RoomAudioSettingsConstructor
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.constant.ConfigConstants.DISABLE_ALPHA
import io.agora.voice.common.constant.ConfigConstants.ENABLE_ALPHA
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.ToastTools
import io.agora.voice.common.utils.onStopTrackingTouch

class RoomAudioSettingsSheetDialog : BaseSheetDialog<VoiceDialogAudioSettingBinding>() {

    companion object {
        const val KEY_AUDIO_SETTINGS_INFO = "audio_settings"
    }

    public val audioSettingsInfo: io.agora.scene.voice.model.RoomAudioSettingsBean by lazy {
        arguments?.getSerializable(KEY_AUDIO_SETTINGS_INFO) as io.agora.scene.voice.model.RoomAudioSettingsBean
    }

    var audioSettingsListener: OnClickAudioSettingsListener? = null

    private var mainHandler: Handler = Handler(Looper.getMainLooper())

    private var botTask: Runnable? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogAudioSettingBinding {
        return VoiceDialogAudioSettingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            setOnApplyWindowInsets(root)
            if (audioSettingsInfo.enable) {
                mcbAgoraBot.alpha = ENABLE_ALPHA
                pbAgoraBotVolume.alpha = ENABLE_ALPHA
                mtAgoraBotVolumeValue.alpha = ENABLE_ALPHA
                mcbAgoraBotDisable.isVisible = false
            } else {
                mcbAgoraBot.alpha = DISABLE_ALPHA
                pbAgoraBotVolume.alpha = DISABLE_ALPHA
                mtAgoraBotVolumeValue.alpha = DISABLE_ALPHA
                mcbAgoraBotDisable.isVisible = true
            }
            mcbAgoraBot.isEnabled = audioSettingsInfo.enable
            pbAgoraBotVolume.isEnabled = audioSettingsInfo.enable

            pbAgoraBotVolume.progress = audioSettingsInfo.botVolume
            mtAgoraBotVolumeValue.text = audioSettingsInfo.botVolume.toString()
            mtBestSoundEffectArrow.text =
                RoomAudioSettingsConstructor.getSoundEffectName(view.context, audioSettingsInfo.soundSelection)

            updateAINSView()
            updateAIAECView()
            updateAIAGCView()
            updateBotStateView()
            updateBGMView()
            updateEarBackState()
            updateSoundCardState()

            mcbAgoraBot.setOnCheckedChangeListener { button, isChecked ->
                if (!button.isPressed) return@setOnCheckedChangeListener
                "isChecked：$isChecked".logD("mcbAgoraBot")
                audioSettingsListener?.onBotCheckedChanged(button, isChecked)
                mcbAgoraBot.isEnabled = false
                mcbAgoraBot.alpha = DISABLE_ALPHA
                startBotTask()
            }
            mcbAgoraBotDisable.setOnClickListener {
                activity?.let {
                    ToastTools.showTips(it, getString(R.string.voice_chatroom_only_host_can_change_robot))
                }
            }
            mtAINSArrow.setOnClickListener {
                audioSettingsListener?.onAINS(audioSettingsInfo.AINSMode, audioSettingsInfo.enable)
            }
            mtAECArrow.setOnClickListener {
                audioSettingsListener?.onAIAEC(audioSettingsInfo.isAIAECOn, audioSettingsInfo.enable)
            }
            mtAGCArrow.setOnClickListener {
                audioSettingsListener?.onAGC(audioSettingsInfo.isAIAGCOn, audioSettingsInfo.enable)
            }
            tvInEarArrow.setOnClickListener {
                audioSettingsListener?.onEarBackSetting()
            }
            tvSoundCardArrow.setOnClickListener {
                audioSettingsListener?.onVirtualSoundCardSetting()
            }
            tvBGMArrow.setOnClickListener {
                audioSettingsListener?.onBGMSetting()
            }
            mtBestSoundEffectArrow.setOnClickListener {
                audioSettingsListener?.onSoundEffect(audioSettingsInfo.soundSelection, audioSettingsInfo.enable)
            }
            pbAgoraBotVolume.onStopTrackingTouch {
                it?.progress?.let { progress ->
                    mtAgoraBotVolumeValue.text = progress.toString()
                    audioSettingsListener?.onBotVolumeChange(progress)
                }
            }
        }
    }
    /**
     * 更新AINS
     */
    fun updateAINSView() {
        binding?.apply {
            mtAINSArrow.text = view?.let {
                RoomAudioSettingsConstructor.getAINSName(it.context, audioSettingsInfo.AINSMode)
            }
        }
    }
    /**
     * 更新AIAEC
     */
    fun updateAIAECView() {
        binding?.apply {
            if (audioSettingsInfo.isAIAECOn) {
                mtAECArrow.text = view?.context?.getString(R.string.voice_chatroom_on)
            } else {
                mtAECArrow.text = view?.context?.getString(R.string.voice_chatroom_off)
            }
        }
    }

    /**
     * 更新AIAGC
     */
    fun updateAIAGCView() {
        binding?.apply {
            if (audioSettingsInfo.isAIAGCOn) {
                mtAGCArrow.text = view?.context?.getString(R.string.voice_chatroom_on)
            } else {
                mtAGCArrow.text = view?.context?.getString(R.string.voice_chatroom_off)
            }
        }
    }

    fun updateBGMView() {
        val params = AgoraRtcEngineController.get().bgmManager().params
        if (params.song.isNotEmpty()) {
            binding?.tvBGMArrow?.text = "${params.song}-${params.singer}"
        } else {
            binding?.tvBGMArrow?.text = ""
        }
    }

    fun updateEarBackState() {
        if (AgoraRtcEngineController.get().earBackManager()?.params?.isOn == true) {
            binding?.tvInEarArrow?.text = view?.context?.getString(R.string.voice_chatroom_on)
        } else {
            binding?.tvInEarArrow?.text = view?.context?.getString(R.string.voice_chatroom_off)
        }
    }

    fun updateSoundCardState() {
        if (AgoraRtcEngineController.get().soundCardManager()?.isEnable() == true) {
            binding?.tvSoundCardArrow?.text = view?.context?.getString(R.string.voice_chatroom_on)
        } else {
            binding?.tvSoundCardArrow?.text = view?.context?.getString(R.string.voice_chatroom_off)
        }
    }

    /**
     * 更新机器人ui
     */
    fun updateBotStateView() {
        binding?.mcbAgoraBot?.post {
            binding?.mcbAgoraBot?.isChecked = audioSettingsInfo.botOpen
        }
    }

    override fun dismiss() {
        botTask?.let {
            mainHandler.removeCallbacks(it)
        }
        super.dismiss()
    }

    private fun startBotTask() {
        botTask = Runnable {
            binding?.mcbAgoraBot?.isEnabled = true
            binding?.mcbAgoraBot?.alpha = ENABLE_ALPHA
        }.also {
            mainHandler.postDelayed(it, 1000)
        }
    }

    interface OnClickAudioSettingsListener {
        /**AI降噪*/
        fun onAINS(mode: Int, isEnable: Boolean)

        /**AI回声消除*/
        fun onAIAEC(isOn: Boolean, isEnable: Boolean)

        /**人声自动增益*/
        fun onAGC(isOn: Boolean, isEnable: Boolean)

        /**耳返设置*/
        fun onEarBackSetting()
        /**耳返设置*/
        fun onVirtualSoundCardSetting()
        /** BGM 设置*/
        fun onBGMSetting()
        /**机器人开关*/
        fun onBotCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)

        /**机器人音量*/
        fun onBotVolumeChange(progress: Int)

        /**最佳音效*/
        fun onSoundEffect(soundSelectionType: Int, isEnable: Boolean)
    }
}