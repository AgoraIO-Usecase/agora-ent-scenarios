package io.agora.scene.voice.ui.dialog;

import java.lang.System;

/**
 * @author create by zhangwei03
 *
 * 公告
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 $2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0002$%B\u0005\u00a2\u0006\u0002\u0010\u0003J\u000e\u0010\u000f\u001a\u00020\u00002\u0006\u0010\u000f\u001a\u00020\u0006J\u001a\u0010\u0016\u001a\u00020\u00022\u0006\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0014J\u0010\u0010\u001b\u001a\u00020\n2\u0006\u0010\u001c\u001a\u00020\u001dH\u0002J\u001a\u0010\u001e\u001a\u00020\n2\u0006\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\"H\u0016J\u0010\u0010#\u001a\u00020\n2\u0006\u0010\u001c\u001a\u00020\u001dH\u0002R7\u0010\u0004\u001a\u001f\u0012\u0013\u0012\u00110\u0006\u00a2\u0006\f\b\u0007\u0012\b\b\b\u0012\u0004\b\b(\t\u0012\u0004\u0012\u00020\n\u0018\u00010\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u000e\u0010\u000f\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0010\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\u0015\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006&"}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomNoticeSheetDialog;", "Lio/agora/voice/common/ui/dialog/BaseSheetDialog;", "Lio/agora/scene/voice/databinding/VoiceDialogRoomNoticeBinding;", "()V", "confirmCallback", "Lkotlin/Function1;", "", "Lkotlin/ParameterName;", "name", "str", "", "getConfirmCallback", "()Lkotlin/jvm/functions/Function1;", "setConfirmCallback", "(Lkotlin/jvm/functions/Function1;)V", "contentText", "roomKitBean", "Lio/agora/scene/voice/model/RoomKitBean;", "getRoomKitBean", "()Lio/agora/scene/voice/model/RoomKitBean;", "roomKitBean$delegate", "Lkotlin/Lazy;", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "hideKeyboard", "editText", "Landroid/widget/EditText;", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "showKeyboard", "Companion", "NameLengthFilter", "voice_debug"})
public final class RoomNoticeSheetDialog extends io.agora.voice.common.ui.dialog.BaseSheetDialog<io.agora.scene.voice.databinding.VoiceDialogRoomNoticeBinding> {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.dialog.RoomNoticeSheetDialog.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_ROOM_KIT_BEAN = "room_kit_bean";
    private final kotlin.Lazy roomKitBean$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> confirmCallback;
    private java.lang.String contentText = "";
    
    public RoomNoticeSheetDialog() {
        super();
    }
    
    private final io.agora.scene.voice.model.RoomKitBean getRoomKitBean() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> getConfirmCallback() {
        return null;
    }
    
    public final void setConfirmCallback(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceDialogRoomNoticeBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void showKeyboard(android.widget.EditText editText) {
    }
    
    private final void hideKeyboard(android.widget.EditText editText) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.dialog.RoomNoticeSheetDialog contentText(@org.jetbrains.annotations.NotNull()
    java.lang.String contentText) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\r\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0004\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J8\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u0003H\u0016J\u0010\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u0006H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomNoticeSheetDialog$NameLengthFilter;", "Landroid/text/InputFilter;", "maxEn", "", "(Lio/agora/scene/voice/ui/dialog/RoomNoticeSheetDialog;I)V", "regEx", "", "filter", "", "source", "start", "end", "dest", "Landroid/text/Spanned;", "dstart", "dend", "getChineseCount", "str", "voice_debug"})
    public final class NameLengthFilter implements android.text.InputFilter {
        private final int maxEn = 0;
        private final java.lang.String regEx = "[\\u4e00-\\u9fa5]";
        
        public NameLengthFilter(int maxEn) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        @java.lang.Override()
        public java.lang.CharSequence filter(@org.jetbrains.annotations.NotNull()
        java.lang.CharSequence source, int start, int end, @org.jetbrains.annotations.NotNull()
        android.text.Spanned dest, int dstart, int dend) {
            return null;
        }
        
        private final int getChineseCount(java.lang.String str) {
            return 0;
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomNoticeSheetDialog$Companion;", "", "()V", "KEY_ROOM_KIT_BEAN", "", "voice_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}