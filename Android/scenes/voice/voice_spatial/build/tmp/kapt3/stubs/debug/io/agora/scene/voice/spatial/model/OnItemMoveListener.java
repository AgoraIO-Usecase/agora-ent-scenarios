package io.agora.scene.voice.spatial.model;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\bf\u0018\u0000*\u0004\b\u0000\u0010\u00012\u00020\u0002J%\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00028\u00002\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016\u00a2\u0006\u0002\u0010\n\u00a8\u0006\u000b"}, d2 = {"Lio/agora/scene/voice/spatial/model/OnItemMoveListener;", "T", "", "onItemMove", "", "data", "position", "Lio/agora/scene/voice/spatial/model/SeatPositionInfo;", "viewType", "", "(Ljava/lang/Object;Lio/agora/scene/voice/spatial/model/SeatPositionInfo;J)V", "voice_spatial_debug"})
public abstract interface OnItemMoveListener<T extends java.lang.Object> {
    
    public abstract void onItemMove(T data, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.SeatPositionInfo position, long viewType);
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 3)
    public final class DefaultImpls {
        
        public static <T extends java.lang.Object>void onItemMove(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.spatial.model.OnItemMoveListener<T> $this, T data, @org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.spatial.model.SeatPositionInfo position, long viewType) {
        }
    }
}