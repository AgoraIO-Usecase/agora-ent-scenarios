package com.agora.entfulldemo.home.constructor

import android.content.Context
import com.agora.entfulldemo.R

/**
 * @author create by zhangwei03
 */
object ScenesConstructor {


    @JvmStatic
    fun buildData(context: Context): MutableList<ScenesModel> {
        return mutableListOf(
//            ScenesModel(
//                "io.agora.scene.ktv.create.RoomListActivity",
//                context.getString(R.string.ktv_online),
//                R.mipmap.bg_btn_home1,
//                R.mipmap.bg_btn_home_ktv,
//                true
//            ),
//            ScenesModel(
//                "io.agora.scene.voice.ui.activity.VoiceRoomListActivity",
//                context.getString(R.string.app_voice_chat),
//                R.mipmap.bg_btn_home1,
//                R.mipmap.bg_btn_home_ktv,
//                true
//            ),
            ScenesModel(
                "io.agora.scene.show.RoomListActivity",
                context.getString(R.string.app_show_live),
                R.mipmap.bg_btn_home1,
                R.mipmap.bg_btn_home_ktv,
                true
            ),
//            ScenesModel(
//                "",
//                context.getString(R.string.app_meta_live),
//                R.mipmap.bg_btn_home2,
//                R.mipmap.bg_btn_home_live,
//                false
//            ),
//            ScenesModel(
//                "",
//                context.getString(R.string.app_meta_chatting),
//                R.mipmap.bg_btn_home3,
//                R.mipmap.bg_btn_home_chat,
//                false
//            ),
//            ScenesModel(
//                "",
//                context.getString(R.string.app_interactive_games),
//                R.mipmap.bg_btn_home4,
//                R.mipmap.bg_btn_home_youxi,
//                false
//            )
        )
    }
}