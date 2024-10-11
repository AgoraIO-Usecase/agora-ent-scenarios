package io.agora.scene.aichat.service.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class AIBaseResponse<T> : Serializable {
    @SerializedName("errorCode", alternate = ["code"])
    var code: Int? = null

    @SerializedName("message", alternate = ["msg"])
    var message: String? = ""

    @SerializedName(value = "obj", alternate = ["result", "data"])
    var data: T? = null

    @SerializedName("requestId")
    var requestId: String? = ""

    val isSuccess: Boolean
        get() {
            return 0 == code || code == 200
        }
}

/**
 * Ai create token req
 *
 * @property channelName
 * @property uid
 * @property expire
 * @property appCert
 * @constructor Create empty A i create token req
 */
data class AICreateTokenReq constructor(
    val channelName: String,
    val uid: String,
    val expire: Long = 60 * 60 * 24,
    val appCert: String = ""
)

/**
 * AI token response
 *
 * @property token
 * @constructor Create empty A i token
 */
data class AITokenResult constructor(
    val token: String
)

/**
 * Ai create user
 *
 * @property username
 * @property userType
 * @constructor Create empty A i create user
 */
data class AICreateUserReq constructor(
    val username: String,
    @CreateUserType val userType: Int,
)

/**
 * Ai user response
 *
 * @property username
 * @constructor Create empty Ai user
 */
data class AIUserResult constructor(
    val username: String,
)

/**
 * Ai agent response
 *
 * @property index
 * @property username
 * @constructor Create empty A i agent
 */
data class AIAgentResult constructor(
    val index: Int,
    val username: String,
)

/**
 * Tts req
 *
 * @property text
 * @property voiceId 音色，枚举值见文档https://platform.minimaxi.com/document/T2A%20V2?key=66719005a427f0c8a5701643 voice_setting 中的voice_id
 * @constructor Create empty Tts req
 */
data class TTSReq constructor(
    val text: String,
    val voiceId: String,
)

/**
 * Tts result data
 *
 * @property audio 当前返回的为mp3 格式的十六进制编码
 * @constructor Create empty T t s data
 */
data class TTSResult constructor(
    val audio: String //
)

/**
 * Start voice call req
 *
 * @property uid
 * @property voiceId
 * @property prompt
 * @constructor Create empty Start voice call req
 */
data class StartVoiceCallReq constructor(
    val uid: Int,
    val voiceId: String,
    val prompt: String,
    val greeting: String = "",
    val systemName: String = ""
)

/**
 * Update voice call req
 *
 * @property isFlushAllowed
 * @constructor Create empty Update voice call req
 */
data class UpdateVoiceCallReq constructor(
    val isFlushAllowed: Boolean,
)