package io.agora.scene.voice.spatial.ui.widget.mic;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J8\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052&\u0010\u0006\u001a\"\u0012\u0004\u0012\u00020\b\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n0\t\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u0007H&J\u0010\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rH&J\b\u0010\u000e\u001a\u00020\bH&J\u001e\u0010\u000f\u001a\u00020\u00032\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u00112\u0006\u0010\u0013\u001a\u00020\u0005H&J\u001c\u0010\u0014\u001a\u00020\u00032\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00120\u0016H&J\u0018\u0010\u0017\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\b2\u0006\u0010\u0019\u001a\u00020\bH&J\u0010\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\u001cH&J\u0018\u0010\u001d\u001a\u00020\u00032\u0006\u0010\u001e\u001a\u00020\b2\u0006\u0010\u0019\u001a\u00020\bH&\u00a8\u0006\u001f"}, d2 = {"Lio/agora/scene/voice/spatial/ui/widget/mic/IRoomMicView;", "", "activeBot", "", "active", "", "each", "Lkotlin/Function2;", "", "Lkotlin/Pair;", "Landroid/graphics/PointF;", "findMicByUid", "uid", "", "myRtcUid", "onInitMic", "micInfoList", "", "Lio/agora/scene/voice/spatial/model/VoiceMicInfoModel;", "isBotActive", "onSeatUpdated", "newMicMap", "", "updateBotVolume", "speakerType", "volume", "updateSpatialPosition", "info", "Lio/agora/scene/voice/spatial/model/SeatPositionInfo;", "updateVolume", "index", "voice_spatial_release"})
public abstract interface IRoomMicView {
    
    /**
     * 初始化麦位数据
     */
    public abstract void onInitMic(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.spatial.model.VoiceMicInfoModel> micInfoList, boolean isBotActive);
    
    /**
     * 开关机器人
     */
    public abstract void activeBot(boolean active, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super kotlin.Pair<? extends android.graphics.PointF, ? extends android.graphics.PointF>, kotlin.Unit> each);
    
    /**
     * 音量指示
     */
    public abstract void updateVolume(int index, int volume);
    
    /**
     * 机器人音量指示
     * @return 机器人空间位置更新
     */
    public abstract void updateBotVolume(int speakerType, int volume);
    
    /**
     * 多麦位更新
     */
    public abstract void onSeatUpdated(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel> newMicMap);
    
    /**
     * 是否在麦位上,-1 不在
     */
    public abstract int findMicByUid(@org.jetbrains.annotations.NotNull()
    java.lang.String uid);
    
    public abstract int myRtcUid();
    
    /**
     * 更新空间音频麦位位置
     */
    public abstract void updateSpatialPosition(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.SeatPositionInfo info);
}