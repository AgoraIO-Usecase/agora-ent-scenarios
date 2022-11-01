package io.agora.scene.base.api;

public class ErrorCode {
    /**
     * 验证码发送失败
     */
    public final static int ERROR_SEND_V_CODE = 10005;

    /**
     * 未知错误
     */
    public final static int UNKNOWN_ERROR = 1002;

    /**
     * 服务器内部错误
     */
    public final static int SERVER_ERROR = 5000;

    /**
     * 网络异常
     */
    public final static int NETWORK_ERROR = 1004;

    /**
     * API解析异常（或者第三方数据结构更改）等其他异常
     */
    public final static int API_ERROR = 1005;
    /**
     * TOKEN错误
     */
    public final static int TOKEN_ERROR = 401;
}
