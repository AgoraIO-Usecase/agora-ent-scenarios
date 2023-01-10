package io.agora.voice.common.net.model;

public enum VRError {

    EM_NO_ERROR("no error",0),
    GENERAL_ERROR("Unknown error type",1);

    private int code;
    private String errMsg;

    VRError(String errMsg, int code){
        this.code = code;
        this.errMsg = errMsg;
    }

    public int errCode() {
        return code;
    }

    public String errMsg() {
        return errMsg;
    }
}
