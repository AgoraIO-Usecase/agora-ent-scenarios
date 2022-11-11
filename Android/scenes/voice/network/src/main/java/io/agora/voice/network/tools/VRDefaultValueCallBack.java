package io.agora.voice.network.tools;

public interface VRDefaultValueCallBack<T> extends VRValueCallBack<T> {

    @Override
    default void onSuccess(T var1){}

    @Override
    default void onError(int var1, String var2){}
}