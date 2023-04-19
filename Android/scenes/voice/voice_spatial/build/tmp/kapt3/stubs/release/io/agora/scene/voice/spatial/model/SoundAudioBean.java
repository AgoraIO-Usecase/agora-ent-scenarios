package io.agora.scene.voice.spatial.model;

import java.lang.System;

/**
 * 语聊脚本
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0017\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001B1\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0006H\u00c6\u0003J;\u0010\u001c\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010\u001d\u001a\u00020\u001e2\b\u0010\u001f\u001a\u0004\u0018\u00010 H\u00d6\u0003J\t\u0010!\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\"\u001a\u00020\u0006H\u00d6\u0001R\u001a\u0010\u0005\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u001a\u0010\u0007\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000b\"\u0004\b\u000f\u0010\rR\u001a\u0010\b\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u000b\"\u0004\b\u0011\u0010\rR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0013\u00a8\u0006#"}, d2 = {"Lio/agora/scene/voice/spatial/model/SoundAudioBean;", "Lio/agora/scene/voice/spatial/model/BaseRoomBean;", "speakerType", "", "soundId", "audioUrl", "", "audioUrlHigh", "audioUrlMedium", "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getAudioUrl", "()Ljava/lang/String;", "setAudioUrl", "(Ljava/lang/String;)V", "getAudioUrlHigh", "setAudioUrlHigh", "getAudioUrlMedium", "setAudioUrlMedium", "getSoundId", "()I", "setSoundId", "(I)V", "getSpeakerType", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "", "hashCode", "toString", "voice_spatial_release"})
public final class SoundAudioBean implements io.agora.scene.voice.spatial.model.BaseRoomBean {
    private final int speakerType = 0;
    private int soundId;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String audioUrl;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String audioUrlHigh;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String audioUrlMedium;
    
    /**
     * 语聊脚本
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.model.SoundAudioBean copy(int speakerType, int soundId, @org.jetbrains.annotations.NotNull()
    java.lang.String audioUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String audioUrlHigh, @org.jetbrains.annotations.NotNull()
    java.lang.String audioUrlMedium) {
        return null;
    }
    
    /**
     * 语聊脚本
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 语聊脚本
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 语聊脚本
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public SoundAudioBean(int speakerType, int soundId, @org.jetbrains.annotations.NotNull()
    java.lang.String audioUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String audioUrlHigh, @org.jetbrains.annotations.NotNull()
    java.lang.String audioUrlMedium) {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    public final int getSpeakerType() {
        return 0;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int getSoundId() {
        return 0;
    }
    
    public final void setSoundId(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAudioUrl() {
        return null;
    }
    
    public final void setAudioUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAudioUrlHigh() {
        return null;
    }
    
    public final void setAudioUrlHigh(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAudioUrlMedium() {
        return null;
    }
    
    public final void setAudioUrlMedium(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
}