package io.agora.scene.voice.spatial.net.callback;

public interface ResultCallBack<T>
{
    /**
     * Occurs when the callback function executes successfully with a value returned.
     *
     * @param value     The class type of value is T.
     *
     */
    void onSuccess(T value);

    /**
     * Occurs when the request fails.
     *
     * @param error     The error code. See {@link Error}.
     * @param errorMsg  A description of the issue that caused this call to fail.
     */
    void onError(final int error, final String errorMsg);

    default void onError(final int error){
        onError(error,"");
    }
}
