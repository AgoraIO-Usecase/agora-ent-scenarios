package io.agora.scene.voice.model;

import java.lang.System;

/**
 * 降噪选择
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\bJ\t\u0010\u0011\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J1\u0010\u0015\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u00d6\u0003J\t\u0010\u001a\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u001b\u001a\u00020\u0005H\u00d6\u0001R\u001a\u0010\u0007\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\n\u00a8\u0006\u001c"}, d2 = {"Lio/agora/scene/voice/model/AINSSoundsBean;", "Lio/agora/scene/voice/model/BaseRoomBean;", "soundType", "", "soundName", "", "soundSubName", "soundMode", "(ILjava/lang/String;Ljava/lang/String;I)V", "getSoundMode", "()I", "setSoundMode", "(I)V", "getSoundName", "()Ljava/lang/String;", "getSoundSubName", "getSoundType", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "", "hashCode", "toString", "voice_release"})
public final class AINSSoundsBean implements io.agora.scene.voice.model.BaseRoomBean {
    private final int soundType = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String soundName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String soundSubName = null;
    private int soundMode;
    
    /**
     * 降噪选择
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.AINSSoundsBean copy(int soundType, @org.jetbrains.annotations.NotNull()
    java.lang.String soundName, @org.jetbrains.annotations.NotNull()
    java.lang.String soundSubName, int soundMode) {
        return null;
    }
    
    /**
     * 降噪选择
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 降噪选择
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 降噪选择
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public AINSSoundsBean() {
        super();
    }
    
    public AINSSoundsBean(int soundType, @org.jetbrains.annotations.NotNull()
    java.lang.String soundName, @org.jetbrains.annotations.NotNull()
    java.lang.String soundSubName, int soundMode) {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    public final int getSoundType() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSoundName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSoundSubName() {
        return null;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int getSoundMode() {
        return 0;
    }
    
    public final void setSoundMode(int p0) {
    }
}