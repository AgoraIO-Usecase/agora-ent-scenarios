package io.agora.voice.common.net.callback;

public interface VRValueCallBack<T> {
    void onSuccess(T var1);

    void onError(int var1, String var2);
}