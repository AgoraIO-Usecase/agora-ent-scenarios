package io.agora.scene.voice.ui.fragment;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\f\u0018\u0000 02\b\u0012\u0004\u0012\u00020\u00020\u00012\u00020\u0003:\u00010B\u0005\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0019\u001a\u00020\u001aH\u0002J\u001c\u0010\u001b\u001a\u0004\u0018\u00010\u00022\u0006\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0014J\b\u0010 \u001a\u00020\u001aH\u0002J\b\u0010!\u001a\u00020\u001aH\u0002J\b\u0010\"\u001a\u00020\u001aH\u0002J&\u0010#\u001a\u0004\u0018\u00010\r2\u0006\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001f2\b\u0010$\u001a\u0004\u0018\u00010%H\u0016J\b\u0010&\u001a\u00020\u001aH\u0016J \u0010\'\u001a\u00020\u001a2\u0006\u0010(\u001a\u00020\r2\u0006\u0010)\u001a\u00020\u000b2\u0006\u0010*\u001a\u00020\u0013H\u0016J\b\u0010+\u001a\u00020\u001aH\u0016J\u001a\u0010,\u001a\u00020\u001a2\u0006\u0010(\u001a\u00020\r2\b\u0010$\u001a\u0004\u0018\u00010%H\u0016J\u0006\u0010-\u001a\u00020\u001aJ\u0010\u0010.\u001a\u00020\u001a2\b\u0010/\u001a\u0004\u0018\u00010\u0015R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\t\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u000f0\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u00061"}, d2 = {"Lio/agora/scene/voice/ui/fragment/ChatroomRaisedHandsFragment;", "Lio/agora/voice/common/ui/BaseUiFragment;", "Lio/agora/scene/voice/databinding/VoiceFragmentHandsListLayoutBinding;", "Lio/agora/scene/voice/ui/adapter/ChatroomRaisedAdapter$onActionListener;", "()V", "adapter", "Lio/agora/scene/voice/ui/adapter/ChatroomRaisedAdapter;", "baseAdapter", "Lio/agora/voice/common/ui/adapter/RoomBaseRecyclerViewAdapter;", "Lio/agora/scene/voice/model/VoiceMemberModel;", "currentIndex", "", "emptyView", "Landroid/view/View;", "isLoadingNextPage", "", "isRefreshing", "map", "", "", "onFragmentListener", "Lio/agora/scene/voice/ui/dialog/ChatroomHandsDialog$OnFragmentListener;", "roomId", "userListViewModel", "Lio/agora/scene/voice/viewmodel/VoiceUserListViewModel;", "finishRefresh", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "initListener", "initView", "initViewModel", "onCreateView", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onItemActionClick", "view", "index", "uid", "onResume", "onViewCreated", "reset", "setFragmentListener", "listener", "Companion", "voice_release"})
public final class ChatroomRaisedHandsFragment extends io.agora.voice.common.ui.BaseUiFragment<io.agora.scene.voice.databinding.VoiceFragmentHandsListLayoutBinding> implements io.agora.scene.voice.ui.adapter.ChatroomRaisedAdapter.onActionListener {
    private io.agora.scene.voice.viewmodel.VoiceUserListViewModel userListViewModel;
    private io.agora.voice.common.ui.adapter.RoomBaseRecyclerViewAdapter<io.agora.scene.voice.model.VoiceMemberModel> baseAdapter;
    private io.agora.scene.voice.ui.adapter.ChatroomRaisedAdapter adapter;
    private io.agora.scene.voice.ui.dialog.ChatroomHandsDialog.OnFragmentListener onFragmentListener;
    private java.lang.String roomId;
    private final java.util.Map<java.lang.String, java.lang.Boolean> map = null;
    private boolean isRefreshing = false;
    private boolean isLoadingNextPage = false;
    private android.view.View emptyView;
    private int currentIndex = 0;
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.fragment.ChatroomRaisedHandsFragment.Companion Companion = null;
    private static final java.lang.String TAG = "ChatroomRaisedHandsFragment";
    
    public ChatroomRaisedHandsFragment() {
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
    
    @org.jetbrains.annotations.Nullable()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceFragmentHandsListLayoutBinding getViewBinding(@org.jetbrains.annotations.NotNull()
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
    android.view.View view, int index, @org.jetbrains.annotations.NotNull()
    java.lang.String uid) {
    }
    
    public final void setFragmentListener(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.ui.dialog.ChatroomHandsDialog.OnFragmentListener listener) {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lio/agora/scene/voice/ui/fragment/ChatroomRaisedHandsFragment$Companion;", "", "()V", "TAG", "", "voice_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}