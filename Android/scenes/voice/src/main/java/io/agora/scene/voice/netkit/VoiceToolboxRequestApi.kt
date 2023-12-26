package io.agora.scene.voice.netkit

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

    private var BASE_URL = ""
    private val generateToken = "/v2/token/generate"
    private val createImRoom = "/v1/webdemo/im/chat/create"

    fun setBaseUrl(baseUrl: String) {
        BASE_URL = baseUrl
    }

    fun generateToken():String{
        return "$BASE_URL$generateToken"
    }

    fun createImRoom():String{
        return "$BASE_URL$createImRoom"
    }
}