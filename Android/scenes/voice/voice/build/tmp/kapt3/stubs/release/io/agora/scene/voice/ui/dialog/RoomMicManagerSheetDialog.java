package io.agora.scene.voice.ui.dialog;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \'2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\'B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u001a\u0010\u001d\u001a\u00020\u00022\u0006\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010!H\u0014J\u001a\u0010\"\u001a\u00020\u001c2\u0006\u0010#\u001a\u00020$2\b\u0010%\u001a\u0004\u0018\u00010&H\u0016R\u001b\u0010\u0004\u001a\u00020\u00058BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0004\u0010\u0006R\u001b\u0010\t\u001a\u00020\u00058BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\n\u0010\b\u001a\u0004\b\t\u0010\u0006R\u001d\u0010\u000b\u001a\u0004\u0018\u00010\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000f\u0010\b\u001a\u0004\b\r\u0010\u000eR\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\u0015\u001a\n\u0012\u0004\u0012\u00020\u0014\u0018\u00010\u0016X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001a\u00a8\u0006("}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomMicManagerSheetDialog;", "Lio/agora/voice/common/ui/dialog/BaseSheetDialog;", "Lio/agora/scene/voice/databinding/VoiceDialogMicManagerBinding;", "()V", "isMyself", "", "()Z", "isMyself$delegate", "Lkotlin/Lazy;", "isOwner", "isOwner$delegate", "micInfo", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "getMicInfo", "()Lio/agora/scene/voice/model/VoiceMicInfoModel;", "micInfo$delegate", "micManagerAdapter", "Lio/agora/scene/voice/ui/adapter/RoomMicManagerAdapter;", "micManagerList", "", "Lio/agora/scene/voice/model/MicManagerBean;", "onItemClickListener", "Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;", "getOnItemClickListener", "()Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;", "setOnItemClickListener", "(Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;)V", "bindingMicInfo", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "Companion", "voice_release"})
public final class RoomMicManagerSheetDialog extends io.agora.voice.common.ui.dialog.BaseSheetDialog<io.agora.scene.voice.databinding.VoiceDialogMicManagerBinding> {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.dialog.RoomMicManagerSheetDialog.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_MIC_INFO = "mic_info";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_IS_OWNER = "owner_id";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_IS_MYSELF = "is_myself";
    private io.agora.scene.voice.ui.adapter.RoomMicManagerAdapter micManagerAdapter;
    private final java.util.List<io.agora.scene.voice.model.MicManagerBean> micManagerList = null;
    private final kotlin.Lazy micInfo$delegate = null;
    private final kotlin.Lazy isOwner$delegate = null;
    private final kotlin.Lazy isMyself$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.MicManagerBean> onItemClickListener;
    
    public RoomMicManagerSheetDialog() {
        super();
    }
    
    private final io.agora.scene.voice.model.VoiceMicInfoModel getMicInfo() {
        return null;
    }
    
    private final boolean isOwner() {
        return false;
    }
    
    private final boolean isMyself() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.MicManagerBean> getOnItemClickListener() {
        return null;
    }
    
    public final void setOnItemClickListener(@org.jetbrains.annotations.Nullable()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.MicManagerBean> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceDialogMicManagerBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void bindingMicInfo(io.agora.scene.voice.model.VoiceMicInfoModel micInfo) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lio/agora/scene/voice/ui/dialog/RoomMicManagerSheetDialog$Companion;", "", "()V", "KEY_IS_MYSELF", "", "KEY_IS_OWNER", "KEY_MIC_INFO", "voice_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}