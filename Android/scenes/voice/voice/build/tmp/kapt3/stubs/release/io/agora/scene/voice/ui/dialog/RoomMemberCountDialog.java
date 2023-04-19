package io.agora.scene.voice.ui.dialog;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010!\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001$B\r\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005J\b\u0010\u0011\u001a\u00020\u0012H\u0002J\u001a\u0010\u0013\u001a\u00020\u00022\u0006\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0014J\u0010\u0010\u0018\u001a\u00020\u00122\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J\u0006\u0010\u001b\u001a\u00020\u0012J\u000e\u0010\u001c\u001a\u00020\u00122\u0006\u0010\u001d\u001a\u00020\fJ\b\u0010\u001e\u001a\u00020\u0012H\u0016J\u001a\u0010\u001f\u001a\u00020\u00122\u0006\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0016R\"\u0010\u0006\u001a\u0016\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\t0\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomMemberCountDialog;", "Lio/agora/voice/common/ui/dialog/BaseFixedHeightSheetDialog;", "Lio/agora/scene/voice/databinding/VoiceDialogRoomSoundSelectionBinding;", "onClickKickListener", "Lio/agora/scene/voice/ui/dialog/RoomMemberCountDialog$OnClickKickMemberListener;", "(Lio/agora/scene/voice/ui/dialog/RoomMemberCountDialog$OnClickKickMemberListener;)V", "adapter", "Lio/agora/voice/common/ui/adapter/BaseRecyclerViewAdapter;", "Lio/agora/scene/voice/databinding/VoiceItemHandsRaisedBinding;", "Lio/agora/scene/voice/model/VoiceMemberModel;", "Lio/agora/scene/voice/ui/adapter/viewholder/RoomMemberCountViewHolder;", "currentMemberCount", "", "isFirst", "", "roomMemberList", "", "checkData", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "initAdapter", "recyclerView", "Landroidx/recyclerview/widget/RecyclerView;", "notifyItemAddRefresh", "notifyItemRemovedRefresh", "index", "onStart", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "OnClickKickMemberListener", "voice_release"})
public final class RoomMemberCountDialog extends io.agora.voice.common.ui.dialog.BaseFixedHeightSheetDialog<io.agora.scene.voice.databinding.VoiceDialogRoomSoundSelectionBinding> {
    private final io.agora.scene.voice.ui.dialog.RoomMemberCountDialog.OnClickKickMemberListener onClickKickListener = null;
    private java.util.List<io.agora.scene.voice.model.VoiceMemberModel> roomMemberList;
    private io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter<io.agora.scene.voice.databinding.VoiceItemHandsRaisedBinding, io.agora.scene.voice.model.VoiceMemberModel, io.agora.scene.voice.ui.adapter.viewholder.RoomMemberCountViewHolder> adapter;
    private int currentMemberCount = 0;
    private boolean isFirst = true;
    
    public RoomMemberCountDialog(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.ui.dialog.RoomMemberCountDialog.OnClickKickMemberListener onClickKickListener) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceDialogRoomSoundSelectionBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initAdapter(androidx.recyclerview.widget.RecyclerView recyclerView) {
    }
    
    public final void notifyItemRemovedRefresh(int index) {
    }
    
    public final void notifyItemAddRefresh() {
    }
    
    @java.lang.Override()
    public void onStart() {
    }
    
    private final void checkData() {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H&\u00a8\u0006\b"}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomMemberCountDialog$OnClickKickMemberListener;", "", "onKickMember", "", "member", "Lio/agora/scene/voice/model/VoiceMemberModel;", "index", "", "voice_release"})
    public static abstract interface OnClickKickMemberListener {
        
        public abstract void onKickMember(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.model.VoiceMemberModel member, int index);
    }
}