package io.agora.scene.voice.model;

import java.lang.System;

/**
 * 房间初始化属性，不会更改
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\"\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001BU\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\t\u00a2\u0006\u0002\u0010\rJ\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0003H\u00c6\u0003J\t\u0010(\u001a\u00020\tH\u00c6\u0003J\t\u0010)\u001a\u00020\u000bH\u00c6\u0003J\t\u0010*\u001a\u00020\tH\u00c6\u0003JY\u0010+\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\tH\u00c6\u0001J\u0013\u0010,\u001a\u00020\u000b2\b\u0010-\u001a\u0004\u0018\u00010.H\u00d6\u0003J\t\u0010/\u001a\u00020\tH\u00d6\u0001J\t\u00100\u001a\u00020\u0003H\u00d6\u0001R\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u001a\u0010\u0005\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u000f\"\u0004\b\u0013\u0010\u0011R\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\u001a\u0010\u0007\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u000f\"\u0004\b\u0018\u0010\u0011R\u001a\u0010\u0006\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u000f\"\u0004\b\u001a\u0010\u0011R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u000f\"\u0004\b\u001c\u0010\u0011R\u001a\u0010\b\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001d\u0010\u001e\"\u0004\b\u001f\u0010 R\u001a\u0010\f\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\u001e\"\u0004\b\"\u0010 \u00a8\u00061"}, d2 = {"Lio/agora/scene/voice/model/RoomKitBean;", "Ljava/io/Serializable;", "roomId", "", "channelId", "chatroomId", "ownerId", "ownerChatUid", "roomType", "", "isOwner", "", "soundEffect", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZI)V", "getChannelId", "()Ljava/lang/String;", "setChannelId", "(Ljava/lang/String;)V", "getChatroomId", "setChatroomId", "()Z", "setOwner", "(Z)V", "getOwnerChatUid", "setOwnerChatUid", "getOwnerId", "setOwnerId", "getRoomId", "setRoomId", "getRoomType", "()I", "setRoomType", "(I)V", "getSoundEffect", "setSoundEffect", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "other", "", "hashCode", "toString", "voice_release"})
public final class RoomKitBean implements java.io.Serializable {
    @org.jetbrains.annotations.NotNull()
    private java.lang.String roomId;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String channelId;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String chatroomId;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String ownerId;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String ownerChatUid;
    private int roomType;
    private boolean isOwner;
    private int soundEffect;
    
    /**
     * 房间初始化属性，不会更改
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.RoomKitBean copy(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.lang.String channelId, @org.jetbrains.annotations.NotNull()
    java.lang.String chatroomId, @org.jetbrains.annotations.NotNull()
    java.lang.String ownerId, @org.jetbrains.annotations.NotNull()
    java.lang.String ownerChatUid, int roomType, boolean isOwner, int soundEffect) {
        return null;
    }
    
    /**
     * 房间初始化属性，不会更改
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 房间初始化属性，不会更改
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 房间初始化属性，不会更改
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public RoomKitBean() {
        super();
    }
    
    public RoomKitBean(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.lang.String channelId, @org.jetbrains.annotations.NotNull()
    java.lang.String chatroomId, @org.jetbrains.annotations.NotNull()
    java.lang.String ownerId, @org.jetbrains.annotations.NotNull()
    java.lang.String ownerChatUid, int roomType, boolean isOwner, int soundEffect) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoomId() {
        return null;
    }
    
    public final void setRoomId(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getChannelId() {
        return null;
    }
    
    public final void setChannelId(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getChatroomId() {
        return null;
    }
    
    public final void setChatroomId(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getOwnerId() {
        return null;
    }
    
    public final void setOwnerId(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getOwnerChatUid() {
        return null;
    }
    
    public final void setOwnerChatUid(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final int component6() {
        return 0;
    }
    
    public final int getRoomType() {
        return 0;
    }
    
    public final void setRoomType(int p0) {
    }
    
    public final boolean component7() {
        return false;
    }
    
    public final boolean isOwner() {
        return false;
    }
    
    public final void setOwner(boolean p0) {
    }
    
    public final int component8() {
        return 0;
    }
    
    public final int getSoundEffect() {
        return 0;
    }
    
    public final void setSoundEffect(int p0) {
    }
}