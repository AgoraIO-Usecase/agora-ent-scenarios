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
            ConfigConstants.AINSMode.AINS_Tradition_Strong -> context.getString(R.string.voice_chatroom_setting_traditional_strong)
            ConfigConstants.AINSMode.AINS_Tradition_Weakness -> context.getString(R.string.voice_chatroom_setting_traditional_weakness)
            ConfigConstants.AINSMode.AINS_AI_Strong -> context.getString(R.string.voice_chatroom_setting_ai_strong)
            ConfigConstants.AINSMode.AINS_AI_Weakness -> context.getString(R.string.voice_chatroom_setting_ai_weakness)
            ConfigConstants.AINSMode.AINS_Custom -> context.getString(R.string.voice_chatroom_setting_custom)
            else -> {
                context.getString(R.string.voice_chatroom_off)
            }
        }
    }
}