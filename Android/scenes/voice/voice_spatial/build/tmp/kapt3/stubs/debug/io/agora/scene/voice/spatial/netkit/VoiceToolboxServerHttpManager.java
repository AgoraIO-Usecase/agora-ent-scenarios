package io.agora.scene.voice.spatial.netkit;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u0000 \u001e2\u00020\u0001:\u0002\u001e\u001fB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0005\u001a\u00020\u0006H\u0002JM\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u00042\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u00042\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\f0\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011\u00a2\u0006\u0002\u0010\u0013J^\u0010\u0014\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u000426\u0010\u0019\u001a2\u0012\u0013\u0012\u00110\f\u00a2\u0006\f\b\u001b\u0012\b\b\u001c\u0012\u0004\b\b(\u001d\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\u001b\u0012\b\b\u001c\u0012\u0004\b\b(\u0016\u0012\u0004\u0012\u00020\b0\u001aR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lio/agora/scene/voice/spatial/netkit/VoiceToolboxServerHttpManager;", "", "()V", "TAG", "", "context", "Landroid/content/Context;", "generateToken", "", "channelName", "uid", "expire", "", "src", "types", "", "callBack", "Lio/agora/voice/common/net/callback/VRValueCallBack;", "Lio/agora/scene/voice/spatial/netkit/VRGenerateTokenResponse;", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;[Ljava/lang/Integer;Lio/agora/voice/common/net/callback/VRValueCallBack;)V", "requestToolboxService", "channelId", "chatroomId", "chatroomName", "chatOwner", "completion", "Lkotlin/Function2;", "Lkotlin/ParameterName;", "name", "error", "Companion", "InstanceHelper", "voice_spatial_debug"})
public final class VoiceToolboxServerHttpManager {
    private final java.lang.String TAG = "VoiceToolboxServerHttpManager";
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.netkit.VoiceToolboxServerHttpManager.Companion Companion = null;
    
    public VoiceToolboxServerHttpManager() {
        super();
    }
    
    private final android.content.Context context() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @kotlin.jvm.JvmStatic()
    public static final io.agora.scene.voice.spatial.netkit.VoiceToolboxServerHttpManager get() {
        return null;
    }
    
    /**
     * 生成RTC/RTM/Chat等Token007
     * @param channelName 频道名
     * @param expire 过期时间, 单位秒
     * @param src 来源/请求方 android
     * @param types 类型 1: RTC Token，2: RTM Token,3: Chat Token
     * @param uid 用户ID
     */
    public final void generateToken(@org.jetbrains.annotations.NotNull()
    java.lang.String channelName, @org.jetbrains.annotations.NotNull()
    java.lang.String uid, int expire, @org.jetbrains.annotations.NotNull()
    java.lang.String src, @org.jetbrains.annotations.NotNull()
    java.lang.Integer[] types, @org.jetbrains.annotations.NotNull()
    io.agora.voice.common.net.callback.VRValueCallBack<io.agora.scene.voice.spatial.netkit.VRGenerateTokenResponse> callBack) {
    }
    
    /**
     * toolbox service api 置换token, 获取im 配置
     * @param channelId rtc 频道号
     * @param chatroomId im roomId
     * @param chatroomName im 房间名
     * @param chatOwner im 房间房主
     */
    public final void requestToolboxService(@org.jetbrains.annotations.NotNull()
    java.lang.String channelId, @org.jetbrains.annotations.NotNull()
    java.lang.String chatroomId, @org.jetbrains.annotations.NotNull()
    java.lang.String chatroomName, @org.jetbrains.annotations.NotNull()
    java.lang.String chatOwner, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.String, kotlin.Unit> completion) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c0\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lio/agora/scene/voice/spatial/netkit/VoiceToolboxServerHttpManager$InstanceHelper;", "", "()V", "sSingle", "Lio/agora/scene/voice/spatial/netkit/VoiceToolboxServerHttpManager;", "getSSingle", "()Lio/agora/scene/voice/spatial/netkit/VoiceToolboxServerHttpManager;", "voice_spatial_debug"})
    public static final class InstanceHelper {
        @org.jetbrains.annotations.NotNull()
        public static final io.agora.scene.voice.spatial.netkit.VoiceToolboxServerHttpManager.InstanceHelper INSTANCE = null;
        @org.jetbrains.annotations.NotNull()
        private static final io.agora.scene.voice.spatial.netkit.VoiceToolboxServerHttpManager sSingle = null;
        
        private InstanceHelper() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final io.agora.scene.voice.spatial.netkit.VoiceToolboxServerHttpManager getSSingle() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007\u00a8\u0006\u0005"}, d2 = {"Lio/agora/scene/voice/spatial/netkit/VoiceToolboxServerHttpManager$Companion;", "", "()V", "get", "Lio/agora/scene/voice/spatial/netkit/VoiceToolboxServerHttpManager;", "voice_spatial_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        @kotlin.jvm.JvmStatic()
        public final io.agora.scene.voice.spatial.netkit.VoiceToolboxServerHttpManager get() {
            return null;
        }
    }
}