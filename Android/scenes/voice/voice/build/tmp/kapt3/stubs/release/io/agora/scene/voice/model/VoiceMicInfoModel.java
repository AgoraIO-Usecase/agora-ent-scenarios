package io.agora.scene.voice.model;

import java.lang.System;

/**
 * 麦位数据
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u001d\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001BC\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u001e\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010 \u001a\u00020\u0003H\u00c6\u0003J\t\u0010!\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\"\u001a\u00020\tH\u00c6\u0003J\t\u0010#\u001a\u00020\u0003H\u00c6\u0003JG\u0010$\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010%\u001a\u00020\t2\b\u0010&\u001a\u0004\u0018\u00010\'H\u00d6\u0003J\t\u0010(\u001a\u00020\u0003H\u00d6\u0001J\t\u0010)\u001a\u00020*H\u00d6\u0001R\u001a\u0010\n\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\u001e\u0010\u0002\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\r\"\u0004\b\u0015\u0010\u000fR\u001e\u0010\u0006\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\r\"\u0004\b\u0017\u0010\u000fR\u001a\u0010\b\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\u0019\"\u0004\b\u001a\u0010\u001bR\u001a\u0010\u0007\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\r\"\u0004\b\u001d\u0010\u000f\u00a8\u0006+"}, d2 = {"Lio/agora/scene/voice/model/VoiceMicInfoModel;", "Lio/agora/scene/voice/model/BaseRoomBean;", "micIndex", "", "member", "Lio/agora/scene/voice/model/VoiceMemberModel;", "micStatus", "userStatus", "ownerTag", "", "audioVolumeType", "(ILio/agora/scene/voice/model/VoiceMemberModel;IIZI)V", "getAudioVolumeType", "()I", "setAudioVolumeType", "(I)V", "getMember", "()Lio/agora/scene/voice/model/VoiceMemberModel;", "setMember", "(Lio/agora/scene/voice/model/VoiceMemberModel;)V", "getMicIndex", "setMicIndex", "getMicStatus", "setMicStatus", "getOwnerTag", "()Z", "setOwnerTag", "(Z)V", "getUserStatus", "setUserStatus", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "", "hashCode", "toString", "", "voice_release"})
public final class VoiceMicInfoModel implements io.agora.scene.voice.model.BaseRoomBean {
    @com.google.gson.annotations.SerializedName(value = "mic_index")
    private int micIndex;
    @org.jetbrains.annotations.Nullable()
    private io.agora.scene.voice.model.VoiceMemberModel member;
    @com.google.gson.annotations.SerializedName(value = "status")
    private int micStatus;
    @kotlin.jvm.Transient()
    private transient int userStatus;
    @kotlin.jvm.Transient()
    private transient boolean ownerTag;
    @kotlin.jvm.Transient()
    private transient int audioVolumeType;
    
    /**
     * 麦位数据
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.VoiceMicInfoModel copy(int micIndex, @org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.model.VoiceMemberModel member, int micStatus, int userStatus, boolean ownerTag, int audioVolumeType) {
        return null;
    }
    
    /**
     * 麦位数据
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 麦位数据
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 麦位数据
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public VoiceMicInfoModel() {
        super();
    }
    
    public VoiceMicInfoModel(int micIndex, @org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.model.VoiceMemberModel member, int micStatus, int userStatus, boolean ownerTag, int audioVolumeType) {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    public final int getMicIndex() {
        return 0;
    }
    
    public final void setMicIndex(int p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.model.VoiceMemberModel component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.model.VoiceMemberModel getMember() {
        return null;
    }
    
    public final void setMember(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.model.VoiceMemberModel p0) {
    }
    
    public final int component3() {
        return 0;
    }
    
    public final int getMicStatus() {
        return 0;
    }
    
    public final void setMicStatus(int p0) {
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int getUserStatus() {
        return 0;
    }
    
    public final void setUserStatus(int p0) {
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final boolean getOwnerTag() {
        return false;
    }
    
    public final void setOwnerTag(boolean p0) {
    }
    
    public final int component6() {
        return 0;
    }
    
    public final int getAudioVolumeType() {
        return 0;
    }
    
    public final void setAudioVolumeType(int p0) {
    }
}