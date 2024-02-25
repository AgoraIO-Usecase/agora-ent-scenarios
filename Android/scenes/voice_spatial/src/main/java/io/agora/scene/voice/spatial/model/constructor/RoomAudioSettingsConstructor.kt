package io.agora.scene.voice.spatial.model.constructor

import android.content.Context
import io.agora.scene.voice.spatial.R
import io.agora.voice.common.constant.ConfigConstants

object RoomAudioSettingsConstructor {

    fun getSoundEffectName(context: Context, soundSelectionType: Int): String {
        return when (soundSelectionType) {
            ConfigConstants.SoundSelection.Social_Chat -> context.getString(R.string.voice_spatial_social_chat)
            ConfigConstants.SoundSelection.Karaoke -> context.getString(R.string.voice_spatial_karaoke)
            ConfigConstants.SoundSelection.Gaming_Buddy -> context.getString(R.string.voice_spatial_gaming_buddy)
            ConfigConstants.SoundSelection.Professional_Broadcaster -> context.getString(R.string.voice_spatial_professional_broadcaster)
            else -> {
                context.getString(R.string.voice_spatial_none)
            }
        }
    }

    fun getAINSName(context: Context, ainsMode: Int): String {
        return when (ainsMode) {
            ConfigConstants.AINSMode.AINS_Tradition_Strong -> context.getString(R.string.voice_spatial_high)
            ConfigConstants.AINSMode.AINS_Tradition_Weakness -> context.getString(R.string.voice_spatial_medium)
            else -> {
                context.getString(R.string.voice_spatial_off)
            }
        }
    }
}