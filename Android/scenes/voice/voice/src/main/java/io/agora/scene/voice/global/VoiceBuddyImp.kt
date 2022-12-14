package io.agora.scene.voice.global

import android.app.Application
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.voice.BuildConfig

/**
 * @author create by zhangwei03
 */
class VoiceBuddyImp : IVoiceBuddy {

    private var chatToken: String = ""
    private var rtcToken: String = ""

    override fun isBuildTest(): Boolean {
        return BuildConfig.voice_env_is_test
    }

    override fun application(): Application {
        return AgoraApplication.the()
    }

    override fun toolboxServiceUrl(): String {
        return BuildConfig.toolbox_server_host
    }

    override fun headUrl(): String {
        return UserManager.getInstance().user?.headUrl ?: ""
    }

    override fun nickName(): String {
        return UserManager.getInstance().user?.name ?: ""
    }

    override fun userId(): String {
        return (UserManager.getInstance().user?.userNo ?: "").toString()
    }

    override fun userToken(): String {
        return UserManager.getInstance().user?.token ?: ""
    }

    override fun rtcUid(): Int {
        return UserManager.getInstance().user?.id?.toInt() ?: 0
    }

    override fun rtcAppId(): String {
        return io.agora.scene.base.BuildConfig.AGORA_APP_ID
    }

    override fun rtcAppCert(): String {
        return io.agora.scene.base.BuildConfig.AGORA_APP_CERTIFICATE
    }

    override fun rtcToken(): String {
        return rtcToken
    }

    override fun chatUserName(): String {
        // 环信 chatUserName 由user.id 生成 与iOS 保持统一
        return (UserManager.getInstance().user?.id ?: "").toString()
    }

    override fun chatAppKey(): String {
        return BuildConfig.im_app_key
    }

    override fun chatToken(): String {
        return chatToken
    }

    override fun setupRtcToken(rtcToken: String) {
        this.rtcToken = rtcToken
    }

    override fun setupChatToken(chatToken: String) {
        this.chatToken = chatToken
    }
}