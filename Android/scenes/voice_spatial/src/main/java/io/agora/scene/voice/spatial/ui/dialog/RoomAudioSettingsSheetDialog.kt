package io.agora.scene.voice.spatial.ui.dialog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.isVisible
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogAudioSettingBinding
import io.agora.scene.voice.spatial.global.ConfigConstants
import io.agora.scene.voice.spatial.global.ConfigConstants.DISABLE_ALPHA
import io.agora.scene.voice.spatial.global.ConfigConstants.ENABLE_ALPHA
import io.agora.scene.voice.spatial.model.RoomAudioSettingsBean
import io.agora.scene.voice.spatial.ui.BaseSheetDialog
import io.agora.scene.widget.doOnStopTrackingTouch
import io.agora.scene.widget.toast.CustomToast

class RoomAudioSettingsSheetDialog constructor() : BaseSheetDialog<VoiceSpatialDialogAudioSettingBinding>() {

    companion object {
        private const val TAG = "RoomAudioSettingsSheetDialog"
        const val KEY_AUDIO_SETTINGS_INFO = "audio_settings"
    }

    public val audioSettingsInfo: RoomAudioSettingsBean by lazy {
        arguments?.getSerializable(KEY_AUDIO_SETTINGS_INFO) as RoomAudioSettingsBean
    }

    var audioSettingsListener: OnClickAudioSettingsListener? = null

    private var mainHandler: Handler = Handler(Looper.getMainLooper())

    private var botTask: Runnable? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceSpatialDialogAudioSettingBinding {
        return VoiceSpatialDialogAudioSettingBinding.inflate(inflater, container, false)
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

            mcbAgoraBot.setOnCheckedChangeListener { button, isChecked ->
                VoiceSpatialLogger.d(TAG, "isChecked：$isChecked")
                audioSettingsListener?.onBotCheckedChanged(button, isChecked)
                mcbAgoraBot.isEnabled = false
                mcbAgoraBot.alpha = DISABLE_ALPHA
                startBotTask()
            }
            mcbAgoraBotDisable.setOnClickListener {
                activity?.let {
                    CustomToast.showTips(getString(R.string.voice_spatial_only_host_can_change_robot))
                }
            }
            mtSpatialAudioArrow.setOnClickListener {
                audioSettingsListener?.onSpatialAudio(audioSettingsInfo.spatialOpen, audioSettingsInfo.enable)
            }
            pbAgoraBotVolume.doOnStopTrackingTouch {
                it?.progress?.let { progress ->
                    mtAgoraBotVolumeValue.text = progress.toString()
                    audioSettingsListener?.onBotVolumeChange(progress)
                }
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
        /**变声*/
        fun onVoiceChanger(mode: Int, isEnable: Boolean)

        /**机器人开关*/
        fun onBotCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)

        /**机器人音量*/
        fun onBotVolumeChange(progress: Int)

        /**空间音频*/
        fun onSpatialAudio(isOpen: Boolean, isEnable: Boolean)
    }
}