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

    private var chatUserName: String = ""
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

    override fun chatUserName(): String {
        // 环信 chatUserName 由用户userId 生成
        return chatUserName.ifEmpty { userId() }
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

    override fun setupChatConfig(chatUserName: String, chatToken: String) {
        if (chatUserName.isNotEmpty()) {
            this.chatUserName = chatUserName
        }
        this.chatToken = chatToken
    }
}