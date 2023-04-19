package io.agora.scene.voice.ui.adapter;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\u0018\u00002\u0014\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00040\u0001BA\u0012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00030\u0006\u0012\u000e\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\b\u0012\u000e\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u0003\u0018\u00010\n\u0012\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00040\f\u00a2\u0006\u0002\u0010\rJ\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u0018\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u0015H\u0016J\u0016\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u0017\u001a\u00020\u00152\u0006\u0010\u0018\u001a\u00020\u0015\u00a8\u0006\u0019"}, d2 = {"Lio/agora/scene/voice/ui/adapter/Room2DBotMicAdapter;", "Lio/agora/voice/common/ui/adapter/BaseRecyclerViewAdapter;", "Lio/agora/scene/voice/databinding/VoiceItemRoom2dBotMicBinding;", "Lio/agora/scene/voice/model/BotMicInfoBean;", "Lio/agora/scene/voice/ui/adapter/viewholder/Room2DBotMicViewHolder;", "dataList", "", "listener", "Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;", "childListener", "Lio/agora/voice/common/ui/adapter/listener/OnItemChildClickListener;", "viewHolderClass", "Ljava/lang/Class;", "(Ljava/util/List;Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;Lio/agora/voice/common/ui/adapter/listener/OnItemChildClickListener;Ljava/lang/Class;)V", "activeBot", "", "active", "", "onBindViewHolder", "holder", "position", "", "updateVolume", "speaker", "volume", "voice_debug"})
public final class Room2DBotMicAdapter extends io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter<io.agora.scene.voice.databinding.VoiceItemRoom2dBotMicBinding, io.agora.scene.voice.model.BotMicInfoBean, io.agora.scene.voice.ui.adapter.viewholder.Room2DBotMicViewHolder> {
    
    public Room2DBotMicAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.model.BotMicInfoBean> dataList, @org.jetbrains.annotations.Nullable()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.BotMicInfoBean> listener, @org.jetbrains.annotations.Nullable()
    io.agora.voice.common.ui.adapter.listener.OnItemChildClickListener<io.agora.scene.voice.model.BotMicInfoBean> childListener, @org.jetbrains.annotations.NotNull()
    java.lang.Class<io.agora.scene.voice.ui.adapter.viewholder.Room2DBotMicViewHolder> viewHolderClass) {
        super(null, null);
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.ui.adapter.viewholder.Room2DBotMicViewHolder holder, int position) {
    }
    
    public final void activeBot(boolean active) {
    }
    
    /**
     * 更新音量
     */
    public final void updateVolume(int speaker, int volume) {
    }
}