package io.agora.scene.voice.spatial.ui.dialog

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomSpatialAudioBinding
import io.agora.scene.voice.spatial.global.ConfigConstants.DISABLE_ALPHA
import io.agora.scene.voice.spatial.global.ConfigConstants.ENABLE_ALPHA
import io.agora.scene.widget.doOnProgressChanged

class RoomSpatialAudioSheetDialog constructor() : BaseBottomSheetDialogFragment<VoiceSpatialDialogRoomSpatialAudioBinding>() {

    companion object {
        const val KEY_SPATIAL_OPEN = "key_spatial_open"
        const val KEY_IS_ENABLED = "is_enabled"

        const val KEY_BLUE_AIR_ABSORB_ENABLED = "blue_absorb_is_enabled"
        const val KEY_RED_AIR_ABSORB_ENABLED = "red_absorb_is_enabled"
        const val KEY_BLUE_BLUR_ENABLED = "blue_blur_is_enabled"
        const val KEY_RED_BLUR_ENABLED = "red_blur_is_enabled"
        const val KEY_BLUE_ATTENUATION = "blue_attenuation"
        const val KEY_RED_ATTENUATION = "red_attenuation"
    }

    private val isEnabled: Boolean by lazy {
        arguments?.getBoolean(KEY_IS_ENABLED, true) ?: true
    }

    private val isBlueAbsorbEnabled: Boolean by lazy {
        arguments?.getBoolean(KEY_BLUE_AIR_ABSORB_ENABLED, false) ?: true
    }
    private val isRedAbsorbEnabled: Boolean by lazy {
        arguments?.getBoolean(KEY_RED_AIR_ABSORB_ENABLED, false) ?: true
    }
    private val isBlueBlurEnabled: Boolean by lazy {
        arguments?.getBoolean(KEY_BLUE_BLUR_ENABLED, false) ?: true
    }
    private val isRedBlurEnabled: Boolean by lazy {
        arguments?.getBoolean(KEY_RED_BLUR_ENABLED, false) ?: true
    }
    private val blueAttenuation: Int by lazy {
        arguments?.getInt(KEY_BLUE_ATTENUATION, 0) ?: 0
    }
    private val redAttenuation: Int by lazy {
        arguments?.getInt(KEY_RED_ATTENUATION, 0) ?: 0
    }

    var audioSettingsListener: OnClickSpatialAudioRobotsSettingsListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_spatial_BottomSheetDialogAnimation
        dialog?.setCanceledOnTouchOutside(false)
        arguments?.apply {
            val spatialOpen = getBoolean(KEY_SPATIAL_OPEN)
        }
        mBinding?.apply {
            ivBottomSheetBack.setOnClickListener {
                dismiss()
            }

            mcbBlueBotAirAbsorb.isChecked = isBlueAbsorbEnabled
            mcbBlueBotVoiceBlur.isChecked = isBlueBlurEnabled
            mcbRedBotAirAbsorb.isChecked = isRedAbsorbEnabled
            mcbRedBotVoiceBlur.isChecked = isRedBlurEnabled
            pbBlueBotAttenuationFactor.progress = blueAttenuation
            pbRedBotAttenuationFactor.progress = redAttenuation
            mtBlueBotAttenuationFactorValue.text = blueAttenuation.toString()
            mtRedBotAttenuationFactorValue.text = redAttenuation.toString()

            if (isEnabled) {
                pbBlueBotAttenuationFactor.alpha = ENABLE_ALPHA
                mcbBlueBotAirAbsorb.alpha = ENABLE_ALPHA
                mcbBlueBotVoiceBlur.alpha = ENABLE_ALPHA
                pbRedBotAttenuationFactor.alpha = ENABLE_ALPHA
                mcbRedBotAirAbsorb.alpha = ENABLE_ALPHA
                mcbRedBotVoiceBlur.alpha = ENABLE_ALPHA
            } else {
                pbBlueBotAttenuationFactor.alpha = DISABLE_ALPHA
                mcbBlueBotAirAbsorb.alpha = DISABLE_ALPHA
                mcbBlueBotVoiceBlur.alpha = DISABLE_ALPHA
                pbRedBotAttenuationFactor.alpha = DISABLE_ALPHA
                mcbRedBotAirAbsorb.alpha = DISABLE_ALPHA
                mcbRedBotVoiceBlur.alpha = DISABLE_ALPHA
            }
            pbBlueBotAttenuationFactor.isEnabled = isEnabled
            mcbBlueBotAirAbsorb.isEnabled = isEnabled
            mcbBlueBotVoiceBlur.isEnabled = isEnabled
            pbRedBotAttenuationFactor.isEnabled = isEnabled
            mcbRedBotAirAbsorb.isEnabled = isEnabled
            mcbRedBotVoiceBlur.isEnabled = isEnabled

            // Blue robot attenuation factor
            pbBlueBotAttenuationFactor.doOnProgressChanged { _, progress, _ ->
                mtBlueBotAttenuationFactorValue.text = progress.toString()
                audioSettingsListener?.onBlueBotAttenuationChange(progress)
            }

            // Blue robot air attenuation switch
            mcbBlueBotAirAbsorb.setOnCheckedChangeListener { button, isChecked ->
                audioSettingsListener?.onBlueBotAirAbsorbCheckedChanged(button, isChecked)
            }

            // Blue robot voice blur switch
            mcbBlueBotVoiceBlur.setOnCheckedChangeListener { button, isChecked ->
                audioSettingsListener?.onBlueBotVoiceBlurCheckedChanged(button, isChecked)
            }

            // Red robot attenuation factor
            pbRedBotAttenuationFactor.doOnProgressChanged { _, progress, _ ->
                mtRedBotAttenuationFactorValue.text = progress.toString()
                audioSettingsListener?.onRedBotAttenuationChange(progress)
            }

            // Red robot air attenuation switch
            mcbRedBotAirAbsorb.setOnCheckedChangeListener { button, isChecked ->
                audioSettingsListener?.onRedBotAirAbsorbCheckedChanged(button, isChecked)
            }

            // Red robot voice blur switch
            mcbRedBotVoiceBlur.setOnCheckedChangeListener { button, isChecked ->
                audioSettingsListener?.onRedBotVoiceBlurCheckedChanged(button, isChecked)
            }
        }
    }

    interface OnClickSpatialAudioRobotsSettingsListener {
        /** Blue robot attenuation factor */
        fun onBlueBotAttenuationChange(progress: Int)

        /** Blue robot air attenuation switch */
        fun onBlueBotAirAbsorbCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)

        /** Blue robot voice blur switch */
        fun onBlueBotVoiceBlurCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)

        /** Red robot attenuation factor */
        fun onRedBotAttenuationChange(progress: Int)

        /** Red robot air attenuation switch */
        fun onRedBotAirAbsorbCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)

        /** Red robot voice blur switch */
        fun onRedBotVoiceBlurCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)
    }
}