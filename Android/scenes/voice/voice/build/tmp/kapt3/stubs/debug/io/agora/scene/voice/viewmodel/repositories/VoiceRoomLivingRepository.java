package io.agora.scene.voice.viewmodel.repositories;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006J\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u00062\u0006\u0010\u000b\u001a\u00020\fJ.\u0010\r\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\b0\u000e0\u00070\u00062\u0006\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u000fJ\u001a\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u00062\u0006\u0010\u0013\u001a\u00020\nJ\u001a\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00070\u00062\u0006\u0010\u0016\u001a\u00020\u0017J\u001a\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\u0019\u001a\u00020\u000fJ\u001a\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\u0019\u001a\u00020\u000fJ\u001a\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\u0019\u001a\u00020\u000fJ\"\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u00062\u0006\u0010\u001d\u001a\u00020\f2\u0006\u0010\u001e\u001a\u00020\nJ\u001a\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\u0019\u001a\u00020\u000fJ\u001a\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\u0019\u001a\u00020\u000fJ\u0012\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u0006J!\u0010\"\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u00062\b\u0010\u0019\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\u0002\u0010#J\u001a\u0010$\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\u0019\u001a\u00020\u000fJ\u001a\u0010%\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\u0019\u001a\u00020\u000fJ\u001a\u0010&\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\u0019\u001a\u00020\u000fJ&\u0010\'\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\n0(0\u00070\u00062\u0006\u0010)\u001a\u00020\fJ&\u0010*\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\n0(0\u00070\u00062\u0006\u0010+\u001a\u00020\u000fJ\u0012\u0010,\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u0006R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"Lio/agora/scene/voice/viewmodel/repositories/VoiceRoomLivingRepository;", "Lio/agora/scene/voice/viewmodel/repositories/BaseRepository;", "()V", "voiceServiceProtocol", "Lio/agora/scene/voice/service/VoiceServiceProtocol;", "acceptMicSeatInvitation", "Landroidx/lifecycle/LiveData;", "Lio/agora/voice/common/net/Resource;", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "cancelMicSeatApply", "", "chatUid", "", "changeMic", "", "", "oldIndex", "newIndex", "enableRobot", "useRobot", "fetchRoomDetail", "Lio/agora/scene/voice/model/VoiceRoomInfo;", "voiceRoomModel", "Lio/agora/scene/voice/model/VoiceRoomModel;", "forbidMic", "micIndex", "kickOff", "leaveMic", "leaveSyncManagerRoom", "roomId", "isRoomOwnerLeave", "lockMic", "muteLocal", "refuseInvite", "startMicSeatApply", "(Ljava/lang/Integer;)Landroidx/lifecycle/LiveData;", "unForbidMic", "unLockMic", "unMuteLocal", "updateAnnouncement", "Lkotlin/Pair;", "content", "updateRobotVolume", "value", "updateRoomMember", "voice_debug"})
public final class VoiceRoomLivingRepository extends io.agora.scene.voice.viewmodel.repositories.BaseRepository {
    
    /**
     * voice chat protocol
     */
    private final io.agora.scene.voice.service.VoiceServiceProtocol voiceServiceProtocol = null;
    
    public VoiceRoomLivingRepository() {
        super();
    }
    
    /**
     * 获取详情
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceRoomInfo>> fetchRoomDetail(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceRoomModel voiceRoomModel) {
        return null;
    }
    
    /**
     * 开启/关闭机器人
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> enableRobot(boolean useRobot) {
        return null;
    }
    
    /**
     * 更新房间公告
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.String, java.lang.Boolean>>> updateAnnouncement(@org.jetbrains.annotations.NotNull()
    java.lang.String content) {
        return null;
    }
    
    /**
     * 更新机器人音量
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.Integer, java.lang.Boolean>>> updateRobotVolume(int value) {
        return null;
    }
    
    /**
     * 提交上麦申请
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> startMicSeatApply(@org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex) {
        return null;
    }
    
    /**
     * 撤销上麦申请
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> cancelMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid) {
        return null;
    }
    
    /**
     * 本地禁麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> muteLocal(int micIndex) {
        return null;
    }
    
    /**
     * 取消本地禁麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> unMuteLocal(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> leaveMic(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> forbidMic(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> unForbidMic(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> kickOff(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> refuseInvite() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> lockMic(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> unLockMic(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel>>> changeMic(int oldIndex, int newIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> acceptMicSeatInvitation() {
        return null;
    }
    
    /**
     * 离开syncManager 房间
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> leaveSyncManagerRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, boolean isRoomOwnerLeave) {
        return null;
    }
    
    /**
     * 更新成员列表
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> updateRoomMember() {
        return null;
    }
}