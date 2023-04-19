package io.agora.scene.voice.ui.widget.gift;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u00012\u00020\u0003B\u0005\u00a2\u0006\u0002\u0010\u0004J\u001a\u0010\r\u001a\u00020\u00022\u0006\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0014J\b\u0010\u0012\u001a\u00020\u0013H\u0002J\u0018\u0010\u0014\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u000b\u001a\u00020\fH\u0016J\b\u0010\u0017\u001a\u00020\u0013H\u0016J\u001a\u0010\u0018\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u00162\b\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0016J\u0010\u0010\u001b\u001a\u00020\u00132\b\u0010\t\u001a\u0004\u0018\u00010\nR\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lio/agora/scene/voice/ui/widget/gift/LiveGiftListFragment;", "Lio/agora/voice/common/ui/BaseUiFragment;", "Lio/agora/scene/voice/databinding/VoiceFragmentGiftListLayoutBinding;", "Lio/agora/voice/common/ui/adapter/listener/OnAdapterItemClickListener;", "()V", "adapter", "Lio/agora/scene/voice/ui/widget/gift/GiftListAdapter;", "giftBean", "Lio/agora/scene/voice/model/GiftBean;", "listener", "Lio/agora/scene/voice/ui/widget/gift/OnConfirmClickListener;", "position", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "initView", "", "onItemClick", "view", "Landroid/view/View;", "onResume", "onViewCreated", "savedInstanceState", "Landroid/os/Bundle;", "setOnItemSelectClickListener", "voice_debug"})
public final class LiveGiftListFragment extends io.agora.voice.common.ui.BaseUiFragment<io.agora.scene.voice.databinding.VoiceFragmentGiftListLayoutBinding> implements io.agora.voice.common.ui.adapter.listener.OnAdapterItemClickListener {
    private io.agora.scene.voice.ui.widget.gift.GiftListAdapter adapter;
    private io.agora.scene.voice.model.GiftBean giftBean;
    private io.agora.scene.voice.ui.widget.gift.OnConfirmClickListener listener;
    private int position = 0;
    
    public LiveGiftListFragment() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceFragmentGiftListLayoutBinding getViewBinding(@org.jetbrains.annotations.NotNull()
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
    
    @java.lang.Override()
    public void onItemClick(@org.jetbrains.annotations.NotNull()
    android.view.View view, int position) {
    }
    
    public final void setOnItemSelectClickListener(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.ui.widget.gift.OnConfirmClickListener listener) {
    }
}