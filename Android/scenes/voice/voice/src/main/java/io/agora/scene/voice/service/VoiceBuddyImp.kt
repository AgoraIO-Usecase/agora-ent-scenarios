package io.agora.scene.voice.service

import android.app.Application
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.voice.BuildConfig
import io.agora.voice.buddy.IVoiceBuddy

/**
 * @author create by zhangwei03
 */
class VoiceBuddyImp : IVoiceBuddy {

    private var chatToken: String = ""
    private var rtcToken: String = ""
    private var chatUsername: String = ""

    override fun application(): Application {
        return AgoraApplication.the()
    }

    override fun toolboxServiceUrl(): String {
        return ""
    }

    override fun headUrl(): String {
        return UserManager.getInstance().user?.headUrl ?: ""
    }

    override fun userName(): String {
        return UserManager.getInstance().user?.name ?: ""
    }

    override fun userId(): String {
        return UserManager.getInstance().user?.userNo ?: ""
    }

    override fun userToken(): String {
        return UserManager.getInstance().user?.token ?: ""
    }

    override fun rtcUid(): Int {
        // TODO: id 类型long？
        return UserManager.getInstance().user?.id?.toInt() ?: 0
    }

    override fun rtcAppId(): String {
        return BuildConfig.agora_app_id
    }

    override fun rtcAppCert(): String {
        return BuildConfig.agora_app_cert
    }

    override fun rtcToken(): String {
        return rtcToken
    }

    override fun chatUsername(): String {
        return chatUsername
    }

    override fun chatAppKey(): String {
        return BuildConfig.im_app_key
    }

    override fun chatToken(): String {
        return chatToken
    }

    override fun setupChatConfig(chatUsername: String, chatToken: String, rtcToken: String) {
        this.chatUsername = chatUsername
        this.chatToken = chatToken
        this.rtcToken = rtcToken
    }
}