package io.agora.scene.voice.spatial.global

import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.rtckit.RtcChannelTemp

object VSpatialCenter {

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


    @JvmStatic
    var rtcChannelTemp = RtcChannelTemp()

    fun destroy() {
        rtcToken = ""
        rtcChannelTemp.reset()
    }

    fun generateAllToken(callback: (token: String?, exception: Exception?) -> Unit) {
        TokenGenerator.generateTokens(
            channelName = "", //  Universal token
            uid = rtcUid.toString(),
            genType = TokenGenerator.TokenGeneratorType.token007,
            tokenTypes = arrayOf(
                TokenGenerator.AgoraTokenType.rtc,
            ),
            success = { token ->
                VoiceSpatialLogger.d("VSpatialCenter", "generate tokens success")
                rtcToken = token
                callback.invoke(token, null)
            },
            failure = {
                VoiceSpatialLogger.e("VSpatialCenter", "generate tokens failed,$it")
                callback.invoke(null, it)
            })
    }
}