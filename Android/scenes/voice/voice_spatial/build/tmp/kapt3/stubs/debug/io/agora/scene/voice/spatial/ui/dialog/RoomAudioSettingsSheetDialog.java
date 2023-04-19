package io.agora.scene.voice.spatial.ui.dialog;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\u0018\u0000 $2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0002$%B\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u0014\u001a\u00020\u0015H\u0016J\u001a\u0010\u0016\u001a\u00020\u00022\u0006\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0014J\u001a\u0010\u001b\u001a\u00020\u00152\u0006\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010 \u001a\u00020\u0015H\u0002J\u000e\u0010!\u001a\u00020\u00152\u0006\u0010\"\u001a\u00020#R\u001b\u0010\u0004\u001a\u00020\u00058FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\b\u0010\t\u001a\u0004\b\u0006\u0010\u0007R\u001c\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomAudioSettingsSheetDialog;", "Lio/agora/voice/common/ui/dialog/BaseSheetDialog;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialDialogAudioSettingBinding;", "()V", "audioSettingsInfo", "Lio/agora/scene/voice/spatial/model/RoomAudioSettingsBean;", "getAudioSettingsInfo", "()Lio/agora/scene/voice/spatial/model/RoomAudioSettingsBean;", "audioSettingsInfo$delegate", "Lkotlin/Lazy;", "audioSettingsListener", "Lio/agora/scene/voice/spatial/ui/dialog/RoomAudioSettingsSheetDialog$OnClickAudioSettingsListener;", "getAudioSettingsListener", "()Lio/agora/scene/voice/spatial/ui/dialog/RoomAudioSettingsSheetDialog$OnClickAudioSettingsListener;", "setAudioSettingsListener", "(Lio/agora/scene/voice/spatial/ui/dialog/RoomAudioSettingsSheetDialog$OnClickAudioSettingsListener;)V", "botTask", "Ljava/lang/Runnable;", "mainHandler", "Landroid/os/Handler;", "dismiss", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "startBotTask", "updateBoxCheckBoxView", "openBot", "", "Companion", "OnClickAudioSettingsListener", "voice_spatial_debug"})
public final class RoomAudioSettingsSheetDialog extends io.agora.voice.common.ui.dialog.BaseSheetDialog<io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogAudioSettingBinding> {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.ui.dialog.RoomAudioSettingsSheetDialog.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_AUDIO_SETTINGS_INFO = "audio_settings";
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy audioSettingsInfo$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private io.agora.scene.voice.spatial.ui.dialog.RoomAudioSettingsSheetDialog.OnClickAudioSettingsListener audioSettingsListener;
    private android.os.Handler mainHandler;
    private java.lang.Runnable botTask;
    
    public RoomAudioSettingsSheetDialog() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.model.RoomAudioSettingsBean getAudioSettingsInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.spatial.ui.dialog.RoomAudioSettingsSheetDialog.OnClickAudioSettingsListener getAudioSettingsListener() {
        return null;
    }
    
    public final void setAudioSettingsListener(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.ui.dialog.RoomAudioSettingsSheetDialog.OnClickAudioSettingsListener p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogAudioSettingBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    /**
     * 更新机器人ui
     */
    public final void updateBoxCheckBoxView(boolean openBot) {
    }
    
    @java.lang.Override()
    public void dismiss() {
    }
    
    private final void startBotTask() {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H&J\u0010\u0010\b\u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\nH&J\u0018\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\u0007H&J\u0018\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\r\u001a\u00020\u0007H&\u00a8\u0006\u0010"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomAudioSettingsSheetDialog$OnClickAudioSettingsListener;", "", "onBotCheckedChanged", "", "buttonView", "Landroid/widget/CompoundButton;", "isChecked", "", "onBotVolumeChange", "progress", "", "onSpatialAudio", "isOpen", "isEnable", "onVoiceChanger", "mode", "voice_spatial_debug"})
    public static abstract interface OnClickAudioSettingsListener {
        
        /**
         * 变声
         */
        public abstract void onVoiceChanger(int mode, boolean isEnable);
        
        /**
         * 机器人开关
         */
        public abstract void onBotCheckedChanged(@org.jetbrains.annotations.NotNull()
        android.widget.CompoundButton buttonView, boolean isChecked);
        
        /**
         * 机器人音量
         */
        public abstract void onBotVolumeChange(int progress);
        
        /**
         * 空间音频
         */
        public abstract void onSpatialAudio(boolean isOpen, boolean isEnable);
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomAudioSettingsSheetDialog$Companion;", "", "()V", "KEY_AUDIO_SETTINGS_INFO", "", "voice_spatial_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}