package io.agora.scene.voice.global

import io.agora.scene.base.BuildConfig
import io.agora.scene.base.manager.UserManager
import io.agora.scene.voice.rtckit.RtcChannelTemp

object VoiceCenter {

    @JvmStatic
    val headUrl get() = UserManager.getInstance().user?.headUrl ?: ""

    @JvmStatic
    val nickname get() = UserManager.getInstance().user?.name ?: ""

    @JvmStatic
    val userId get() = UserManager.getInstance().user?.id?.toString() ?: ""

    @JvmStatic
    val rtcUid get() = UserManager.getInstance().user?.id?.toInt() ?: 0

    @JvmStatic
    val rtcAppId get() = BuildConfig.AGORA_APP_ID

    @JvmStatic
    val rtcAppCert get() = BuildConfig.AGORA_APP_CERTIFICATE

    @JvmStatic
    var rtcToken: String = ""

    // Chat username generated from user.id to maintain consistency with iOS
    @JvmStatic
    val chatUid get() = rtcUid.toString()

    @JvmStatic
    var chatToken: String = ""

    @JvmStatic
    val chatAppKey get() = io.agora.scene.voice.BuildConfig.IM_APP_KEY

    @JvmStatic
    val chatClientId get() = io.agora.scene.voice.BuildConfig.IM_APP_CLIENT_ID

    @JvmStatic
    val chatClientSecret get() = io.agora.scene.voice.BuildConfig.IM_APP_CLIENT_SECRET

    @JvmStatic
    var rtcChannelTemp = RtcChannelTemp()

    fun destroy(){
        rtcToken = ""
        chatToken = ""
        rtcChannelTemp.reset()
    }
}