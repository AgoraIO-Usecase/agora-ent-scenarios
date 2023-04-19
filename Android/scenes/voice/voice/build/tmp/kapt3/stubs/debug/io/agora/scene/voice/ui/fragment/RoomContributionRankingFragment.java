package io.agora.scene.voice.ui.fragment;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000  2\b\u0012\u0004\u0012\u00020\u00020\u00012\u00020\u0003:\u0001 B\u0005\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u001a\u0010\u0012\u001a\u00020\u00022\u0006\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0014J\u0010\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\b\u0010\u001a\u001a\u00020\u000fH\u0016J\u001a\u0010\u001b\u001a\u00020\u000f2\u0006\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016R\"\u0010\u0005\u001a\u0016\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lio/agora/scene/voice/ui/fragment/RoomContributionRankingFragment;", "Lio/agora/voice/common/ui/BaseUiFragment;", "Lio/agora/scene/voice/databinding/VoiceFragmentContributionRankingBinding;", "Landroidx/swiperefreshlayout/widget/SwipeRefreshLayout$OnRefreshListener;", "()V", "contributionAdapter", "Lio/agora/voice/common/ui/adapter/BaseRecyclerViewAdapter;", "Lio/agora/scene/voice/databinding/VoiceItemContributionRankingBinding;", "Lio/agora/scene/voice/model/VoiceRankUserModel;", "Lio/agora/scene/voice/ui/adapter/viewholder/RoomContributionRankingViewHolder;", "roomKitBean", "Lio/agora/scene/voice/model/RoomKitBean;", "roomRankViewModel", "Lio/agora/scene/voice/viewmodel/VoiceUserListViewModel;", "checkEmpty", "", "total", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "initAdapter", "recyclerView", "Landroidx/recyclerview/widget/RecyclerView;", "onRefresh", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "Companion", "voice_debug"})
public final class RoomContributionRankingFragment extends io.agora.voice.common.ui.BaseUiFragment<io.agora.scene.voice.databinding.VoiceFragmentContributionRankingBinding> implements androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.fragment.RoomContributionRankingFragment.Companion Companion = null;
    private static final java.lang.String KEY_ROOM_INFO = "room_info";
    private io.agora.scene.voice.model.RoomKitBean roomKitBean;
    private io.agora.scene.voice.viewmodel.VoiceUserListViewModel roomRankViewModel;
    private io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter<io.agora.scene.voice.databinding.VoiceItemContributionRankingBinding, io.agora.scene.voice.model.VoiceRankUserModel, io.agora.scene.voice.ui.adapter.viewholder.RoomContributionRankingViewHolder> contributionAdapter;
    
    public RoomContributionRankingFragment() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceFragmentContributionRankingBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void checkEmpty(int total) {
    }
    
    private final void initAdapter(androidx.recyclerview.widget.RecyclerView recyclerView) {
    }
    
    @java.lang.Override()
    public void onRefresh() {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lio/agora/scene/voice/ui/fragment/RoomContributionRankingFragment$Companion;", "", "()V", "KEY_ROOM_INFO", "", "getInstance", "Lio/agora/scene/voice/ui/fragment/RoomContributionRankingFragment;", "roomKitBean", "Lio/agora/scene/voice/model/RoomKitBean;", "voice_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final io.agora.scene.voice.ui.fragment.RoomContributionRankingFragment getInstance(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.model.RoomKitBean roomKitBean) {
            return null;
        }
    }
}