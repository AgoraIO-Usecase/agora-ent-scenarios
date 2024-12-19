package io.agora.scene.base

import io.agora.scene.base.utils.SPUtil

object ServerConfig {

    private const val SERVER_HOST = "https://gateway-fulldemo.apprtc.cn/"
    private const val SERVER_HOST_DEV = "https://gateway-fulldemo-staging.agoralab.co/"
    private const val TOOLBOX_SERVER_HOST = "https://service.apprtc.cn/toolbox"
    private const val TOOLBOX_SERVER_HOST_DEV = "https://service-staging.agora.io/toolbox"
    private const val AI_CHAT_SERVER_HOST_DEV = "https://ai-chat-service-staging.sh3t.agoralab.co"
    private const val AI_CHAT_SERVER_HOST = "https://ai-chat-service.apprtc.cn"

    const val Env_Mode = "env_mode"

    var envRelease: Boolean = SPUtil.getBoolean(Env_Mode, true)
        set(newValue) {
            field = newValue
            SPUtil.putBoolean(Env_Mode, newValue)
        }

    @JvmStatic
    val serverHost: String
        get() {
            return if (envRelease) {
                SERVER_HOST
            } else {
                SERVER_HOST_DEV
            }
        }

    @JvmStatic
    val toolBoxUrl: String
        get() {
            return if (envRelease) {
                TOOLBOX_SERVER_HOST
            } else {
                TOOLBOX_SERVER_HOST_DEV
            }
        }

    @JvmStatic
    val roomManagerUrl: String
        get() {
            return toolBoxUrl.replace("toolbox", "room-manager")
        }

    @JvmStatic
    val aiChatUrl: String
        get() {
            return if (envRelease) {
                AI_CHAT_SERVER_HOST
            } else {
                AI_CHAT_SERVER_HOST_DEV
            }
        }
}