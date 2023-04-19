package io.agora.scene.voice.spatial.netkit;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\b\u0016\u0018\u0000*\u0006\b\u0000\u0010\u0001 \u00012\u00020\u0002B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0006\u0010\u0012\u001a\u00020\u0013R\u0014\u0010\u0004\u001a\u00020\u0005X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0015\u0010\b\u001a\u0004\u0018\u00018\u0000\u00a2\u0006\n\n\u0002\u0010\u000b\u001a\u0004\b\t\u0010\nR\u0014\u0010\f\u001a\u00020\rX\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0014\u0010\u0010\u001a\u00020\rX\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000f\u00a8\u0006\u0014"}, d2 = {"Lio/agora/scene/voice/spatial/netkit/VoiceToolboxBaseResponse;", "T", "", "()V", "code", "", "getCode", "()I", "data", "getData", "()Ljava/lang/Object;", "Ljava/lang/Object;", "msg", "", "getMsg", "()Ljava/lang/String;", "tip", "getTip", "isSuccess", "", "voice_spatial_release"})
public class VoiceToolboxBaseResponse<T extends java.lang.Object> {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String tip = "";
    private final int code = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String msg = "";
    @org.jetbrains.annotations.Nullable()
    private final T data = null;
    
    public VoiceToolboxBaseResponse() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTip() {
        return null;
    }
    
    public final int getCode() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMsg() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final T getData() {
        return null;
    }
    
    public final boolean isSuccess() {
        return false;
    }
}