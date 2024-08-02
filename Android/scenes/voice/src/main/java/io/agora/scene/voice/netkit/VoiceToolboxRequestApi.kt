package io.agora.scene.voice.netkit

import io.agora.scene.base.ServerConfig

/**
 * @author create by zhangwei03
 */
class VoiceToolboxRequestApi {
    companion object {
        fun get() = InstanceHelper.sSingle
    }
    object InstanceHelper {
        val sSingle = VoiceToolboxRequestApi()
    }

    private val generateToken = "/v2/token/generate"
    private val createImRoom = "/v1/webdemo/im/chat/create"


    fun generateToken():String{
        return "${ServerConfig.toolBoxUrl}$generateToken"
    }

    fun createImRoom():String{
        return "${ServerConfig.toolBoxUrl}$createImRoom"
    }
}