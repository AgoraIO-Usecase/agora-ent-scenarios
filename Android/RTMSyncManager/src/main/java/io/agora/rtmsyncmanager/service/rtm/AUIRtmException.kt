package io.agora.rtmsyncmanager.service.rtm

class AUIRtmException(
    val code: Int,
    val reason: String,
    val operation: String
) : Throwable(reason)