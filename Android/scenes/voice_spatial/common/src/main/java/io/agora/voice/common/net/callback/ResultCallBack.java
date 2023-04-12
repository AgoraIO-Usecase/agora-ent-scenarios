package io.agora.voice.common.net.callback;

public interface ResultCallBack<T>
{
    /**
     * \~chinese
     * 回调函数成功执行，返回参数的值。
     *
     * @param value     value 的 class 类型是 T。
     *
     * \~english
     * Occurs when the callback function executes successfully with a value returned.
     *
     * @param value     The class type of value is T.
     *
     */
    void onSuccess(T value);

    /**
     * \~chinese
     * 请求失败时的回调函数。
     *
     * @param error     错误代码，详见 {@link Error}。
     * @param errorMsg  错误信息。
     *
     * \~english
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
