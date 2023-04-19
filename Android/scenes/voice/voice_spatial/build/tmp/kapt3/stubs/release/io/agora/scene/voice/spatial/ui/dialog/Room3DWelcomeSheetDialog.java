package io.agora.scene.voice.spatial.ui.dialog;

import java.lang.System;

/**
 * ---------------------------------------------------------------------------------------------
 * 功能描述: 3D音频空间欢迎页
 * ---------------------------------------------------------------------------------------------
 * 时　　间: 2023/2/4
 * ---------------------------------------------------------------------------------------------
 * 代码创建: Leo
 * ---------------------------------------------------------------------------------------------
 * 代码备注:
 * ---------------------------------------------------------------------------------------------
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u000f2\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u000fB\u0005\u00a2\u0006\u0002\u0010\u0003J\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u00022\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\bH\u0014J\u001a\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0016\u00a8\u0006\u0010"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/Room3DWelcomeSheetDialog;", "Lio/agora/voice/common/ui/dialog/BaseSheetDialog;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialDialogRoomWelcomeBinding;", "()V", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "Companion", "voice_spatial_release"})
public final class Room3DWelcomeSheetDialog extends io.agora.voice.common.ui.dialog.BaseSheetDialog<io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomWelcomeBinding> {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.ui.dialog.Room3DWelcomeSheetDialog.Companion Companion = null;
    private static boolean needShow = true;
    
    public Room3DWelcomeSheetDialog() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    @java.lang.Override()
    protected io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomWelcomeBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lio/agora/scene/voice/spatial/ui/dialog/Room3DWelcomeSheetDialog$Companion;", "", "()V", "needShow", "", "getNeedShow", "()Z", "setNeedShow", "(Z)V", "voice_spatial_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final boolean getNeedShow() {
            return false;
        }
        
        public final void setNeedShow(boolean p0) {
        }
    }
}