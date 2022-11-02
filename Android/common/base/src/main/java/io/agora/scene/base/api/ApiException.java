package io.agora.scene.base.api;

public class ApiException extends RuntimeException {
    public int errCode;
    public ApiException(int errCode, String msg) {
        super(msg);
        this.errCode = errCode;
    }
}
