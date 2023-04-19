package io.agora.scene.voice.spatial.model;

import java.lang.System;

/**
 * 麦位数据
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b(\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001Ba\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\f\u0012\b\b\u0002\u0010\u000e\u001a\u00020\t\u00a2\u0006\u0002\u0010\u000fJ\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010*\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010+\u001a\u00020\u0003H\u00c6\u0003J\t\u0010,\u001a\u00020\u0003H\u00c6\u0003J\t\u0010-\u001a\u00020\tH\u00c6\u0003J\t\u0010.\u001a\u00020\u0003H\u00c6\u0003J\t\u0010/\u001a\u00020\fH\u00c6\u0003J\t\u00100\u001a\u00020\fH\u00c6\u0003J\t\u00101\u001a\u00020\tH\u00c6\u0003Je\u00102\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\f2\b\b\u0002\u0010\u000e\u001a\u00020\tH\u00c6\u0001J\u0013\u00103\u001a\u00020\t2\b\u00104\u001a\u0004\u0018\u000105H\u00d6\u0003J\t\u00106\u001a\u00020\u0003H\u00d6\u0001J\t\u00107\u001a\u000208H\u00d6\u0001R\u001a\u0010\n\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\u001a\u0010\r\u001a\u00020\fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u0015\"\u0004\b\u0016\u0010\u0017R\u001a\u0010\u000e\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u0018\"\u0004\b\u0019\u0010\u001aR\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u001e\u0010\u0002\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u0011\"\u0004\b \u0010\u0013R\u001e\u0010\u0006\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\u0011\"\u0004\b\"\u0010\u0013R\u001a\u0010\b\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b#\u0010\u0018\"\u0004\b$\u0010\u001aR\u001a\u0010\u000b\u001a\u00020\fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b%\u0010\u0015\"\u0004\b&\u0010\u0017R\u001a\u0010\u0007\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\'\u0010\u0011\"\u0004\b(\u0010\u0013\u00a8\u00069"}, d2 = {"Lio/agora/scene/voice/spatial/model/VoiceMicInfoModel;", "Lio/agora/scene/voice/spatial/model/BaseRoomBean;", "micIndex", "", "member", "Lio/agora/scene/voice/spatial/model/VoiceMemberModel;", "micStatus", "userStatus", "ownerTag", "", "audioVolumeType", "position", "Landroid/graphics/PointF;", "forward", "isSpatialSet", "(ILio/agora/scene/voice/spatial/model/VoiceMemberModel;IIZILandroid/graphics/PointF;Landroid/graphics/PointF;Z)V", "getAudioVolumeType", "()I", "setAudioVolumeType", "(I)V", "getForward", "()Landroid/graphics/PointF;", "setForward", "(Landroid/graphics/PointF;)V", "()Z", "setSpatialSet", "(Z)V", "getMember", "()Lio/agora/scene/voice/spatial/model/VoiceMemberModel;", "setMember", "(Lio/agora/scene/voice/spatial/model/VoiceMemberModel;)V", "getMicIndex", "setMicIndex", "getMicStatus", "setMicStatus", "getOwnerTag", "setOwnerTag", "getPosition", "setPosition", "getUserStatus", "setUserStatus", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "", "hashCode", "toString", "", "voice_spatial_debug"})
public final class VoiceMicInfoModel implements io.agora.scene.voice.spatial.model.BaseRoomBean {
    @com.google.gson.annotations.SerializedName(value = "mic_index")
    private int micIndex;
    @org.jetbrains.annotations.Nullable()
    private io.agora.scene.voice.spatial.model.VoiceMemberModel member;
    @com.google.gson.annotations.SerializedName(value = "status")
    private int micStatus;
    @kotlin.jvm.Transient()
    private transient int userStatus;
    @kotlin.jvm.Transient()
    private transient boolean ownerTag;
    @kotlin.jvm.Transient()
    private transient int audioVolumeType;
    @org.jetbrains.annotations.NotNull()
    @kotlin.jvm.Transient()
    private transient android.graphics.PointF position;
    @org.jetbrains.annotations.NotNull()
    @kotlin.jvm.Transient()
    private transient android.graphics.PointF forward;
    @kotlin.jvm.Transient()
    private transient boolean isSpatialSet;
    
    /**
     * 麦位数据
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.model.VoiceMicInfoModel copy(int micIndex, @org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.model.VoiceMemberModel member, int micStatus, int userStatus, boolean ownerTag, int audioVolumeType, @org.jetbrains.annotations.NotNull()
    android.graphics.PointF position, @org.jetbrains.annotations.NotNull()
    android.graphics.PointF forward, boolean isSpatialSet) {
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
    io.agora.scene.voice.spatial.model.VoiceMemberModel member, int micStatus, int userStatus, boolean ownerTag, int audioVolumeType, @org.jetbrains.annotations.NotNull()
    android.graphics.PointF position, @org.jetbrains.annotations.NotNull()
    android.graphics.PointF forward, boolean isSpatialSet) {
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
    public final io.agora.scene.voice.spatial.model.VoiceMemberModel component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.spatial.model.VoiceMemberModel getMember() {
        return null;
    }
    
    public final void setMember(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.model.VoiceMemberModel p0) {
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
    
    @org.jetbrains.annotations.NotNull()
    public final android.graphics.PointF component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.graphics.PointF getPosition() {
        return null;
    }
    
    public final void setPosition(@org.jetbrains.annotations.NotNull()
    android.graphics.PointF p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.graphics.PointF component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.graphics.PointF getForward() {
        return null;
    }
    
    public final void setForward(@org.jetbrains.annotations.NotNull()
    android.graphics.PointF p0) {
    }
    
    public final boolean component9() {
        return false;
    }
    
    public final boolean isSpatialSet() {
        return false;
    }
    
    public final void setSpatialSet(boolean p0) {
    }
}