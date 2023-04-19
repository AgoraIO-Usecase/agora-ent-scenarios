package io.agora.scene.voice.ui.adapter;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\u0018\u00002\u0014\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00040\u0001:\u0001\u0010B1\u0012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00030\u0006\u0012\u000e\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\b\u0012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00040\n\u00a2\u0006\u0002\u0010\u000bJ\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f\u00a8\u0006\u0011"}, d2 = {"Lio/agora/scene/voice/ui/adapter/VoiceRoomSoundSelectionAdapter;", "Lio/agora/voice/common/ui/adapter/BaseRecyclerViewAdapter;", "Lio/agora/scene/voice/databinding/VoiceItemSoundSelectionBinding;", "Lio/agora/scene/voice/model/SoundSelectionBean;", "Lio/agora/scene/voice/ui/adapter/VoiceRoomSoundSelectionAdapter$SoundSelectViewHolder;", "dataList", "", "listener", "Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;", "viewHolderClass", "Ljava/lang/Class;", "(Ljava/util/List;Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;Ljava/lang/Class;)V", "setSelectedPosition", "", "position", "", "SoundSelectViewHolder", "voice_release"})
public final class VoiceRoomSoundSelectionAdapter extends io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter<io.agora.scene.voice.databinding.VoiceItemSoundSelectionBinding, io.agora.scene.voice.model.SoundSelectionBean, io.agora.scene.voice.ui.adapter.VoiceRoomSoundSelectionAdapter.SoundSelectViewHolder> {
    
    public VoiceRoomSoundSelectionAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.model.SoundSelectionBean> dataList, @org.jetbrains.annotations.Nullable()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.SoundSelectionBean> listener, @org.jetbrains.annotations.NotNull()
    java.lang.Class<io.agora.scene.voice.ui.adapter.VoiceRoomSoundSelectionAdapter.SoundSelectViewHolder> viewHolderClass) {
        super(null, null);
    }
    
    public final void setSelectedPosition(int position) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\u0018\u00002\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00030\u0001B\r\u0012\u0006\u0010\u0004\u001a\u00020\u0002\u00a2\u0006\u0002\u0010\u0005J\u001a\u0010\u0004\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\u00032\u0006\u0010\b\u001a\u00020\tH\u0016J\u0018\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\tH\u0002R\u000e\u0010\u0004\u001a\u00020\u0002X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lio/agora/scene/voice/ui/adapter/VoiceRoomSoundSelectionAdapter$SoundSelectViewHolder;", "Lio/agora/voice/common/ui/adapter/BaseRecyclerViewAdapter$BaseViewHolder;", "Lio/agora/scene/voice/databinding/VoiceItemSoundSelectionBinding;", "Lio/agora/scene/voice/model/SoundSelectionBean;", "binding", "(Lio/agora/scene/voice/databinding/VoiceItemSoundSelectionBinding;)V", "", "soundSelectionBean", "selectedIndex", "", "setData", "bean", "selectedPosition", "voice_release"})
    public static final class SoundSelectViewHolder extends io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter.BaseViewHolder<io.agora.scene.voice.databinding.VoiceItemSoundSelectionBinding, io.agora.scene.voice.model.SoundSelectionBean> {
        private final io.agora.scene.voice.databinding.VoiceItemSoundSelectionBinding binding = null;
        
        public SoundSelectViewHolder(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.databinding.VoiceItemSoundSelectionBinding binding) {
            super(null);
        }
        
        @java.lang.Override()
        public void binding(@org.jetbrains.annotations.Nullable()
        io.agora.scene.voice.model.SoundSelectionBean soundSelectionBean, int selectedIndex) {
        }
        
        private final void setData(io.agora.scene.voice.model.SoundSelectionBean bean, int selectedPosition) {
        }
    }
}