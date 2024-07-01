package io.agora.imkitmanager.service.http

data class ChatCommonResp<Data>(
    val tip: String = "",
    val code: Int = 0,
    val msg: String?,
    val data: Data?
)