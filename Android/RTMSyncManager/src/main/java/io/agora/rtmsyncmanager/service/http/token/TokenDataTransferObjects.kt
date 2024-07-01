package io.agora.rtmsyncmanager.service.http.token

/**
 * Data class for generating a token request.
 * @property appId The application ID.
 * @property appCert The application certificate.
 * @property channelName The name of the channel.
 * @property userId The user ID.
 */
data class TokenGenerateReq(
    val appId: String,
    val appCert: String,
    val channelName: String,
    val userId: String
)

/**
 * Data class for generating a token response.
 * @property appId The application ID.
 * @property rtcToken The RTC token.
 * @property rtmToken The RTM token.
 */
data class TokenGenerateResp(
    val appId: String,
    val rtcToken: String,
    val rtmToken: String
)