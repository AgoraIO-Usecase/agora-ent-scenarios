package io.agora.scene.voice.ui.widget.top;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\u0010\u0010\u0006\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\u001a\u0010\u0007\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\tH&J\u0010\u0010\n\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u000b"}, d2 = {"Lio/agora/scene/voice/ui/widget/top/OnLiveTopClickListener;", "", "onClickBack", "", "view", "Landroid/view/View;", "onClickNotice", "onClickRank", "pageIndex", "", "onClickSoundSocial", "voice_debug"})
public abstract interface OnLiveTopClickListener {
    
    /**
     * 返回
     */
    public abstract void onClickBack(@org.jetbrains.annotations.NotNull()
    android.view.View view);
    
    /**
     * 排行榜
     */
    public abstract void onClickRank(@org.jetbrains.annotations.NotNull()
    android.view.View view, int pageIndex);
    
    /**
     * 公告
     */
    public abstract void onClickNotice(@org.jetbrains.annotations.NotNull()
    android.view.View view);
    
    /**
     * 音效
     */
    public abstract void onClickSoundSocial(@org.jetbrains.annotations.NotNull()
    android.view.View view);
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 3)
    public final class DefaultImpls {
    }
}