package io.agora.scene.voice.ui.widget.primary;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002BM\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00126\u0010\u0007\u001a2\u0012\u0013\u0012\u00110\t\u00a2\u0006\f\b\n\u0012\b\b\u000b\u0012\u0004\b\b(\f\u0012\u0013\u0012\u00110\r\u00a2\u0006\f\b\n\u0012\b\b\u000b\u0012\u0004\b\b(\u000e\u0012\u0004\u0012\u00020\u000f0\b\u00a2\u0006\u0002\u0010\u0010J\b\u0010$\u001a\u00020\u000fH\u0016R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\f\u001a\u00020\t2\u0006\u0010\u0013\u001a\u00020\t@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u0014R\u001e\u0010\u000e\u001a\u00020\r2\u0006\u0010\u0013\u001a\u00020\r@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R>\u0010\u0007\u001a2\u0012\u0013\u0012\u00110\t\u00a2\u0006\f\b\n\u0012\b\b\u000b\u0012\u0004\b\b(\f\u0012\u0013\u0012\u00110\r\u00a2\u0006\f\b\n\u0012\b\b\u000b\u0012\u0004\b\b(\u000e\u0012\u0004\u0012\u00020\u000f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0017\u001a\u00020\u00188BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001b\u0010\u001c\u001a\u0004\b\u0019\u0010\u001aR#\u0010\u001d\u001a\n \u001f*\u0004\u0018\u00010\u001e0\u001e8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\"\u0010\u001c\u001a\u0004\b \u0010!R\u000e\u0010#\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lio/agora/scene/voice/ui/widget/primary/KeyboardStatusWatcher;", "Landroid/widget/PopupWindow;", "Landroid/view/ViewTreeObserver$OnGlobalLayoutListener;", "activity", "Landroidx/fragment/app/FragmentActivity;", "lifecycleOwner", "Landroidx/lifecycle/LifecycleOwner;", "listener", "Lkotlin/Function2;", "", "Lkotlin/ParameterName;", "name", "isKeyboardShowed", "", "keyboardHeight", "", "(Landroidx/fragment/app/FragmentActivity;Landroidx/lifecycle/LifecycleOwner;Lkotlin/jvm/functions/Function2;)V", "TAG", "", "<set-?>", "()Z", "getKeyboardHeight", "()I", "popupView", "Landroid/widget/FrameLayout;", "getPopupView", "()Landroid/widget/FrameLayout;", "popupView$delegate", "Lkotlin/Lazy;", "rootView", "Landroid/view/View;", "kotlin.jvm.PlatformType", "getRootView", "()Landroid/view/View;", "rootView$delegate", "visibleHeight", "onGlobalLayout", "voice_debug"})
public final class KeyboardStatusWatcher extends android.widget.PopupWindow implements android.view.ViewTreeObserver.OnGlobalLayoutListener {
    private final androidx.fragment.app.FragmentActivity activity = null;
    private final androidx.lifecycle.LifecycleOwner lifecycleOwner = null;
    private final kotlin.jvm.functions.Function2<java.lang.Boolean, java.lang.Integer, kotlin.Unit> listener = null;
    private final kotlin.Lazy rootView$delegate = null;
    private final java.lang.String TAG = "Keyboard-Tag";
    
    /**
     * 可见区域高度
     */
    private int visibleHeight = 0;
    
    /**
     * 软键盘是否显示
     */
    private boolean isKeyboardShowed = false;
    
    /**
     * 最近一次弹出的软键盘高度
     */
    private int keyboardHeight = 0;
    
    /**
     * PopupWindow 布局
     */
    private final kotlin.Lazy popupView$delegate = null;
    
    public KeyboardStatusWatcher(@org.jetbrains.annotations.NotNull()
    androidx.fragment.app.FragmentActivity activity, @org.jetbrains.annotations.NotNull()
    androidx.lifecycle.LifecycleOwner lifecycleOwner, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Boolean, ? super java.lang.Integer, kotlin.Unit> listener) {
        super(null);
    }
    
    private final android.view.View getRootView() {
        return null;
    }
    
    public final boolean isKeyboardShowed() {
        return false;
    }
    
    public final int getKeyboardHeight() {
        return 0;
    }
    
    /**
     * PopupWindow 布局
     */
    private final android.widget.FrameLayout getPopupView() {
        return null;
    }
    
    /**
     * 监听布局大小变化
     */
    @java.lang.Override()
    public void onGlobalLayout() {
    }
}