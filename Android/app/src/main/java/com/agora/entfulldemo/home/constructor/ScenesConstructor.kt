package com.agora.entfulldemo.home.constructor

import android.content.Context
import com.agora.entfulldemo.R

/**
 * Scene constructor for building different types of scenes
 */
object ScenesConstructor {

    // KTV - Solo & Chorus
    private const val ktvSoloChorusClazz = "io.agora.scene.ktv.create.RoomListActivity"

    // KTV - Grab-to-Sing
    private const val ktvSingBattleClazz = "io.agora.scene.ktv.singbattle.create.RoomListActivity"

    // KTV - Group Chorus
    private const val ktvCantataClazz = "io.agora.scene.cantata.create.RoomListActivity"

    // KTV - Relay Singing
    private const val ktvSingRelayClazz = "io.agora.scene.ktv.singrelay.create.RoomListActivity"

    // Voice - Standard Chatroom
    private const val voiceChatroomClazz = "io.agora.scene.voice.ui.activity.VoiceRoomListActivity"

    // Voice - Spatial Audio Version
    private const val voiceSpatialClazz = "io.agora.scene.voice.spatial.ui.activity.VoiceRoomListActivity"

    // Live - Show Room
    private const val liveShowClazz = "io.agora.scene.show.RoomListActivity"

    // Live - Pure 1v1 Private Room
    private const val livePure1vClazz = "io.agora.scene.pure1v1.ui.RoomListActivity"

    // Live - Multi-player Battle
    private const val liveMultiPlayersClazz = ""

    // Live - Show Room to 1v1 Private Room
    private const val liveShow1v1Clazz = "io.agora.scene.showTo1v1.ui.RoomListActivity"

    // Bullet Screen Games
    private const val joyGameClazz = "io.agora.scene.joy.create.RoomListActivity"

    // Casual Games
    private const val leisureGameClazz = "io.agora.scene.playzone.hall.PlayGameHallActivity"

    // AI Social Companion Chat
    private const val aiChatGameClazz = "io.agora.scene.aichat.list.AIChatListActivity"

    @JvmStatic
    fun buildScene(context: Context, sceneType: HomeScenesType): List<HomeSceneModel> {
        val subScenes = mutableListOf<HomeSceneModel>()
        when (sceneType) {
            HomeScenesType.KTV -> {
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.KTV_SoloChorus,
                        clazzName = ktvSoloChorusClazz,
                        name = context.getString(R.string.app_home_scene_ktv_solo_chorus),
                        tip = context.getString(R.string.app_home_scene_ktv_solo_chorus_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_ktv_solo_chorus,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.KTV_Cantata,
                        clazzName = ktvCantataClazz,
                        name = context.getString(R.string.app_home_scene_ktv_cantata),
                        tip = context.getString(R.string.app_home_scene_ktv_cantata_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_ktv_cantata,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.KTV_SingBattle,
                        clazzName = ktvSingBattleClazz,
                        name = context.getString(R.string.app_home_scene_ktv_sing_battle),
                        tip = context.getString(R.string.app_home_scene_ktv_sing_battle_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_ktv_singbattle,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.KTV_SingRelay,
                        clazzName = ktvSingRelayClazz,
                        name = context.getString(R.string.app_home_scene_ktv_sing_relay),
                        tip = context.getString(R.string.app_home_scene_ktv_sing_relay_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_ktv_singrelay,
                        active = true
                    )
                )
                return subScenes
            }

            HomeScenesType.Voice -> {
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.Voice_ChatRoom,
                        clazzName = voiceChatroomClazz,
                        name = context.getString(R.string.app_home_scene_voice_chatroom),
                        tip = context.getString(R.string.app_home_scene_voice_chatroom_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_voice_chatroom,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.Voice_Spatial,
                        clazzName = voiceSpatialClazz,
                        name = context.getString(R.string.app_home_scene_voice_spatial),
                        tip = context.getString(R.string.app_home_scene_voice_spatial_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_voice_spatial,
                        active = true
                    )
                )
            }

            HomeScenesType.Live -> {
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.Live_Show,
                        clazzName = liveShowClazz,
                        name = context.getString(R.string.app_home_scene_live_show),
                        tip = context.getString(R.string.app_home_scene_live_show_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_live_show,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.Live_Pure1v1,
                        clazzName = livePure1vClazz,
                        name = context.getString(R.string.app_home_scene_live_pure1v1),
                        tip = context.getString(R.string.app_home_scene_live_pure1v1_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_live_pure1v1,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.Live_Show1v1,
                        clazzName = liveShow1v1Clazz,
                        name = context.getString(R.string.app_home_scene_live_show1v1),
                        tip = context.getString(R.string.app_home_scene_live_show1v1_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_live_showto1v1,
                        active = true
                    )
                )
//                subScenes.add(
//                    HomeSceneModel(
//                        scene = HomeSubScenes.Live_MultiPlayer,
//                        clazzName = liveMultiPlayersClazz,
//                        name = context.getString(R.string.app_home_scene_live_multiplayer),
//                        tip = context.getString(R.string.app_home_scene_live_multiplayer_tips),
//                        background = io.agora.scene.widget.R.drawable.bg_scene_live_multiplayer,
//                        active = false
//                    )
//                )
            }

            HomeScenesType.Game -> {
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.Game_Joy,
                        clazzName = joyGameClazz,
                        name = context.getString(R.string.app_home_scene_game_joy),
                        tip = context.getString(R.string.app_home_scene_game_joy_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_game_joy,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.Play_Zone,
                        clazzName = leisureGameClazz,
                        name = context.getString(R.string.app_home_scene_game_leisure),
                        tip = context.getString(R.string.app_home_scene_game_leisure_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_play_zone,
                        active = true
                    )
                )
            }

            HomeScenesType.AIGC -> {
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.AIGC_ChatBot,
                        clazzName = aiChatGameClazz,
                        name = context.getString(R.string.app_home_scene_aigc_chatbot),
                        tip = context.getString(R.string.app_home_scene_aigc_chatbot_tips),
                        background = io.agora.scene.widget.R.drawable.bg_scene_aigc_chatbot,
                        active = true
                    )
                )
            }

            else -> {
                val voiceScenes = buildScene(context, HomeScenesType.Voice)
                subScenes.addAll(voiceScenes)

                val liveScenes = buildScene(context, HomeScenesType.Live)
                subScenes.addAll(liveScenes)

                val ktvScenes = buildScene(context, HomeScenesType.KTV)
                subScenes.addAll(ktvScenes)

                val gameScenes = buildScene(context, HomeScenesType.Game)
                subScenes.addAll(gameScenes)

//                val aigcScenes = buildScene(context, HomeScenesType.AIGC)
//                subScenes.addAll(aigcScenes)
            }
        }
        return subScenes
    }
}
