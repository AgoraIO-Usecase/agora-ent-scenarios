package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.core.view.isVisible
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import io.agora.voice.common.utils.ToastTools
import io.agora.voice.common.utils.doOnProgressChanged
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.constant.ConfigConstants.DISABLE_ALPHA
import io.agora.voice.common.constant.ConfigConstants.ENABLE_ALPHA
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogAudioSettingBinding
import io.agora.scene.voice.model.constructor.RoomAudioSettingsConstructor

class RoomAudioSettingsSheetDialog constructor() : BaseSheetDialog<VoiceDialogAudioSettingBinding>() {

    companion object {
        const val KEY_AUDIO_SETTINGS_INFO = "audio_settings"
    }

    private val audioSettingsInfo: io.agora.scene.voice.model.RoomAudioSettingsBean by lazy {
        arguments?.getSerializable(KEY_AUDIO_SETTINGS_INFO) as io.agora.scene.voice.model.RoomAudioSettingsBean
    }

    var audioSettingsListener: OnClickAudioSettingsListener? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogAudioSettingBinding {
        return VoiceDialogAudioSettingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            setOnApplyWindowInsets(root)
            if (audioSettingsInfo.roomType == ConfigConstants.RoomType.Common_Chatroom) {
                ivSpatialAudio.isVisible = false
                mtSpatialAudio.isVisible = false
                mtSpatialAudioArrow.isVisible = false
            }
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

            mcbAgoraBot.isChecked = audioSettingsInfo.botOpen
            pbAgoraBotVolume.progress = audioSettingsInfo.botVolume
            mtAgoraBotVolumeValue.text = audioSettingsInfo.botVolume.toString()
            mtBestSoundEffectArrow.text =
                RoomAudioSettingsConstructor.getSoundEffectName(view.context, audioSettingsInfo.soundSelection)
            mtNoiseSuppressionArrow.text =
                RoomAudioSettingsConstructor.getAINSName(view.context, audioSettingsInfo.anisMode)
            mtSpatialAudioArrow.text = view.context.getString(R.string.voice_chatroom_off)

            mcbAgoraBot.setOnCheckedChangeListener { button, isChecked ->
                audioSettingsListener?.onBotCheckedChanged(button, isChecked)
            }
            mcbAgoraBotDisable.setOnClickListener {
                activity?.let {
                    ToastTools.showTips(it, getString(R.string.voice_chatroom_only_host_can_change_robot))
                }
            }
            mtBestSoundEffectArrow.setOnClickListener {
                audioSettingsListener?.onSoundEffect(audioSettingsInfo.soundSelection, audioSettingsInfo.enable)
            }
            mtNoiseSuppressionArrow.setOnClickListener {
                audioSettingsListener?.onNoiseSuppression(audioSettingsInfo.anisMode, audioSettingsInfo.enable)
            }
            mtSpatialAudioArrow.setOnClickListener {
                audioSettingsListener?.onSpatialAudio(audioSettingsInfo.spatialOpen, audioSettingsInfo.enable)
            }
            pbAgoraBotVolume.doOnProgressChanged { seekBar, progress, fromUser ->
                mtAgoraBotVolumeValue.text = progress.toString()
                audioSettingsListener?.onBotVolumeChange(seekBar, progress, fromUser)
            }
        }
    }

    /**
     * 更新机器人ui
     */
    fun updateBoxCheckBoxView(openBot: Boolean) {
        if (audioSettingsInfo.botOpen == openBot) return
        audioSettingsInfo.botOpen = openBot
        binding?.mcbAgoraBot?.isChecked = audioSettingsInfo.botOpen
    }

    interface OnClickAudioSettingsListener {

        /**机器人开关*/
        fun onBotCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)

        /**机器人音量*/
        fun onBotVolumeChange(seekBar: SeekBar?, progress: Int, fromUser: Boolean)

        /**最佳音效*/
        fun onSoundEffect(soundSelectionType: Int, isEnable: Boolean)

        /**AI降噪*/
        fun onNoiseSuppression(ainsMode: Int, isEnable: Boolean)

        /**空间音频*/
        fun onSpatialAudio(isOpen: Boolean, isEnable: Boolean)
    }
}