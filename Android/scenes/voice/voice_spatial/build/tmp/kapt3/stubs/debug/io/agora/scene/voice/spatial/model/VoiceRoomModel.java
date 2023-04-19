package io.agora.scene.voice.spatial.model;

import java.lang.System;

/**
 * 房间数据
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b?\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001B\u00ad\u0001\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\t\u0012\b\b\u0002\u0010\u000b\u001a\u00020\t\u0012\b\b\u0002\u0010\f\u001a\u00020\u0005\u0012\b\b\u0002\u0010\r\u001a\u00020\t\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0005\u0012\u0010\b\u0002\u0010\u0012\u001a\n\u0012\u0004\u0012\u00020\u0014\u0018\u00010\u0013\u0012\u0010\b\u0002\u0010\u0015\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u0013\u0012\b\b\u0002\u0010\u0016\u001a\u00020\t\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0018J\u000b\u0010B\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010C\u001a\u00020\u0010H\u00c6\u0003J\t\u0010D\u001a\u00020\u0005H\u00c6\u0003J\u0011\u0010E\u001a\n\u0012\u0004\u0012\u00020\u0014\u0018\u00010\u0013H\u00c6\u0003J\u0011\u0010F\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u0013H\u00c6\u0003J\t\u0010G\u001a\u00020\tH\u00c6\u0003J\t\u0010H\u001a\u00020\u0005H\u00c6\u0003J\t\u0010I\u001a\u00020\u0005H\u00c6\u0003J\t\u0010J\u001a\u00020\u0007H\u00c6\u0003J\t\u0010K\u001a\u00020\tH\u00c6\u0003J\t\u0010L\u001a\u00020\tH\u00c6\u0003J\t\u0010M\u001a\u00020\tH\u00c6\u0003J\t\u0010N\u001a\u00020\u0005H\u00c6\u0003J\t\u0010O\u001a\u00020\tH\u00c6\u0003J\t\u0010P\u001a\u00020\u0005H\u00c6\u0003J\u00b1\u0001\u0010Q\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\t2\b\b\u0002\u0010\f\u001a\u00020\u00052\b\b\u0002\u0010\r\u001a\u00020\t2\b\b\u0002\u0010\u000e\u001a\u00020\u00052\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00052\u0010\b\u0002\u0010\u0012\u001a\n\u0012\u0004\u0012\u00020\u0014\u0018\u00010\u00132\u0010\b\u0002\u0010\u0015\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u00132\b\b\u0002\u0010\u0016\u001a\u00020\t2\b\b\u0002\u0010\u0017\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010R\u001a\u00020\u00072\b\u0010S\u001a\u0004\u0018\u00010TH\u00d6\u0003J\t\u0010U\u001a\u00020\tH\u00d6\u0001J\t\u0010V\u001a\u00020\u0005H\u00d6\u0001R\u001e\u0010\u0017\u001a\u00020\u00058\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR\u001e\u0010\u000e\u001a\u00020\u00058\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001d\u0010\u001a\"\u0004\b\u001e\u0010\u001cR\u001e\u0010\n\u001a\u00020\t8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u001e\u0010\u000f\u001a\u00020\u00108\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b#\u0010$\"\u0004\b%\u0010&R\u001a\u0010\u0016\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\'\u0010 \"\u0004\b(\u0010\"R\u001e\u0010\u0006\u001a\u00020\u00078\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010)\"\u0004\b*\u0010+R\u001e\u0010\b\u001a\u00020\t8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b,\u0010 \"\u0004\b-\u0010\"R\"\u0010\u0015\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u0013X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b.\u0010/\"\u0004\b0\u00101R\u001c\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b2\u00103\"\u0004\b4\u00105R\"\u0010\u0012\u001a\n\u0012\u0004\u0012\u00020\u0014\u0018\u00010\u0013X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b6\u0010/\"\u0004\b7\u00101R\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b8\u0010\u001a\"\u0004\b9\u0010\u001cR\u001e\u0010\f\u001a\u00020\u00058\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b:\u0010\u001a\"\u0004\b;\u0010\u001cR\u001e\u0010\u0011\u001a\u00020\u00058\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b<\u0010\u001a\"\u0004\b=\u0010\u001cR\u001e\u0010\u000b\u001a\u00020\t8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b>\u0010 \"\u0004\b?\u0010\"R\u001e\u0010\r\u001a\u00020\t8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b@\u0010 \"\u0004\bA\u0010\"\u00a8\u0006W"}, d2 = {"Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "Lio/agora/scene/voice/spatial/model/BaseRoomBean;", "owner", "Lio/agora/scene/voice/spatial/model/VoiceMemberModel;", "roomId", "", "isPrivate", "", "memberCount", "", "clickCount", "roomType", "roomName", "soundEffect", "channelId", "createdAt", "", "roomPassword", "rankingList", "", "Lio/agora/scene/voice/spatial/model/VoiceRankUserModel;", "memberList", "giftAmount", "announcement", "(Lio/agora/scene/voice/spatial/model/VoiceMemberModel;Ljava/lang/String;ZIIILjava/lang/String;ILjava/lang/String;JLjava/lang/String;Ljava/util/List;Ljava/util/List;ILjava/lang/String;)V", "getAnnouncement", "()Ljava/lang/String;", "setAnnouncement", "(Ljava/lang/String;)V", "getChannelId", "setChannelId", "getClickCount", "()I", "setClickCount", "(I)V", "getCreatedAt", "()J", "setCreatedAt", "(J)V", "getGiftAmount", "setGiftAmount", "()Z", "setPrivate", "(Z)V", "getMemberCount", "setMemberCount", "getMemberList", "()Ljava/util/List;", "setMemberList", "(Ljava/util/List;)V", "getOwner", "()Lio/agora/scene/voice/spatial/model/VoiceMemberModel;", "setOwner", "(Lio/agora/scene/voice/spatial/model/VoiceMemberModel;)V", "getRankingList", "setRankingList", "getRoomId", "setRoomId", "getRoomName", "setRoomName", "getRoomPassword", "setRoomPassword", "getRoomType", "setRoomType", "getSoundEffect", "setSoundEffect", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "", "hashCode", "toString", "voice_spatial_debug"})
public final class VoiceRoomModel implements io.agora.scene.voice.spatial.model.BaseRoomBean {
    @org.jetbrains.annotations.Nullable()
    private io.agora.scene.voice.spatial.model.VoiceMemberModel owner;
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "room_id")
    private java.lang.String roomId;
    @com.google.gson.annotations.SerializedName(value = "is_private")
    private boolean isPrivate;
    @com.google.gson.annotations.SerializedName(value = "member_count")
    private int memberCount;
    @com.google.gson.annotations.SerializedName(value = "click_count")
    private int clickCount;
    @com.google.gson.annotations.SerializedName(value = "type")
    private int roomType;
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "name")
    private java.lang.String roomName;
    @com.google.gson.annotations.SerializedName(value = "sound_effect")
    private int soundEffect;
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "channel_id")
    private java.lang.String channelId;
    @com.google.gson.annotations.SerializedName(value = "created_at")
    private long createdAt;
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "roomPassword")
    private java.lang.String roomPassword;
    @org.jetbrains.annotations.Nullable()
    @kotlin.jvm.Transient()
    private transient java.util.List<io.agora.scene.voice.spatial.model.VoiceRankUserModel> rankingList;
    @org.jetbrains.annotations.Nullable()
    @kotlin.jvm.Transient()
    private transient java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel> memberList;
    @kotlin.jvm.Transient()
    private transient int giftAmount;
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "announcement")
    private java.lang.String announcement;
    
    /**
     * 房间数据
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.model.VoiceRoomModel copy(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.model.VoiceMemberModel owner, @org.jetbrains.annotations.NotNull()
    java.lang.String roomId, boolean isPrivate, int memberCount, int clickCount, int roomType, @org.jetbrains.annotations.NotNull()
    java.lang.String roomName, int soundEffect, @org.jetbrains.annotations.NotNull()
    java.lang.String channelId, long createdAt, @org.jetbrains.annotations.NotNull()
    java.lang.String roomPassword, @org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.spatial.model.VoiceRankUserModel> rankingList, @org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel> memberList, int giftAmount, @org.jetbrains.annotations.NotNull()
    java.lang.String announcement) {
        return null;
    }
    
    /**
     * 房间数据
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 房间数据
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 房间数据
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public VoiceRoomModel() {
        super();
    }
    
    public VoiceRoomModel(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.model.VoiceMemberModel owner, @org.jetbrains.annotations.NotNull()
    java.lang.String roomId, boolean isPrivate, int memberCount, int clickCount, int roomType, @org.jetbrains.annotations.NotNull()
    java.lang.String roomName, int soundEffect, @org.jetbrains.annotations.NotNull()
    java.lang.String channelId, long createdAt, @org.jetbrains.annotations.NotNull()
    java.lang.String roomPassword, @org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.spatial.model.VoiceRankUserModel> rankingList, @org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel> memberList, int giftAmount, @org.jetbrains.annotations.NotNull()
    java.lang.String announcement) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.spatial.model.VoiceMemberModel component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.spatial.model.VoiceMemberModel getOwner() {
        return null;
    }
    
    public final void setOwner(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.model.VoiceMemberModel p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoomId() {
        return null;
    }
    
    public final void setRoomId(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final boolean component3() {
        return false;
    }
    
    public final boolean isPrivate() {
        return false;
    }
    
    public final void setPrivate(boolean p0) {
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int getMemberCount() {
        return 0;
    }
    
    public final void setMemberCount(int p0) {
    }
    
    public final int component5() {
        return 0;
    }
    
    public final int getClickCount() {
        return 0;
    }
    
    public final void setClickCount(int p0) {
    }
    
    public final int component6() {
        return 0;
    }
    
    public final int getRoomType() {
        return 0;
    }
    
    public final void setRoomType(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoomName() {
        return null;
    }
    
    public final void setRoomName(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final int component8() {
        return 0;
    }
    
    public final int getSoundEffect() {
        return 0;
    }
    
    public final void setSoundEffect(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getChannelId() {
        return null;
    }
    
    public final void setChannelId(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final long component10() {
        return 0L;
    }
    
    public final long getCreatedAt() {
        return 0L;
    }
    
    public final void setCreatedAt(long p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoomPassword() {
        return null;
    }
    
    public final void setRoomPassword(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<io.agora.scene.voice.spatial.model.VoiceRankUserModel> component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<io.agora.scene.voice.spatial.model.VoiceRankUserModel> getRankingList() {
        return null;
    }
    
    public final void setRankingList(@org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.spatial.model.VoiceRankUserModel> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel> component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel> getMemberList() {
        return null;
    }
    
    public final void setMemberList(@org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel> p0) {
    }
    
    public final int component14() {
        return 0;
    }
    
    public final int getGiftAmount() {
        return 0;
    }
    
    public final void setGiftAmount(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAnnouncement() {
        return null;
    }
    
    public final void setAnnouncement(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
}