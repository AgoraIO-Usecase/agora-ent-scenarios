package com.agora.entfulldemo.home.constructor

import android.content.Context
import com.agora.entfulldemo.R
import io.agora.scene.base.AgoraScene

/**
 * @author create by zhangwei03
 */
object ScenesConstructor {


    @JvmStatic
    fun buildData(context: Context): List<ScenesModel> {
        return mutableListOf(
            ScenesModel(
                AgoraScene.ChatRoom,
                "io.agora.scene.voice.ui.activity.VoiceRoomListActivity",
                context.getString(R.string.app_voice_chat),
                R.mipmap.bg_btn_home3,
                R.mipmap.bg_btn_home_chat,
                true
            ),
            ScenesModel(
                AgoraScene.SpatialAudioChatRoom,
                "io.agora.scene.voice.spatial.ui.activity.VoiceRoomListActivity",
                context.getString(R.string.app_voice_chat),
                R.mipmap.bg_btn_home5,
                0,
                true,
                context.getString(R.string.app_voice_chat_spatialTip)
            ),
            ScenesModel(
                AgoraScene.KTV,
                "io.agora.scene.ktv.create.RoomListActivity",
                context.getString(R.string.ktv_online),
                R.mipmap.bg_btn_home1,
                R.mipmap.bg_btn_home_ktv,
                true
            ),
            ScenesModel(
                AgoraScene.LiveShow,
                "io.agora.scene.show.RoomListActivity",
                context.getString(R.string.app_show_live),
                R.mipmap.bg_btn_home2,
                R.mipmap.bg_btn_home_live,
                true
            )
        )
    }
}