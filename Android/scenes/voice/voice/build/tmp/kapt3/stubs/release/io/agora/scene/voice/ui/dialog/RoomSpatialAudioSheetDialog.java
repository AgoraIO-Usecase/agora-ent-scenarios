package io.agora.scene.voice.ui.dialog;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u00142\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0014B\u0005\u00a2\u0006\u0002\u0010\u0003J\u001a\u0010\t\u001a\u00020\u00022\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0014J\u001a\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0016R\u001b\u0010\u0004\u001a\u00020\u00058BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0004\u0010\u0006\u00a8\u0006\u0015"}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomSpatialAudioSheetDialog;", "Lio/agora/voice/common/ui/dialog/BaseFixedHeightSheetDialog;", "Lio/agora/scene/voice/databinding/VoiceDialogRoomSpatialAudioBinding;", "()V", "isEnabled", "", "()Z", "isEnabled$delegate", "Lkotlin/Lazy;", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "Companion", "voice_release"})
public final class RoomSpatialAudioSheetDialog extends io.agora.voice.common.ui.dialog.BaseFixedHeightSheetDialog<io.agora.scene.voice.databinding.VoiceDialogRoomSpatialAudioBinding> {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.dialog.RoomSpatialAudioSheetDialog.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_SPATIAL_OPEN = "key_spatial_open";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_IS_ENABLED = "is_enabled";
    private final kotlin.Lazy isEnabled$delegate = null;
    
    public RoomSpatialAudioSheetDialog() {
        super();
    }
    
    private final boolean isEnabled() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceDialogRoomSpatialAudioBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomSpatialAudioSheetDialog$Companion;", "", "()V", "KEY_IS_ENABLED", "", "KEY_SPATIAL_OPEN", "voice_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}