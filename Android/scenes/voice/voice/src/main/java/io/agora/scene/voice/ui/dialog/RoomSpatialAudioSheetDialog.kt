package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.voice.R
import io.agora.voice.common.ui.dialog.BaseFixedHeightSheetDialog
import io.agora.voice.common.constant.ConfigConstants.DISABLE_ALPHA
import io.agora.voice.common.constant.ConfigConstants.ENABLE_ALPHA
import io.agora.voice.common.utils.ToastTools
import io.agora.voice.common.utils.doOnProgressChanged
import io.agora.scene.voice.databinding.VoiceDialogRoomSpatialAudioBinding

class RoomSpatialAudioSheetDialog constructor() : BaseFixedHeightSheetDialog<VoiceDialogRoomSpatialAudioBinding>() {

    companion object {
        const val KEY_SPATIAL_OPEN = "key_spatial_open"
        const val KEY_IS_ENABLED = "is_enabled"
    }

    private val isEnabled: Boolean by lazy {
        arguments?.getBoolean(KEY_IS_ENABLED, true) ?: true
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogRoomSpatialAudioBinding {
        return VoiceDialogRoomSpatialAudioBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation
        dialog?.setCanceledOnTouchOutside(false)
        arguments?.apply {
            val spatialOpen = getBoolean(KEY_SPATIAL_OPEN)
            binding?.mcbBlueBotSpatialAudio?.isChecked = spatialOpen
        }
        binding?.apply {
            setOnApplyWindowInsets(root)
            ivBottomSheetBack.setOnClickListener {
                onHandleOnBackPressed()
            }
            if (isEnabled) {
                mcbBlueBotSpatialAudio.alpha = ENABLE_ALPHA
                pbBlueBotAttenuationFactor.alpha = ENABLE_ALPHA
                mcbBlueBotAirAbsorb.alpha = ENABLE_ALPHA
                mcbBlueBotVoiceBlur.alpha = ENABLE_ALPHA
                mcbRedBotSpatialAudio.alpha = ENABLE_ALPHA
                pbRedBotAttenuationFactor.alpha = ENABLE_ALPHA
                mcbRedBotAirAbsorb.alpha = ENABLE_ALPHA
                mcbRedBotVoiceBlur.alpha = ENABLE_ALPHA
            } else {
                mcbBlueBotSpatialAudio.alpha = DISABLE_ALPHA
                pbBlueBotAttenuationFactor.alpha = DISABLE_ALPHA
                mcbBlueBotAirAbsorb.alpha = DISABLE_ALPHA
                mcbBlueBotVoiceBlur.alpha = DISABLE_ALPHA
                mcbRedBotSpatialAudio.alpha = DISABLE_ALPHA
                pbRedBotAttenuationFactor.alpha = DISABLE_ALPHA
                mcbRedBotAirAbsorb.alpha = DISABLE_ALPHA
                mcbRedBotVoiceBlur.alpha = DISABLE_ALPHA
            }
            mcbBlueBotSpatialAudio.isEnabled = isEnabled
            pbBlueBotAttenuationFactor.isEnabled = isEnabled
            mcbBlueBotAirAbsorb.isEnabled = isEnabled
            mcbBlueBotVoiceBlur.isEnabled = isEnabled
            mcbRedBotSpatialAudio.isEnabled = isEnabled
            pbRedBotAttenuationFactor.isEnabled = isEnabled
            mcbRedBotAirAbsorb.isEnabled = isEnabled
            mcbRedBotVoiceBlur.isEnabled = isEnabled

            mcbBlueBotSpatialAudio.setOnClickListener {
                activity?.let {
                    ToastTools.showTips(it, getString(R.string.voice_chatroom_only_host_can_change_robot))
                }
            }

            pbBlueBotAttenuationFactor.doOnProgressChanged { _, progress, _ ->
                mtBlueBotAttenuationFactorValue.text = progress.toString()
            }
            pbRedBotAttenuationFactor.doOnProgressChanged { _, progress, _ ->
                mtRedBotAttenuationFactorValue.text = progress.toString()
            }
        }
    }
}