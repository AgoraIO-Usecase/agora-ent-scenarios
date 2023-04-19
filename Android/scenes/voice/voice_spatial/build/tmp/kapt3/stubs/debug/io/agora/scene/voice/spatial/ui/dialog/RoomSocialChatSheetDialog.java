package io.agora.scene.voice.spatial.ui.dialog;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u001cB\u0005\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0002J\u000e\u0010\u0004\u001a\u00020\u00002\u0006\u0010\u0004\u001a\u00020\u0005J\u0014\u0010\u0006\u001a\u00020\u00002\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007J\u001a\u0010\u0014\u001a\u00020\u00022\u0006\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0018H\u0014J\u001a\u0010\u0019\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\b\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u0016J\u000e\u0010\u000f\u001a\u00020\u00002\u0006\u0010\u000f\u001a\u00020\u0005R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0006\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\t\u001a\u0004\u0018\u00010\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u000e\u0010\u000f\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomSocialChatSheetDialog;", "Lio/agora/voice/common/ui/dialog/BaseSheetDialog;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialDialogRoomSocialChatBinding;", "()V", "contentText", "", "customers", "", "Lio/agora/scene/voice/spatial/model/CustomerUsageBean;", "onClickSocialChatListener", "Lio/agora/scene/voice/spatial/ui/dialog/RoomSocialChatSheetDialog$OnClickSocialChatListener;", "getOnClickSocialChatListener", "()Lio/agora/scene/voice/spatial/ui/dialog/RoomSocialChatSheetDialog$OnClickSocialChatListener;", "setOnClickSocialChatListener", "(Lio/agora/scene/voice/spatial/ui/dialog/RoomSocialChatSheetDialog$OnClickSocialChatListener;)V", "titleText", "addCustomerMargin", "", "view", "Landroid/view/View;", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "savedInstanceState", "Landroid/os/Bundle;", "OnClickSocialChatListener", "voice_spatial_debug"})
public final class RoomSocialChatSheetDialog extends io.agora.voice.common.ui.dialog.BaseSheetDialog<io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomSocialChatBinding> {
    private java.lang.String titleText = "";
    private java.lang.String contentText = "";
    private java.util.List<io.agora.scene.voice.spatial.model.CustomerUsageBean> customers;
    @org.jetbrains.annotations.Nullable()
    private io.agora.scene.voice.spatial.ui.dialog.RoomSocialChatSheetDialog.OnClickSocialChatListener onClickSocialChatListener;
    
    public RoomSocialChatSheetDialog() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomSocialChatBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void addCustomerMargin(android.view.View view) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.spatial.ui.dialog.RoomSocialChatSheetDialog.OnClickSocialChatListener getOnClickSocialChatListener() {
        return null;
    }
    
    public final void setOnClickSocialChatListener(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.ui.dialog.RoomSocialChatSheetDialog.OnClickSocialChatListener p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.dialog.RoomSocialChatSheetDialog titleText(@org.jetbrains.annotations.NotNull()
    java.lang.String titleText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.dialog.RoomSocialChatSheetDialog contentText(@org.jetbrains.annotations.NotNull()
    java.lang.String contentText) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.dialog.RoomSocialChatSheetDialog customers(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.spatial.model.CustomerUsageBean> customers) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&\u00a8\u0006\u0004"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomSocialChatSheetDialog$OnClickSocialChatListener;", "", "onMoreSound", "", "voice_spatial_debug"})
    public static abstract interface OnClickSocialChatListener {
        
        public abstract void onMoreSound();
    }
}