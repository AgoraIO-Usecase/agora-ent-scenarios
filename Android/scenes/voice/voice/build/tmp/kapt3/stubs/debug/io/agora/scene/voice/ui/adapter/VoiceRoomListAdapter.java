package io.agora.scene.voice.ui.adapter;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u0014\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00040\u0001:\u0001\fB3\u0012\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u0006\u0012\u000e\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\b\u0012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00040\n\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\r"}, d2 = {"Lio/agora/scene/voice/ui/adapter/VoiceRoomListAdapter;", "Lio/agora/voice/common/ui/adapter/BaseRecyclerViewAdapter;", "Lio/agora/scene/voice/databinding/VoiceFragmentRoomItemLayoutBinding;", "Lio/agora/scene/voice/model/VoiceRoomModel;", "Lio/agora/scene/voice/ui/adapter/VoiceRoomListAdapter$VoiceRoomListViewHolder;", "dataList", "", "listener", "Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;", "viewHolderClass", "Ljava/lang/Class;", "(Ljava/util/List;Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;Ljava/lang/Class;)V", "VoiceRoomListViewHolder", "voice_debug"})
public final class VoiceRoomListAdapter extends io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter<io.agora.scene.voice.databinding.VoiceFragmentRoomItemLayoutBinding, io.agora.scene.voice.model.VoiceRoomModel, io.agora.scene.voice.ui.adapter.VoiceRoomListAdapter.VoiceRoomListViewHolder> {
    
    public VoiceRoomListAdapter(@org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.model.VoiceRoomModel> dataList, @org.jetbrains.annotations.Nullable()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.VoiceRoomModel> listener, @org.jetbrains.annotations.NotNull()
    java.lang.Class<io.agora.scene.voice.ui.adapter.VoiceRoomListAdapter.VoiceRoomListViewHolder> viewHolderClass) {
        super(null, null);
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\u0018\u00002\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00030\u0001B\r\u0012\u0006\u0010\u0004\u001a\u00020\u0002\u00a2\u0006\u0002\u0010\u0005J\u001a\u0010\u0004\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\u00032\u0006\u0010\b\u001a\u00020\tH\u0016J\u0010\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\tH\u0002J\u0018\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u0010\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0012H\u0002R\u000e\u0010\u0004\u001a\u00020\u0002X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lio/agora/scene/voice/ui/adapter/VoiceRoomListAdapter$VoiceRoomListViewHolder;", "Lio/agora/voice/common/ui/adapter/BaseRecyclerViewAdapter$BaseViewHolder;", "Lio/agora/scene/voice/databinding/VoiceFragmentRoomItemLayoutBinding;", "Lio/agora/scene/voice/model/VoiceRoomModel;", "binding", "(Lio/agora/scene/voice/databinding/VoiceFragmentRoomItemLayoutBinding;)V", "", "data", "selectedIndex", "", "itemType", "type", "setData", "item", "context", "Landroid/content/Context;", "showPrivate", "isShow", "", "voice_debug"})
    public static final class VoiceRoomListViewHolder extends io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter.BaseViewHolder<io.agora.scene.voice.databinding.VoiceFragmentRoomItemLayoutBinding, io.agora.scene.voice.model.VoiceRoomModel> {
        private final io.agora.scene.voice.databinding.VoiceFragmentRoomItemLayoutBinding binding = null;
        
        public VoiceRoomListViewHolder(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.databinding.VoiceFragmentRoomItemLayoutBinding binding) {
            super(null);
        }
        
        @java.lang.Override()
        public void binding(@org.jetbrains.annotations.Nullable()
        io.agora.scene.voice.model.VoiceRoomModel data, int selectedIndex) {
        }
        
        private final void setData(io.agora.scene.voice.model.VoiceRoomModel item, android.content.Context context) {
        }
        
        private final void itemType(int type) {
        }
        
        private final void showPrivate(boolean isShow) {
        }
    }
}