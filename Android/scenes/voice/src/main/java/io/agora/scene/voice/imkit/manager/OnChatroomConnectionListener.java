package io.agora.scene.voice.imkit.manager;

public interface OnChatroomConnectionListener {
    /**
     * Connection callback
     */
    void onConnected();

    /**
     * Disconnection callback
     * @param error
     */
    void onDisconnected(int error);

    /**
     * Token will expire callback (called when half of token validity period remains)
     */
    void onTokenWillExpire();

    /**
     * Token expired callback
     */
    void onTokenExpired();
}
