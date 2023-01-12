package io.agora.scene.voice.model.annotation

import androidx.annotation.IntDef

/**
 * 麦位管理点击事件
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
        // 邀请（房主操作）
        const val Invite = 0

        // 禁言（房主操作）
        const val ForbidMic = 1

        // 取消禁言（房主操作）
        const val UnForbidMic = 2

        // 关麦（麦位用户操作包括房主操作自己）
        const val Mute = 3

        // 开麦（麦位用户操作包括房主操作自己）
        const val UnMute = 4

        // 关闭座位（房主操作）
        const val Lock = 5

        // 打开座位（房主操作）
        const val UnLock = 6

        // 强制下麦（房主操作）
        const val KickOff = 7

        // 下麦（嘉宾操作）
        const val OffStage = 8

        // 同意邀请/同意申请
        const val Accept = 9
    }
}
