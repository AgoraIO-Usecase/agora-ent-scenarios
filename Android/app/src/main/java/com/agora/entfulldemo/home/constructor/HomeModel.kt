package com.agora.entfulldemo.home.constructor

import androidx.annotation.DrawableRes

enum class HomeScenesType {
    Full, // 全场景
    KTV, // ktv
    Voice, // 语聊房
    Live, // 直播
    Game, // 弹幕玩法
}

enum class HomeSubScenes {
    KTV_SoloChorus, // ktv-独唱&合唱
    KTV_SingBattle, // ktv-抢唱
    KTV_Cantata, // ktv-大合唱
    KTV_SingRelay, // ktv-接唱
    Voice_ChatRoom, // 语聊房-普通房
    Voice_Spatial, // 语聊房-空间音频版
    Live_Show, // 直播-秀场
    Live_Pure1v1, // 直播-纯 1v1 私密房
    Live_MultiPlayer, // 直播-多人团战
    Live_Show1v1, // 直播秀场转 1v1 私密房
    Game_Joy, // 弹幕玩法
}

/**
 * 场景 model
 */
data class HomeSceneModel constructor(
    val scene: HomeSubScenes,
    val clazzName: String,
    val name: String,
    val tip: String,
    @DrawableRes val background: Int,
    val active: Boolean = true,
)
