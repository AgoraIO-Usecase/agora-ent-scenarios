package io.agora.scene.voice.spatial.ui.widget.top;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\u0016\u0010\u0006\u001a\u00020\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH&J\u0010\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u0016J\u0010\u0010\r\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u0016J\u0010\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u0016\u00a8\u0006\u000f"}, d2 = {"Lio/agora/scene/voice/spatial/ui/widget/top/IRoomLiveTopView;", "", "onChatroomInfo", "", "voiceRoomModel", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "onRankMember", "topRankUsers", "", "Lio/agora/scene/voice/spatial/model/VoiceRankUserModel;", "onUpdateGiftCount", "count", "", "onUpdateMemberCount", "onUpdateWatchCount", "voice_spatial_release"})
public abstract interface IRoomLiveTopView {
    
    /**
     * 头部初始化
     */
    public abstract void onChatroomInfo(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel);
    
    public abstract void onRankMember(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.spatial.model.VoiceRankUserModel> topRankUsers);
    
    public abstract void onUpdateMemberCount(int count);
    
    public abstract void onUpdateWatchCount(int count);
    
    public abstract void onUpdateGiftCount(int count);
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 3)
    public final class DefaultImpls {
        
        public static void onUpdateMemberCount(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.spatial.ui.widget.top.IRoomLiveTopView $this, int count) {
        }
        
        public static void onUpdateWatchCount(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.spatial.ui.widget.top.IRoomLiveTopView $this, int count) {
        }
        
        public static void onUpdateGiftCount(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.spatial.ui.widget.top.IRoomLiveTopView $this, int count) {
        }
    }
}