package io.agora.scene.voice.model.constructor;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u0002J*\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u0006J.\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\t0\u000f2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\t0\u000f2\u0006\u0010\r\u001a\u00020\u0006J\u0012\u0010\u0011\u001a\u00020\u0012*\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015\u00a8\u0006\u0016"}, d2 = {"Lio/agora/scene/voice/model/constructor/RoomInfoConstructor;", "", "()V", "curUserIsHost", "", "ownerId", "", "extendMicInfoList", "", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "vMicInfoList", "roomType", "", "ownerUid", "extendMicInfoMap", "", "micInfoMap", "convertByVoiceRoomModel", "", "Lio/agora/scene/voice/model/RoomKitBean;", "voiceRoomModel", "Lio/agora/scene/voice/model/VoiceRoomModel;", "voice_release"})
public final class RoomInfoConstructor {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.model.constructor.RoomInfoConstructor INSTANCE = null;
    
    private RoomInfoConstructor() {
        super();
    }
    
    /**
     * VoiceRoomModel convert RoomKitBean
     */
    public final void convertByVoiceRoomModel(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.RoomKitBean $this$convertByVoiceRoomModel, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceRoomModel voiceRoomModel) {
    }
    
    /**
     * Check if you are a host
     */
    private final boolean curUserIsHost(java.lang.String ownerId) {
        return false;
    }
    
    /**
     * 扩展麦位数据
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> extendMicInfoList(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> vMicInfoList, int roomType, @org.jetbrains.annotations.NotNull()
    java.lang.String ownerUid) {
        return null;
    }
    
    /**
     * 扩展麦位数据
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel> extendMicInfoMap(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, io.agora.scene.voice.model.VoiceMicInfoModel> micInfoMap, @org.jetbrains.annotations.NotNull()
    java.lang.String ownerUid) {
        return null;
    }
}