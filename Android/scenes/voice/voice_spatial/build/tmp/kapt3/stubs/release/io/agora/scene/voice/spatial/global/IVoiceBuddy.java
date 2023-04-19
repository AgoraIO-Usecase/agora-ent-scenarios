package io.agora.scene.voice.spatial.global;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0007H&J\b\u0010\b\u001a\u00020\u0005H&J\b\u0010\t\u001a\u00020\u0005H&J\b\u0010\n\u001a\u00020\u0005H&J\b\u0010\u000b\u001a\u00020\u0005H&J\b\u0010\f\u001a\u00020\rH&J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0005H&J\u0010\u0010\u0011\u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\u0005H&J\b\u0010\u0012\u001a\u00020\u0005H&J\b\u0010\u0013\u001a\u00020\u0005H&J\b\u0010\u0014\u001a\u00020\u0005H&\u00a8\u0006\u0015"}, d2 = {"Lio/agora/scene/voice/spatial/global/IVoiceBuddy;", "", "application", "Landroid/app/Application;", "headUrl", "", "isBuildTest", "", "nickName", "rtcAppCert", "rtcAppId", "rtcToken", "rtcUid", "", "setupChatToken", "", "chatToken", "setupRtcToken", "toolboxServiceUrl", "userId", "userToken", "voice_spatial_release"})
public abstract interface IVoiceBuddy {
    
    public abstract boolean isBuildTest();
    
    /**
     * app
     */
    @org.jetbrains.annotations.NotNull()
    public abstract android.app.Application application();
    
    /**
     * api url
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String toolboxServiceUrl();
    
    /**
     * user avatar
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String headUrl();
    
    /**
     * user nickname
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String nickName();
    
    /**
     * user id
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String userId();
    
    /**
     * user token
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String userToken();
    
    /**
     * rtc user id
     */
    public abstract int rtcUid();
    
    /**
     * rtc app id
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String rtcAppId();
    
    /**
     * rtc app certificate
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String rtcAppCert();
    
    /**
     * rtc channel token
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String rtcToken();
    
    public abstract void setupRtcToken(@org.jetbrains.annotations.NotNull()
    java.lang.String rtcToken);
    
    public abstract void setupChatToken(@org.jetbrains.annotations.NotNull()
    java.lang.String chatToken);
}