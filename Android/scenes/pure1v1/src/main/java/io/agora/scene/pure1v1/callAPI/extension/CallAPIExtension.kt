package io.agora.scene.pure1v1.callAPI.extension

import io.agora.scene.pure1v1.callAPI.CallConfig
import io.agora.scene.pure1v1.callAPI.PrepareConfig


fun Long.getCostMilliseconds(): Long {
    return System.currentTimeMillis() - this
}

fun PrepareConfig.cloneConfig(): PrepareConfig {
    return PrepareConfig(roomId, rtcToken, rtmToken, localView, remoteView, autoAccept, autoJoinRTC)
}

fun CallConfig.cloneConfig(): CallConfig {
    return CallConfig(appId, userId, rtcEngine, rtmClient)
}