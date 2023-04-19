package io.agora.scene.voice.spatial.viewmodel.repositories;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\t\u001a\u00020\nJ\u0018\u0010\u000b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u00070\u0006J\u0018\u0010\u000e\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u00070\u0006J\u0018\u0010\u000f\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u00070\u0006J\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\u0011\u001a\u00020\u0012J)\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00070\u00062\u0006\u0010\t\u001a\u00020\n2\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\u0002\u0010\u0015R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lio/agora/scene/voice/spatial/viewmodel/repositories/VoiceUserListRepository;", "Lio/agora/scene/voice/spatial/viewmodel/repositories/BaseRepository;", "()V", "voiceServiceProtocol", "Lio/agora/scene/voice/spatial/service/VoiceServiceProtocol;", "acceptMicSeatApply", "Landroidx/lifecycle/LiveData;", "Lio/agora/voice/common/net/Resource;", "Lio/agora/scene/voice/spatial/model/VoiceMicInfoModel;", "userId", "", "fetchApplicantsList", "", "Lio/agora/scene/voice/spatial/model/VoiceMemberModel;", "fetchInvitedList", "fetchRoomMembers", "kickOff", "micIndex", "", "startMicSeatInvitation", "", "(Ljava/lang/String;Ljava/lang/Integer;)Landroidx/lifecycle/LiveData;", "voice_spatial_debug"})
public final class VoiceUserListRepository extends io.agora.scene.voice.spatial.viewmodel.repositories.BaseRepository {
    
    /**
     * voice chat protocol
     */
    private final io.agora.scene.voice.spatial.service.VoiceServiceProtocol voiceServiceProtocol = null;
    
    public VoiceUserListRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> kickOff(int micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> startMicSeatInvitation(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> acceptMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
        return null;
    }
    
    /**
     * 举手列表
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel>>> fetchApplicantsList() {
        return null;
    }
    
    /**
     * 邀请列表
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel>>> fetchInvitedList() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel>>> fetchRoomMembers() {
        return null;
    }
}