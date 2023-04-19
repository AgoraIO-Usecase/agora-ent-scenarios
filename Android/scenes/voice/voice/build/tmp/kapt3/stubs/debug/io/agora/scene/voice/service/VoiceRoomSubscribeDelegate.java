package io.agora.scene.voice.service;

import java.lang.System;

/**
 * @author create by zhangwei03
 *
 * im kv 回调协议
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0005H\u0016J\u001a\u0010\u0007\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0016J\u0010\u0010\n\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u0016J\u001a\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\u00052\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0016J\u0010\u0010\r\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u0016J\u0010\u0010\u000e\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\u0005H\u0016J\u001a\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0016J\u0010\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0016J,\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u00132\u0006\u0010\u0014\u001a\u00020\u0005H\u0016J\u0018\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0016\u001a\u00020\u0017H\u0016J\u0018\u0010\u0018\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0019\u001a\u00020\u001aH\u0016J\u0018\u0010\u001b\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\u0005H\u0016\u00a8\u0006\u001c"}, d2 = {"Lio/agora/scene/voice/service/VoiceRoomSubscribeDelegate;", "", "onAnnouncementChanged", "", "roomId", "", "content", "onReceiveGift", "message", "Lio/agora/scene/voice/imkit/bean/ChatMessageData;", "onReceiveSeatInvitation", "onReceiveSeatInvitationRejected", "chatUid", "onReceiveSeatRequest", "onReceiveSeatRequestRejected", "onReceiveTextMsg", "onRoomDestroyed", "onSeatUpdated", "attributeMap", "", "fromId", "onUserBeKicked", "reason", "Lio/agora/scene/voice/service/VoiceRoomServiceKickedReason;", "onUserJoinedRoom", "voiceMember", "Lio/agora/scene/voice/model/VoiceMemberModel;", "onUserLeftRoom", "voice_debug"})
public abstract interface VoiceRoomSubscribeDelegate {
    
    /**
     * 收到礼物消息
     * @param roomId 环信IMSDK聊天室id
     * @param message
     */
    public abstract void onReceiveGift(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.imkit.bean.ChatMessageData message);
    
    /**
     * 接收到普通消息
     * @param message
     */
    public abstract void onReceiveTextMsg(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.imkit.bean.ChatMessageData message);
    
    /**
     * 收到上麦申请消息
     * @param message 消息对象
     */
    public abstract void onReceiveSeatRequest(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.imkit.bean.ChatMessageData message);
    
    /**
     * 收到取消上麦申请消息
     * @param chatUid 环信IM SDK 用户id
     */
    public abstract void onReceiveSeatRequestRejected(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid);
    
    /**
     * 接收邀请消息
     * @param message IM消息对象
     */
    public abstract void onReceiveSeatInvitation(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.imkit.bean.ChatMessageData message);
    
    /**
     * 接收拒绝邀请消息
     * @param chatUid
     */
    public abstract void onReceiveSeatInvitationRejected(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.imkit.bean.ChatMessageData message);
    
    /**
     * 聊天室公告更新
     * @param roomId 环信IM SDK聊天室id
     * @param content 公告变化内容
     */
    public abstract void onAnnouncementChanged(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.lang.String content);
    
    /**
     * 用户加入聊天室回调，带所有用户信息
     * @param roomId 环信IM SDK聊天室id
     * @param voiceMember 用户数据
     */
    public abstract void onUserJoinedRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceMemberModel voiceMember);
    
    /**
     * 用户离开房间
     * @param roomId 环信IM SDK聊天室id
     * @param chatUid 离开的环信用户id
     */
    public abstract void onUserLeftRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.lang.String chatUid);
    
    /**
     * 聊天室成员被踢出房间
     * @param roomId 环信IM SDK聊天室id
     * @param reason 被踢出房间
     */
    public abstract void onUserBeKicked(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.service.VoiceRoomServiceKickedReason reason);
    
    /**
     * 房间销毁
     */
    public abstract void onRoomDestroyed(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId);
    
    /**
     * 聊天室自定义麦位属性发生变化
     * @param roomId 环信IM SDK聊天室id
     * @param attributeMap 变换的属性kv
     * @param fromId 谁操作发生的变化
     */
    public abstract void onSeatUpdated(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> attributeMap, @org.jetbrains.annotations.NotNull()
    java.lang.String fromId);
    
    /**
     * @author create by zhangwei03
     *
     * im kv 回调协议
     */
    @kotlin.Metadata(mv = {1, 6, 0}, k = 3)
    public final class DefaultImpls {
        
        /**
         * 收到礼物消息
         * @param roomId 环信IMSDK聊天室id
         * @param message
         */
        public static void onReceiveGift(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        java.lang.String roomId, @org.jetbrains.annotations.Nullable()
        io.agora.scene.voice.imkit.bean.ChatMessageData message) {
        }
        
        /**
         * 接收到普通消息
         * @param message
         */
        public static void onReceiveTextMsg(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        java.lang.String roomId, @org.jetbrains.annotations.Nullable()
        io.agora.scene.voice.imkit.bean.ChatMessageData message) {
        }
        
        /**
         * 收到上麦申请消息
         * @param message 消息对象
         */
        public static void onReceiveSeatRequest(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.imkit.bean.ChatMessageData message) {
        }
        
        /**
         * 收到取消上麦申请消息
         * @param chatUid 环信IM SDK 用户id
         */
        public static void onReceiveSeatRequestRejected(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        java.lang.String chatUid) {
        }
        
        /**
         * 接收邀请消息
         * @param message IM消息对象
         */
        public static void onReceiveSeatInvitation(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.imkit.bean.ChatMessageData message) {
        }
        
        /**
         * 接收拒绝邀请消息
         * @param chatUid
         */
        public static void onReceiveSeatInvitationRejected(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
        io.agora.scene.voice.imkit.bean.ChatMessageData message) {
        }
        
        /**
         * 聊天室公告更新
         * @param roomId 环信IM SDK聊天室id
         * @param content 公告变化内容
         */
        public static void onAnnouncementChanged(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        java.lang.String roomId, @org.jetbrains.annotations.NotNull()
        java.lang.String content) {
        }
        
        /**
         * 用户加入聊天室回调，带所有用户信息
         * @param roomId 环信IM SDK聊天室id
         * @param voiceMember 用户数据
         */
        public static void onUserJoinedRoom(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        java.lang.String roomId, @org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.model.VoiceMemberModel voiceMember) {
        }
        
        /**
         * 用户离开房间
         * @param roomId 环信IM SDK聊天室id
         * @param chatUid 离开的环信用户id
         */
        public static void onUserLeftRoom(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        java.lang.String roomId, @org.jetbrains.annotations.NotNull()
        java.lang.String chatUid) {
        }
        
        /**
         * 聊天室成员被踢出房间
         * @param roomId 环信IM SDK聊天室id
         * @param reason 被踢出房间
         */
        public static void onUserBeKicked(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        java.lang.String roomId, @org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomServiceKickedReason reason) {
        }
        
        /**
         * 房间销毁
         */
        public static void onRoomDestroyed(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        java.lang.String roomId) {
        }
        
        /**
         * 聊天室自定义麦位属性发生变化
         * @param roomId 环信IM SDK聊天室id
         * @param attributeMap 变换的属性kv
         * @param fromId 谁操作发生的变化
         */
        public static void onSeatUpdated(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.service.VoiceRoomSubscribeDelegate $this, @org.jetbrains.annotations.NotNull()
        java.lang.String roomId, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.String, java.lang.String> attributeMap, @org.jetbrains.annotations.NotNull()
        java.lang.String fromId) {
        }
    }
}