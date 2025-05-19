package io.agora.scene.voice.spatial.model.annotation

import androidx.annotation.IntDef

/**
 * Seat status in voice chat room
 *
 * 0:Normal state 1:Mute 2:Mute 3:Lock 4:Lock and mute -1:Idle 5:Robot exclusive activation state -2:Robot exclusive closed state
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    MicStatus.Unknown,
    MicStatus.Idle,
    MicStatus.Normal,
    MicStatus.Mute,
    MicStatus.ForceMute,
    MicStatus.Lock,
    MicStatus.LockForceMute,
    MicStatus.BotInactive,
    MicStatus.BotActivated,
)
annotation class MicStatus {
    companion object {
        const val Unknown = -100
        const val Idle = -1
        const val Normal = 0
        const val Mute = 1
        const val ForceMute = 2
        const val Lock = 3
        const val LockForceMute = 4

        // Robot activation state
        const val BotActivated = 5

        // Robot pending activation state
        const val BotInactive = -2
    }
}
