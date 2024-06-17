package io.agora.rtmsyncmanager.service.rtm

/**
 * Data class for handling RTM receipts.
 * @property uniqueId The unique ID of the receipt.
 * @property closure The closure to be invoked when the receipt is handled. This may include error handling.
 * @property runnable The runnable to be executed when the receipt is handled.
 */
data class AUIRtmReceiptHandler(
    val uniqueId: String,
    val closure: (AUIRtmException?) -> Unit,
    val runnable: Runnable,
)