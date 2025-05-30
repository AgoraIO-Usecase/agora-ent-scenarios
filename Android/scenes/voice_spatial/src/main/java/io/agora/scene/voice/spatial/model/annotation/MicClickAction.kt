package io.agora.scene.voice.spatial.model.annotation

import androidx.annotation.IntDef

/**
 * Seat management click events
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    MicClickAction.Invite,
    MicClickAction.ForbidMic,
    MicClickAction.UnForbidMic,
    MicClickAction.Mute,
    MicClickAction.UnMute,
    MicClickAction.Lock,
    MicClickAction.UnLock,
    MicClickAction.KickOff,
    MicClickAction.OffStage,
    MicClickAction.Accept,
)
annotation class MicClickAction {
    companion object {
        // Invite (host operation)
        const val Invite = 0

        // Mute (host operation)
        const val ForbidMic = 1

        // Unmute (host operation)
        const val UnForbidMic = 2

        // Mute (seat user operation including host operation)
        const val Mute = 3

        // Unmute (seat user operation including host operation)
        const val UnMute = 4

        // Lock (host operation)
        const val Lock = 5

        // Unlock (host operation)
        const val UnLock = 6

        // Kick off (host operation)
        const val KickOff = 7

        // Off stage (guest operation)
        const val OffStage = 8

        // Accept (guest operation)
        const val Accept = 9
    }
}
