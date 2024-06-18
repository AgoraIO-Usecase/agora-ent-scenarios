package io.agora.rtmsyncmanager.service.http

/**
 * Data class for a common response.
 * @property code The response code. A value of 0 indicates success.
 * @property message The response message. This may contain additional information about the response.
 * @property data The response data. This is the actual content of the response.
 * @property ts The timestamp when the response was generated.
 */
data class CommonResp<Data>(
    val code: Int = 0,
    val message: String?,
    val data: Data?,
    val ts: Long? = 0
)

/**
 * Data class for a payload response.
 * @property roomId The ID of the room.
 * @property createTime The time when the room was created.
 * @property updateTime The time when the room was last updated.
 * @property payload The payload of the room. This contains additional information about the room.
 */
data class PayloadResp<Payload>(
    val roomId: String,
    val createTime: Long,
    val updateTime: Long,
    val payload: Payload?
)