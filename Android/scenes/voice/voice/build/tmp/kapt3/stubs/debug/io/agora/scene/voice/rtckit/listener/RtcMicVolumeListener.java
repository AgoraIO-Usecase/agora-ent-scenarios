package io.agora.scene.voice.rtckit.listener;

import java.lang.System;

/**
 * @author create by zhangwei03
 *
 * 麦位音量监听
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\b&\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH&J\u0018\u0010\t\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u0006H&\u00a8\u0006\f"}, d2 = {"Lio/agora/scene/voice/rtckit/listener/RtcMicVolumeListener;", "", "()V", "onBotVolume", "", "speaker", "", "finished", "", "onUserVolume", "rtcUid", "volume", "voice_debug"})
public abstract class RtcMicVolumeListener {
    
    public RtcMicVolumeListener() {
        super();
    }
    
    /**
     * 模拟机器人音量显示
     */
    public abstract void onBotVolume(int speaker, boolean finished);
    
    /**
     * 用户语聊音量
     */
    public abstract void onUserVolume(int rtcUid, int volume);
}