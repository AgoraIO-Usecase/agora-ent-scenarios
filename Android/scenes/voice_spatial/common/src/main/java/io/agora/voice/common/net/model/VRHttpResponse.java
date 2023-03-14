package io.agora.voice.common.net.model;

import java.io.InputStream;

public class VRHttpResponse {
    public InputStream inputStream;
    public InputStream errorStream;
    public long contentLength;
    public Exception exception;
    /**
     * 服务器返回的code
     */
    public int code;
    /**
     * 服务器返回的内容
     */
    public String content;

    @Override
    public String toString() {
        return "HttpResponse{" +
                "contentLength=" + contentLength +
                ", code=" + code +
                ", content='" + content + '\'' +
                '}';
    }
}