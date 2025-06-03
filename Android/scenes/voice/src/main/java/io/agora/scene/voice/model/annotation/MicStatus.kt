package io.agora.scene.voice.model.annotation

import androidx.annotation.IntDef

/**
 * Voice chat room mic position status
 *
 * 0:Normal status 1:Muted 2:Forbidden 3:Locked 4:Locked and forbidden -1:Idle 5:Bot activated status -2:Bot inactive status
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

        // Bot activated status
        const val BotActivated = 5

        // Bot inactive status
        const val BotInactive = -2
    }
}
