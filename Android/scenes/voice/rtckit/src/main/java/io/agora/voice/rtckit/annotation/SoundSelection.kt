package io.agora.voice.rtckit.annotation

import androidx.annotation.IntDef

@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@IntDef(
    SoundSelection.SocialChat,
    SoundSelection.Karaoke,
    SoundSelection.GamingBuddy,
    SoundSelection.SoundCardHQ,
)
annotation class SoundSelection {
    companion object {
        const val SocialChat = 0

        const val Karaoke = 1

        const val GamingBuddy = 2

        const val SoundCardHQ = 3
    }
}

