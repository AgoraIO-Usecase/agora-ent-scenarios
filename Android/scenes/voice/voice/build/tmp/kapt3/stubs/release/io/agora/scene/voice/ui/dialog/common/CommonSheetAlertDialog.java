package io.agora.scene.voice.ui.dialog.common;

import java.lang.System;

/**
 * 确定/取消
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0017B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0002J\u000e\u0010\u0006\u001a\u00020\u00002\u0006\u0010\u0006\u001a\u00020\u0007J\u001a\u0010\u000e\u001a\u00020\u00022\u0006\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0014J\u000e\u0010\b\u001a\u00020\u00002\u0006\u0010\b\u001a\u00020\u0007J\u001a\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0016J\u000e\u0010\t\u001a\u00020\u00002\u0006\u0010\t\u001a\u00020\u0007J\u000e\u0010\u0016\u001a\u00020\u00002\u0006\u0010\u0004\u001a\u00020\u0005R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lio/agora/scene/voice/ui/dialog/common/CommonSheetAlertDialog;", "Lio/agora/voice/common/ui/dialog/BaseSheetDialog;", "Lio/agora/scene/voice/databinding/VoiceDialogBottomSheetAlertBinding;", "()V", "clickListener", "Lio/agora/scene/voice/ui/dialog/common/CommonSheetAlertDialog$OnClickBottomListener;", "contentText", "", "leftText", "rightText", "addMargin", "", "view", "Landroid/view/View;", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "savedInstanceState", "Landroid/os/Bundle;", "setOnClickListener", "OnClickBottomListener", "voice_release"})
public final class CommonSheetAlertDialog extends io.agora.voice.common.ui.dialog.BaseSheetDialog<io.agora.scene.voice.databinding.VoiceDialogBottomSheetAlertBinding> {
    private java.lang.String contentText = "";
    private java.lang.String leftText = "";
    private java.lang.String rightText = "";
    private io.agora.scene.voice.ui.dialog.common.CommonSheetAlertDialog.OnClickBottomListener clickListener;
    
    public CommonSheetAlertDialog() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceDialogBottomSheetAlertBinding getViewBinding(@org.jetbrains.annotations.NotNull()
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
    public final io.agora.scene.voice.ui.dialog.common.CommonSheetAlertDialog contentText(@org.jetbrains.annotations.NotNull()
    java.lang.String contentText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.dialog.common.CommonSheetAlertDialog leftText(@org.jetbrains.annotations.NotNull()
    java.lang.String leftText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.dialog.common.CommonSheetAlertDialog rightText(@org.jetbrains.annotations.NotNull()
    java.lang.String rightText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.dialog.common.CommonSheetAlertDialog setOnClickListener(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.ui.dialog.common.CommonSheetAlertDialog.OnClickBottomListener clickListener) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H\u0016J\b\u0010\u0004\u001a\u00020\u0003H&\u00a8\u0006\u0005"}, d2 = {"Lio/agora/scene/voice/ui/dialog/common/CommonSheetAlertDialog$OnClickBottomListener;", "", "onCancelClick", "", "onConfirmClick", "voice_release"})
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
            io.agora.scene.voice.ui.dialog.common.CommonSheetAlertDialog.OnClickBottomListener $this) {
            }
        }
    }
}