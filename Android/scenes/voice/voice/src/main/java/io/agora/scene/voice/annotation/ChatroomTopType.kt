package io.agora.secnceui.annotation

import androidx.annotation.IntDef

/**
 * @author create by zhangwei03
 */
@IntDef(
    ChatroomTopType.Owner, ChatroomTopType.ChatroomName, ChatroomTopType.Members, ChatroomTopType.Gifts,
    ChatroomTopType.Watches, ChatroomTopType.RankNo1, ChatroomTopType.RankNo2, ChatroomTopType.RankNo3
)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class ChatroomTopType {
    companion object {
        /**聊天室房主*/
        const val Owner = 0

        /**聊天室名称*/
        const val ChatroomName = 1

        /**聊天室观众数*/
        const val Members = 2

        /**礼物数*/
        const val Gifts = 3

        /**观看数*/
        const val Watches = 4

        /**排行榜No.1*/
        const val RankNo1 = 5

        /**排行榜No.2*/
        const val RankNo2 = 6

        /**排行榜No.3*/
        const val RankNo3 = 7
    }
}

