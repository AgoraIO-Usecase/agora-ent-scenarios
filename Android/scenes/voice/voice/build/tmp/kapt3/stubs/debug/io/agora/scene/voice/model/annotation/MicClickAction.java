package io.agora.scene.voice.model.annotation;

import java.lang.System;

/**
 * 麦位管理点击事件
 */
@androidx.annotation.IntDef(value = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9})
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u001b\n\u0002\b\u0002\b\u0087\u0002\u0018\u0000 \u00022\u00020\u0001:\u0001\u0002B\u0000\u00a8\u0006\u0003"}, d2 = {"Lio/agora/scene/voice/model/annotation/MicClickAction;", "", "Companion", "voice_debug"})
@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.SOURCE)
@kotlin.annotation.Retention(value = kotlin.annotation.AnnotationRetention.SOURCE)
public abstract @interface MicClickAction {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.model.annotation.MicClickAction.Companion Companion = null;
    public static final int Invite = 0;
    public static final int ForbidMic = 1;
    public static final int UnForbidMic = 2;
    public static final int Mute = 3;
    public static final int UnMute = 4;
    public static final int Lock = 5;
    public static final int UnLock = 6;
    public static final int KickOff = 7;
    public static final int OffStage = 8;
    public static final int Accept = 9;
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\n\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lio/agora/scene/voice/model/annotation/MicClickAction$Companion;", "", "()V", "Accept", "", "ForbidMic", "Invite", "KickOff", "Lock", "Mute", "OffStage", "UnForbidMic", "UnLock", "UnMute", "voice_debug"})
    public static final class Companion {
        public static final int Invite = 0;
        public static final int ForbidMic = 1;
        public static final int UnForbidMic = 2;
        public static final int Mute = 3;
        public static final int UnMute = 4;
        public static final int Lock = 5;
        public static final int UnLock = 6;
        public static final int KickOff = 7;
        public static final int OffStage = 8;
        public static final int Accept = 9;
        
        private Companion() {
            super();
        }
    }
}