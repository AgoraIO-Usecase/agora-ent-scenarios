package io.agora.scene.voice.spatial.viewmodel.repositories;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J*\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\nJ:\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\u00070\u00062\u0006\u0010\u000f\u001a\u00020\n2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u00112\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\nJ \u0010\u0013\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\u00140\u00070\u00062\u0006\u0010\u0015\u001a\u00020\u0011J\u001a\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\u00070\u00062\u0006\u0010\t\u001a\u00020\nR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lio/agora/scene/voice/spatial/viewmodel/repositories/VoiceCreateRepository;", "Lio/agora/scene/voice/spatial/viewmodel/repositories/BaseRepository;", "()V", "voiceServiceProtocol", "Lio/agora/scene/voice/spatial/service/VoiceServiceProtocol;", "checkPassword", "Landroidx/lifecycle/LiveData;", "Lio/agora/voice/common/net/Resource;", "", "roomId", "", "password", "userInput", "createRoom", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "roomName", "soundEffect", "", "roomType", "fetchRoomList", "", "page", "joinRoom", "voice_spatial_debug"})
public final class VoiceCreateRepository extends io.agora.scene.voice.spatial.viewmodel.repositories.BaseRepository {
    
    /**
     * voice chat protocol
     */
    private final io.agora.scene.voice.spatial.service.VoiceServiceProtocol voiceServiceProtocol = null;
    
    public VoiceCreateRepository() {
        super();
    }
    
    /**
     * 获取房间列表
     * @param page 第几页，暂未用到
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.spatial.model.VoiceRoomModel>>> fetchRoomList(int page) {
        return null;
    }
    
    /**
     * 私密房间密码校验，本地模拟验证
     * @param roomId 房间id
     * @param password 房间密码
     * @param userInput 用户输入
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> checkPassword(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String userInput) {
        return null;
    }
    
    /**
     * @param roomName 房间名
     * @param soundEffect 房间音效类型
     * @param roomType 房间类型 0 普通房间，1 3d 房间
     * @param password  私有房间，有秘密
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceRoomModel>> createRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomName, int soundEffect, int roomType, @org.jetbrains.annotations.Nullable()
    java.lang.String password) {
        return null;
    }
    
    /**
     * 加入房间
     * @param roomId 房间id
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceRoomModel>> joinRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId) {
        return null;
    }
}