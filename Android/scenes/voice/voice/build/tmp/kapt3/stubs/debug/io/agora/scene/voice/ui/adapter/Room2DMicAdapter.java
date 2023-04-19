package io.agora.scene.voice.ui.adapter;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\b\u0004\u0018\u00002\u0014\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00040\u0001B3\u0012\u000e\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\u0006\u0012\u000e\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\b\u0012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00040\n\u00a2\u0006\u0002\u0010\u000bJ\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u0010H\u0016J\u000e\u0010\u0011\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u0003J\u001a\u0010\u0011\u001a\u00020\r2\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00030\u0014J\u0016\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0016\u001a\u00020\u00102\u0006\u0010\u0017\u001a\u00020\u0010\u00a8\u0006\u0018"}, d2 = {"Lio/agora/scene/voice/ui/adapter/Room2DMicAdapter;", "Lio/agora/voice/common/ui/adapter/BaseRecyclerViewAdapter;", "Lio/agora/scene/voice/databinding/VoiceItemRoom2dMicBinding;", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "Lio/agora/scene/voice/ui/adapter/viewholder/Room2DMicViewHolder;", "dataList", "", "listener", "Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;", "viewHolderClass", "Ljava/lang/Class;", "(Ljava/util/List;Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;Ljava/lang/Class;)V", "onBindViewHolder", "", "holder", "position", "", "onSeatUpdated", "micInfoModel", "newMicMap", "", "updateVolume", "index", "volume", "voice_debug"})
public final class Room2DMicAdapter extends io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter<io.agora.scene.voice.databinding.VoiceItemRoom2dMicBinding, io.agora.scene.voice.model.VoiceMicInfoModel, io.agora.scene.voice.ui.adapter.viewholder.Room2DMicViewHolder> {
    
    public Room2DMicAdapter(@org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> dataList, @org.jetbrains.annotations.Nullable()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.VoiceMicInfoModel> listener, @org.jetbrains.annotations.NotNull()
    java.lang.Class<io.agora.scene.voice.ui.adapter.viewholder.Room2DMicViewHolder> viewHolderClass) {
        super(null, null);
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.ui.adapter.viewholder.Room2DMicViewHolder holder, int position) {
    }
    
    public final void onSeatUpdated(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel> newMicMap) {
    }
    
    public final void onSeatUpdated(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceMicInfoModel micInfoModel) {
    }
    
    public final void updateVolume(int index, int volume) {
    }
}