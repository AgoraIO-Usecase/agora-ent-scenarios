package io.agora.scene.voice.global

import android.app.Application
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager

/**
 * @author create by zhangwei03
 */
class VoiceBuddyImp : IVoiceBuddy {

    private var chatToken: String = ""
    private var rtcToken: String = ""
    private var rtmToken: String = ""

    override fun headUrl(): String {
        return UserManager.getInstance().user?.headUrl ?: ""
    }

    override fun nickName(): String {
        return UserManager.getInstance().user?.name ?: ""
    }

    override fun userId(): String {
        return UserManager.getInstance().user?.id?.toString() ?: ""
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
        return io.agora.scene.voice.BuildConfig.IM_APP_KEY
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

    override fun rtmToken(): String {
        return rtmToken
    }

    override fun setupRtmToken(rtmToken: String) {
        this.rtmToken = rtmToken
    }
}