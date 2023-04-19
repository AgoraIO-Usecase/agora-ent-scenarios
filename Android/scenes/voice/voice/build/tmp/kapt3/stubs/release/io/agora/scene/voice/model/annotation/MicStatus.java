package io.agora.scene.voice.model.annotation;

import java.lang.System;

/**
 * 语聊房麦位状态
 *
 * 0:正常状态 1:闭麦 2:禁言 3:锁麦 4:锁麦和禁言 -1:空闲 5:机器人专属激活状态 -2:机器人专属关闭状态
 */
@androidx.annotation.IntDef(value = {-100, -1, 0, 1, 2, 3, 4, -2, 5})
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u001b\n\u0002\b\u0002\b\u0087\u0002\u0018\u0000 \u00022\u00020\u0001:\u0001\u0002B\u0000\u00a8\u0006\u0003"}, d2 = {"Lio/agora/scene/voice/model/annotation/MicStatus;", "", "Companion", "voice_release"})
@java.lang.annotation.Target(value = {java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.TYPE_USE})
@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.SOURCE)
@kotlin.annotation.Retention(value = kotlin.annotation.AnnotationRetention.SOURCE)
@kotlin.annotation.Target(allowedTargets = {kotlin.annotation.AnnotationTarget.CLASS, kotlin.annotation.AnnotationTarget.PROPERTY, kotlin.annotation.AnnotationTarget.VALUE_PARAMETER, kotlin.annotation.AnnotationTarget.TYPE})
public abstract @interface MicStatus {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.model.annotation.MicStatus.Companion Companion = null;
    public static final int Unknown = -100;
    public static final int Idle = -1;
    public static final int Normal = 0;
    public static final int Mute = 1;
    public static final int ForceMute = 2;
    public static final int Lock = 3;
    public static final int LockForceMute = 4;
    public static final int BotActivated = 5;
    public static final int BotInactive = -2;
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\t\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lio/agora/scene/voice/model/annotation/MicStatus$Companion;", "", "()V", "BotActivated", "", "BotInactive", "ForceMute", "Idle", "Lock", "LockForceMute", "Mute", "Normal", "Unknown", "voice_release"})
    public static final class Companion {
        public static final int Unknown = -100;
        public static final int Idle = -1;
        public static final int Normal = 0;
        public static final int Mute = 1;
        public static final int ForceMute = 2;
        public static final int Lock = 3;
        public static final int LockForceMute = 4;
        public static final int BotActivated = 5;
        public static final int BotInactive = -2;
        
        private Companion() {
            super();
        }
    }
}