package io.agora.scene.voice.ui.widget.mic;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\b\u0006\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH&J\b\u0010\n\u001a\u00020\u0007H&J\u001e\u0010\u000b\u001a\u00020\u00032\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\u0006\u0010\u000f\u001a\u00020\u0005H&J\u001c\u0010\u0010\u001a\u00020\u00032\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u000e0\u0012H&J\u0018\u0010\u0013\u001a\u00020\u00032\u0006\u0010\u0014\u001a\u00020\u00072\u0006\u0010\u0015\u001a\u00020\u0007H&J\u0018\u0010\u0016\u001a\u00020\u00032\u0006\u0010\u0017\u001a\u00020\u00072\u0006\u0010\u0015\u001a\u00020\u0007H&\u00a8\u0006\u0018"}, d2 = {"Lio/agora/scene/voice/ui/widget/mic/IRoomMicView;", "", "activeBot", "", "active", "", "findMicByUid", "", "uid", "", "myRtcUid", "onInitMic", "micInfoList", "", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "isBotActive", "onSeatUpdated", "newMicMap", "", "updateBotVolume", "speakerType", "volume", "updateVolume", "index", "voice_release"})
public abstract interface IRoomMicView {
    
    /**
     * 初始化麦位数据
     */
    public abstract void onInitMic(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> micInfoList, boolean isBotActive);
    
    /**
     * 开关机器人
     */
    public abstract void activeBot(boolean active);
    
    /**
     * 音量指示
     */
    public abstract void updateVolume(int index, int volume);
    
    /**
     * 机器人音量指示
     */
    public abstract void updateBotVolume(int speakerType, int volume);
    
    /**
     * 多麦位更新
     */
    public abstract void onSeatUpdated(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel> newMicMap);
    
    /**
     * 是否在麦位上,-1 不在
     */
    public abstract int findMicByUid(@org.jetbrains.annotations.NotNull()
    java.lang.String uid);
    
    public abstract int myRtcUid();
}