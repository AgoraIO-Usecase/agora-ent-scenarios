package io.agora.voice.common.net;

import androidx.annotation.Nullable;

/**
 * Uses to parse Resource<T>
 * hideErrorMsg is false by default
 * @param <T>
 */
public abstract class OnResourceParseCallback<T> {
    public boolean hideErrorMsg;

    public OnResourceParseCallback() {}

    /**
     * Whether to display error messages
     * @param hideErrorMsg
     */
    public OnResourceParseCallback(boolean hideErrorMsg) {
        this.hideErrorMsg = hideErrorMsg;
    }
    /**
     * success
     * @param data
     */
    public abstract void onSuccess(@Nullable T data);

    /**
     * fail
     * @param code
     * @param message
     */
    public void onError(int code, String message){}

    /**
     * in progress
     */
    public void onLoading(@Nullable T data){}

    /**
     * hide loading
     */
    public void onHideLoading(){}
}
