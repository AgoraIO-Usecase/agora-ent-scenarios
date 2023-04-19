package io.agora.scene.voice.ui.widget.encryption;

import java.lang.System;

/**
 * 输入密码 dialog
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u001fB\u0005\u00a2\u0006\u0002\u0010\u0003J\u001c\u0010\u0010\u001a\u0004\u0018\u00010\u00022\u0006\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0014J\u000e\u0010\f\u001a\u00020\u00002\u0006\u0010\f\u001a\u00020\rJ\b\u0010\u0015\u001a\u00020\u0016H\u0016J\b\u0010\u0017\u001a\u00020\u0016H\u0016J\u001a\u0010\u0018\u001a\u00020\u00162\u0006\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0016J\u000e\u0010\u000e\u001a\u00020\u00002\u0006\u0010\u000e\u001a\u00020\rJ\u000e\u0010\u001d\u001a\u00020\u00002\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\u001e\u001a\u00020\u00002\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\u000f\u001a\u00020\u00002\u0006\u0010\u000f\u001a\u00020\rR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lio/agora/scene/voice/ui/widget/encryption/RoomEncryptionInputDialog;", "Lio/agora/voice/common/ui/dialog/BaseFragmentDialog;", "Lio/agora/scene/voice/databinding/VoiceDialogEncryptionBinding;", "()V", "BOND", "", "clickListener", "Lio/agora/scene/voice/ui/widget/encryption/RoomEncryptionInputDialog$OnClickBottomListener;", "handler", "Landroid/os/Handler;", "isCancel", "", "leftText", "", "rightText", "titleText", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onDestroy", "", "onResume", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "setDialogCancelable", "setOnClickListener", "OnClickBottomListener", "voice_release"})
public final class RoomEncryptionInputDialog extends io.agora.voice.common.ui.dialog.BaseFragmentDialog<io.agora.scene.voice.databinding.VoiceDialogEncryptionBinding> {
    private final int BOND = 1;
    private boolean isCancel = false;
    private io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog.OnClickBottomListener clickListener;
    private java.lang.String leftText = "";
    private java.lang.String rightText = "";
    private java.lang.String titleText = "";
    private final android.os.Handler handler = null;
    
    public RoomEncryptionInputDialog() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceDialogEncryptionBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    public void onResume() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog setOnClickListener(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog.OnClickBottomListener clickListener) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog leftText(@org.jetbrains.annotations.NotNull()
    java.lang.String leftText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog rightText(@org.jetbrains.annotations.NotNull()
    java.lang.String rightText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog titleText(@org.jetbrains.annotations.NotNull()
    java.lang.String titleText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog setDialogCancelable(boolean isCancel) {
        return null;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H\u0016J\u0010\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\u0007"}, d2 = {"Lio/agora/scene/voice/ui/widget/encryption/RoomEncryptionInputDialog$OnClickBottomListener;", "", "onCancelClick", "", "onConfirmClick", "password", "", "voice_release"})
    public static abstract interface OnClickBottomListener {
        
        /**
         * 点击确定按钮事件
         */
        public abstract void onConfirmClick(@org.jetbrains.annotations.NotNull()
        java.lang.String password);
        
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
            io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputDialog.OnClickBottomListener $this) {
            }
        }
    }
}