package io.agora.scene.voice.rtckit;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010 \n\u0002\b\f\u0018\u0000 62\u00020\u0001:\u000267B\u0005\u00a2\u0006\u0002\u0010\u0002J(\u0010\u0013\u001a\u00020\u00072\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00122\u0006\u0010\u0017\u001a\u00020\u00122\u0006\u0010\u0018\u001a\u00020\u0007H\u0002J\u000e\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u0012J\u0006\u0010\u001c\u001a\u00020\u001aJ\u000e\u0010\u001d\u001a\u00020\u001a2\u0006\u0010\u001e\u001a\u00020\u0007J\u0010\u0010\u001f\u001a\u00020\u00072\u0006\u0010 \u001a\u00020!H\u0002J>\u0010\"\u001a\u00020\u001a2\u0006\u0010 \u001a\u00020!2\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00122\u0006\u0010\u0017\u001a\u00020\u00122\b\b\u0002\u0010#\u001a\u00020\u00072\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006J\u001a\u0010$\u001a\u00020\u001a2\u0006\u0010%\u001a\u00020\u00152\b\b\u0002\u0010&\u001a\u00020\u0012H\u0002J\u001e\u0010\'\u001a\u00020\u001a2\u0006\u0010(\u001a\u00020\u00122\u0006\u0010)\u001a\u00020\u00152\u0006\u0010*\u001a\u00020\u0012J\u0014\u0010\'\u001a\u00020\u001a2\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00100,J\u0006\u0010-\u001a\u00020\u001aJ\u000e\u0010.\u001a\u00020\u001a2\u0006\u0010/\u001a\u00020\u0007J\u000e\u00100\u001a\u00020\u001a2\u0006\u0010/\u001a\u00020\u0007J\u000e\u00101\u001a\u00020\u001a2\u0006\u0010/\u001a\u00020\u0007J\u000e\u00102\u001a\u00020\u001a2\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u00103\u001a\u00020\u001a2\u0006\u0010#\u001a\u00020\u0007J\u000e\u00104\u001a\u00020\u001a2\u0006\u00105\u001a\u00020\u0012R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00068"}, d2 = {"Lio/agora/scene/voice/rtckit/AgoraRtcEngineController;", "", "()V", "firstMediaPlayerObserver", "Lio/agora/scene/voice/rtckit/listener/MediaPlayerObserver;", "joinCallback", "Lio/agora/voice/common/net/callback/VRValueCallBack;", "", "mediaPlayer", "Lio/agora/mediaplayer/IMediaPlayer;", "micVolumeListener", "Lio/agora/scene/voice/rtckit/listener/RtcMicVolumeListener;", "rtcEngine", "Lio/agora/rtc2/RtcEngineEx;", "soundAudioQueue", "Lkotlin/collections/ArrayDeque;", "Lio/agora/scene/voice/model/SoundAudioBean;", "soundSpeakerType", "", "checkJoinChannel", "channelId", "", "rtcUid", "soundEffect", "isBroadcaster", "deNoise", "", "anisMode", "destroy", "enableLocalAudio", "enable", "initRtcEngine", "context", "Landroid/content/Context;", "joinChannel", "broadcaster", "openMediaPlayer", "url", "soundSpeaker", "playMusic", "soundId", "audioUrl", "speakerType", "soundAudioList", "", "resetMediaPlayer", "setAIAECOn", "isOn", "setAIAGCOn", "setApmOn", "setMicVolumeListener", "switchRole", "updateEffectVolume", "volume", "Companion", "InstanceHelper", "voice_debug"})
public final class AgoraRtcEngineController {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.rtckit.AgoraRtcEngineController.Companion Companion = null;
    private static final java.lang.String TAG = "AgoraRtcEngineController";
    private io.agora.rtc2.RtcEngineEx rtcEngine;
    private io.agora.scene.voice.rtckit.listener.RtcMicVolumeListener micVolumeListener;
    private io.agora.voice.common.net.callback.VRValueCallBack<java.lang.Boolean> joinCallback;
    
    /**
     * 音效队列
     */
    private final kotlin.collections.ArrayDeque<io.agora.scene.voice.model.SoundAudioBean> soundAudioQueue = null;
    private int soundSpeakerType = 0;
    private io.agora.mediaplayer.IMediaPlayer mediaPlayer;
    private final io.agora.scene.voice.rtckit.listener.MediaPlayerObserver firstMediaPlayerObserver = null;
    
    public AgoraRtcEngineController() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @kotlin.jvm.JvmStatic()
    public static final io.agora.scene.voice.rtckit.AgoraRtcEngineController get() {
        return null;
    }
    
    public final void setMicVolumeListener(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.rtckit.listener.RtcMicVolumeListener micVolumeListener) {
    }
    
    /**
     * 加入rtc频道
     */
    public final void joinChannel(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String channelId, int rtcUid, int soundEffect, boolean broadcaster, @org.jetbrains.annotations.NotNull()
    io.agora.voice.common.net.callback.VRValueCallBack<java.lang.Boolean> joinCallback) {
    }
    
    private final boolean initRtcEngine(android.content.Context context) {
        return false;
    }
    
    private final boolean checkJoinChannel(java.lang.String channelId, int rtcUid, int soundEffect, boolean isBroadcaster) {
        return false;
    }
    
    /**
     * 切换角色
     * @param broadcaster
     */
    public final void switchRole(boolean broadcaster) {
    }
    
    /**
     * Ai 降噪
     * @param anisMode 降噪模式
     */
    public final void deNoise(int anisMode) {
    }
    
    /**
     * AI 回声消除（AIAEC）
     */
    public final void setAIAECOn(boolean isOn) {
    }
    
    /**
     * AI 人声增强（AIAGC）
     */
    public final void setAIAGCOn(boolean isOn) {
    }
    
    /**
     * APM全链路音频开关
     */
    public final void setApmOn(boolean isOn) {
    }
    
    /**
     * 播放音效列表
     * @param soundAudioList 音效列表
     */
    public final void playMusic(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.model.SoundAudioBean> soundAudioList) {
    }
    
    /**
     * 播放单个音效
     * @param soundId sound id
     * @param audioUrl cdn url
     * @param speakerType 模拟哪个机器人
     */
    public final void playMusic(int soundId, @org.jetbrains.annotations.NotNull()
    java.lang.String audioUrl, int speakerType) {
    }
    
    /**
     * reset mpk
     */
    public final void resetMediaPlayer() {
    }
    
    public final void updateEffectVolume(int volume) {
    }
    
    /**
     * 本地mute/unmute
     */
    public final void enableLocalAudio(boolean enable) {
    }
    
    public final void destroy() {
    }
    
    private final void openMediaPlayer(java.lang.String url, int soundSpeaker) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lio/agora/scene/voice/rtckit/AgoraRtcEngineController$InstanceHelper;", "", "()V", "sSingle", "Lio/agora/scene/voice/rtckit/AgoraRtcEngineController;", "getSSingle", "()Lio/agora/scene/voice/rtckit/AgoraRtcEngineController;", "voice_debug"})
    public static final class InstanceHelper {
        @org.jetbrains.annotations.NotNull()
        public static final io.agora.scene.voice.rtckit.AgoraRtcEngineController.InstanceHelper INSTANCE = null;
        @org.jetbrains.annotations.NotNull()
        private static final io.agora.scene.voice.rtckit.AgoraRtcEngineController sSingle = null;
        
        private InstanceHelper() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final io.agora.scene.voice.rtckit.AgoraRtcEngineController getSSingle() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0005\u001a\u00020\u0006H\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lio/agora/scene/voice/rtckit/AgoraRtcEngineController$Companion;", "", "()V", "TAG", "", "get", "Lio/agora/scene/voice/rtckit/AgoraRtcEngineController;", "voice_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        @kotlin.jvm.JvmStatic()
        public final io.agora.scene.voice.rtckit.AgoraRtcEngineController get() {
            return null;
        }
    }
}