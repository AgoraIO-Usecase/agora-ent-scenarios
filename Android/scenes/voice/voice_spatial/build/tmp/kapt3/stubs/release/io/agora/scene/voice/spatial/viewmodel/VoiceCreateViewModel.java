package io.agora.scene.voice.spatial.viewmodel;

import java.lang.System;

/**
 * 创建房间 && 房间列表等
 *
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00172\u0006\u0010\u0019\u001a\u00020\u0017J\u0012\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u001bJ$\u0010\u001c\u001a\u00020\u00152\u0006\u0010\u001d\u001a\u00020\u00172\b\b\u0002\u0010\u001e\u001a\u00020\u001f2\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0017J\u0012\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u001bJ$\u0010!\u001a\u00020\u00152\u0006\u0010\u001d\u001a\u00020\u00172\b\b\u0002\u0010\u001e\u001a\u00020\u001f2\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0017J\u000e\u0010\"\u001a\u00020\u00152\u0006\u0010#\u001a\u00020\u001fJ\u000e\u0010$\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017J\u0012\u0010%\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u001bJ\u0018\u0010&\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\r0\u00070\u001bR\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\f\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\r0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u000e\u001a\u00020\u000f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\u0013\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\'"}, d2 = {"Lio/agora/scene/voice/spatial/viewmodel/VoiceCreateViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_checkPasswordObservable", "Lio/agora/voice/common/viewmodel/SingleSourceLiveData;", "Lio/agora/voice/common/net/Resource;", "", "_createRoomObservable", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "_joinRoomObservable", "_roomListObservable", "", "voiceRoomRepository", "Lio/agora/scene/voice/spatial/viewmodel/repositories/VoiceCreateRepository;", "getVoiceRoomRepository", "()Lio/agora/scene/voice/spatial/viewmodel/repositories/VoiceCreateRepository;", "voiceRoomRepository$delegate", "Lkotlin/Lazy;", "checkPassword", "", "roomId", "", "password", "userInput", "checkPasswordObservable", "Landroidx/lifecycle/LiveData;", "createRoom", "roomName", "soundEffect", "", "createRoomObservable", "createSpatialRoom", "getRoomList", "page", "joinRoom", "joinRoomObservable", "roomListObservable", "voice_spatial_release"})
public final class VoiceCreateViewModel extends androidx.lifecycle.AndroidViewModel {
    private final kotlin.Lazy voiceRoomRepository$delegate = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.spatial.model.VoiceRoomModel>>> _roomListObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _checkPasswordObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceRoomModel>> _createRoomObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceRoomModel>> _joinRoomObservable = null;
    
    public VoiceCreateViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    private final io.agora.scene.voice.spatial.viewmodel.repositories.VoiceCreateRepository getVoiceRoomRepository() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.spatial.model.VoiceRoomModel>>> roomListObservable() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> checkPasswordObservable() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceRoomModel>> createRoomObservable() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceRoomModel>> joinRoomObservable() {
        return null;
    }
    
    /**
     * 获取房间列表
     * @param page 第几页，暂未用到
     */
    public final void getRoomList(int page) {
    }
    
    /**
     * 私密房间密码校验，本地模拟验证
     * @param roomId 房间id
     * @param password 房间密码
     * @param userInput 用户输入
     */
    public final void checkPassword(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String userInput) {
    }
    
    /**
     * 创建普通房间
     * @param roomName 房间名
     * @param soundEffect 房间音效类型
     * @param password  私有房间，有秘密
     */
    public final void createRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomName, int soundEffect, @org.jetbrains.annotations.Nullable()
    java.lang.String password) {
    }
    
    /**
     * 创建3d音频房间
     * @param roomName 房间名
     * @param soundEffect 房间音效类型
     * @param password  私有房间，有秘密
     */
    public final void createSpatialRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomName, int soundEffect, @org.jetbrains.annotations.Nullable()
    java.lang.String password) {
    }
    
    /**
     * 加入房间
     * @param roomId 房间id
     */
    public final void joinRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId) {
    }
}