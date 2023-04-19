package io.agora.scene.voice.model;

import java.lang.System;

/**
 * 贡献榜
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0013\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001B3\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000b\u0010\u0015\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u0016\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u0017\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0007H\u00c6\u0003J7\u0010\u0019\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u00d6\u0003J\t\u0010\u001e\u001a\u00020\u0007H\u00d6\u0001J\t\u0010\u001f\u001a\u00020\u0003H\u00d6\u0001R\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR \u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u000e\"\u0004\b\u0012\u0010\u0010R\u001c\u0010\u0005\u001a\u0004\u0018\u00010\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u000e\"\u0004\b\u0014\u0010\u0010\u00a8\u0006 "}, d2 = {"Lio/agora/scene/voice/model/VoiceRankUserModel;", "Lio/agora/scene/voice/model/BaseRoomBean;", "chatUid", "", "name", "portrait", "amount", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V", "getAmount", "()I", "setAmount", "(I)V", "getChatUid", "()Ljava/lang/String;", "setChatUid", "(Ljava/lang/String;)V", "getName", "setName", "getPortrait", "setPortrait", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "", "hashCode", "toString", "voice_debug"})
public final class VoiceRankUserModel implements io.agora.scene.voice.model.BaseRoomBean {
    @org.jetbrains.annotations.Nullable()
    @com.google.gson.annotations.SerializedName(value = "chat_uid")
    private java.lang.String chatUid;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String name;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String portrait;
    private int amount;
    
    /**
     * 贡献榜
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.VoiceRankUserModel copy(@org.jetbrains.annotations.Nullable()
    java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.lang.String portrait, int amount) {
        return null;
    }
    
    /**
     * 贡献榜
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 贡献榜
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 贡献榜
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public VoiceRankUserModel() {
        super();
    }
    
    public VoiceRankUserModel(@org.jetbrains.annotations.Nullable()
    java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.lang.String portrait, int amount) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getChatUid() {
        return null;
    }
    
    public final void setChatUid(@org.jetbrains.annotations.Nullable()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getName() {
        return null;
    }
    
    public final void setName(@org.jetbrains.annotations.Nullable()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPortrait() {
        return null;
    }
    
    public final void setPortrait(@org.jetbrains.annotations.Nullable()
    java.lang.String p0) {
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int getAmount() {
        return 0;
    }
    
    public final void setAmount(int p0) {
    }
}