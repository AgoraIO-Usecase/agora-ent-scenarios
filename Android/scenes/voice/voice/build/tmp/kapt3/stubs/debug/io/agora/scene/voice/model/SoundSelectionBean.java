package io.agora.scene.voice.model;

import java.lang.System;

/**
 * 最佳音效介绍
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001BI\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\u0010\b\u0002\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b\u00a2\u0006\u0002\u0010\rJ\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\tH\u00c6\u0003J\u0011\u0010\u001e\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000bH\u00c6\u0003JM\u0010\u001f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\t2\u0010\b\u0002\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000bH\u00c6\u0001J\u0013\u0010 \u001a\u00020\t2\b\u0010!\u001a\u0004\u0018\u00010\"H\u00d6\u0003J\t\u0010#\u001a\u00020\u0003H\u00d6\u0001J\t\u0010$\u001a\u00020\u0006H\u00d6\u0001R\u0019\u0010\n\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u001a\u0010\b\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0016R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0011\u00a8\u0006%"}, d2 = {"Lio/agora/scene/voice/model/SoundSelectionBean;", "Lio/agora/scene/voice/model/BaseRoomBean;", "soundSelectionType", "", "index", "soundName", "", "soundIntroduce", "isCurrentUsing", "", "customer", "", "Lio/agora/scene/voice/model/CustomerUsageBean;", "(IILjava/lang/String;Ljava/lang/String;ZLjava/util/List;)V", "getCustomer", "()Ljava/util/List;", "getIndex", "()I", "()Z", "setCurrentUsing", "(Z)V", "getSoundIntroduce", "()Ljava/lang/String;", "getSoundName", "getSoundSelectionType", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "", "hashCode", "toString", "voice_debug"})
public final class SoundSelectionBean implements io.agora.scene.voice.model.BaseRoomBean {
    private final int soundSelectionType = 0;
    private final int index = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String soundName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String soundIntroduce = null;
    private boolean isCurrentUsing;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<io.agora.scene.voice.model.CustomerUsageBean> customer = null;
    
    /**
     * 最佳音效介绍
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.SoundSelectionBean copy(int soundSelectionType, int index, @org.jetbrains.annotations.NotNull()
    java.lang.String soundName, @org.jetbrains.annotations.NotNull()
    java.lang.String soundIntroduce, boolean isCurrentUsing, @org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.model.CustomerUsageBean> customer) {
        return null;
    }
    
    /**
     * 最佳音效介绍
     */
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    /**
     * 最佳音效介绍
     */
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    /**
     * 最佳音效介绍
     */
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public SoundSelectionBean() {
        super();
    }
    
    public SoundSelectionBean(int soundSelectionType, int index, @org.jetbrains.annotations.NotNull()
    java.lang.String soundName, @org.jetbrains.annotations.NotNull()
    java.lang.String soundIntroduce, boolean isCurrentUsing, @org.jetbrains.annotations.Nullable()
    java.util.List<io.agora.scene.voice.model.CustomerUsageBean> customer) {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    public final int getSoundSelectionType() {
        return 0;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int getIndex() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSoundName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSoundIntroduce() {
        return null;
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final boolean isCurrentUsing() {
        return false;
    }
    
    public final void setCurrentUsing(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<io.agora.scene.voice.model.CustomerUsageBean> component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<io.agora.scene.voice.model.CustomerUsageBean> getCustomer() {
        return null;
    }
}