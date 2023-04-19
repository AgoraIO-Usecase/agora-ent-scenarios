package io.agora.scene.voice.ui.dialog;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0015\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010$\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u0000 -2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0002-.B\u0005\u00a2\u0006\u0002\u0010\u0003J\u001a\u0010\u001a\u001a\u00020\u001b2\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00050\u001dJ\u001a\u0010\u001e\u001a\u00020\u00022\u0006\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\"H\u0014J\u0006\u0010#\u001a\u00020\u001bJ\u0006\u0010$\u001a\u00020\u001bJ\u001a\u0010%\u001a\u00020\u001b2\u0006\u0010&\u001a\u00020\'2\b\u0010(\u001a\u0004\u0018\u00010\bH\u0016J\u0010\u0010)\u001a\u00020\u001b2\b\u0010*\u001a\u0004\u0018\u00010\u0012J\b\u0010+\u001a\u00020\u001bH\u0002J\u000e\u0010,\u001a\u00020\u001b2\u0006\u0010\f\u001a\u00020\rR\u0016\u0010\u0004\u001a\n \u0006*\u0004\u0018\u00010\u00050\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006/"}, d2 = {"Lio/agora/scene/voice/ui/dialog/ChatroomHandsDialog;", "Lio/agora/voice/common/ui/dialog/BaseSheetDialog;", "Lio/agora/scene/voice/databinding/VoiceRoomHandLayoutBinding;", "()V", "TAG", "", "kotlin.jvm.PlatformType", "bundle", "Landroid/os/Bundle;", "fragments", "", "Landroidx/fragment/app/Fragment;", "index", "", "inviteHandsFragment", "Lio/agora/scene/voice/ui/fragment/ChatroomInviteHandsFragment;", "mCount", "onFragmentListener", "Lio/agora/scene/voice/ui/dialog/ChatroomHandsDialog$OnFragmentListener;", "raisedHandsFragment", "Lio/agora/scene/voice/ui/fragment/ChatroomRaisedHandsFragment;", "roomId", "title", "Lcom/google/android/material/textview/MaterialTextView;", "titles", "", "check", "", "map", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "initListener", "initView", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "setFragmentListener", "listener", "setupWithViewPager", "update", "Companion", "OnFragmentListener", "voice_release"})
public final class ChatroomHandsDialog extends io.agora.voice.common.ui.dialog.BaseSheetDialog<io.agora.scene.voice.databinding.VoiceRoomHandLayoutBinding> {
    private final int[] titles = null;
    private final java.util.List<androidx.fragment.app.Fragment> fragments = null;
    private com.google.android.material.textview.MaterialTextView title;
    private int index = 0;
    private int mCount = 0;
    private java.lang.String roomId;
    private final android.os.Bundle bundle = null;
    private io.agora.scene.voice.ui.fragment.ChatroomRaisedHandsFragment raisedHandsFragment;
    private io.agora.scene.voice.ui.fragment.ChatroomInviteHandsFragment inviteHandsFragment;
    private final java.lang.String TAG = null;
    private io.agora.scene.voice.ui.dialog.ChatroomHandsDialog.OnFragmentListener onFragmentListener;
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.dialog.ChatroomHandsDialog.Companion Companion = null;
    
    public ChatroomHandsDialog() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceRoomHandLayoutBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    public final void initView() {
    }
    
    public final void initListener() {
    }
    
    private final void setupWithViewPager() {
    }
    
    public final void update(int index) {
    }
    
    public final void check(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.Integer, java.lang.String> map) {
    }
    
    public final void setFragmentListener(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.ui.dialog.ChatroomHandsDialog.OnFragmentListener listener) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.dialog.ChatroomHandsDialog getNewInstance() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0016J\u0010\u0010\u0006\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\bH\u0016\u00a8\u0006\t"}, d2 = {"Lio/agora/scene/voice/ui/dialog/ChatroomHandsDialog$OnFragmentListener;", "", "getItemCount", "", "count", "", "onAcceptMicSeatApply", "voiceMicInfoModel", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "voice_release"})
    public static abstract interface OnFragmentListener {
        
        public abstract void getItemCount(int count);
        
        public abstract void onAcceptMicSeatApply(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.model.VoiceMicInfoModel voiceMicInfoModel);
        
        @kotlin.Metadata(mv = {1, 6, 0}, k = 3)
        public static final class DefaultImpls {
            
            public static void getItemCount(@org.jetbrains.annotations.NotNull()
            io.agora.scene.voice.ui.dialog.ChatroomHandsDialog.OnFragmentListener $this, int count) {
            }
            
            public static void onAcceptMicSeatApply(@org.jetbrains.annotations.NotNull()
            io.agora.scene.voice.ui.dialog.ChatroomHandsDialog.OnFragmentListener $this, @org.jetbrains.annotations.NotNull()
            io.agora.scene.voice.model.VoiceMicInfoModel voiceMicInfoModel) {
            }
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001a\u0010\u0003\u001a\u00020\u00048FX\u0087\u0004\u00a2\u0006\f\u0012\u0004\b\u0005\u0010\u0002\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lio/agora/scene/voice/ui/dialog/ChatroomHandsDialog$Companion;", "", "()V", "newInstance", "Lio/agora/scene/voice/ui/dialog/ChatroomHandsDialog;", "getNewInstance$annotations", "getNewInstance", "()Lio/agora/scene/voice/ui/dialog/ChatroomHandsDialog;", "voice_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @kotlin.jvm.JvmStatic()
        @java.lang.Deprecated()
        public static void getNewInstance$annotations() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final io.agora.scene.voice.ui.dialog.ChatroomHandsDialog getNewInstance() {
            return null;
        }
    }
}