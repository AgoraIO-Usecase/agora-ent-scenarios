package io.agora.scene.base.listener;


import io.agora.scene.base.data.sync.AgoraException;

public interface EventListener {
    void onSuccess();

    void onReceive();

    void onError(String error);

    void onSubscribeError(AgoraException ex);
}