package io.agora.rtmsyncmanager.service.http

data class CommonResp<Data>(
    val code: Int = 0,
    val message: String?,
    val data: Data?
)

data class PayloadResp<Payload>(
    val roomId: String,
    val createTime: Long,
    val updateTime: Long,
    val payload: Payload?
)