package io.agora.voice.network.tools;

public interface VRValueCallBack<T> {
    void onSuccess(T var1);

    void onError(int var1, String var2);
}