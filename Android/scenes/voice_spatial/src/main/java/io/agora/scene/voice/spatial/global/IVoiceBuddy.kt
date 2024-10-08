package io.agora.scene.voice.spatial.global

import android.app.Application

/**
 * @author create by zhangwei03
 */
interface IVoiceBuddy {
    /** app */
    fun application(): Application

    /** user avatar */
    fun headUrl(): String

    /** user nickname */
    fun nickName(): String

    /** user id */
    fun userId(): String

    /** user token */
    fun userToken(): String

    /** rtc user id */
    fun rtcUid(): Int

    /** rtc app id*/
    fun rtcAppId(): String

    /** rtc app certificate*/
    fun rtcAppCert(): String

    /** rtc channel token */
    fun rtcToken(): String

    fun setupRtcToken(rtcToken: String)

    fun setupChatToken(chatToken: String)
}