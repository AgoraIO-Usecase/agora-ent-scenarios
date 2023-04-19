package io.agora.scene.voice.global;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0005H&J\b\u0010\u0007\u001a\u00020\u0005H&J\b\u0010\b\u001a\u00020\u0005H&J\b\u0010\t\u001a\u00020\nH&J\b\u0010\u000b\u001a\u00020\u0005H&J\b\u0010\f\u001a\u00020\u0005H&J\b\u0010\r\u001a\u00020\u0005H&J\b\u0010\u000e\u001a\u00020\u0005H&J\b\u0010\u000f\u001a\u00020\u0010H&J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0006\u001a\u00020\u0005H&J\u0010\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u000e\u001a\u00020\u0005H&J\b\u0010\u0014\u001a\u00020\u0005H&J\b\u0010\u0015\u001a\u00020\u0005H&J\b\u0010\u0016\u001a\u00020\u0005H&\u00a8\u0006\u0017"}, d2 = {"Lio/agora/scene/voice/global/IVoiceBuddy;", "", "application", "Landroid/app/Application;", "chatAppKey", "", "chatToken", "chatUserName", "headUrl", "isBuildTest", "", "nickName", "rtcAppCert", "rtcAppId", "rtcToken", "rtcUid", "", "setupChatToken", "", "setupRtcToken", "toolboxServiceUrl", "userId", "userToken", "voice_release"})
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
    
    /**
     * im app key
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String chatAppKey();
    
    /**
     * im user login token
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String chatToken();
    
    /**
     * im user id
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String chatUserName();
    
    public abstract void setupRtcToken(@org.jetbrains.annotations.NotNull()
    java.lang.String rtcToken);
    
    public abstract void setupChatToken(@org.jetbrains.annotations.NotNull()
    java.lang.String chatToken);
}