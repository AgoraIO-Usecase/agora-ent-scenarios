package io.agora.scene.voice.model;

import java.lang.System;

/**
 * 麦位管理
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0011\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001B!\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0007H\u00c6\u0003J\'\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\u00052\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u00d6\u0003J\t\u0010\u001a\u001a\u00020\u0007H\u00d6\u0001J\t\u0010\u001b\u001a\u00020\u0003H\u00d6\u0001R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\u001c"}, d2 = {"Lio/agora/scene/voice/model/MicManagerBean;", "Lio/agora/scene/voice/model/BaseRoomBean;", "name", "", "enable", "", "micClickAction", "", "(Ljava/lang/String;ZI)V", "getEnable", "()Z", "setEnable", "(Z)V", "getMicClickAction", "()I", "setMicClickAction", "(I)V", "getName", "()Ljava/lang/String;", "component1", "component2", "component3", "copy", "equals", "other", "", "hashCode", "toString", "voice_debug"})
public final class MicManagerBean implements io.agora.scene.voice.model.BaseRoomBean {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    private boolean enable;
    private int micClickAction;
    
    /**
     * 麦位管理
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.MicManagerBean copy(@org.jetbrains.annotations.NotNull()
    java.lang.String name, boolean enable, @io.agora.scene.voice.model.annotation.MicClickAction()
    int micClickAction) {
        return null;
    }
    
    /**
     * 麦位管理
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 麦位管理
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 麦位管理
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public MicManagerBean(@org.jetbrains.annotations.NotNull()
    java.lang.String name, boolean enable, @io.agora.scene.voice.model.annotation.MicClickAction()
    int micClickAction) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final boolean getEnable() {
        return false;
    }
    
    public final void setEnable(boolean p0) {
    }
    
    public final int component3() {
        return 0;
    }
    
    public final int getMicClickAction() {
        return 0;
    }
    
    public final void setMicClickAction(int p0) {
    }
}