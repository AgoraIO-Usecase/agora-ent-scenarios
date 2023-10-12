package com.agora.entfulldemo.login.constructor

import com.agora.entfulldemo.R

object AdvertiseConstructor {
    fun buildData(): List<AdvertiseModel> {
        return mutableListOf(
            AdvertiseModel(
                R.drawable.app_guide_chatroom,
                R.string.app_guide_chatroom,
               R.string.app_guide_chatroom_introduce
            ),
            AdvertiseModel(
                R.drawable.app_guide_ktv,
                R.string.app_guide_ktv,
                R.string.app_guide_ktv_introduce
            ),
            AdvertiseModel(
                R.drawable.app_guide_live,
                R.string.app_guide_live,
                R.string.app_guide_live_introduce
            )
        )
    }
}