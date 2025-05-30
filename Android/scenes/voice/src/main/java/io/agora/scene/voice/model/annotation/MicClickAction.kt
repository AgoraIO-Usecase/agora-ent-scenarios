package io.agora.scene.voice.model.annotation

import androidx.annotation.IntDef

/**
 * Mic position management click events
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
        // Invite (owner operation)
        const val Invite = 0

        // Mute (owner operation)
        const val ForbidMic = 1

        // Unmute (owner operation)
        const val UnForbidMic = 2

        // Mute mic (mic user operation including owner operating their own mic)
        const val Mute = 3

        // Unmute mic (mic user operation including owner operating their own mic)
        const val UnMute = 4

        // Close position (owner operation)
        const val Lock = 5

        // Open position (owner operation)
        const val UnLock = 6

        // Force user off mic (owner operation)
        const val KickOff = 7

        // Leave mic (guest operation)
        const val OffStage = 8

        // Accept invitation/application
        const val Accept = 9
    }
}
