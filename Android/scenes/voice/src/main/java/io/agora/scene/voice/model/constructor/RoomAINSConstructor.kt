package io.agora.scene.voice.model.constructor

import android.content.Context
import io.agora.scene.voice.global.ConfigConstants
import io.agora.scene.voice.R
import io.agora.scene.voice.model.AINSModeBean
import io.agora.scene.voice.model.AINSSoundsBean
import io.agora.scene.voice.model.AINSType

object RoomAINSConstructor {

    /**
     * Noise reduction level
     */
    fun builderDefaultAINSList(
        context: Context,
        anisMode: Int,
        anisMusicMode: Int,
        anisMicMode: Int
    ): MutableList<AINSModeBean> {
        return if (anisMode == ConfigConstants.AINSMode.AINS_Off) {
            mutableListOf(buildAIBean(context, AINSType.AINS_Default, anisMode))
        } else {
            mutableListOf(
                buildAIBean(context, AINSType.AINS_Default, anisMode),
                buildAIBean(context, AINSType.AINS_Music, anisMusicMode),
                buildAIBean(context, AINSType.AINS_Mic, anisMicMode)
            )
        }
    }

    fun buildAIBean(context: Context, ainsType: AINSType, anisMode: Int): AINSModeBean {
        return when (ainsType) {
            AINSType.AINS_Music -> AINSModeBean(
                ainsType,
                context.getString(R.string.voice_chatroom_setting_music),
                anisMode
            )

            AINSType.AINS_Mic -> AINSModeBean(
                ainsType,
                context.getString(R.string.voice_chatroom_setting_mic),
                anisMode
            )

            else -> AINSModeBean(ainsType, context.getString(R.string.voice_chatroom_setting_ains), anisMode)
        }
    }

    /**
     * Noise reduction sound effects
     */
    fun builderDefaultSoundList(context: Context): MutableList<AINSSoundsBean> {
        return mutableListOf(
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_TVSound,
                context.getString(R.string.voice_chatroom_sounds_tv)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_KitchenSound,
                context.getString(R.string.voice_chatroom_sounds_kitchen)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_StreetSound,
                context.getString(R.string.voice_chatroom_sounds_street),
                context.getString(R.string.voice_chatroom_sounds_street_tips)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_MachineSound,
                context.getString(R.string.voice_chatroom_sounds_machine),
                context.getString(R.string.voice_chatroom_sounds_machine_tips)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_OfficeSound,
                context.getString(R.string.voice_chatroom_sounds_office),
                context.getString(R.string.voice_chatroom_sounds_office_tips)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_HomeSound,
                context.getString(R.string.voice_chatroom_sounds_home),
                context.getString(R.string.voice_chatroom_sounds_home_tips)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_ConstructionSound,
                context.getString(R.string.voice_chatroom_sounds_construction),
                context.getString(R.string.voice_chatroom_sounds_construction_tips)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_AlertSound,
                context.getString(R.string.voice_room_sounds_alert)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_ApplauseSound,
                context.getString(R.string.voice_room_sounds_applause)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_WindSound,
                context.getString(R.string.voice_room_sounds_wind)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_MicPopFilterSound,
                context.getString(R.string.voice_room_sounds_mic_pop_filter)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_AudioFeedback,
                context.getString(R.string.voice_room_sounds_audio_feedback)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_MicrophoneFingerRub,
                context.getString(R.string.voice_room_sounds_microphone_finger_rub_sound)
            ),
            AINSSoundsBean(
                ConfigConstants.AINSSoundType.AINS_MicrophoneScreenTap,
                context.getString(R.string.voice_room_sounds_microphone_screen_tap_sound)
            )
        )
    }
}