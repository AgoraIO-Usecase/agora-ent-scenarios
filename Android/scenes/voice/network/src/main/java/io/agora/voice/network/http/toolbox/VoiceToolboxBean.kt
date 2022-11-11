package io.agora.voice.network.http.toolbox

/**
 * @author create by zhangwei03
 */
open class VoiceToolboxBaseResponse<out T> {
    val tip: String = "";
    val code: Int = 0;
    val msg: String = "";
    val data: T? = null
}

data class VRGenerateTokenResponse(
    val token: String,
) : VoiceToolboxBaseResponse<VRGenerateTokenResponse>()

data class VRCreateRoomResponse(
    val appId: String,
    val chatId: String,
    val userName: String,
) : VoiceToolboxBaseResponse<VRCreateRoomResponse>()
