package io.agora.voice.common.net;

import io.agora.voice.common.net.model.ErrorCode;

/**
 * Result base class
 * @param <T> The entity class of the request result
 */
public class Result<T> {
    public int code;
    public T result;

    public Result(){
    }

    public Result(int code, T result) {
        this.code = code;
        this.result = result;
    }

    public Result(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public boolean isSuccess(){
        return code == ErrorCode.EM_NO_ERROR;
    }

}
