package io.agora.scene.voice.netkit.model;

import java.io.InputStream;

public class VRHttpResponse {
    public InputStream inputStream;
    public InputStream errorStream;
    public long contentLength;
    public Exception exception;
    public int code;
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