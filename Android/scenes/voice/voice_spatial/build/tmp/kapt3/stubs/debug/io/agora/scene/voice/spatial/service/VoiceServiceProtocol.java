package io.agora.scene.voice.spatial.service;

import java.lang.System;

/**
 * @author create by zhangwei03
 *
 * voice chat room protocol define
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0013\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u0000 C2\u00020\u0001:\u0001CJJ\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u000528\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\f\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JB\u0010\u000e\u001a\u00020\u000328\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\f\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JH\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u000526\u0010\u0006\u001a2\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\u0010\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&J^\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\b2D\u0010\u0006\u001a@\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012!\u0012\u001f\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\f\u0018\u00010\u0014\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JH\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u001726\u0010\u0006\u001a2\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\u0018\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JF\u0010\u0019\u001a\u00020\u00032<\u0010\u0006\u001a8\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020\u001b0\u001a\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JJ\u0010\u001c\u001a\u00020\u00032\u0006\u0010\u001d\u001a\u00020\u001828\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\u001e\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JP\u0010\u001f\u001a\u00020\u00032\b\b\u0002\u0010 \u001a\u00020\b2<\u0010\u0006\u001a8\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020\u00180\u001a\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JF\u0010!\u001a\u00020\u00032<\u0010\u0006\u001a8\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020\u001b0\u001a\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JJ\u0010\"\u001a\u00020\u00032\u0006\u0010#\u001a\u00020\b28\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\f\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&J\u000e\u0010$\u001a\b\u0012\u0004\u0012\u00020&0%H&JJ\u0010\'\u001a\u00020\u00032\u0006\u0010(\u001a\u00020\u000528\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\u0018\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JJ\u0010)\u001a\u00020\u00032\u0006\u0010#\u001a\u00020\b28\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\f\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JJ\u0010*\u001a\u00020\u00032\u0006\u0010#\u001a\u00020\b28\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\f\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JP\u0010+\u001a\u00020\u00032\u0006\u0010(\u001a\u00020\u00052\u0006\u0010,\u001a\u00020\u001026\u0010\u0006\u001a2\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\u0010\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JJ\u0010-\u001a\u00020\u00032\u0006\u0010#\u001a\u00020\b28\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\f\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JJ\u0010.\u001a\u00020\u00032\u0006\u0010/\u001a\u00020\u001028\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\u001b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&J@\u00100\u001a\u00020\u000326\u0010\u0006\u001a2\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\u0010\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&J\b\u00101\u001a\u00020\u0003H&JQ\u00102\u001a\u00020\u00032\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\b26\u0010\u0006\u001a2\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\u0010\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&\u00a2\u0006\u0002\u00103JW\u00104\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\b\u0010#\u001a\u0004\u0018\u00010\b26\u0010\u0006\u001a2\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\u0010\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&\u00a2\u0006\u0002\u00105J\u0010\u00106\u001a\u00020\u00032\u0006\u00107\u001a\u00020&H&J\u0016\u00108\u001a\u00020\u00032\f\u00109\u001a\b\u0012\u0004\u0012\u00020\u00030:H&JJ\u0010;\u001a\u00020\u00032\u0006\u0010#\u001a\u00020\b28\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\f\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JJ\u0010<\u001a\u00020\u00032\u0006\u0010#\u001a\u00020\b28\u0010\u0006\u001a4\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0015\u0012\u0013\u0018\u00010\f\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&J\b\u0010=\u001a\u00020\u0003H&JH\u0010>\u001a\u00020\u00032\u0006\u0010?\u001a\u00020\u000526\u0010\u0006\u001a2\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\u0010\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&JH\u0010@\u001a\u00020\u00032\u0006\u0010A\u001a\u00020B26\u0010\u0006\u001a2\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u000b\u0012\u0013\u0012\u00110\u0010\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u00030\u0007H&\u00a8\u0006D"}, d2 = {"Lio/agora/scene/voice/spatial/service/VoiceServiceProtocol;", "", "acceptMicSeatApply", "", "userId", "", "completion", "Lkotlin/Function2;", "", "Lkotlin/ParameterName;", "name", "error", "Lio/agora/scene/voice/spatial/model/VoiceMicInfoModel;", "result", "acceptMicSeatInvitation", "cancelMicSeatApply", "", "changeMic", "oldIndex", "newIndex", "", "createRoom", "inputModel", "Lio/agora/scene/voice/spatial/model/VoiceCreateRoomModel;", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "fetchApplicantsList", "", "Lio/agora/scene/voice/spatial/model/VoiceMemberModel;", "fetchRoomDetail", "voiceRoomModel", "Lio/agora/scene/voice/spatial/model/VoiceRoomInfo;", "fetchRoomList", "page", "fetchRoomMembers", "forbidMic", "micIndex", "getSubscribeDelegates", "", "Lio/agora/scene/voice/spatial/service/VoiceRoomSubscribeDelegate;", "joinRoom", "roomId", "kickOff", "leaveMic", "leaveRoom", "isRoomOwnerLeave", "lockMic", "muteLocal", "mute", "refuseInvite", "reset", "startMicSeatApply", "(Ljava/lang/Integer;Lkotlin/jvm/functions/Function2;)V", "startMicSeatInvitation", "(Ljava/lang/String;Ljava/lang/Integer;Lkotlin/jvm/functions/Function2;)V", "subscribeEvent", "delegate", "subscribeRoomTimeUp", "onRoomTimeUp", "Lkotlin/Function0;", "unForbidMic", "unLockMic", "unsubscribeEvent", "updateAnnouncement", "content", "updateRobotInfo", "info", "Lio/agora/scene/voice/spatial/model/RobotSpatialAudioModel;", "Companion", "voice_spatial_debug"})
public abstract interface VoiceServiceProtocol {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.service.VoiceServiceProtocol.Companion Companion = null;
    public static final int ERR_OK = 0;
    public static final int ERR_FAILED = 1;
    public static final int ERR_LOGIN_ERROR = 2;
    public static final int ERR_LOGIN_SUCCESS = 3;
    public static final int ERR_ROOM_UNAVAILABLE = 4;
    public static final int ERR_ROOM_NAME_INCORRECT = 5;
    public static final int ERR_ROOM_LIST_EMPTY = 1003;
    
    @org.jetbrains.annotations.NotNull()
    @kotlin.jvm.JvmStatic()
    public static io.agora.scene.voice.spatial.service.VoiceServiceProtocol getImplInstance() {
        return null;
    }
    
    /**
     * 注册订阅
     * @param delegate 聊天室内IM回调处理
     */
    public abstract void subscribeEvent(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.service.VoiceRoomSubscribeDelegate delegate);
    
    /**
     * 取消订阅
     */
    public abstract void unsubscribeEvent();
    
    public abstract void reset();
    
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<io.agora.scene.voice.spatial.service.VoiceRoomSubscribeDelegate> getSubscribeDelegates();
    
    /**
     * 获取房间列表
     * @param page 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
     */
    public abstract void fetchRoomList(int page, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.spatial.model.VoiceRoomModel>, kotlin.Unit> completion);
    
    /**
     * 创建房间
     * @param inputModel 输入的房间信息
     */
    public abstract void createRoom(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceCreateRoomModel inputModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceRoomModel, kotlin.Unit> completion);
    
    /**
     * 加入房间
     * @param roomId 房间id
     */
    public abstract void joinRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceRoomModel, kotlin.Unit> completion);
    
    /**
     * 离开房间
     * @param roomId 房间id
     */
    public abstract void leaveRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, boolean isRoomOwnerLeave, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion);
    
    /**
     * 获取房间详情
     * @param voiceRoomModel 房间概要
     */
    public abstract void fetchRoomDetail(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceRoomInfo, kotlin.Unit> completion);
    
    /**
     * 获取用户列表
     */
    public abstract void fetchRoomMembers(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel>, kotlin.Unit> completion);
    
    /**
     * 申请列表
     */
    public abstract void fetchApplicantsList(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel>, kotlin.Unit> completion);
    
    /**
     * 申请上麦
     * @param micIndex 麦位index
     */
    public abstract void startMicSeatApply(@org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion);
    
    /**
     * 同意申请
     * @param userId 用户id
     */
    public abstract void acceptMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion);
    
    /**
     * 取消上麦
     * @param chatUid im uid
     */
    public abstract void cancelMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion);
    
    /**
     * 邀请用户上麦
     * @param chatUid im uid
     */
    public abstract void startMicSeatInvitation(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion);
    
    /**
     * 接受邀请
     */
    public abstract void acceptMicSeatInvitation(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion);
    
    /**
     * 拒绝邀请
     */
    public abstract void refuseInvite(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion);
    
    /**
     * mute
     * @param mute
     */
    public abstract void muteLocal(boolean mute, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMemberModel, kotlin.Unit> completion);
    
    /**
     * 禁言指定麦位置
     * @param micIndex 麦位index
     */
    public abstract void forbidMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion);
    
    /**
     * 取消禁言指定麦位置
     * @param micIndex 麦位index
     */
    public abstract void unForbidMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion);
    
    /**
     * 锁麦
     * @param micIndex 麦位index
     */
    public abstract void lockMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion);
    
    /**
     * 取消锁麦
     * @param micIndex 麦位index
     */
    public abstract void unLockMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion);
    
    /**
     * 踢用户下麦
     * @param micIndex 麦位index
     */
    public abstract void kickOff(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion);
    
    /**
     * 下麦
     * @param micIndex 麦位index
     */
    public abstract void leaveMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion);
    
    /**
     * 换麦
     * @param oldIndex 老麦位index
     * @param newIndex 新麦位index
     */
    public abstract void changeMic(int oldIndex, int newIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel>, kotlin.Unit> completion);
    
    /**
     * 更新公告
     * @param content 公告内容
     */
    public abstract void updateAnnouncement(@org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion);
    
    /**
     * 更新机器人配置
     * @param info 机器人配置
     */
    public abstract void updateRobotInfo(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.RobotSpatialAudioModel info, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion);
    
    public abstract void subscribeRoomTimeUp(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRoomTimeUp);
    
    /**
     * @author create by zhangwei03
     *
     * voice chat room protocol define
     */
    @kotlin.Metadata(mv = {1, 6, 0}, k = 3)
    public final class DefaultImpls {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0011\u001a\u00020\u0012H\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u000b\u001a\u00020\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000f\u0010\u0010\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0013"}, d2 = {"Lio/agora/scene/voice/spatial/service/VoiceServiceProtocol$Companion;", "", "()V", "ERR_FAILED", "", "ERR_LOGIN_ERROR", "ERR_LOGIN_SUCCESS", "ERR_OK", "ERR_ROOM_LIST_EMPTY", "ERR_ROOM_NAME_INCORRECT", "ERR_ROOM_UNAVAILABLE", "instance", "Lio/agora/scene/voice/spatial/service/VoiceSyncManagerServiceImp;", "getInstance", "()Lio/agora/scene/voice/spatial/service/VoiceSyncManagerServiceImp;", "instance$delegate", "Lkotlin/Lazy;", "getImplInstance", "Lio/agora/scene/voice/spatial/service/VoiceServiceProtocol;", "voice_spatial_debug"})
    public static final class Companion {
        public static final int ERR_OK = 0;
        public static final int ERR_FAILED = 1;
        public static final int ERR_LOGIN_ERROR = 2;
        public static final int ERR_LOGIN_SUCCESS = 3;
        public static final int ERR_ROOM_UNAVAILABLE = 4;
        public static final int ERR_ROOM_NAME_INCORRECT = 5;
        public static final int ERR_ROOM_LIST_EMPTY = 1003;
        private static final kotlin.Lazy instance$delegate = null;
        
        private Companion() {
            super();
        }
        
        private final io.agora.scene.voice.spatial.service.VoiceSyncManagerServiceImp getInstance() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        @kotlin.jvm.JvmStatic()
        public final io.agora.scene.voice.spatial.service.VoiceServiceProtocol getImplInstance() {
            return null;
        }
    }
}