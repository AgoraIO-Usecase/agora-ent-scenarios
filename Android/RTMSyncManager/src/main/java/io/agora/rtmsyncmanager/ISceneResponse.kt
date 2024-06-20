package io.agora.rtmsyncmanager

/**
 * Interface for handling various scene responses.
 */
interface ISceneResponse {

    /**
     * Called when the scene metadata is about to be initialized.
     * @param channelName The name of the channel.
     * @return A map containing the metadata. Default is null.
     */
    fun onWillInitSceneMetadata(channelName: String): Map<String, Any>? { return null }

    /**
     * Called when the token is about to expire.
     * @param channelName The name of the channel.
     */
    fun onTokenPrivilegeWillExpire(channelName: String?) { }

    /**
     * Called when the scene expires.
     * @param channelName The name of the channel.
     */
    fun onSceneExpire(channelName: String) { }

    /**
     * Called when the scene is destroyed.
     * @param channelName The name of the channel.
     */
    fun onSceneDestroy(channelName: String) { }

    /**
     * Called when a user is kicked out of the scene.
     * @param channelName The name of the channel.
     * @param userId The ID of the user who was kicked out.
     */
    fun onSceneUserBeKicked(channelName: String, userId: String) { }
}