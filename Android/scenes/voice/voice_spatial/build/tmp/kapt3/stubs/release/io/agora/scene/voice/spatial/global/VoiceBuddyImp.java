package io.agora.scene.voice.spatial.global;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0006\u001a\u00020\u0007H\u0016J\b\u0010\b\u001a\u00020\u0004H\u0016J\b\u0010\t\u001a\u00020\nH\u0016J\b\u0010\u000b\u001a\u00020\u0004H\u0016J\b\u0010\f\u001a\u00020\u0004H\u0016J\b\u0010\r\u001a\u00020\u0004H\u0016J\b\u0010\u0005\u001a\u00020\u0004H\u0016J\b\u0010\u000e\u001a\u00020\u000fH\u0016J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0003\u001a\u00020\u0004H\u0016J\u0010\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u0004H\u0016J\b\u0010\u0013\u001a\u00020\u0004H\u0016J\b\u0010\u0014\u001a\u00020\u0004H\u0016J\b\u0010\u0015\u001a\u00020\u0004H\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lio/agora/scene/voice/spatial/global/VoiceBuddyImp;", "Lio/agora/scene/voice/spatial/global/IVoiceBuddy;", "()V", "chatToken", "", "rtcToken", "application", "Landroid/app/Application;", "headUrl", "isBuildTest", "", "nickName", "rtcAppCert", "rtcAppId", "rtcUid", "", "setupChatToken", "", "setupRtcToken", "toolboxServiceUrl", "userId", "userToken", "voice_spatial_release"})
public final class VoiceBuddyImp implements io.agora.scene.voice.spatial.global.IVoiceBuddy {
    private java.lang.String chatToken = "";
    private java.lang.String rtcToken = "";
    
    public VoiceBuddyImp() {
        super();
    }
    
    @java.lang.Override()
    public boolean isBuildTest() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public android.app.Application application() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toolboxServiceUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String headUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String nickName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String userId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String userToken() {
        return null;
    }
    
    @java.lang.Override()
    public int rtcUid() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String rtcAppId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String rtcAppCert() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String rtcToken() {
        return null;
    }
    
    @java.lang.Override()
    public void setupRtcToken(@org.jetbrains.annotations.NotNull()
    java.lang.String rtcToken) {
    }
    
    @java.lang.Override()
    public void setupChatToken(@org.jetbrains.annotations.NotNull()
    java.lang.String chatToken) {
    }
}