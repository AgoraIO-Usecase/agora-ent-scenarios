package io.agora.scene.voice.ui.widget.barrage;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\u0010\u0010\u0006\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0007"}, d2 = {"Lio/agora/scene/voice/ui/widget/barrage/StatusChangeListener;", "", "onLongSubtitleRollEnd", "", "textView", "Landroid/widget/TextView;", "onShortSubtitleShow", "voice_debug"})
public abstract interface StatusChangeListener {
    
    /**
     * 当字幕数未超过当前行数限制时回调
     */
    public abstract void onShortSubtitleShow(@org.jetbrains.annotations.NotNull()
    android.widget.TextView textView);
    
    /**
     * 当字幕数较长超出行数限制并完整展示后回调
     */
    public abstract void onLongSubtitleRollEnd(@org.jetbrains.annotations.NotNull()
    android.widget.TextView textView);
}