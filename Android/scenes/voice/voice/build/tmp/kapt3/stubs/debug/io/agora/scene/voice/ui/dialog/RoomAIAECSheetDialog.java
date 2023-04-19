package io.agora.scene.voice.ui.dialog;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 (2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001(B\u0005\u00a2\u0006\u0002\u0010\u0003J\u001c\u0010\u001b\u001a\u0004\u0018\u00010\u00022\u0006\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0014J\b\u0010 \u001a\u00020\u0016H\u0016J\u001a\u0010!\u001a\u00020\u00162\u0006\u0010\"\u001a\u00020#2\b\u0010$\u001a\u0004\u0018\u00010%H\u0016J\u0006\u0010&\u001a\u00020\u0016J\u0006\u0010\'\u001a\u00020\u0016R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0007X\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\r\u001a\u00020\u000e8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0010\u0010\u0011\u001a\u0004\b\r\u0010\u000fR7\u0010\u0012\u001a\u001f\u0012\u0013\u0012\u00110\u000e\u00a2\u0006\f\b\u0014\u0012\b\b\u0015\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\u0016\u0018\u00010\u0013X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001a\u00a8\u0006)"}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomAIAECSheetDialog;", "Lio/agora/voice/common/ui/dialog/BaseSheetDialog;", "Lio/agora/scene/voice/databinding/VoiceDialogChatroomAiaecBinding;", "()V", "afterDrawable", "Lcom/github/penfeizhou/animation/apng/APNGDrawable;", "afterSoundId", "", "afterTimer", "Ljava/util/Timer;", "beforeDrawable", "beforeSoundId", "beforeTimer", "isOn", "", "()Z", "isOn$delegate", "Lkotlin/Lazy;", "onClickCheckBox", "Lkotlin/Function1;", "Lkotlin/ParameterName;", "name", "", "getOnClickCheckBox", "()Lkotlin/jvm/functions/Function1;", "setOnClickCheckBox", "(Lkotlin/jvm/functions/Function1;)V", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onDestroy", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "resetTimer", "setupOnClickPlayButton", "Companion", "voice_debug"})
public final class RoomAIAECSheetDialog extends io.agora.voice.common.ui.dialog.BaseSheetDialog<io.agora.scene.voice.databinding.VoiceDialogChatroomAiaecBinding> {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.dialog.RoomAIAECSheetDialog.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_IS_ON = "isOn";
    private final int beforeSoundId = 201001;
    private final int afterSoundId = 201002;
    private final kotlin.Lazy isOn$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onClickCheckBox;
    private com.github.penfeizhou.animation.apng.APNGDrawable beforeDrawable;
    private java.util.Timer beforeTimer;
    private com.github.penfeizhou.animation.apng.APNGDrawable afterDrawable;
    private java.util.Timer afterTimer;
    
    public RoomAIAECSheetDialog() {
        super();
    }
    
    private final boolean isOn() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<java.lang.Boolean, kotlin.Unit> getOnClickCheckBox() {
        return null;
    }
    
    public final void setOnClickCheckBox(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceDialogChatroomAiaecBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    public final void resetTimer() {
    }
    
    public final void setupOnClickPlayButton() {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomAIAECSheetDialog$Companion;", "", "()V", "KEY_IS_ON", "", "voice_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}