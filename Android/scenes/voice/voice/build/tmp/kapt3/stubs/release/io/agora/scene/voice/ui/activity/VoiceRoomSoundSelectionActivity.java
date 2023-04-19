package io.agora.scene.voice.ui.activity;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 #2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0002\"#B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\fH\u0002J\u0006\u0010\u0015\u001a\u00020\u0013J\b\u0010\u0016\u001a\u00020\u0013H\u0002J\u0010\u0010\u0017\u001a\u00020\u00022\u0006\u0010\u0018\u001a\u00020\u0019H\u0014J\b\u0010\u001a\u001a\u00020\u0013H\u0002J\b\u0010\u001b\u001a\u00020\u0013H\u0002J\b\u0010\u001c\u001a\u00020\u0013H\u0002J\b\u0010\u001d\u001a\u00020\u0013H\u0002J\u0012\u0010\u001e\u001a\u00020\u00132\b\u0010\u001f\u001a\u0004\u0018\u00010 H\u0014J\b\u0010!\u001a\u00020\u0013H\u0002R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lio/agora/scene/voice/ui/activity/VoiceRoomSoundSelectionActivity;", "Lio/agora/voice/common/ui/BaseUiActivity;", "Lio/agora/scene/voice/databinding/VoiceActivitySoundSelectionLayoutBinding;", "()V", "curVoiceRoomModel", "Lio/agora/scene/voice/model/VoiceRoomModel;", "encryption", "", "isPublic", "", "roomName", "roomType", "", "soundEffect", "soundSelectAdapter", "Lio/agora/scene/voice/ui/adapter/VoiceRoomSoundSelectionAdapter;", "voiceRoomViewModel", "Lio/agora/scene/voice/viewmodel/VoiceCreateViewModel;", "checkPrivate", "", "sound_effect", "createSpatialRoom", "finishCreateActivity", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "goVoiceRoom", "initAdapter", "initIntent", "initListener", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "voiceRoomObservable", "BottomOffsetDecoration", "Companion", "voice_release"})
public final class VoiceRoomSoundSelectionActivity extends io.agora.voice.common.ui.BaseUiActivity<io.agora.scene.voice.databinding.VoiceActivitySoundSelectionLayoutBinding> {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.activity.VoiceRoomSoundSelectionActivity.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_CHATROOM_CREATE_NAME = "chatroom_create_name";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_CHATROOM_CREATE_IS_PUBLIC = "chatroom_create_is_public";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_CHATROOM_CREATE_ENCRYPTION = "chatroom_create_encryption";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_CHATROOM_CREATE_ROOM_TYPE = "chatroom_create_room_type";
    private io.agora.scene.voice.ui.adapter.VoiceRoomSoundSelectionAdapter soundSelectAdapter;
    private io.agora.scene.voice.viewmodel.VoiceCreateViewModel voiceRoomViewModel;
    private boolean isPublic = true;
    private java.lang.String roomName = "";
    private java.lang.String encryption = "";
    private int roomType = 0;
    private int soundEffect = 1;
    private io.agora.scene.voice.model.VoiceRoomModel curVoiceRoomModel;
    
    public VoiceRoomSoundSelectionActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceActivitySoundSelectionLayoutBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater) {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initIntent() {
    }
    
    private final void initAdapter() {
    }
    
    private final void initListener() {
    }
    
    private final void voiceRoomObservable() {
    }
    
    private final void checkPrivate(int sound_effect) {
    }
    
    public final void createSpatialRoom() {
    }
    
    private final void goVoiceRoom() {
    }
    
    /**
     * 结束创建activity
     */
    private final void finishCreateActivity() {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J(\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lio/agora/scene/voice/ui/activity/VoiceRoomSoundSelectionActivity$BottomOffsetDecoration;", "Landroidx/recyclerview/widget/RecyclerView$ItemDecoration;", "mBottomOffset", "", "(I)V", "getItemOffsets", "", "outRect", "Landroid/graphics/Rect;", "view", "Landroid/view/View;", "parent", "Landroidx/recyclerview/widget/RecyclerView;", "state", "Landroidx/recyclerview/widget/RecyclerView$State;", "voice_release"})
    public static final class BottomOffsetDecoration extends androidx.recyclerview.widget.RecyclerView.ItemDecoration {
        private final int mBottomOffset = 0;
        
        public BottomOffsetDecoration(int mBottomOffset) {
            super();
        }
        
        @java.lang.Override()
        public void getItemOffsets(@org.jetbrains.annotations.NotNull()
        android.graphics.Rect outRect, @org.jetbrains.annotations.NotNull()
        android.view.View view, @org.jetbrains.annotations.NotNull()
        androidx.recyclerview.widget.RecyclerView parent, @org.jetbrains.annotations.NotNull()
        androidx.recyclerview.widget.RecyclerView.State state) {
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J.\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u00042\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0010\u001a\u00020\u0011R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lio/agora/scene/voice/ui/activity/VoiceRoomSoundSelectionActivity$Companion;", "", "()V", "KEY_CHATROOM_CREATE_ENCRYPTION", "", "KEY_CHATROOM_CREATE_IS_PUBLIC", "KEY_CHATROOM_CREATE_NAME", "KEY_CHATROOM_CREATE_ROOM_TYPE", "startActivity", "", "activity", "Landroid/app/Activity;", "roomName", "isPublic", "", "encryption", "roomType", "", "voice_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void startActivity(@org.jetbrains.annotations.NotNull()
        android.app.Activity activity, @org.jetbrains.annotations.NotNull()
        java.lang.String roomName, boolean isPublic, @org.jetbrains.annotations.NotNull()
        java.lang.String encryption, int roomType) {
        }
    }
}