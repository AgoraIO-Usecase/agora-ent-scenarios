package io.agora.scene.voice.spatial.ui.dialog;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 *2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0002*+B\u0005\u00a2\u0006\u0002\u0010\u0003J\u001a\u0010\u001f\u001a\u00020\u00022\u0006\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0014J\u001a\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\'2\b\u0010(\u001a\u0004\u0018\u00010)H\u0016R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\rR\u001b\u0010\u0010\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\u000f\u001a\u0004\b\u0010\u0010\u0012R\u001b\u0010\u0014\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0015\u0010\u000f\u001a\u0004\b\u0014\u0010\u0012R\u001b\u0010\u0016\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0017\u0010\u000f\u001a\u0004\b\u0016\u0010\u0012R\u001b\u0010\u0018\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0019\u0010\u000f\u001a\u0004\b\u0018\u0010\u0012R\u001b\u0010\u001a\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001b\u0010\u000f\u001a\u0004\b\u001a\u0010\u0012R\u001b\u0010\u001c\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001e\u0010\u000f\u001a\u0004\b\u001d\u0010\r\u00a8\u0006,"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomSpatialAudioSheetDialog;", "Lio/agora/voice/common/ui/dialog/BaseFixedHeightSheetDialog;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialDialogRoomSpatialAudioBinding;", "()V", "audioSettingsListener", "Lio/agora/scene/voice/spatial/ui/dialog/RoomSpatialAudioSheetDialog$OnClickSpatialAudioRobotsSettingsListener;", "getAudioSettingsListener", "()Lio/agora/scene/voice/spatial/ui/dialog/RoomSpatialAudioSheetDialog$OnClickSpatialAudioRobotsSettingsListener;", "setAudioSettingsListener", "(Lio/agora/scene/voice/spatial/ui/dialog/RoomSpatialAudioSheetDialog$OnClickSpatialAudioRobotsSettingsListener;)V", "blueAttenuation", "", "getBlueAttenuation", "()I", "blueAttenuation$delegate", "Lkotlin/Lazy;", "isBlueAbsorbEnabled", "", "()Z", "isBlueAbsorbEnabled$delegate", "isBlueBlurEnabled", "isBlueBlurEnabled$delegate", "isEnabled", "isEnabled$delegate", "isRedAbsorbEnabled", "isRedAbsorbEnabled$delegate", "isRedBlurEnabled", "isRedBlurEnabled$delegate", "redAttenuation", "getRedAttenuation", "redAttenuation$delegate", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "Companion", "OnClickSpatialAudioRobotsSettingsListener", "voice_spatial_release"})
public final class RoomSpatialAudioSheetDialog extends io.agora.voice.common.ui.dialog.BaseFixedHeightSheetDialog<io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomSpatialAudioBinding> {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.ui.dialog.RoomSpatialAudioSheetDialog.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_SPATIAL_OPEN = "key_spatial_open";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_IS_ENABLED = "is_enabled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_BLUE_AIR_ABSORB_ENABLED = "blue_absorb_is_enabled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_RED_AIR_ABSORB_ENABLED = "red_absorb_is_enabled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_BLUE_BLUR_ENABLED = "blue_blur_is_enabled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_RED_BLUR_ENABLED = "red_blur_is_enabled";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_BLUE_ATTENUATION = "blue_attenuation";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_RED_ATTENUATION = "red_attenuation";
    private final kotlin.Lazy isEnabled$delegate = null;
    private final kotlin.Lazy isBlueAbsorbEnabled$delegate = null;
    private final kotlin.Lazy isRedAbsorbEnabled$delegate = null;
    private final kotlin.Lazy isBlueBlurEnabled$delegate = null;
    private final kotlin.Lazy isRedBlurEnabled$delegate = null;
    private final kotlin.Lazy blueAttenuation$delegate = null;
    private final kotlin.Lazy redAttenuation$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private io.agora.scene.voice.spatial.ui.dialog.RoomSpatialAudioSheetDialog.OnClickSpatialAudioRobotsSettingsListener audioSettingsListener;
    
    public RoomSpatialAudioSheetDialog() {
        super();
    }
    
    private final boolean isEnabled() {
        return false;
    }
    
    private final boolean isBlueAbsorbEnabled() {
        return false;
    }
    
    private final boolean isRedAbsorbEnabled() {
        return false;
    }
    
    private final boolean isBlueBlurEnabled() {
        return false;
    }
    
    private final boolean isRedBlurEnabled() {
        return false;
    }
    
    private final int getBlueAttenuation() {
        return 0;
    }
    
    private final int getRedAttenuation() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.spatial.ui.dialog.RoomSpatialAudioSheetDialog.OnClickSpatialAudioRobotsSettingsListener getAudioSettingsListener() {
        return null;
    }
    
    public final void setAudioSettingsListener(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.ui.dialog.RoomSpatialAudioSheetDialog.OnClickSpatialAudioRobotsSettingsListener p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomSpatialAudioBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H&J\u0010\u0010\b\u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\nH&J\u0018\u0010\u000b\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H&J\u0018\u0010\f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H&J\u0010\u0010\r\u001a\u00020\u00032\u0006\u0010\t\u001a\u00020\nH&J\u0018\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H&\u00a8\u0006\u000f"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomSpatialAudioSheetDialog$OnClickSpatialAudioRobotsSettingsListener;", "", "onBlueBotAirAbsorbCheckedChanged", "", "buttonView", "Landroid/widget/CompoundButton;", "isChecked", "", "onBlueBotAttenuationChange", "progress", "", "onBlueBotVoiceBlurCheckedChanged", "onRedBotAirAbsorbCheckedChanged", "onRedBotAttenuationChange", "onRedBotVoiceBlurCheckedChanged", "voice_spatial_release"})
    public static abstract interface OnClickSpatialAudioRobotsSettingsListener {
        
        /**
         * 蓝色机器人衰减系数
         */
        public abstract void onBlueBotAttenuationChange(int progress);
        
        /**
         * 蓝色机器人空气衰减开关
         */
        public abstract void onBlueBotAirAbsorbCheckedChanged(@org.jetbrains.annotations.NotNull()
        android.widget.CompoundButton buttonView, boolean isChecked);
        
        /**
         * 蓝色机器人人声模糊开关
         */
        public abstract void onBlueBotVoiceBlurCheckedChanged(@org.jetbrains.annotations.NotNull()
        android.widget.CompoundButton buttonView, boolean isChecked);
        
        /**
         * 红色机器人衰减系数
         */
        public abstract void onRedBotAttenuationChange(int progress);
        
        /**
         * 红色机器人空气衰减开关
         */
        public abstract void onRedBotAirAbsorbCheckedChanged(@org.jetbrains.annotations.NotNull()
        android.widget.CompoundButton buttonView, boolean isChecked);
        
        /**
         * 红色机器人人声模糊开关
         */
        public abstract void onRedBotVoiceBlurCheckedChanged(@org.jetbrains.annotations.NotNull()
        android.widget.CompoundButton buttonView, boolean isChecked);
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomSpatialAudioSheetDialog$Companion;", "", "()V", "KEY_BLUE_AIR_ABSORB_ENABLED", "", "KEY_BLUE_ATTENUATION", "KEY_BLUE_BLUR_ENABLED", "KEY_IS_ENABLED", "KEY_RED_AIR_ABSORB_ENABLED", "KEY_RED_ATTENUATION", "KEY_RED_BLUR_ENABLED", "KEY_SPATIAL_OPEN", "voice_spatial_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}