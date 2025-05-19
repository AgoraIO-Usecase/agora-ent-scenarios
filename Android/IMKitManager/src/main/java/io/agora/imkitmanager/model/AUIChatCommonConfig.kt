package io.agora.imkitmanager.model

import android.content.Context

data class AUIChatCommonConfig(
    var context: Context,
    var appId: String = "",
    var appCert: String = "",
    var host: String = "",
    var owner: AUIChatUserInfo,
    var imAppKey: String = "",
    var imClientId: String = "",
    var imClientSecret: String = "",
    var chatLogCallback: ChatLogCallback? = null
)

interface ChatLogCallback {
    fun onDebugInfo(tag: String, message: String) {}
    fun onErrorInfo(tag: String, message: String) {}
}
