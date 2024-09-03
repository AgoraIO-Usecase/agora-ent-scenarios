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
data class AICreateTokenReq(
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
data class AIToken(
    val token: String
)

enum class AICreateUserType {
    User,
    Agent
}

/**
 * Ai create user
 *
 * @property username
 * @property userType
 * @constructor Create empty A i create user
 */
data class AICreateUserReq(
    val username: String,
    val userType: Int,
)

/**
 * Ai user response
 *
 * @property username
 * @constructor Create empty A i user
 */
data class AIUser(
    val username: String,
)

/**
 * Ai agent response
 *
 * @property index
 * @property username
 * @constructor Create empty A i agent
 */
data class AIAgent(
    val index: Int,
    val username: String,
)