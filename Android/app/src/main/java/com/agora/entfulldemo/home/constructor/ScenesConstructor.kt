package com.agora.entfulldemo.home.constructor

import android.content.Context
import com.agora.entfulldemo.R

/**
 * @author create by zhangwei03
 */
object ScenesConstructor {


    @JvmStatic
    fun buildData(context: Context): List<ScenesModel> {
        return mutableListOf(
            ScenesModel(
                AgoraScenes.OneToOne,
                "io.agora.scene.pure1v1.ui.RoomListActivity",
                context.getString(R.string.app_one_to_one),
                R.mipmap.bg_btn_home6,
                R.mipmap.bg_btn_home_pure1v1,
                true
            )
        )
    }
}