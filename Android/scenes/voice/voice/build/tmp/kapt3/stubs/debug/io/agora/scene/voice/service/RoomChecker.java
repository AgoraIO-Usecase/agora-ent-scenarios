package io.agora.scene.voice.service;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\bJ\u000e\u0010\u0010\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n \f*\u0004\u0018\u00010\u000b0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lio/agora/scene/voice/service/RoomChecker;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "expireDuration", "", "keyLastUpdateTime", "", "keyRoomSet", "sharedPreferences", "Landroid/content/SharedPreferences;", "kotlin.jvm.PlatformType", "joinRoom", "", "roomId", "leaveRoom", "voice_debug"})
public final class RoomChecker {
    private final android.content.Context context = null;
    private final android.content.SharedPreferences sharedPreferences = null;
    private final java.lang.String keyRoomSet = "VoiceRoomSet";
    private final java.lang.String keyLastUpdateTime = "VoiceLastUpdateTime";
    private final int expireDuration = 120000;
    
    public RoomChecker(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final boolean joinRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId) {
        return false;
    }
    
    public final boolean leaveRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId) {
        return false;
    }
}