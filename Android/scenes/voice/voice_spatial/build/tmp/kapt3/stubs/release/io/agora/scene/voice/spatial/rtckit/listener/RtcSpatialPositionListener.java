package io.agora.scene.voice.spatial.rtckit.listener;

import java.lang.System;

/**
 * @author create by hezhengqing
 *
 * 远端空间位置变化监听
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b&\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\u0007"}, d2 = {"Lio/agora/scene/voice/spatial/rtckit/listener/RtcSpatialPositionListener;", "", "()V", "onRemoteSpatialChanged", "", "position", "Lio/agora/scene/voice/spatial/model/SeatPositionInfo;", "voice_spatial_release"})
public abstract class RtcSpatialPositionListener {
    
    public RtcSpatialPositionListener() {
        super();
    }
    
    /**
     * 远端空间位置变化
     */
    public abstract void onRemoteSpatialChanged(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.SeatPositionInfo position);
}