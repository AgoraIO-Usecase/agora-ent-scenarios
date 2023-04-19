package io.agora.scene.voice.spatial.ui.widget.top;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\t\u0018\u00002\u00020\u00012\u00020\u00022\u00020\u0003B\u000f\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006B\u0019\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\u0002\u0010\tB!\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fB)\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\r\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\u000eJ\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0004\u001a\u00020\u0005H\u0002J\u0010\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0014H\u0016J\u0012\u0010\u0019\u001a\u00020\u00162\b\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u0016J\u0016\u0010\u001c\u001a\u00020\u00162\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001f0\u001eH\u0016J\u0010\u0010 \u001a\u00020\u00162\u0006\u0010!\u001a\u00020\u000bH\u0016J\u0010\u0010\"\u001a\u00020\u00162\u0006\u0010!\u001a\u00020\u000bH\u0016J\u0010\u0010#\u001a\u00020\u00162\u0006\u0010!\u001a\u00020\u000bH\u0016J\u000e\u0010$\u001a\u00020\u00162\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010%\u001a\u00020\u00162\u0006\u0010&\u001a\u00020\u000bJ\u0006\u0010\'\u001a\u00020\u0016R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lio/agora/scene/voice/spatial/ui/widget/top/RoomLiveTopView;", "Landroidx/constraintlayout/widget/ConstraintLayout;", "Landroid/view/View$OnClickListener;", "Lio/agora/scene/voice/spatial/ui/widget/top/IRoomLiveTopView;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "defStyleRes", "(Landroid/content/Context;Landroid/util/AttributeSet;II)V", "binding", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialViewRoomLiveTopBinding;", "onLiveTopClickListener", "Lio/agora/scene/voice/spatial/ui/widget/top/OnLiveTopClickListener;", "roomDetailInfo", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "init", "", "onChatroomInfo", "voiceRoomModel", "onClick", "v", "Landroid/view/View;", "onRankMember", "topGifts", "", "Lio/agora/scene/voice/spatial/model/VoiceRankUserModel;", "onUpdateGiftCount", "count", "onUpdateMemberCount", "onUpdateWatchCount", "setOnLiveTopClickListener", "setRoomType", "roomType", "setTitleMaxWidth", "voice_spatial_debug"})
public final class RoomLiveTopView extends androidx.constraintlayout.widget.ConstraintLayout implements android.view.View.OnClickListener, io.agora.scene.voice.spatial.ui.widget.top.IRoomLiveTopView {
    private io.agora.scene.voice.spatial.databinding.VoiceSpatialViewRoomLiveTopBinding binding;
    private io.agora.scene.voice.spatial.model.VoiceRoomModel roomDetailInfo;
    private io.agora.scene.voice.spatial.ui.widget.top.OnLiveTopClickListener onLiveTopClickListener;
    
    public RoomLiveTopView(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    public RoomLiveTopView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    public RoomLiveTopView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    public RoomLiveTopView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(null);
    }
    
    public final void setOnLiveTopClickListener(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.ui.widget.top.OnLiveTopClickListener onLiveTopClickListener) {
    }
    
    private final void init(android.content.Context context) {
    }
    
    public final void setTitleMaxWidth() {
    }
    
    public final void setRoomType(int roomType) {
    }
    
    @java.lang.Override()
    public void onChatroomInfo(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel) {
    }
    
    @java.lang.Override()
    public void onRankMember(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.spatial.model.VoiceRankUserModel> topGifts) {
    }
    
    @java.lang.Override()
    public void onUpdateMemberCount(int count) {
    }
    
    @java.lang.Override()
    public void onUpdateWatchCount(int count) {
    }
    
    @java.lang.Override()
    public void onUpdateGiftCount(int count) {
    }
    
    @java.lang.Override()
    public void onClick(@org.jetbrains.annotations.Nullable()
    android.view.View v) {
    }
}