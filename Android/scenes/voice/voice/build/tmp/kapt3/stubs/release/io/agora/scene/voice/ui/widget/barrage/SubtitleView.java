package io.agora.scene.voice.ui.widget.barrage;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\u0018\u0000 )2\u00020\u0001:\u0001)B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004B\u0019\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0007B!\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u0014\u001a\u00020\t2\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\tH\u0002J\b\u0010\u0018\u001a\u00020\tH\u0002J\b\u0010\u0019\u001a\u00020\tH\u0002J\u001a\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0002\u001a\u00020\u00032\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u0002J\b\u0010\u001c\u001a\u00020\u001dH\u0016J\u0012\u0010\u001e\u001a\u00020\u001b2\b\u0010\u001f\u001a\u0004\u0018\u00010 H\u0014J\u0018\u0010!\u001a\u00020\u001b2\u0006\u0010\"\u001a\u00020\t2\u0006\u0010#\u001a\u00020\tH\u0014J\u000e\u0010$\u001a\u00020\u001b2\u0006\u0010%\u001a\u00020\tJ\u000e\u0010&\u001a\u00020\u001b2\u0006\u0010\u000b\u001a\u00020\fJ\u0010\u0010\'\u001a\u00020\u001b2\u0006\u0010(\u001a\u00020\tH\u0016R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lio/agora/scene/voice/ui/widget/barrage/SubtitleView;", "Landroidx/appcompat/widget/AppCompatTextView;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "listener", "Lio/agora/scene/voice/ui/widget/barrage/StatusChangeListener;", "mMarqueeMode", "mScrollX", "", "mViewHeight", "mViewWidth", "rect", "Landroid/graphics/Rect;", "getLineMaxNumber", "textView", "Landroid/widget/TextView;", "maxWidth", "getTextContentHeight", "getTextContentWidth", "initAttrs", "", "isFocused", "", "onDraw", "canvas", "Landroid/graphics/Canvas;", "onMeasure", "widthMeasureSpec", "heightMeasureSpec", "setScrollSpeed", "speed", "setSubtitleStatusChanged", "setTextColor", "color", "Companion", "voice_release"})
public final class SubtitleView extends androidx.appcompat.widget.AppCompatTextView {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.widget.barrage.SubtitleView.Companion Companion = null;
    private static final int SPEED_FAST = 9;
    private static final int SPEED_MEDIUM = 6;
    private static final int SPEED_SLOW = 3;
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private float mScrollX = 0.0F;
    private int mMarqueeMode = 3;
    private final android.graphics.Rect rect = null;
    private io.agora.scene.voice.ui.widget.barrage.StatusChangeListener listener;
    
    public SubtitleView(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    public SubtitleView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    public SubtitleView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    @java.lang.Override()
    public boolean isFocused() {
        return false;
    }
    
    @java.lang.Override()
    protected void onDraw(@org.jetbrains.annotations.Nullable()
    android.graphics.Canvas canvas) {
    }
    
    private final void initAttrs(android.content.Context context, android.util.AttributeSet attrs) {
    }
    
    public final void setScrollSpeed(int speed) {
    }
    
    @java.lang.Override()
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    }
    
    @java.lang.Override()
    public void setTextColor(int color) {
    }
    
    /**
     * 测量文字宽度
     * @return 文字宽度
     */
    private final int getTextContentWidth() {
        return 0;
    }
    
    /**
     * 测量文字高度
     * @return 文字高度
     */
    private final int getTextContentHeight() {
        return 0;
    }
    
    /**
     * 获取textView 一行最大显示字数
     */
    private final int getLineMaxNumber(android.widget.TextView textView, int maxWidth) {
        return 0;
    }
    
    public final void setSubtitleStatusChanged(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.ui.widget.barrage.StatusChangeListener listener) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0014\u0010\u0003\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0014\u0010\u0007\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0006R\u0014\u0010\t\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u0006\u00a8\u0006\u000b"}, d2 = {"Lio/agora/scene/voice/ui/widget/barrage/SubtitleView$Companion;", "", "()V", "SPEED_FAST", "", "getSPEED_FAST", "()I", "SPEED_MEDIUM", "getSPEED_MEDIUM", "SPEED_SLOW", "getSPEED_SLOW", "voice_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final int getSPEED_FAST() {
            return 0;
        }
        
        public final int getSPEED_MEDIUM() {
            return 0;
        }
        
        public final int getSPEED_SLOW() {
            return 0;
        }
    }
}