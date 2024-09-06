package io.agora.rtmsyncmanager.service.rtm

import java.util.UUID

/**
 * Data class for handling RTM publish models.
 * @property uniqueId The unique ID of the model. Default is a random UUID.
 * @property interfaceName The name of the interface.
 * @property data The data of the model.
 * @property channelName The name of the channel.
 */
data class AUIRtmPublishModel<Model>(
    val uniqueId: String? = UUID.randomUUID().toString(),
    val interfaceName: String?,
    val data: Model?,
    val channelName: String?
)

/**
 * Data class for handling RTM receipt models.
 * @property uniqueId The unique ID of the receipt.
 * @property code The code of the receipt.
 * @property channelName The name of the channel.
 * @property reason The reason for the receipt.
 */
data class AUIRtmReceiptModel(
    val uniqueId: String,
    val code: Int,
    val channelName: String,
    val reason: String
)