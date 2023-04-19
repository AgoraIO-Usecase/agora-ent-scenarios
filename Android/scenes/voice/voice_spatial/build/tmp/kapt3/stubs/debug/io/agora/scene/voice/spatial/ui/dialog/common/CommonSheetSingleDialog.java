package io.agora.scene.voice.spatial.ui.dialog.common;

import java.lang.System;

/**
 * 单按钮
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0015B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0002J\u001a\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0014J\u001a\u0010\u0011\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0016J\u000e\u0010\u0014\u001a\u00020\u00002\u0006\u0010\u0004\u001a\u00020\u0005J\u000e\u0010\u0006\u001a\u00020\u00002\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/common/CommonSheetSingleDialog;", "Lio/agora/voice/common/ui/dialog/BaseSheetDialog;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialDialogBottomSheetSingleBinding;", "()V", "clickListener", "Lio/agora/scene/voice/spatial/ui/dialog/common/CommonSheetSingleDialog$OnClickBottomListener;", "singleText", "", "addMargin", "", "view", "Landroid/view/View;", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "savedInstanceState", "Landroid/os/Bundle;", "setOnClickListener", "OnClickBottomListener", "voice_spatial_debug"})
public final class CommonSheetSingleDialog extends io.agora.voice.common.ui.dialog.BaseSheetDialog<io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogBottomSheetSingleBinding> {
    private java.lang.String singleText = "";
    private io.agora.scene.voice.spatial.ui.dialog.common.CommonSheetSingleDialog.OnClickBottomListener clickListener;
    
    public CommonSheetSingleDialog() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogBottomSheetSingleBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void addMargin(android.view.View view) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.dialog.common.CommonSheetSingleDialog singleText(@org.jetbrains.annotations.NotNull()
    java.lang.String singleText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.dialog.common.CommonSheetSingleDialog setOnClickListener(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.ui.dialog.common.CommonSheetSingleDialog.OnClickBottomListener clickListener) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&\u00a8\u0006\u0004"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/common/CommonSheetSingleDialog$OnClickBottomListener;", "", "onSingleClick", "", "voice_spatial_debug"})
    public static abstract interface OnClickBottomListener {
        
        public abstract void onSingleClick();
    }
}