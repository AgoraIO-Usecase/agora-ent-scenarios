package io.agora.scene.voice.spatial.ui.fragment;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u00012\u00020\u0003:\u0001+B\u0005\u00a2\u0006\u0002\u0010\u0004J\u001a\u0010\u0016\u001a\u00020\u00022\u0006\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0014J\b\u0010\u001b\u001a\u00020\rH\u0002J\u0010\u0010\u001c\u001a\u00020\r2\u0006\u0010\u001d\u001a\u00020\u0006H\u0002J\u0010\u0010\u001e\u001a\u00020\r2\u0006\u0010\u001f\u001a\u00020 H\u0002J\u0010\u0010!\u001a\u00020\r2\u0006\u0010\u001d\u001a\u00020\u0006H\u0002J\b\u0010\"\u001a\u00020\rH\u0016J\b\u0010#\u001a\u00020\rH\u0016J\u001a\u0010$\u001a\u00020\r2\u0006\u0010%\u001a\u00020&2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0016J\u0010\u0010)\u001a\u00020\r2\u0006\u0010\u001d\u001a\u00020\u0006H\u0002J\b\u0010*\u001a\u00020\rH\u0002R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R7\u0010\u0007\u001a\u001f\u0012\u0013\u0012\u00110\t\u00a2\u0006\f\b\n\u0012\b\b\u000b\u0012\u0004\b\b(\f\u0012\u0004\u0012\u00020\r\u0018\u00010\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lio/agora/scene/voice/spatial/ui/fragment/VoiceRoomListFragment;", "Lio/agora/voice/common/ui/BaseUiFragment;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialFragmentRoomListLayoutBinding;", "Landroidx/swiperefreshlayout/widget/SwipeRefreshLayout$OnRefreshListener;", "()V", "curVoiceRoomModel", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "itemCountListener", "Lkotlin/Function1;", "", "Lkotlin/ParameterName;", "name", "count", "", "getItemCountListener", "()Lkotlin/jvm/functions/Function1;", "setItemCountListener", "(Lkotlin/jvm/functions/Function1;)V", "listAdapter", "Lio/agora/scene/voice/spatial/ui/adapter/VoiceRoomListAdapter;", "voiceRoomViewModel", "Lio/agora/scene/voice/spatial/viewmodel/VoiceCreateViewModel;", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "goChatroomPage", "gotoJoinRoom", "voiceRoomModel", "initAdapter", "recyclerView", "Lio/agora/scene/voice/spatial/ui/widget/recyclerview/EmptyRecyclerView;", "onItemClick", "onRefresh", "onResume", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "showInputDialog", "voiceRoomObservable", "BottomOffsetDecoration", "voice_spatial_release"})
public final class VoiceRoomListFragment extends io.agora.voice.common.ui.BaseUiFragment<io.agora.scene.voice.spatial.databinding.VoiceSpatialFragmentRoomListLayoutBinding> implements androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {
    private io.agora.scene.voice.spatial.viewmodel.VoiceCreateViewModel voiceRoomViewModel;
    private io.agora.scene.voice.spatial.ui.adapter.VoiceRoomListAdapter listAdapter;
    private io.agora.scene.voice.spatial.model.VoiceRoomModel curVoiceRoomModel;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> itemCountListener;
    
    public VoiceRoomListFragment() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<java.lang.Integer, kotlin.Unit> getItemCountListener() {
        return null;
    }
    
    public final void setItemCountListener(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.spatial.databinding.VoiceSpatialFragmentRoomListLayoutBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initAdapter(io.agora.scene.voice.spatial.ui.widget.recyclerview.EmptyRecyclerView recyclerView) {
    }
    
    @java.lang.Override()
    public void onResume() {
    }
    
    private final void voiceRoomObservable() {
    }
    
    private final void gotoJoinRoom(io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel) {
    }
    
    private final void onItemClick(io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel) {
    }
    
    private final void goChatroomPage() {
    }
    
    private final void showInputDialog(io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel) {
    }
    
    @java.lang.Override()
    public void onRefresh() {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J(\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lio/agora/scene/voice/spatial/ui/fragment/VoiceRoomListFragment$BottomOffsetDecoration;", "Landroidx/recyclerview/widget/RecyclerView$ItemDecoration;", "mBottomOffset", "", "(I)V", "getItemOffsets", "", "outRect", "Landroid/graphics/Rect;", "view", "Landroid/view/View;", "parent", "Landroidx/recyclerview/widget/RecyclerView;", "state", "Landroidx/recyclerview/widget/RecyclerView$State;", "voice_spatial_release"})
    public static final class BottomOffsetDecoration extends androidx.recyclerview.widget.RecyclerView.ItemDecoration {
        private final int mBottomOffset = 0;
        
        public BottomOffsetDecoration(int mBottomOffset) {
            super();
        }
        
        @java.lang.Override()
        public void getItemOffsets(@org.jetbrains.annotations.NotNull()
        android.graphics.Rect outRect, @org.jetbrains.annotations.NotNull()
        android.view.View view, @org.jetbrains.annotations.NotNull()
        androidx.recyclerview.widget.RecyclerView parent, @org.jetbrains.annotations.NotNull()
        androidx.recyclerview.widget.RecyclerView.State state) {
        }
    }
}