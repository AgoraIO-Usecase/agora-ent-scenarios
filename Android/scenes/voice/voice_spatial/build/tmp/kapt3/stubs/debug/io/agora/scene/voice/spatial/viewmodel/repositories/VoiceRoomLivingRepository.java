package io.agora.scene.voice.spatial.viewmodel.repositories;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0002\u0010\b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bJ\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\b2\u0006\u0010\r\u001a\u00020\u000eJ.\u0010\u000f\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\n0\u00100\t0\b2\u0006\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0011J\u001a\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\b2\u0006\u0010\u0015\u001a\u00020\fJ\u001a\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\b2\u0006\u0010\u0015\u001a\u00020\fJ\u001a\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\b2\u0006\u0010\u0015\u001a\u00020\fJ\u001a\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\b2\u0006\u0010\u0015\u001a\u00020\fJ\u001a\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\b2\u0006\u0010\u001a\u001a\u00020\fJ\u001a\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001c0\t0\b2\u0006\u0010\u001d\u001a\u00020\u001eJ\u001a\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010 \u001a\u00020\u0011J\u001a\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010 \u001a\u00020\u0011J\u001a\u0010\"\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010 \u001a\u00020\u0011J\"\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\b2\u0006\u0010$\u001a\u00020\u000e2\u0006\u0010%\u001a\u00020\fJ\u001a\u0010&\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010 \u001a\u00020\u0011J\u001a\u0010\'\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020(0\t0\b2\u0006\u0010)\u001a\u00020\fJ\u0012\u0010*\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\bJ!\u0010+\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\b2\b\u0010 \u001a\u0004\u0018\u00010\u0011\u00a2\u0006\u0002\u0010,J\u001a\u0010-\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010 \u001a\u00020\u0011J\u001a\u0010.\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010 \u001a\u00020\u0011J&\u0010/\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\f000\t0\b2\u0006\u00101\u001a\u00020\u000eJ&\u00102\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u000203\u0012\u0004\u0012\u00020\f000\t0\b2\u0006\u00104\u001a\u000203J&\u00105\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u000203\u0012\u0004\u0012\u00020\f000\t0\b2\u0006\u00104\u001a\u000203J&\u00106\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\f000\t0\b2\u0006\u00107\u001a\u00020\u0011R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00068"}, d2 = {"Lio/agora/scene/voice/spatial/viewmodel/repositories/VoiceRoomLivingRepository;", "Lio/agora/scene/voice/spatial/viewmodel/repositories/BaseRepository;", "()V", "voiceRobotInfo", "Lio/agora/scene/voice/spatial/model/RobotSpatialAudioModel;", "voiceServiceProtocol", "Lio/agora/scene/voice/spatial/service/VoiceServiceProtocol;", "acceptMicSeatInvitation", "Landroidx/lifecycle/LiveData;", "Lio/agora/voice/common/net/Resource;", "Lio/agora/scene/voice/spatial/model/VoiceMicInfoModel;", "cancelMicSeatApply", "", "userId", "", "changeMic", "", "", "oldIndex", "newIndex", "enableBlueRobotAirAbsorb", "active", "enableBlueRobotBlur", "enableRedRobotAirAbsorb", "enableRedRobotBlur", "enableRobot", "useRobot", "fetchRoomDetail", "Lio/agora/scene/voice/spatial/model/VoiceRoomInfo;", "voiceRoomModel", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "forbidMic", "micIndex", "kickOff", "leaveMic", "leaveSyncManagerRoom", "roomId", "isRoomOwnerLeave", "lockMic", "muteLocal", "Lio/agora/scene/voice/spatial/model/VoiceMemberModel;", "mute", "refuseInvite", "startMicSeatApply", "(Ljava/lang/Integer;)Landroidx/lifecycle/LiveData;", "unForbidMic", "unLockMic", "updateAnnouncement", "Lkotlin/Pair;", "content", "updateBlueRoBotAttenuation", "", "attenuation", "updateRedRoBotAttenuation", "updateRobotVolume", "value", "voice_spatial_debug"})
public final class VoiceRoomLivingRepository extends io.agora.scene.voice.spatial.viewmodel.repositories.BaseRepository {
    
    /**
     * voice chat protocol
     */
    private final io.agora.scene.voice.spatial.service.VoiceServiceProtocol voiceServiceProtocol = null;
    private final io.agora.scene.voice.spatial.model.RobotSpatialAudioModel voiceRobotInfo = null;
    
    public VoiceRoomLivingRepository() {
        super();
    }
    
    /**
     * 获取详情
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceRoomInfo>> fetchRoomDetail(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel) {
        return null;
    }
    
    /**
     * 开启/关闭机器人
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> enableRobot(boolean useRobot) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> enableBlueRobotAirAbsorb(boolean active) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> enableRedRobotAirAbsorb(boolean active) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> enableBlueRobotBlur(boolean active) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> enableRedRobotBlur(boolean active) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.Double, java.lang.Boolean>>> updateBlueRoBotAttenuation(double attenuation) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.Double, java.lang.Boolean>>> updateRedRoBotAttenuation(double attenuation) {
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
    java.lang.String userId) {
        return null;
    }
    
    /**
     * 本地禁麦 on / off
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMemberModel>> muteLocal(boolean mute) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> leaveMic(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> forbidMic(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> unForbidMic(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> kickOff(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> refuseInvite() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> lockMic(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> unLockMic(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel>>> changeMic(int oldIndex, int newIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> acceptMicSeatInvitation() {
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
}