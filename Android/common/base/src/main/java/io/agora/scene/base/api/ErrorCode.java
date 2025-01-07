package io.agora.scene.base.api;

public class ErrorCode {
    /**
     * Failed to send verification code
     */
    public final static int ERROR_SEND_V_CODE = 10005;

    /**
     * Unknown error
     */
    public final static int UNKNOWN_ERROR = 1002;

    /**
     * Internal server error
     */
    public final static int SERVER_ERROR = 5000;

    /**
     * Network error
     */
    public final static int NETWORK_ERROR = 1004;

    /**
     * API parsing error (or third-party data structure changes)
     */
    public final static int API_ERROR = 1005;
    /**
     * Token error
     */
    public final static int TOKEN_ERROR = 401;
}
