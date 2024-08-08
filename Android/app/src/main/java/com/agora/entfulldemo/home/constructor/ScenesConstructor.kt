package com.agora.entfulldemo.home.constructor

import android.content.Context
import com.agora.entfulldemo.R

/**
 * @author create by zhangwei03
 */
object ScenesConstructor {

    // ktv-独唱&合唱
    private const val ktvSoloChorusClazz = "io.agora.scene.ktv.create.RoomListActivity"

    // ktv-抢唱
    private const val ktvSingBattleClazz = "io.agora.scene.ktv.singbattle.create.RoomListActivity"

    // ktv-大合唱
    private const val ktvCantataClazz = "io.agora.scene.cantata.ui.activity.RoomListActivity"

    // ktv-接唱
    private const val ktvSingRelayClazz = "io.agora.scene.ktv.singrelay.create.RoomListActivity"

    // 语聊房-普通版
    private const val voiceChatroomClazz = "io.agora.scene.voice.ui.activity.VoiceRoomListActivity"

    // 语聊房-空间音频版
    private const val voiceSpatialClazz = "io.agora.scene.voice.spatial.ui.activity.VoiceRoomListActivity"

    // 直播-秀场
    private const val liveShowClazz = "io.agora.scene.show.RoomListActivity"

    // 直播-纯 1v1 私密房
    private const val livePure1vClazz = "io.agora.scene.pure1v1.ui.RoomListActivity"

    // 直播-多人团战
    private const val liveMultiPlayersClazz = ""

    // 直播-秀场转 1v1 私密房
    private const val liveShow1v1Clazz = "io.agora.scene.showTo1v1.ui.RoomListActivity"

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
                        background = R.drawable.bg_scene_ktv_solo_chorus,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.KTV_SingBattle,
                        clazzName = ktvSingBattleClazz,
                        name = context.getString(R.string.app_home_scene_ktv_sing_battle),
                        tip = context.getString(R.string.app_home_scene_ktv_sing_battle_tips),
                        background = R.drawable.bg_scene_ktv_singbattle,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.KTV_SingRelay,
                        clazzName = ktvSingRelayClazz,
                        name = context.getString(R.string.app_home_scene_ktv_sing_relay),
                        tip = context.getString(R.string.app_home_scene_ktv_sing_relay_tips),
                        background = R.drawable.bg_scene_ktv_singrelay,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.KTV_Cantata,
                        clazzName = ktvCantataClazz,
                        name = context.getString(R.string.app_home_scene_ktv_cantata),
                        tip = context.getString(R.string.app_home_scene_ktv_cantata_tips),
                        background = R.drawable.bg_scene_ktv_cantata,
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
                        background = R.drawable.bg_scene_voice_chatroom,
                        active = true
                    )
                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.Voice_Spatial,
                        clazzName = voiceSpatialClazz,
                        name = context.getString(R.string.app_home_scene_voice_spatial),
                        tip = context.getString(R.string.app_home_scene_voice_spatial_tips),
                        background = R.drawable.bg_scene_voice_spatial,
                        active = true
                    )
                )
            }

            HomeScenesType.Live -> {
//                subScenes.add(
//                    HomeSceneModel(
//                        scene = HomeSubScenes.Live_Show,
//                        clazzName = liveShowClazz,
//                        name = context.getString(R.string.app_home_scene_live_show),
//                        tip = context.getString(R.string.app_home_scene_live_show_tips),
//                        background = R.drawable.bg_scene_live_show,
//                        active = true
//                    )
//                )
                subScenes.add(
                    HomeSceneModel(
                        scene = HomeSubScenes.Live_Pure1v1,
                        clazzName = livePure1vClazz,
                        name = context.getString(R.string.app_home_scene_live_pure1v1),
                        tip = context.getString(R.string.app_home_scene_live_pure1v1_tips),
                        background = R.drawable.bg_scene_live_pure1v1,
                        active = true
                    )
                )
//                subScenes.add(
//                    HomeSceneModel(
//                        scene = HomeSubScenes.Live_Show1v1,
//                        clazzName = liveShow1v1Clazz,
//                        name = context.getString(R.string.app_home_scene_live_show1v1),
//                        tip = context.getString(R.string.app_home_scene_live_show1v1_tips),
//                        background = R.drawable.bg_scene_live_showto1v1,
//                        active = true
//                    )
//                )
//                subScenes.add(
//                    HomeSceneModel(
//                        scene = HomeSubScenes.Live_MultiPlayer,
//                        clazzName = liveMultiPlayersClazz,
//                        name = context.getString(R.string.app_home_scene_live_multiplayer),
//                        tip = context.getString(R.string.app_home_scene_live_multiplayer_tips),
//                        background = R.drawable.bg_scene_live_multiplayer,
//                        active = false
//                    )
//                )
            }

            else -> {
                val ktvScenes = buildScene(context, HomeScenesType.KTV)
                subScenes.addAll(ktvScenes)

                val voiceScenes = buildScene(context, HomeScenesType.Voice)
                subScenes.addAll(voiceScenes)

                val liveScenes = buildScene(context, HomeScenesType.Live)
                subScenes.addAll(liveScenes)
            }
        }
        return subScenes
    }
}
