package io.agora.scene.voice.general.net;

public interface VRValueCallBack<T> {
    void onSuccess(T var1);

    void onError(int var1, String var2);
}