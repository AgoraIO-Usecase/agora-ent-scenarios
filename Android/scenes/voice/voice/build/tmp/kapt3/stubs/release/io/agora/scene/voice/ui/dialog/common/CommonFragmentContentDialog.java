package io.agora.scene.voice.ui.dialog.common;

import java.lang.System;

/**
 * 中间弹框，确认/取消按钮
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0016B\u0005\u00a2\u0006\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00002\u0006\u0010\u0006\u001a\u00020\u0007J\u001a\u0010\t\u001a\u00020\u00022\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0014J\u001a\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0016J\u0010\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u000e\u0010\u0015\u001a\u00020\u00002\u0006\u0010\u0004\u001a\u00020\u0005J\u000e\u0010\b\u001a\u00020\u00002\u0006\u0010\b\u001a\u00020\u0007R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lio/agora/scene/voice/ui/dialog/common/CommonFragmentContentDialog;", "Lio/agora/voice/common/ui/dialog/BaseFragmentDialog;", "Lio/agora/scene/voice/databinding/VoiceDialogCenterFragmentContentBinding;", "()V", "clickListener", "Lio/agora/scene/voice/ui/dialog/common/CommonFragmentContentDialog$OnClickBottomListener;", "contentText", "", "submitText", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "setDialogSize", "setOnClickListener", "OnClickBottomListener", "voice_release"})
public final class CommonFragmentContentDialog extends io.agora.voice.common.ui.dialog.BaseFragmentDialog<io.agora.scene.voice.databinding.VoiceDialogCenterFragmentContentBinding> {
    private java.lang.String contentText = "";
    private java.lang.String submitText = "";
    private io.agora.scene.voice.ui.dialog.common.CommonFragmentContentDialog.OnClickBottomListener clickListener;
    
    public CommonFragmentContentDialog() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceDialogCenterFragmentContentBinding getViewBinding(@org.jetbrains.annotations.NotNull()
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
    public final io.agora.scene.voice.ui.dialog.common.CommonFragmentContentDialog contentText(@org.jetbrains.annotations.NotNull()
    java.lang.String contentText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.dialog.common.CommonFragmentContentDialog submitText(@org.jetbrains.annotations.NotNull()
    java.lang.String submitText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.dialog.common.CommonFragmentContentDialog setOnClickListener(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.ui.dialog.common.CommonFragmentContentDialog.OnClickBottomListener clickListener) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&\u00a8\u0006\u0004"}, d2 = {"Lio/agora/scene/voice/ui/dialog/common/CommonFragmentContentDialog$OnClickBottomListener;", "", "onConfirmClick", "", "voice_release"})
    public static abstract interface OnClickBottomListener {
        
        /**
         * 点击确定按钮事件
         */
        public abstract void onConfirmClick();
    }
}