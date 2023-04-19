package io.agora.scene.voice.model;

import java.lang.System;

/**
 * 房间详情
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B#\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0002\u0010\u0004\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0007J\u000b\u0010\u0010\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010\u0011\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005H\u00c6\u0003J\'\u0010\u0012\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\u0010\b\u0002\u0010\u0004\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005H\u00c6\u0001J\u0013\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001R\"\u0010\u0004\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u001c\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001b"}, d2 = {"Lio/agora/scene/voice/model/VoiceRoomInfo;", "Lio/agora/scene/voice/model/BaseRoomBean;", "roomInfo", "Lio/agora/scene/voice/model/VoiceRoomModel;", "micInfo", "", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "(Lio/agora/scene/voice/model/VoiceRoomModel;Ljava/util/List;)V", "getMicInfo", "()Ljava/util/List;", "setMicInfo", "(Ljava/util/List;)V", "getRoomInfo", "()Lio/agora/scene/voice/model/VoiceRoomModel;", "setRoomInfo", "(Lio/agora/scene/voice/model/VoiceRoomModel;)V", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "voice_debug"})
public final class VoiceRoomInfo implements io.agora.scene.voice.model.BaseRoomBean {
    @org.jetbrains.annotations.Nullable()
    private io.agora.scene.voice.model.VoiceRoomModel roomInfo;
    @org.jetbrains.annotations.Nullable()
    private java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> micInfo;
    
    /**
     * 房间详情
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.VoiceRoomInfo copy(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.model.VoiceRoomModel roomInfo, @org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> micInfo) {
        return null;
    }
    
    /**
     * 房间详情
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 房间详情
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 房间详情
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public VoiceRoomInfo() {
        super();
    }
    
    public VoiceRoomInfo(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.model.VoiceRoomModel roomInfo, @org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> micInfo) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.model.VoiceRoomModel component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.model.VoiceRoomModel getRoomInfo() {
        return null;
    }
    
    public final void setRoomInfo(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.model.VoiceRoomModel p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> getMicInfo() {
        return null;
    }
    
    public final void setMicInfo(@org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> p0) {
    }
}