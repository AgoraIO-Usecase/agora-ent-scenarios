package io.agora.voice.buddy

import android.app.Application

/**
 * @author create by zhangwei03
 */
interface IVoiceBuddy {

    fun application(): Application

    fun toolboxServiceUrl(): String

    fun headUrl(): String

    fun userName(): String

    fun userId(): String

    fun userToken(): String

    fun rtcUid(): Int

    fun rtcAppId(): String

    fun rtcAppCert(): String

    fun rtcToken(): String

    fun chatAppKey(): String

    fun chatToken(): String

    fun chatUsername(): String

    fun setupChatConfig(chatUsername: String, chatToken: String, rtcToken: String)
}