package io.agora.scene.voice.imkit.manager;

public interface OnChatroomConnectionListener {
    /**
     * 连接回调
     */
    void onConnected();

    /**
     * 断开连接回调
     * @param error
     */
    void onDisconnected(int error);

    /**
     * Token即将过期回调（距离token失效时间一半时回调）
     */
    void onTokenWillExpire();

    /**
     * Token已过期回调
     */
    void onTokenExpired();
}
