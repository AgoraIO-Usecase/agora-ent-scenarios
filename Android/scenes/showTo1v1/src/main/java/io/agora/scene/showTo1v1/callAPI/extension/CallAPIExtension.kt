package io.agora.scene.showTo1v1.callAPI.extension

import io.agora.scene.showTo1v1.callAPI.CallConfig
import io.agora.scene.showTo1v1.callAPI.PrepareConfig


fun Long.getCostMilliseconds(): Long {
    return System.currentTimeMillis() - this
}

fun PrepareConfig.cloneConfig(): PrepareConfig {
    return PrepareConfig(roomId, rtcToken, rtmToken, localView, remoteView, autoJoinRTC, callTimeoutMillisecond, userExtension)
}

fun CallConfig.cloneConfig(): CallConfig {
    return CallConfig(appId, userId, rtcEngine, rtmClient)
}