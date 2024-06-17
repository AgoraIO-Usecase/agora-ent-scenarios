package io.agora.rtmsyncmanager.service.arbiter

import io.agora.rtmsyncmanager.service.rtm.AUIRtmException

/**
 * AUIArbiterCallback is an interface that defines the methods to handle the changes in the arbitration process.
 * It includes methods to handle the event when the arbiter changes and when an error occurs.
 */
interface AUIArbiterCallback {

    /**
     * Called when the arbiter changes.
     * @param channelName The name of the channel where the arbiter changed.
     * @param arbiterId The ID of the new arbiter.
     */
    fun onArbiterDidChange(channelName: String, arbiterId: String)

    /**
     * Called when an error occurs.
     * @param channelName The name of the channel where the error occurred.
     * @param error The error that occurred.
     */
    fun onError(channelName: String, error: AUIRtmException)
}