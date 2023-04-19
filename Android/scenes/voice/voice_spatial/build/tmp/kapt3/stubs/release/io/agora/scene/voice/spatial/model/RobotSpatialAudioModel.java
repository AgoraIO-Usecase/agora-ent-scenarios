package io.agora.scene.voice.spatial.model;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\'\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001BU\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0007\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\rJ\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\t\u0010%\u001a\u00020\u0005H\u00c6\u0003J\t\u0010&\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0003H\u00c6\u0003J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\t\u0010)\u001a\u00020\u0007H\u00c6\u0003J\t\u0010*\u001a\u00020\u0003H\u00c6\u0003J\t\u0010+\u001a\u00020\u0003H\u00c6\u0003JY\u0010,\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00072\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010-\u001a\u00020\u00032\b\u0010.\u001a\u0004\u0018\u00010/H\u00d6\u0003J\t\u00100\u001a\u00020\u0005H\u00d6\u0001J\t\u00101\u001a\u000202H\u00d6\u0001R\u001e\u0010\u000b\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u001e\u0010\n\u001a\u00020\u00078\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u001e\u0010\f\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u000f\"\u0004\b\u0017\u0010\u0011R\u001e\u0010\b\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\u000f\"\u0004\b\u0019\u0010\u0011R\u001e\u0010\u0006\u001a\u00020\u00078\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001a\u0010\u0013\"\u0004\b\u001b\u0010\u0015R\u001e\u0010\t\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u000f\"\u0004\b\u001d\u0010\u0011R\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\u001f\"\u0004\b \u0010!R\u001e\u0010\u0002\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\"\u0010\u000f\"\u0004\b#\u0010\u0011\u00a8\u00063"}, d2 = {"Lio/agora/scene/voice/spatial/model/RobotSpatialAudioModel;", "Lio/agora/scene/voice/spatial/model/BaseRoomBean;", "useRobot", "", "robotVolume", "", "redRobotAttenuation", "", "redRobotAbsorb", "redRobotBlur", "blueRobotAttenuation", "blueRobotAbsorb", "blueRobotBlur", "(ZIFZZFZZ)V", "getBlueRobotAbsorb", "()Z", "setBlueRobotAbsorb", "(Z)V", "getBlueRobotAttenuation", "()F", "setBlueRobotAttenuation", "(F)V", "getBlueRobotBlur", "setBlueRobotBlur", "getRedRobotAbsorb", "setRedRobotAbsorb", "getRedRobotAttenuation", "setRedRobotAttenuation", "getRedRobotBlur", "setRedRobotBlur", "getRobotVolume", "()I", "setRobotVolume", "(I)V", "getUseRobot", "setUseRobot", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "other", "", "hashCode", "toString", "", "voice_spatial_release"})
public final class RobotSpatialAudioModel implements io.agora.scene.voice.spatial.model.BaseRoomBean {
    @com.google.gson.annotations.SerializedName(value = "use_robot")
    private boolean useRobot;
    @com.google.gson.annotations.SerializedName(value = "robot_volume")
    private int robotVolume;
    @com.google.gson.annotations.SerializedName(value = "red_robot_attenuation")
    private float redRobotAttenuation;
    @com.google.gson.annotations.SerializedName(value = "red_robot_absorb")
    private boolean redRobotAbsorb;
    @com.google.gson.annotations.SerializedName(value = "red_robot_blur")
    private boolean redRobotBlur;
    @com.google.gson.annotations.SerializedName(value = "blue_robot_attenuation")
    private float blueRobotAttenuation;
    @com.google.gson.annotations.SerializedName(value = "blue_robot_absorb")
    private boolean blueRobotAbsorb;
    @com.google.gson.annotations.SerializedName(value = "blue_robot_blur")
    private boolean blueRobotBlur;
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.model.RobotSpatialAudioModel copy(boolean useRobot, int robotVolume, float redRobotAttenuation, boolean redRobotAbsorb, boolean redRobotBlur, float blueRobotAttenuation, boolean blueRobotAbsorb, boolean blueRobotBlur) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public RobotSpatialAudioModel() {
        super();
    }
    
    public RobotSpatialAudioModel(boolean useRobot, int robotVolume, float redRobotAttenuation, boolean redRobotAbsorb, boolean redRobotBlur, float blueRobotAttenuation, boolean blueRobotAbsorb, boolean blueRobotBlur) {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final boolean getUseRobot() {
        return false;
    }
    
    public final void setUseRobot(boolean p0) {
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int getRobotVolume() {
        return 0;
    }
    
    public final void setRobotVolume(int p0) {
    }
    
    public final float component3() {
        return 0.0F;
    }
    
    public final float getRedRobotAttenuation() {
        return 0.0F;
    }
    
    public final void setRedRobotAttenuation(float p0) {
    }
    
    public final boolean component4() {
        return false;
    }
    
    public final boolean getRedRobotAbsorb() {
        return false;
    }
    
    public final void setRedRobotAbsorb(boolean p0) {
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final boolean getRedRobotBlur() {
        return false;
    }
    
    public final void setRedRobotBlur(boolean p0) {
    }
    
    public final float component6() {
        return 0.0F;
    }
    
    public final float getBlueRobotAttenuation() {
        return 0.0F;
    }
    
    public final void setBlueRobotAttenuation(float p0) {
    }
    
    public final boolean component7() {
        return false;
    }
    
    public final boolean getBlueRobotAbsorb() {
        return false;
    }
    
    public final void setBlueRobotAbsorb(boolean p0) {
    }
    
    public final boolean component8() {
        return false;
    }
    
    public final boolean getBlueRobotBlur() {
        return false;
    }
    
    public final void setBlueRobotBlur(boolean p0) {
    }
}