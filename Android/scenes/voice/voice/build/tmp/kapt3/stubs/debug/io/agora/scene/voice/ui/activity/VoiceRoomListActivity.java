package io.agora.scene.voice.ui.activity;

import java.lang.System;

@com.alibaba.android.arouter.facade.annotation.Route(path = "/voice/VoiceRoomListActivity")
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0015\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\f\u001a\u00020\rH\u0016J\u0012\u0010\u000e\u001a\u0004\u0018\u00010\u00022\u0006\u0010\u000f\u001a\u00020\u0010H\u0014J\b\u0010\u0011\u001a\u00020\rH\u0002J\b\u0010\u0012\u001a\u00020\rH\u0016J\u0012\u0010\u0013\u001a\u00020\r2\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0014J\b\u0010\u0016\u001a\u00020\rH\u0014J\u001a\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u00052\b\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u0016J\u0010\u0010\u001c\u001a\u00020\r2\u0006\u0010\u001d\u001a\u00020\u001eH\u0002J\u0012\u0010\u001f\u001a\u00020\r2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0002J\b\u0010 \u001a\u00020\rH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lio/agora/scene/voice/ui/activity/VoiceRoomListActivity;", "Lio/agora/voice/common/ui/BaseUiActivity;", "Lio/agora/scene/voice/databinding/VoiceAgoraRoomListLayoutBinding;", "()V", "index", "", "title", "Landroid/widget/TextView;", "titles", "", "voiceServiceProtocol", "Lio/agora/scene/voice/service/VoiceServiceProtocol;", "finish", "", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "initListener", "onAttachedToWindow", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onKeyDown", "", "keyCode", "event", "Landroid/view/KeyEvent;", "onTabLayoutSelected", "tab", "Lcom/google/android/material/tabs/TabLayout$Tab;", "onTabLayoutUnselected", "setupWithViewPager", "voice_debug"})
public final class VoiceRoomListActivity extends io.agora.voice.common.ui.BaseUiActivity<io.agora.scene.voice.databinding.VoiceAgoraRoomListLayoutBinding> {
    private android.widget.TextView title;
    private int index = 0;
    private final int[] titles = null;
    private final io.agora.scene.voice.service.VoiceServiceProtocol voiceServiceProtocol = null;
    
    public VoiceRoomListActivity() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    @java.lang.Override()
    protected io.agora.scene.voice.databinding.VoiceAgoraRoomListLayoutBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater) {
        return null;
    }
    
    @java.lang.Override()
    public void onAttachedToWindow() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initListener() {
    }
    
    private final void onTabLayoutSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
    }
    
    private final void onTabLayoutUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
    }
    
    private final void setupWithViewPager() {
    }
    
    @java.lang.Override()
    public void finish() {
    }
    
    @java.lang.Override()
    public boolean onKeyDown(int keyCode, @org.jetbrains.annotations.Nullable()
    android.view.KeyEvent event) {
        return false;
    }
}