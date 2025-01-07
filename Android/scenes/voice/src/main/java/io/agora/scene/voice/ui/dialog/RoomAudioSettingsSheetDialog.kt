package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CompoundButton
import androidx.core.view.isVisible
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.voice.R
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.databinding.VoiceDialogAudioSettingBinding
import io.agora.scene.voice.model.constructor.RoomAudioSettingsConstructor
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.widget.doOnStopTrackingTouch
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.voice.global.ConfigConstants.DISABLE_ALPHA
import io.agora.scene.voice.global.ConfigConstants.ENABLE_ALPHA

class RoomAudioSettingsSheetDialog : BaseBottomSheetDialogFragment<VoiceDialogAudioSettingBinding>() {

    companion object {
        private const val TAG = "RoomAudioSettingsSheetDialog"
        const val KEY_AUDIO_SETTINGS_INFO = "audio_settings"
    }

    public val audioSettingsInfo: io.agora.scene.voice.model.RoomAudioSettingsBean by lazy {
        arguments?.getSerializable(KEY_AUDIO_SETTINGS_INFO) as io.agora.scene.voice.model.RoomAudioSettingsBean
    }

    var audioSettingsListener: OnClickAudioSettingsListener? = null

    private var mainHandler: Handler = Handler(Looper.getMainLooper())

    private var botTask: Runnable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding?.apply {
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
            updateEarBackState()
            updateSoundCardState()

            mcbAgoraBot.setOnCheckedChangeListener { button, isChecked ->
                if (!button.isPressed) return@setOnCheckedChangeListener
                VoiceLogger.d(TAG,"isChecked：$isChecked")
                audioSettingsListener?.onBotCheckedChanged(button, isChecked)
                mcbAgoraBot.isEnabled = false
                mcbAgoraBot.alpha = DISABLE_ALPHA
                startBotTask()
            }
            mcbAgoraBotDisable.setOnClickListener {
                CustomToast.showTips(R.string.voice_chatroom_only_host_can_change_robot)
            }
            // AI Noise Reduction
            mtAINSArrow.setOnClickListener {
                audioSettingsListener?.onAINS(
                    audioSettingsInfo.AINSMode,
                    audioSettingsInfo.AINSMusicMode,
                    audioSettingsInfo.AINSMicMode,
                    audioSettingsInfo.enable
                )
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
            mtBestSoundEffectArrow.setOnClickListener {
                audioSettingsListener?.onSoundEffect(audioSettingsInfo.soundSelection, audioSettingsInfo.enable)
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
     * Update AINS
     */
    fun updateAINSView() {
        mBinding?.apply {
            mtAINSArrow.text = view?.let {
                RoomAudioSettingsConstructor.getAINSName(it.context, audioSettingsInfo.AINSMode)
            }
        }
    }

    /**
     * Update AIAEC
     */
    fun updateAIAECView() {
        mBinding?.apply {
            if (audioSettingsInfo.isAIAECOn) {
                mtAECArrow.text = view?.context?.getString(R.string.voice_chatroom_on)
            } else {
                mtAECArrow.text = view?.context?.getString(R.string.voice_chatroom_off)
            }
        }
    }

    /**
     * Update AIAGC
     */
    fun updateAIAGCView() {
        mBinding?.apply {
            if (audioSettingsInfo.isAIAGCOn) {
                mtAGCArrow.text = view?.context?.getString(R.string.voice_chatroom_on)
            } else {
                mtAGCArrow.text = view?.context?.getString(R.string.voice_chatroom_off)
            }
        }
    }

    fun updateEarBackState() {
        if (AgoraRtcEngineController.get().earBackManager()?.params?.isOn == true) {
            mBinding?.tvInEarArrow?.text = view?.context?.getString(R.string.voice_chatroom_on)
        } else {
            mBinding?.tvInEarArrow?.text = view?.context?.getString(R.string.voice_chatroom_off)
        }
    }

    fun updateSoundCardState() {
        if (AgoraRtcEngineController.get().soundCardManager()?.isEnable() == true) {
            mBinding?.tvSoundCardArrow?.text = view?.context?.getString(R.string.voice_chatroom_on)
        } else {
            mBinding?.tvSoundCardArrow?.text = view?.context?.getString(R.string.voice_chatroom_off)
        }
    }

    /**
     * Update robot UI
     */
    fun updateBotStateView() {
        mBinding?.mcbAgoraBot?.post {
            mBinding?.mcbAgoraBot?.isChecked = audioSettingsInfo.botOpen
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
            mBinding?.mcbAgoraBot?.isEnabled = true
            mBinding?.mcbAgoraBot?.alpha = ENABLE_ALPHA
        }.also {
            mainHandler.postDelayed(it, 1000)
        }
    }

    interface OnClickAudioSettingsListener {
        /**
         * AI Noise Reduction
         * @param mode Noise reduction mode
         * @param musicMode Music protection mode
         * @param micMode Voice protection mode
         */
        fun onAINS(mode: Int, musicMode: Int, micMode: Int, isEnable: Boolean)

        /** AI Echo Cancellation */
        fun onAIAEC(isOn: Boolean, isEnable: Boolean)

        /** Voice Auto Gain Control */
        fun onAGC(isOn: Boolean, isEnable: Boolean)

        /** In-ear Monitoring Settings */
        fun onEarBackSetting()

        /** Virtual Sound Card Settings */
        fun onVirtualSoundCardSetting()

        /** Robot Switch */
        fun onBotCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)

        /** Robot Volume */
        fun onBotVolumeChange(progress: Int)

        /** Best Sound Effect */
        fun onSoundEffect(soundSelectionType: Int, isEnable: Boolean)
    }
}