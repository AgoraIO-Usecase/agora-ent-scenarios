package io.agora.scene.voice.model;

import java.lang.System;

/**
 * 用户数据
 * @see io.agora.scene.base.api.model.User
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u001e\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001BS\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\b\u0012\b\b\u0002\u0010\n\u001a\u00020\b\u00a2\u0006\u0002\u0010\u000bJ\u000b\u0010\u001e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010!\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\"\u001a\u00020\bH\u00c6\u0003J\t\u0010#\u001a\u00020\bH\u00c6\u0003J\t\u0010$\u001a\u00020\bH\u00c6\u0003JW\u0010%\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\bH\u00c6\u0001J\u0013\u0010&\u001a\u00020\'2\b\u0010(\u001a\u0004\u0018\u00010)H\u00d6\u0003J\t\u0010*\u001a\u00020\bH\u00d6\u0001J\t\u0010+\u001a\u00020\u0003H\u00d6\u0001R \u0010\u0004\u001a\u0004\u0018\u00010\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u001e\u0010\t\u001a\u00020\b8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\u001e\u0010\n\u001a\u00020\b8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u0011\"\u0004\b\u0015\u0010\u0013R \u0010\u0005\u001a\u0004\u0018\u00010\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\r\"\u0004\b\u0017\u0010\u000fR \u0010\u0006\u001a\u0004\u0018\u00010\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\r\"\u0004\b\u0019\u0010\u000fR\u001e\u0010\u0007\u001a\u00020\b8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001a\u0010\u0011\"\u0004\b\u001b\u0010\u0013R \u0010\u0002\u001a\u0004\u0018\u00010\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\r\"\u0004\b\u001d\u0010\u000f\u00a8\u0006,"}, d2 = {"Lio/agora/scene/voice/model/VoiceMemberModel;", "Lio/agora/scene/voice/model/BaseRoomBean;", "userId", "", "chatUid", "nickName", "portrait", "rtcUid", "", "micIndex", "micStatus", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;III)V", "getChatUid", "()Ljava/lang/String;", "setChatUid", "(Ljava/lang/String;)V", "getMicIndex", "()I", "setMicIndex", "(I)V", "getMicStatus", "setMicStatus", "getNickName", "setNickName", "getPortrait", "setPortrait", "getRtcUid", "setRtcUid", "getUserId", "setUserId", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "", "hashCode", "toString", "voice_release"})
public final class VoiceMemberModel implements io.agora.scene.voice.model.BaseRoomBean {
    @org.jetbrains.annotations.Nullable()
    @com.google.gson.annotations.SerializedName(value = "uid")
    private java.lang.String userId;
    @org.jetbrains.annotations.Nullable()
    @com.google.gson.annotations.SerializedName(value = "chat_uid")
    private java.lang.String chatUid;
    @org.jetbrains.annotations.Nullable()
    @com.google.gson.annotations.SerializedName(value = "name")
    private java.lang.String nickName;
    @org.jetbrains.annotations.Nullable()
    @com.google.gson.annotations.SerializedName(value = "portrait")
    private java.lang.String portrait;
    @com.google.gson.annotations.SerializedName(value = "rtc_uid")
    private int rtcUid;
    @com.google.gson.annotations.SerializedName(value = "mic_index")
    private int micIndex;
    @com.google.gson.annotations.SerializedName(value = "micStatus")
    private int micStatus;
    
    /**
     * 用户数据
     * @see io.agora.scene.base.api.model.User
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.VoiceMemberModel copy(@org.jetbrains.annotations.Nullable()
    java.lang.String userId, @org.jetbrains.annotations.Nullable()
    java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
    java.lang.String nickName, @org.jetbrains.annotations.Nullable()
    java.lang.String portrait, int rtcUid, int micIndex, int micStatus) {
        return null;
    }
    
    /**
     * 用户数据
     * @see io.agora.scene.base.api.model.User
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 用户数据
     * @see io.agora.scene.base.api.model.User
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 用户数据
     * @see io.agora.scene.base.api.model.User
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public VoiceMemberModel() {
        super();
    }
    
    public VoiceMemberModel(@org.jetbrains.annotations.Nullable()
    java.lang.String userId, @org.jetbrains.annotations.Nullable()
    java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
    java.lang.String nickName, @org.jetbrains.annotations.Nullable()
    java.lang.String portrait, int rtcUid, int micIndex, int micStatus) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUserId() {
        return null;
    }
    
    public final void setUserId(@org.jetbrains.annotations.Nullable()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
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
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getNickName() {
        return null;
    }
    
    public final void setNickName(@org.jetbrains.annotations.Nullable()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPortrait() {
        return null;
    }
    
    public final void setPortrait(@org.jetbrains.annotations.Nullable()
    java.lang.String p0) {
    }
    
    public final int component5() {
        return 0;
    }
    
    public final int getRtcUid() {
        return 0;
    }
    
    public final void setRtcUid(int p0) {
    }
    
    public final int component6() {
        return 0;
    }
    
    public final int getMicIndex() {
        return 0;
    }
    
    public final void setMicIndex(int p0) {
    }
    
    public final int component7() {
        return 0;
    }
    
    public final int getMicStatus() {
        return 0;
    }
    
    public final void setMicStatus(int p0) {
    }
}