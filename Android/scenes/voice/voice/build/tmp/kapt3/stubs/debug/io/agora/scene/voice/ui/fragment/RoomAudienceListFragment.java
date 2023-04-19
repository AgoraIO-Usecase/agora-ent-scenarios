package io.agora.scene.voice.ui.fragment;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010!\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 *2\b\u0012\u0004\u0012\u00020\u00020\u00012\u00020\u0003:\u0001*B\u0005\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0014\u001a\u00020\u0015H\u0002J\u001a\u0010\u0016\u001a\u00020\u00022\u0006\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0014J \u0010\u001b\u001a\u00020\u00152\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u00112\u0006\u0010\u001f\u001a\u00020\u0011H\u0002J\u0010\u0010 \u001a\u00020\u00152\u0006\u0010!\u001a\u00020\"H\u0002J\b\u0010#\u001a\u00020\u0015H\u0002J\b\u0010$\u001a\u00020\u0015H\u0016J\u001a\u0010%\u001a\u00020\u00152\u0006\u0010&\u001a\u00020\'2\b\u0010(\u001a\u0004\u0018\u00010)H\u0016R\"\u0010\u0005\u001a\u0016\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\b0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lio/agora/scene/voice/ui/fragment/RoomAudienceListFragment;", "Lio/agora/voice/common/ui/BaseUiFragment;", "Lio/agora/scene/voice/databinding/VoiceFragmentAudienceListBinding;", "Landroidx/swiperefreshlayout/widget/SwipeRefreshLayout$OnRefreshListener;", "()V", "audienceAdapter", "Lio/agora/voice/common/ui/adapter/BaseRecyclerViewAdapter;", "Lio/agora/scene/voice/databinding/VoiceItemRoomAudienceListBinding;", "Lio/agora/scene/voice/model/VoiceMemberModel;", "Lio/agora/scene/voice/ui/adapter/viewholder/RoomAudienceListViewHolder;", "isEnd", "", "members", "", "roomKitBean", "Lio/agora/scene/voice/model/RoomKitBean;", "total", "", "userListViewModel", "Lio/agora/scene/voice/viewmodel/VoiceUserListViewModel;", "checkEmpty", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "handleRequest", "uid", "", "action", "position", "initAdapter", "recyclerView", "Landroidx/recyclerview/widget/RecyclerView;", "onObservable", "onRefresh", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "Companion", "voice_debug"})
public final class RoomAudienceListFragment extends io.agora.voice.common.ui.BaseUiFragment<io.agora.scene.voice.databinding.VoiceFragmentAudienceListBinding> implements androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.fragment.RoomAudienceListFragment.Companion Companion = null;
    private static final java.lang.String KEY_ROOM_INFO = "room_info";
    private static final io.agora.scene.voice.service.VoiceServiceProtocol voiceServiceProtocol = null;
    private io.agora.scene.voice.model.RoomKitBean roomKitBean;
    private io.agora.scene.voice.viewmodel.VoiceUserListViewModel userListViewModel;
    private int total = 0;
    private boolean isEnd = false;
    private final java.util.List<io.agora.scene.voice.model.VoiceMemberModel> members = null;
    private io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter<io.agora.scene.voice.databinding.VoiceItemRoomAudienceListBinding, io.agora.scene.voice.model.VoiceMemberModel, io.agora.scene.voice.ui.adapter.viewholder.RoomAudienceListViewHolder> audienceAdapter;
    
    public RoomAudienceListFragment() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceFragmentAudienceListBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void checkEmpty() {
    }
    
    private final void initAdapter(androidx.recyclerview.widget.RecyclerView recyclerView) {
    }
    
    private final void onObservable() {
    }
    
    @java.lang.Override()
    public void onRefresh() {
    }
    
    private final void handleRequest(java.lang.String uid, @io.agora.scene.voice.model.annotation.MicClickAction()
    int action, int position) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lio/agora/scene/voice/ui/fragment/RoomAudienceListFragment$Companion;", "", "()V", "KEY_ROOM_INFO", "", "voiceServiceProtocol", "Lio/agora/scene/voice/service/VoiceServiceProtocol;", "getInstance", "Lio/agora/scene/voice/ui/fragment/RoomAudienceListFragment;", "roomKitBean", "Lio/agora/scene/voice/model/RoomKitBean;", "voice_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final io.agora.scene.voice.ui.fragment.RoomAudienceListFragment getInstance(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.model.RoomKitBean roomKitBean) {
            return null;
        }
    }
}