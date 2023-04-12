package io.agora.scene.voice.model.constructor

import android.content.Context
import io.agora.voice.common.constant.ConfigConstants
import io.agora.scene.voice.R

object RoomAudioSettingsConstructor {

    fun getSoundEffectName(context: Context, soundSelectionType: Int): String {
        return when (soundSelectionType) {
            ConfigConstants.SoundSelection.Social_Chat -> context.getString(R.string.voice_chatroom_social_chat)
            ConfigConstants.SoundSelection.Karaoke -> context.getString(R.string.voice_chatroom_karaoke)
            ConfigConstants.SoundSelection.Gaming_Buddy -> context.getString(R.string.voice_chatroom_gaming_buddy)
            ConfigConstants.SoundSelection.Professional_Broadcaster -> context.getString(R.string.voice_chatroom_professional_broadcaster)
            else -> {
                context.getString(R.string.voice_chatroom_none)
            }
        }
    }

    fun getAINSName(context: Context, ainsMode: Int): String {
        return when (ainsMode) {
            ConfigConstants.AINSMode.AINS_High -> context.getString(R.string.voice_chatroom_high)
            ConfigConstants.AINSMode.AINS_Medium -> context.getString(R.string.voice_chatroom_medium)
            else -> {
                context.getString(R.string.voice_chatroom_off)
            }
        }
    }
}