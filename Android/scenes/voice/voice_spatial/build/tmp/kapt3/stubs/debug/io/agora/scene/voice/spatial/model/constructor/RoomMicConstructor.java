package io.agora.scene.voice.spatial.model.constructor;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c0\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\tJ\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0004J$\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u000b0\r2\u0006\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\tJ\u001c\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\u00042\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0011\u001a\u00020\u000bJ$\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00100\u00042\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0011\u001a\u00020\u000b2\u0006\u0010\u0013\u001a\u00020\t\u00a8\u0006\u0014"}, d2 = {"Lio/agora/scene/voice/spatial/model/constructor/RoomMicConstructor;", "", "()V", "builderDefault2dBotMicList", "", "Lio/agora/scene/voice/spatial/model/BotMicInfoBean;", "context", "Landroid/content/Context;", "isUserBot", "", "builderDefault2dMicList", "Lio/agora/scene/voice/spatial/model/VoiceMicInfoModel;", "builderDefault3dMicMap", "", "", "builderGuestMicMangerList", "Lio/agora/scene/voice/spatial/model/MicManagerBean;", "micInfo", "builderOwnerMicMangerList", "isMyself", "voice_spatial_debug"})
public final class RoomMicConstructor {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.model.constructor.RoomMicConstructor INSTANCE = null;
    
    private RoomMicConstructor() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.spatial.model.VoiceMicInfoModel> builderDefault2dMicList() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.spatial.model.BotMicInfoBean> builderDefault2dBotMicList(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean isUserBot) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel> builderDefault3dMicMap(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean isUserBot) {
        return null;
    }
    
    /**
     * 房主点击麦位管理
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.spatial.model.MicManagerBean> builderOwnerMicMangerList(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceMicInfoModel micInfo, boolean isMyself) {
        return null;
    }
    
    /**
     * 嘉宾点击麦位管理
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.spatial.model.MicManagerBean> builderGuestMicMangerList(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceMicInfoModel micInfo) {
        return null;
    }
}