package io.agora.scene.voice.spatial.rtckit;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u009e\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u0014\n\u0002\b\u0007\u0018\u0000 ]2\u00020\u0001:\u0002]^B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"J\u000e\u0010#\u001a\u00020 2\u0006\u0010!\u001a\u00020\"J(\u0010$\u001a\u00020\f2\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020\u00072\u0006\u0010(\u001a\u00020\u00072\u0006\u0010)\u001a\u00020\fH\u0002J\u000e\u0010*\u001a\u00020 2\u0006\u0010+\u001a\u00020\u0007J\u0006\u0010,\u001a\u00020 J\u000e\u0010-\u001a\u00020 2\u0006\u0010.\u001a\u00020\fJ\u000e\u0010/\u001a\u00020 2\u0006\u0010.\u001a\u00020\fJ\u000e\u00100\u001a\u00020 2\u0006\u00101\u001a\u00020\fJ\u000e\u00102\u001a\u00020 2\u0006\u0010.\u001a\u00020\fJ\u000e\u00103\u001a\u00020 2\u0006\u0010.\u001a\u00020\fJ\u0010\u00104\u001a\u00020\f2\u0006\u00105\u001a\u000206H\u0002J>\u00107\u001a\u00020 2\u0006\u00105\u001a\u0002062\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020\u00072\u0006\u0010(\u001a\u00020\u00072\b\b\u0002\u00108\u001a\u00020\f2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bJ \u00109\u001a\u00020 2\u0006\u0010:\u001a\u00020\u00072\u0006\u0010;\u001a\u00020\u00072\u0006\u0010<\u001a\u00020=H\u0002J\u001a\u0010>\u001a\u00020 2\u0006\u0010?\u001a\u00020&2\b\b\u0002\u0010@\u001a\u00020\u0007H\u0002J\u001e\u0010A\u001a\u00020 2\u0006\u0010B\u001a\u00020\u00072\u0006\u0010C\u001a\u00020&2\u0006\u0010D\u001a\u00020\u0007J\u0014\u0010A\u001a\u00020 2\f\u0010E\u001a\b\u0012\u0004\u0012\u00020\u00190FJ\u0006\u0010G\u001a\u00020 J\u000e\u0010H\u001a\u00020 2\u0006\u0010I\u001a\u00020JJ\u000e\u0010K\u001a\u00020 2\u0006\u0010L\u001a\u00020\fJ\u000e\u0010M\u001a\u00020 2\u0006\u0010L\u001a\u00020\fJ\u000e\u0010N\u001a\u00020 2\u0006\u0010L\u001a\u00020\fJ\u000e\u0010O\u001a\u00020 2\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010P\u001a\u00020 2\u0006\u0010\u001d\u001a\u00020\u001eJ\u000e\u0010Q\u001a\u00020 2\u0006\u0010:\u001a\u00020\u0007J\b\u0010R\u001a\u00020 H\u0002J\u000e\u0010S\u001a\u00020 2\u0006\u00108\u001a\u00020\fJ\u000e\u0010T\u001a\u00020 2\u0006\u0010U\u001a\u00020\u0007J \u0010V\u001a\u00020 2\u0006\u0010W\u001a\u00020X2\u0006\u0010Y\u001a\u00020X2\b\b\u0002\u0010@\u001a\u00020\u0007J\u001e\u0010Z\u001a\u00020 2\u0006\u0010:\u001a\u00020\u00072\u0006\u0010W\u001a\u00020X2\u0006\u0010Y\u001a\u00020XJ\u001e\u0010[\u001a\u00020 2\u0006\u0010W\u001a\u00020X2\u0006\u0010Y\u001a\u00020X2\u0006\u0010\\\u001a\u00020XR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00140\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00190\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006_"}, d2 = {"Lio/agora/scene/voice/spatial/rtckit/AgoraRtcEngineController;", "", "()V", "botBluePlayer", "Lio/agora/mediaplayer/IMediaPlayer;", "botRedPlayer", "dataStreamId", "", "firstMediaPlayerObserver", "Lio/agora/scene/voice/spatial/rtckit/listener/MediaPlayerObserver;", "joinCallback", "Lio/agora/voice/common/net/callback/VRValueCallBack;", "", "localVoicePositionInfoRun", "Ljava/lang/Runnable;", "mediaPlayer", "micVolumeListener", "Lio/agora/scene/voice/spatial/rtckit/listener/RtcMicVolumeListener;", "playerVoicePositionInfo", "Ljava/util/HashMap;", "Lio/agora/spatialaudio/RemoteVoicePositionInfo;", "rtcEngine", "Lio/agora/rtc2/RtcEngineEx;", "soundAudioQueue", "Lkotlin/collections/ArrayDeque;", "Lio/agora/scene/voice/spatial/model/SoundAudioBean;", "soundSpeakerType", "spatial", "Lio/agora/spatialaudio/ILocalSpatialAudioEngine;", "spatialListener", "Lio/agora/scene/voice/spatial/rtckit/listener/RtcSpatialPositionListener;", "adjustBlueAttenuation", "", "progress", "", "adjustRedAttenuation", "checkJoinChannel", "channelId", "", "rtcUid", "soundEffect", "isBroadcaster", "deNoise", "anisMode", "destroy", "enableBlueAbsorb", "isChecked", "enableBlueBlur", "enableLocalAudio", "enable", "enableRedAbsorb", "enableRedBlur", "initRtcEngine", "context", "Landroid/content/Context;", "joinChannel", "broadcaster", "onRemoteSpatialStreamMessage", "uid", "streamId", "info", "Lio/agora/scene/voice/spatial/model/DataStreamInfo;", "openMediaPlayer", "url", "soundSpeaker", "playMusic", "soundId", "audioUrl", "speakerType", "soundAudioList", "", "resetMediaPlayer", "sendSelfPosition", "position", "Lio/agora/scene/voice/spatial/model/SeatPositionInfo;", "setAIAECOn", "isOn", "setAIAGCOn", "setApmOn", "setMicVolumeListener", "setSpatialListener", "setupRemoteSpatialAudio", "setupSpatialAudio", "switchRole", "updateEffectVolume", "volume", "updatePlayerPosition", "pos", "", "forward", "updateRemotePosition", "updateSelfPosition", "right", "Companion", "InstanceHelper", "voice_spatial_debug"})
public final class AgoraRtcEngineController {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController.Companion Companion = null;
    private static final java.lang.String TAG = "AgoraRtcEngineController";
    private io.agora.rtc2.RtcEngineEx rtcEngine;
    private io.agora.scene.voice.spatial.rtckit.listener.RtcMicVolumeListener micVolumeListener;
    private io.agora.scene.voice.spatial.rtckit.listener.RtcSpatialPositionListener spatialListener;
    private io.agora.spatialaudio.ILocalSpatialAudioEngine spatial;
    private final java.util.HashMap<java.lang.Integer, io.agora.spatialaudio.RemoteVoicePositionInfo> playerVoicePositionInfo = null;
    private java.lang.Runnable localVoicePositionInfoRun;
    private int dataStreamId = 0;
    private io.agora.voice.common.net.callback.VRValueCallBack<java.lang.Boolean> joinCallback;
    
    /**
     * 音效队列
     */
    private final kotlin.collections.ArrayDeque<io.agora.scene.voice.spatial.model.SoundAudioBean> soundAudioQueue = null;
    private int soundSpeakerType = 0;
    private io.agora.mediaplayer.IMediaPlayer mediaPlayer;
    private io.agora.mediaplayer.IMediaPlayer botBluePlayer;
    private io.agora.mediaplayer.IMediaPlayer botRedPlayer;
    private final io.agora.scene.voice.spatial.rtckit.listener.MediaPlayerObserver firstMediaPlayerObserver = null;
    
    public AgoraRtcEngineController() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @kotlin.jvm.JvmStatic()
    public static final io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController get() {
        return null;
    }
    
    public final void setMicVolumeListener(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.rtckit.listener.RtcMicVolumeListener micVolumeListener) {
    }
    
    public final void setSpatialListener(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.rtckit.listener.RtcSpatialPositionListener spatialListener) {
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
    
    /**
     * 初始化空间音频
     * 设置声音最大距离为 10
     * 最大接收人数为 6
     * 距离单位1值为 1f
     */
    private final void setupSpatialAudio() {
    }
    
    /**
     * 更新自己空间音频位置
     * @param pos 位置[x, y, z]
     * @param forward 朝向[x, y, z]
     * @param right 朝向[x, y, z]
     */
    public final void updateSelfPosition(@org.jetbrains.annotations.NotNull()
    float[] pos, @org.jetbrains.annotations.NotNull()
    float[] forward, @org.jetbrains.annotations.NotNull()
    float[] right) {
    }
    
    /**
     * 发送本地位置到远端
     */
    public final void sendSelfPosition(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.SeatPositionInfo position) {
    }
    
    /**
     * 更新远端音源的配置
     * 人声模糊关闭，空气衰减开启，衰减系数为0.5
     * @param uid 远端音源的uid
     */
    public final void setupRemoteSpatialAudio(int uid) {
    }
    
    /**
     * 更新远端音源的位置
     * @param pos 位置[x, y, z]
     * @param forward 朝向[x, y, z]
     */
    public final void updateRemotePosition(int uid, @org.jetbrains.annotations.NotNull()
    float[] pos, @org.jetbrains.annotations.NotNull()
    float[] forward) {
    }
    
    /**
     * 更新播放器音源位置
     * @param pos 位置[x, y, z]
     * @param forward 朝向[x, y, z]
     */
    public final void updatePlayerPosition(@org.jetbrains.annotations.NotNull()
    float[] pos, @org.jetbrains.annotations.NotNull()
    float[] forward, int soundSpeaker) {
    }
    
    /**
     * 处理远端空间位置变化产生的回调
     */
    private final void onRemoteSpatialStreamMessage(int uid, int streamId, io.agora.scene.voice.spatial.model.DataStreamInfo info) {
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
     * 播放音效列表
     * @param soundAudioList 音效列表
     */
    public final void playMusic(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.spatial.model.SoundAudioBean> soundAudioList) {
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
    
    public final void enableBlueAbsorb(boolean isChecked) {
    }
    
    public final void enableBlueBlur(boolean isChecked) {
    }
    
    public final void enableRedAbsorb(boolean isChecked) {
    }
    
    public final void enableRedBlur(boolean isChecked) {
    }
    
    public final void adjustBlueAttenuation(double progress) {
    }
    
    public final void adjustRedAttenuation(double progress) {
    }
    
    /**
     * APM全链路音频开关
     */
    public final void setApmOn(boolean isOn) {
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
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lio/agora/scene/voice/spatial/rtckit/AgoraRtcEngineController$InstanceHelper;", "", "()V", "sSingle", "Lio/agora/scene/voice/spatial/rtckit/AgoraRtcEngineController;", "getSSingle", "()Lio/agora/scene/voice/spatial/rtckit/AgoraRtcEngineController;", "voice_spatial_debug"})
    public static final class InstanceHelper {
        @org.jetbrains.annotations.NotNull()
        public static final io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController.InstanceHelper INSTANCE = null;
        @org.jetbrains.annotations.NotNull()
        private static final io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController sSingle = null;
        
        private InstanceHelper() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController getSSingle() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0005\u001a\u00020\u0006H\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lio/agora/scene/voice/spatial/rtckit/AgoraRtcEngineController$Companion;", "", "()V", "TAG", "", "get", "Lio/agora/scene/voice/spatial/rtckit/AgoraRtcEngineController;", "voice_spatial_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        @kotlin.jvm.JvmStatic()
        public final io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController get() {
            return null;
        }
    }
}