package io.agora.scene.voice.spatial.rtckit.listener;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\b\u0016\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0016J\u0010\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007H\u0016J\u001c\u0010\b\u001a\u00020\u00042\b\u0010\t\u001a\u0004\u0018\u00010\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u0016J\u0010\u0010\r\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u000fH\u0016J$\u0010\u0010\u001a\u00020\u00042\b\u0010\u0011\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u0013\u001a\u00020\u000f2\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0016J\u0012\u0010\u0016\u001a\u00020\u00042\b\u0010\u0017\u001a\u0004\u0018\u00010\u0018H\u0016J\u001c\u0010\u0019\u001a\u00020\u00042\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001bH\u0016J\u001c\u0010\u001d\u001a\u00020\u00042\b\u0010\u001e\u001a\u0004\u0018\u00010\u001f2\b\u0010 \u001a\u0004\u0018\u00010!H\u0016J\u0010\u0010\"\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\u000fH\u0016J\u001c\u0010$\u001a\u00020\u00042\b\u0010%\u001a\u0004\u0018\u00010\u00152\b\u0010&\u001a\u0004\u0018\u00010\'H\u0016\u00a8\u0006("}, d2 = {"Lio/agora/scene/voice/spatial/rtckit/listener/MediaPlayerObserver;", "Lio/agora/mediaplayer/IMediaPlayerObserver;", "()V", "onAgoraCDNTokenWillExpire", "", "onAudioVolumeIndication", "volume", "", "onMetaData", "type", "Lio/agora/mediaplayer/Constants$MediaPlayerMetadataType;", "data", "", "onPlayBufferUpdated", "playCachedBuffer", "", "onPlayerEvent", "eventCode", "Lio/agora/mediaplayer/Constants$MediaPlayerEvent;", "elapsedTime", "message", "", "onPlayerInfoUpdated", "info", "Lio/agora/mediaplayer/data/PlayerUpdatedInfo;", "onPlayerSrcInfoChanged", "from", "Lio/agora/mediaplayer/data/SrcInfo;", "to", "onPlayerStateChanged", "state", "Lio/agora/mediaplayer/Constants$MediaPlayerState;", "error", "Lio/agora/mediaplayer/Constants$MediaPlayerError;", "onPositionChanged", "position_ms", "onPreloadEvent", "src", "event", "Lio/agora/mediaplayer/Constants$MediaPlayerPreloadEvent;", "voice_spatial_release"})
public class MediaPlayerObserver implements io.agora.mediaplayer.IMediaPlayerObserver {
    
    public MediaPlayerObserver() {
        super();
    }
    
    @java.lang.Override()
    public void onPlayerStateChanged(@org.jetbrains.annotations.Nullable()
    io.agora.mediaplayer.Constants.MediaPlayerState state, @org.jetbrains.annotations.Nullable()
    io.agora.mediaplayer.Constants.MediaPlayerError error) {
    }
    
    @java.lang.Override()
    public void onPositionChanged(long position_ms) {
    }
    
    @java.lang.Override()
    public void onPlayerEvent(@org.jetbrains.annotations.Nullable()
    io.agora.mediaplayer.Constants.MediaPlayerEvent eventCode, long elapsedTime, @org.jetbrains.annotations.Nullable()
    java.lang.String message) {
    }
    
    @java.lang.Override()
    public void onMetaData(@org.jetbrains.annotations.Nullable()
    io.agora.mediaplayer.Constants.MediaPlayerMetadataType type, @org.jetbrains.annotations.Nullable()
    byte[] data) {
    }
    
    @java.lang.Override()
    public void onPlayBufferUpdated(long playCachedBuffer) {
    }
    
    @java.lang.Override()
    public void onPreloadEvent(@org.jetbrains.annotations.Nullable()
    java.lang.String src, @org.jetbrains.annotations.Nullable()
    io.agora.mediaplayer.Constants.MediaPlayerPreloadEvent event) {
    }
    
    @java.lang.Override()
    public void onAgoraCDNTokenWillExpire() {
    }
    
    @java.lang.Override()
    public void onPlayerSrcInfoChanged(@org.jetbrains.annotations.Nullable()
    io.agora.mediaplayer.data.SrcInfo from, @org.jetbrains.annotations.Nullable()
    io.agora.mediaplayer.data.SrcInfo to) {
    }
    
    @java.lang.Override()
    public void onPlayerInfoUpdated(@org.jetbrains.annotations.Nullable()
    io.agora.mediaplayer.data.PlayerUpdatedInfo info) {
    }
    
    @java.lang.Override()
    public void onAudioVolumeIndication(int volume) {
    }
}