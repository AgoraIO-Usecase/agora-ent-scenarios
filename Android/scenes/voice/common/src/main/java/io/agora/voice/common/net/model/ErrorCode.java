package io.agora.voice.common.net.model;


import io.agora.voice.common.R;

/**
 * Define some local error codes
 */
public class ErrorCode extends Error {

    /**
     * no error
     */
    public final static int EM_NO_ERROR = 0;
    /**
     * Network is unavailable
     */
    public static final int NETWORK_ERROR = -2;
    /**
     * Network problem, please try again later
     */
    public static final int ERR_UNKNOWN = -20;


    public enum Error {
        NETWORK_ERROR(ErrorCode.NETWORK_ERROR, R.string.voice_error_network_error),
        UNKNOWN_ERROR(-9999, 0);


        private int code;
        private int messageId;

        private Error(int code, int messageId) {
            this.code = code;
            this.messageId = messageId;
        }

        public static Error parseMessage(int errorCode) {
            for (Error error: Error.values()) {
                if(error.code == errorCode) {
                    return error;
                }
            }
            return UNKNOWN_ERROR;
        }


        public int getMessageId() {
            return messageId;
        }


    }
}
