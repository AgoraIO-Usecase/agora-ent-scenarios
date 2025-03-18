package com.agora.entfulldemo.home.constructor

import androidx.annotation.DrawableRes

enum class HomeScenesType {
    Full,    // Full scene
    KTV,     // KTV scene
    Voice,   // Voice chatroom
    Live,    // Live streaming
    Game,    // Bullet screen games
    AIGC,    // AIGC features
}

enum class HomeSubScenes {
    KTV_SoloChorus,    // KTV - Solo & Chorus
    KTV_Cantata,       // KTV - Group Chorus
    Voice_ChatRoom,    // Voice - Standard Chatroom
    Voice_Spatial,     // Voice - Spatial Audio Version
    Live_Show,         // Live - Show Room
    Live_Pure1v1,      // Live - Pure 1v1 Private Room
    Live_MultiPlayer,  // Live - Multi-player Battle
    Live_Show1v1,      // Live - Show Room to 1v1 Private Room
    Game_Joy,          // Bullet Screen Games
    Play_Zone,         // Casual Games
    AIGC_ChatBot,      // AI Social Companion Chat
}

/**
 * Scene model data class
 */
data class HomeSceneModel constructor(
    val scene: HomeSubScenes,
    val clazzName: String,
    val name: String,
    val tip: String,
    @DrawableRes val background: Int,
    val active: Boolean = true,
)
