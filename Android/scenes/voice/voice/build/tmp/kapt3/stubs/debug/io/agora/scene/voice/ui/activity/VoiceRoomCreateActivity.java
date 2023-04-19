package io.agora.scene.voice.ui.activity;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0002&\'B\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u0016\u001a\u00020\u0017H\u0002J\b\u0010\u0018\u001a\u00020\u0017H\u0002J\u0010\u0010\u0019\u001a\u00020\u00022\u0006\u0010\u001a\u001a\u00020\u001bH\u0014J\u0006\u0010\u001c\u001a\u00020\u0017J\b\u0010\u001d\u001a\u00020\u0017H\u0002J\b\u0010\u001e\u001a\u00020\u0017H\u0002J\u0012\u0010\u001f\u001a\u00020\u00172\b\u0010 \u001a\u0004\u0018\u00010!H\u0014J\b\u0010\"\u001a\u00020\u0017H\u0014J\b\u0010#\u001a\u00020\u0007H\u0002J\b\u0010$\u001a\u00020\u0017H\u0002J\b\u0010%\u001a\u00020\u0017H\u0002R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R!\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000f\u0010\u0010\u001a\u0004\b\r\u0010\u000eR\u000e\u0010\u0011\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lio/agora/scene/voice/ui/activity/VoiceRoomCreateActivity;", "Lio/agora/voice/common/ui/BaseUiActivity;", "Lio/agora/scene/voice/databinding/VoiceActivityCreateRoomLayoutBinding;", "()V", "curVoiceRoomModel", "Lio/agora/scene/voice/model/VoiceRoomModel;", "encryption", "", "isPublic", "", "pageData", "", "Lio/agora/scene/voice/model/PageBean;", "getPageData", "()Ljava/util/List;", "pageData$delegate", "Lkotlin/Lazy;", "roomName", "roomType", "", "voiceRoomViewModel", "Lio/agora/scene/voice/viewmodel/VoiceCreateViewModel;", "checkPrivate", "", "createSpatialRoom", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "goVoiceRoom", "initListener", "initUi", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "randomName", "setupWithViewPager", "voiceRoomObservable", "EmojiInputFilter", "ViewHolder", "voice_debug"})
public final class VoiceRoomCreateActivity extends io.agora.voice.common.ui.BaseUiActivity<io.agora.scene.voice.databinding.VoiceActivityCreateRoomLayoutBinding> {
    private boolean isPublic = true;
    private final kotlin.Lazy pageData$delegate = null;
    private int roomType = 0;
    private java.lang.String encryption = "";
    private java.lang.String roomName = "";
    private io.agora.scene.voice.viewmodel.VoiceCreateViewModel voiceRoomViewModel;
    private io.agora.scene.voice.model.VoiceRoomModel curVoiceRoomModel;
    
    public VoiceRoomCreateActivity() {
        super();
    }
    
    private final java.util.List<io.agora.scene.voice.model.PageBean> getPageData() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceActivityCreateRoomLayoutBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater) {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initListener() {
    }
    
    private final void voiceRoomObservable() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    private final void setupWithViewPager() {
    }
    
    public final void goVoiceRoom() {
    }
    
    private final void initUi() {
    }
    
    private final void checkPrivate() {
    }
    
    private final void createSpatialRoom() {
    }
    
    private final java.lang.String randomName() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\r\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\b\u00a8\u0006\u000f"}, d2 = {"Lio/agora/scene/voice/ui/activity/VoiceRoomCreateActivity$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "(Landroid/view/View;)V", "mContent", "Landroid/widget/TextView;", "getMContent", "()Landroid/widget/TextView;", "mLayout", "Landroidx/constraintlayout/widget/ConstraintLayout;", "getMLayout", "()Landroidx/constraintlayout/widget/ConstraintLayout;", "mTitle", "getMTitle", "voice_debug"})
    public static final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final androidx.constraintlayout.widget.ConstraintLayout mLayout = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView mTitle = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView mContent = null;
        
        public ViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View itemView) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final androidx.constraintlayout.widget.ConstraintLayout getMLayout() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getMTitle() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getMContent() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\r\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J8\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\u00032\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u0003H\u0016R\u0016\u0010\u0005\u001a\n \u0007*\u0004\u0018\u00010\u00060\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lio/agora/scene/voice/ui/activity/VoiceRoomCreateActivity$EmojiInputFilter;", "Landroid/text/InputFilter$LengthFilter;", "max", "", "(I)V", "emoji", "Ljava/util/regex/Pattern;", "kotlin.jvm.PlatformType", "filter", "", "source", "start", "end", "dest", "Landroid/text/Spanned;", "dstart", "dend", "voice_debug"})
    public static final class EmojiInputFilter extends android.text.InputFilter.LengthFilter {
        private java.util.regex.Pattern emoji;
        
        public EmojiInputFilter(int max) {
            super(0);
        }
        
        @org.jetbrains.annotations.NotNull()
        @java.lang.Override()
        public java.lang.CharSequence filter(@org.jetbrains.annotations.NotNull()
        java.lang.CharSequence source, int start, int end, @org.jetbrains.annotations.NotNull()
        android.text.Spanned dest, int dstart, int dend) {
            return null;
        }
    }
}