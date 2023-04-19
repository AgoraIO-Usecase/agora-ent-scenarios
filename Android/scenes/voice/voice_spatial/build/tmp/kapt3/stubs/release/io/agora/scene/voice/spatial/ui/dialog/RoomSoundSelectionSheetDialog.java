package io.agora.scene.voice.spatial.ui.dialog;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u001d2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0002\u001d\u001eB\u0017\u0012\b\b\u0002\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u001a\u0010\u000f\u001a\u00020\u00022\u0006\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0014J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u001a\u0010\u0018\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\b\u001a\u0016\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\f\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000b0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomSoundSelectionSheetDialog;", "Lio/agora/voice/common/ui/dialog/BaseFixedHeightSheetDialog;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialDialogRoomSoundSelectionBinding;", "isEnable", "", "soundSelectionListener", "Lio/agora/scene/voice/spatial/ui/dialog/RoomSoundSelectionSheetDialog$OnClickSoundSelectionListener;", "(ZLio/agora/scene/voice/spatial/ui/dialog/RoomSoundSelectionSheetDialog$OnClickSoundSelectionListener;)V", "soundSelectionAdapter", "Lio/agora/voice/common/ui/adapter/BaseRecyclerViewAdapter;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialItemRoomSoundSelectionBinding;", "Lio/agora/scene/voice/spatial/model/SoundSelectionBean;", "Lio/agora/scene/voice/spatial/ui/adapter/viewholder/RoomSoundSelectionViewHolder;", "soundSelectionList", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "initAdapter", "", "recyclerView", "Landroidx/recyclerview/widget/RecyclerView;", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "Companion", "OnClickSoundSelectionListener", "voice_spatial_release"})
public final class RoomSoundSelectionSheetDialog extends io.agora.voice.common.ui.dialog.BaseFixedHeightSheetDialog<io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomSoundSelectionBinding> {
    private final boolean isEnable = false;
    private final io.agora.scene.voice.spatial.ui.dialog.RoomSoundSelectionSheetDialog.OnClickSoundSelectionListener soundSelectionListener = null;
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.ui.dialog.RoomSoundSelectionSheetDialog.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_CURRENT_SELECTION = "current_selection";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_IS_ENABLE = "is_enable";
    private io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter<io.agora.scene.voice.spatial.databinding.VoiceSpatialItemRoomSoundSelectionBinding, io.agora.scene.voice.spatial.model.SoundSelectionBean, io.agora.scene.voice.spatial.ui.adapter.viewholder.RoomSoundSelectionViewHolder> soundSelectionAdapter;
    private final java.util.List<io.agora.scene.voice.spatial.model.SoundSelectionBean> soundSelectionList = null;
    
    public RoomSoundSelectionSheetDialog(boolean isEnable, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.ui.dialog.RoomSoundSelectionSheetDialog.OnClickSoundSelectionListener soundSelectionListener) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomSoundSelectionBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initAdapter(androidx.recyclerview.widget.RecyclerView recyclerView) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H&\u00a8\u0006\b"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomSoundSelectionSheetDialog$OnClickSoundSelectionListener;", "", "onSoundEffect", "", "soundSelection", "Lio/agora/scene/voice/spatial/model/SoundSelectionBean;", "isCurrentUsing", "", "voice_spatial_release"})
    public static abstract interface OnClickSoundSelectionListener {
        
        public abstract void onSoundEffect(@org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.spatial.model.SoundSelectionBean soundSelection, boolean isCurrentUsing);
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/RoomSoundSelectionSheetDialog$Companion;", "", "()V", "KEY_CURRENT_SELECTION", "", "KEY_IS_ENABLE", "voice_spatial_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}