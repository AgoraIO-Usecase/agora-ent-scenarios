package io.agora.rtmsyncmanager.service.http.token

data class TokenGenerateReq constructor(
    val appId: String,
    val appCert: String,
    val channelName: String,
    val userId: String
)
data class TokenGenerateResp constructor(
    val appId: String,
    val rtcToken: String,
    val rtmToken: String
)