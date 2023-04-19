package io.agora.scene.voice.spatial.ui.dialog.common;

import java.lang.System;

/**
 * 中间弹框，确认/取消按钮
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0018B\u0005\u00a2\u0006\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00002\u0006\u0010\u0006\u001a\u00020\u0007J\u001a\u0010\u000b\u001a\u00020\u00022\u0006\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u0014J\u000e\u0010\b\u001a\u00020\u00002\u0006\u0010\b\u001a\u00020\u0007J\u001a\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0016J\u000e\u0010\t\u001a\u00020\u00002\u0006\u0010\t\u001a\u00020\u0007J\u0010\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0002J\u000e\u0010\u0017\u001a\u00020\u00002\u0006\u0010\u0004\u001a\u00020\u0005J\u000e\u0010\n\u001a\u00020\u00002\u0006\u0010\n\u001a\u00020\u0007R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/common/CommonFragmentAlertDialog;", "Lio/agora/voice/common/ui/dialog/BaseFragmentDialog;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialDialogCenterFragmentAlertBinding;", "()V", "clickListener", "Lio/agora/scene/voice/spatial/ui/dialog/common/CommonFragmentAlertDialog$OnClickBottomListener;", "contentText", "", "leftText", "rightText", "titleText", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "setDialogSize", "setOnClickListener", "OnClickBottomListener", "voice_spatial_release"})
public final class CommonFragmentAlertDialog extends io.agora.voice.common.ui.dialog.BaseFragmentDialog<io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogCenterFragmentAlertBinding> {
    private java.lang.String titleText = "";
    private java.lang.String contentText = "";
    private java.lang.String leftText = "";
    private java.lang.String rightText = "";
    private io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentAlertDialog.OnClickBottomListener clickListener;
    
    public CommonFragmentAlertDialog() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogCenterFragmentAlertBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setDialogSize(android.view.View view) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentAlertDialog titleText(@org.jetbrains.annotations.NotNull()
    java.lang.String titleText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentAlertDialog contentText(@org.jetbrains.annotations.NotNull()
    java.lang.String contentText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentAlertDialog leftText(@org.jetbrains.annotations.NotNull()
    java.lang.String leftText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentAlertDialog rightText(@org.jetbrains.annotations.NotNull()
    java.lang.String rightText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentAlertDialog setOnClickListener(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentAlertDialog.OnClickBottomListener clickListener) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H\u0016J\b\u0010\u0004\u001a\u00020\u0003H&\u00a8\u0006\u0005"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/common/CommonFragmentAlertDialog$OnClickBottomListener;", "", "onCancelClick", "", "onConfirmClick", "voice_spatial_release"})
    public static abstract interface OnClickBottomListener {
        
        /**
         * 点击确定按钮事件
         */
        public abstract void onConfirmClick();
        
        /**
         * 点击取消按钮事件
         */
        public abstract void onCancelClick();
        
        @kotlin.Metadata(mv = {1, 6, 0}, k = 3)
        public static final class DefaultImpls {
            
            /**
             * 点击取消按钮事件
             */
            public static void onCancelClick(@org.jetbrains.annotations.NotNull()
            io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentAlertDialog.OnClickBottomListener $this) {
            }
        }
    }
}