package io.agora.scene.voice.imkit.custorm;

import io.agora.CallBack;
import io.agora.scene.voice.imkit.bean.ChatMessageData;

public abstract class OnMsgCallBack implements CallBack {
    /**
     * For message sending callback, not recommended to use this callback
     */
    @Override
    public void onSuccess() {

    }

    /**
     * Success callback for sending bullet screen messages
     * @param message
     */
    public abstract void onSuccess(ChatMessageData message);

    /**
     * @see #onError(String, int, String)
     * @param code
     * @param error
     */
    @Override
    public void onError(int code, String error) {

    }

    /**
     * Returns message ID to facilitate deletion of corresponding messages based on errors
     * @param messageId
     * @param code
     * @param error
     */
    public void onError(String messageId, int code, String error){

    }

    @Override
    public void onProgress(int i, String s) {

    }
}
