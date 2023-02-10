package com.agora.entfulldemo.home.constructor

import android.content.Context
import com.agora.entfulldemo.R

/**
 * @author create by zhangwei03
 */
object ScenesConstructor {

    enum class SceneType {
        Ktv_Online,
        Voice_Chat,
        Voice_Chat_Spatial,
        Meta_Live,
        Meta_Chat,
        Games,
    }

    @JvmStatic
    fun buildData(context: Context): List<ScenesModel> {
        return mutableListOf(
            ScenesModel(
                SceneType.Voice_Chat,
                context.getString(R.string.app_voice_chat),
                R.mipmap.bg_btn_home3,
                R.mipmap.bg_btn_home_chat,
                true
            ),
            ScenesModel(
                SceneType.Ktv_Online,
                context.getString(R.string.ktv_online),
                R.mipmap.bg_btn_home1,
                R.mipmap.bg_btn_home_ktv,
                true
            ),
            ScenesModel(
                SceneType.Voice_Chat_Spatial,
                context.getString(R.string.app_voice_chat),
                R.mipmap.bg_btn_home5,
                0,
                true,
                context.getString(R.string.app_voice_chat_spatialTip)
            ),
//            ScenesModel(
//                SceneType.Meta_Live,
//                context.getString(R.string.meta_live),
//                R.mipmap.bg_btn_home2,
//                R.mipmap.bg_btn_home_live,
//                false
//            ),
//            ScenesModel(
//                SceneType.Meta_Chat,
//                context.getString(R.string.meta_chatting),
//                R.mipmap.bg_btn_home3,
//                R.mipmap.bg_btn_home_chat,
//                false
//            ),
//            ScenesModel(
//                SceneType.Games,
//                context.getString(R.string.interactive_games),
//                R.mipmap.bg_btn_home4,
//                R.mipmap.bg_btn_home_youxi,
//                false
//            )
        )
    }
}