package io.agora.scene.voice.spatial.ui.fragment;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010$\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u00012\u00020\u0003B\u0005\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u001c\u001a\u00020\u001dH\u0002J\u001a\u0010\u001e\u001a\u00020\u00022\u0006\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\"H\u0014J\b\u0010#\u001a\u00020\u001dH\u0002J\b\u0010$\u001a\u00020\u001dH\u0002J\b\u0010%\u001a\u00020\u001dH\u0002J\u001a\u0010&\u001a\u00020\u001d2\u0012\u0010\'\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u00140(J&\u0010)\u001a\u0004\u0018\u00010\r2\u0006\u0010\u001f\u001a\u00020 2\b\u0010!\u001a\u0004\u0018\u00010\"2\b\u0010*\u001a\u0004\u0018\u00010+H\u0016J\b\u0010,\u001a\u00020\u001dH\u0016J \u0010-\u001a\u00020\u001d2\u0006\u0010.\u001a\u00020\r2\u0006\u0010/\u001a\u00020\u000f2\u0006\u00100\u001a\u00020\u0014H\u0016J\b\u00101\u001a\u00020\u001dH\u0016J\u001a\u00102\u001a\u00020\u001d2\u0006\u0010.\u001a\u00020\r2\b\u0010*\u001a\u0004\u0018\u00010+H\u0016J\u0006\u00103\u001a\u00020\u001dJ\u0010\u00104\u001a\u00020\u001d2\b\u00105\u001a\u0004\u0018\u00010\u0016J\u000e\u00106\u001a\u00020\u001d2\u0006\u0010\u000e\u001a\u00020\u000fR\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\t\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00110\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00067"}, d2 = {"Lio/agora/scene/voice/spatial/ui/fragment/ChatroomInviteHandsFragment;", "Lio/agora/voice/common/ui/BaseUiFragment;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialFragmentHandsListLayoutBinding;", "Lio/agora/scene/voice/spatial/ui/adapter/ChatroomInviteAdapter$onActionListener;", "()V", "adapter", "Lio/agora/scene/voice/spatial/ui/adapter/ChatroomInviteAdapter;", "baseAdapter", "Lio/agora/voice/common/ui/adapter/RoomBaseRecyclerViewAdapter;", "Lio/agora/scene/voice/spatial/model/VoiceMemberModel;", "dataList", "", "emptyView", "Landroid/view/View;", "index", "", "isRefreshing", "", "map", "", "", "onFragmentListener", "Lio/agora/scene/voice/spatial/ui/dialog/ChatroomHandsDialog$OnFragmentListener;", "roomId", "userListViewModel", "Lio/agora/scene/voice/spatial/viewmodel/VoiceUserListViewModel;", "voiceServiceProtocol", "Lio/agora/scene/voice/spatial/service/VoiceServiceProtocol;", "finishRefresh", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "initListener", "initView", "initViewModel", "micChanged", "data", "", "onCreateView", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onItemActionClick", "view", "position", "userId", "onResume", "onViewCreated", "reset", "setFragmentListener", "listener", "setIndex", "voice_spatial_release"})
public final class ChatroomInviteHandsFragment extends io.agora.voice.common.ui.BaseUiFragment<io.agora.scene.voice.spatial.databinding.VoiceSpatialFragmentHandsListLayoutBinding> implements io.agora.scene.voice.spatial.ui.adapter.ChatroomInviteAdapter.onActionListener {
    private io.agora.scene.voice.spatial.viewmodel.VoiceUserListViewModel userListViewModel;
    private final java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel> dataList = null;
    private io.agora.voice.common.ui.adapter.RoomBaseRecyclerViewAdapter<io.agora.scene.voice.spatial.model.VoiceMemberModel> baseAdapter;
    private io.agora.scene.voice.spatial.ui.adapter.ChatroomInviteAdapter adapter;
    private io.agora.scene.voice.spatial.ui.dialog.ChatroomHandsDialog.OnFragmentListener onFragmentListener;
    private java.lang.String roomId;
    private final java.util.Map<java.lang.String, java.lang.Boolean> map = null;
    private boolean isRefreshing = false;
    private android.view.View emptyView;
    private final io.agora.scene.voice.spatial.service.VoiceServiceProtocol voiceServiceProtocol = null;
    private int index = 0;
    
    public ChatroomInviteHandsFragment() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    @java.lang.Override()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.spatial.databinding.VoiceSpatialFragmentHandsListLayoutBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initView() {
    }
    
    @java.lang.Override()
    public void onResume() {
    }
    
    private final void initViewModel() {
    }
    
    private final void initListener() {
    }
    
    private final void finishRefresh() {
    }
    
    public final void reset() {
    }
    
    @java.lang.Override()
    public void onItemActionClick(@org.jetbrains.annotations.NotNull()
    android.view.View view, int position, @org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
    }
    
    public final void setIndex(int index) {
    }
    
    public final void setFragmentListener(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.ui.dialog.ChatroomHandsDialog.OnFragmentListener listener) {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    public final void micChanged(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.Integer, java.lang.String> data) {
    }
}