package io.agora.onetoone.extension

import io.agora.onetoone.CallConfig
import io.agora.onetoone.PrepareConfig


fun Long.getCostMilliseconds(): Long {
    return System.currentTimeMillis() - this
}

fun PrepareConfig.cloneConfig(): PrepareConfig {
    return PrepareConfig(roomId, rtcToken, rtmToken, localView, remoteView, callTimeoutMillisecond, userExtension)
}

fun CallConfig.cloneConfig(): CallConfig {
    return CallConfig(appId, userId, rtcEngine, signalClient)
}