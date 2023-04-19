package io.agora.scene.voice.ui.widget.mic;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0010$\n\u0002\b\t\u0018\u00002\u00020\u00012\u00020\u0002B\u000f\b\u0016\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005B\u0019\b\u0016\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\u0002\u0010\bB!\b\u0016\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\u0006\u0010\t\u001a\u00020\n\u00a2\u0006\u0002\u0010\u000bB)\b\u0016\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\f\u001a\u00020\n\u00a2\u0006\u0002\u0010\rJ\u0010\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cH\u0016J\u0010\u0010\u001d\u001a\u00020\n2\u0006\u0010\u001e\u001a\u00020\u001fH\u0016J\u0010\u0010 \u001a\u00020\u001a2\u0006\u0010\u0003\u001a\u00020\u0004H\u0002J\b\u0010\u0010\u001a\u00020\nH\u0016J\u001e\u0010!\u001a\u00020\u001a2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00130#2\u0006\u0010$\u001a\u00020\u001cH\u0016J\"\u0010%\u001a\u00020\u00002\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00130\u00122\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012J\u001c\u0010&\u001a\u00020\u001a2\u0012\u0010\'\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00130(H\u0016J\u000e\u0010)\u001a\u00020\u001a2\u0006\u0010*\u001a\u00020\nJ\u0006\u0010+\u001a\u00020\u001aJ\u0018\u0010,\u001a\u00020\u001a2\u0006\u0010-\u001a\u00020\n2\u0006\u0010.\u001a\u00020\nH\u0016J\u0018\u0010/\u001a\u00020\u001a2\u0006\u00100\u001a\u00020\n2\u0006\u0010.\u001a\u00020\nH\u0016R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0011\u001a\n\u0012\u0004\u0012\u00020\u0013\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0014\u001a\n\u0012\u0004\u0012\u00020\u0013\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00061"}, d2 = {"Lio/agora/scene/voice/ui/widget/mic/Room2DMicLayout;", "Landroidx/constraintlayout/widget/ConstraintLayout;", "Lio/agora/scene/voice/ui/widget/mic/IRoomMicView;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "defStyleRes", "(Landroid/content/Context;Landroid/util/AttributeSet;II)V", "binding", "Lio/agora/scene/voice/databinding/VoiceViewRoom2dMicLayoutBinding;", "myRtcUid", "onBotMicClickListener", "Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "onMicClickListener", "room2DMicAdapter", "Lio/agora/scene/voice/ui/adapter/Room2DMicAdapter;", "room2DMicBotAdapter", "Lio/agora/scene/voice/ui/adapter/Room2DBotMicAdapter;", "activeBot", "", "active", "", "findMicByUid", "uid", "", "init", "onInitMic", "micInfoList", "", "isBotActive", "onItemClickListener", "onSeatUpdated", "newMicMap", "", "setMyRtcUid", "rtcUid", "setUpInitAdapter", "updateBotVolume", "speakerType", "volume", "updateVolume", "index", "voice_debug"})
public final class Room2DMicLayout extends androidx.constraintlayout.widget.ConstraintLayout implements io.agora.scene.voice.ui.widget.mic.IRoomMicView {
    private io.agora.scene.voice.databinding.VoiceViewRoom2dMicLayoutBinding binding;
    private io.agora.scene.voice.ui.adapter.Room2DMicAdapter room2DMicAdapter;
    private io.agora.scene.voice.ui.adapter.Room2DBotMicAdapter room2DMicBotAdapter;
    private io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.VoiceMicInfoModel> onMicClickListener;
    private io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.VoiceMicInfoModel> onBotMicClickListener;
    private int myRtcUid = -1;
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.ui.widget.mic.Room2DMicLayout onItemClickListener(@org.jetbrains.annotations.NotNull()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.VoiceMicInfoModel> onMicClickListener, @org.jetbrains.annotations.NotNull()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.VoiceMicInfoModel> onBotMicClickListener) {
        return null;
    }
    
    public Room2DMicLayout(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    public Room2DMicLayout(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    public Room2DMicLayout(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    public Room2DMicLayout(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(null);
    }
    
    private final void init(android.content.Context context) {
    }
    
    public final void setUpInitAdapter() {
    }
    
    @java.lang.Override()
    public void onInitMic(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> micInfoList, boolean isBotActive) {
    }
    
    @java.lang.Override()
    public void activeBot(boolean active) {
    }
    
    @java.lang.Override()
    public void updateVolume(int index, int volume) {
    }
    
    @java.lang.Override()
    public void updateBotVolume(int speakerType, int volume) {
    }
    
    @java.lang.Override()
    public int findMicByUid(@org.jetbrains.annotations.NotNull()
    java.lang.String uid) {
        return 0;
    }
    
    @java.lang.Override()
    public void onSeatUpdated(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel> newMicMap) {
    }
    
    public final void setMyRtcUid(int rtcUid) {
    }
    
    @java.lang.Override()
    public int myRtcUid() {
        return 0;
    }
}