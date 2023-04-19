package io.agora.scene.voice.model;

import java.lang.System;

/**
 * 音效设置
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b,\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001Bi\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\u0005\u0012\b\b\u0002\u0010\t\u001a\u00020\u0005\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\r\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u000eJ\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0005H\u00c6\u0003J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\t\u0010)\u001a\u00020\u0005H\u00c6\u0003J\t\u0010*\u001a\u00020\u0005H\u00c6\u0003J\t\u0010+\u001a\u00020\u0005H\u00c6\u0003J\t\u0010,\u001a\u00020\u0003H\u00c6\u0003J\t\u0010-\u001a\u00020\u0003H\u00c6\u0003J\t\u0010.\u001a\u00020\u0003H\u00c6\u0003Jm\u0010/\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\u00052\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\u00032\b\b\u0002\u0010\r\u001a\u00020\u0005H\u00c6\u0001J\u0013\u00100\u001a\u00020\u00032\b\u00101\u001a\u0004\u0018\u000102H\u00d6\u0003J\t\u00103\u001a\u00020\u0005H\u00d6\u0001J\t\u00104\u001a\u000205H\u00d6\u0001R\u001a\u0010\t\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012R\u001a\u0010\u0006\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\u001a\u0010\u0007\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0010\"\u0004\b\u0018\u0010\u0012R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u0014\"\u0004\b\u001a\u0010\u0016R\u001a\u0010\u000b\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\u0014\"\u0004\b\u001b\u0010\u0016R\u001a\u0010\f\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\u0014\"\u0004\b\u001c\u0010\u0016R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001d\u0010\u0010\"\u0004\b\u001e\u0010\u0012R\u001a\u0010\b\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u0010\"\u0004\b \u0010\u0012R\u001a\u0010\n\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\u0014\"\u0004\b\"\u0010\u0016R\u001a\u0010\r\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b#\u0010\u0010\"\u0004\b$\u0010\u0012\u00a8\u00066"}, d2 = {"Lio/agora/scene/voice/model/RoomAudioSettingsBean;", "Lio/agora/scene/voice/model/BaseRoomBean;", "enable", "", "roomType", "", "botOpen", "botVolume", "soundSelection", "AINSMode", "spatialOpen", "isAIAECOn", "isAIAGCOn", "voiceChangerMode", "(ZIZIIIZZZI)V", "getAINSMode", "()I", "setAINSMode", "(I)V", "getBotOpen", "()Z", "setBotOpen", "(Z)V", "getBotVolume", "setBotVolume", "getEnable", "setEnable", "setAIAECOn", "setAIAGCOn", "getRoomType", "setRoomType", "getSoundSelection", "setSoundSelection", "getSpatialOpen", "setSpatialOpen", "getVoiceChangerMode", "setVoiceChangerMode", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "", "hashCode", "toString", "", "voice_debug"})
public final class RoomAudioSettingsBean implements io.agora.scene.voice.model.BaseRoomBean {
    private boolean enable;
    private int roomType;
    private boolean botOpen;
    private int botVolume;
    private int soundSelection;
    private int AINSMode;
    private boolean spatialOpen;
    private boolean isAIAECOn;
    private boolean isAIAGCOn;
    private int voiceChangerMode;
    
    /**
     * 音效设置
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.RoomAudioSettingsBean copy(boolean enable, int roomType, boolean botOpen, int botVolume, int soundSelection, int AINSMode, boolean spatialOpen, boolean isAIAECOn, boolean isAIAGCOn, int voiceChangerMode) {
        return null;
    }
    
    /**
     * 音效设置
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 音效设置
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 音效设置
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public RoomAudioSettingsBean() {
        super();
    }
    
    public RoomAudioSettingsBean(boolean enable, int roomType, boolean botOpen, int botVolume, int soundSelection, int AINSMode, boolean spatialOpen, boolean isAIAECOn, boolean isAIAGCOn, int voiceChangerMode) {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final boolean getEnable() {
        return false;
    }
    
    public final void setEnable(boolean p0) {
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int getRoomType() {
        return 0;
    }
    
    public final void setRoomType(int p0) {
    }
    
    public final boolean component3() {
        return false;
    }
    
    public final boolean getBotOpen() {
        return false;
    }
    
    public final void setBotOpen(boolean p0) {
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int getBotVolume() {
        return 0;
    }
    
    public final void setBotVolume(int p0) {
    }
    
    public final int component5() {
        return 0;
    }
    
    public final int getSoundSelection() {
        return 0;
    }
    
    public final void setSoundSelection(int p0) {
    }
    
    public final int component6() {
        return 0;
    }
    
    public final int getAINSMode() {
        return 0;
    }
    
    public final void setAINSMode(int p0) {
    }
    
    public final boolean component7() {
        return false;
    }
    
    public final boolean getSpatialOpen() {
        return false;
    }
    
    public final void setSpatialOpen(boolean p0) {
    }
    
    public final boolean component8() {
        return false;
    }
    
    public final boolean isAIAECOn() {
        return false;
    }
    
    public final void setAIAECOn(boolean p0) {
    }
    
    public final boolean component9() {
        return false;
    }
    
    public final boolean isAIAGCOn() {
        return false;
    }
    
    public final void setAIAGCOn(boolean p0) {
    }
    
    public final int component10() {
        return 0;
    }
    
    public final int getVoiceChangerMode() {
        return 0;
    }
    
    public final void setVoiceChangerMode(int p0) {
    }
}