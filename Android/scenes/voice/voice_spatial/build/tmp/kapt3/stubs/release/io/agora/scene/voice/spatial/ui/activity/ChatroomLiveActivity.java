package io.agora.scene.voice.spatial.ui.activity;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0086\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010!\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0007\u0018\u0000 >2\b\u0012\u0004\u0012\u00020\u00020\u00012\u00020\u00032\u00020\u00042\u00020\u0005:\u0001>B\u0005\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\bH\u0002J\b\u0010\u001e\u001a\u00020\u001cH\u0016J\u0010\u0010\u001f\u001a\u00020\u00022\u0006\u0010 \u001a\u00020!H\u0014J\b\u0010\"\u001a\u00020\u001cH\u0002J\b\u0010#\u001a\u00020\u001cH\u0002J\b\u0010$\u001a\u00020\u001cH\u0002J\b\u0010%\u001a\u00020\u001cH\u0016J\u0012\u0010&\u001a\u00020\u001c2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0014J\b\u0010)\u001a\u00020\u001cH\u0014J\b\u0010*\u001a\u00020\u001cH\u0002J\u001e\u0010+\u001a\u00020\u001c2\u0006\u0010,\u001a\u00020-2\f\u0010.\u001a\b\u0012\u0004\u0012\u0002000/H\u0016J\u001e\u00101\u001a\u00020\u001c2\u0006\u0010,\u001a\u00020-2\f\u0010.\u001a\b\u0012\u0004\u0012\u0002000/H\u0016J\u0010\u00102\u001a\u00020\u001c2\u0006\u0010,\u001a\u00020-H\u0016J\u0010\u00103\u001a\u00020\u001c2\u0006\u0010,\u001a\u00020-H\u0016J-\u00104\u001a\u00020\u001c2\u0006\u0010,\u001a\u00020-2\u000e\u00105\u001a\n\u0012\u0006\b\u0001\u0012\u000200062\u0006\u00107\u001a\u000208H\u0016\u00a2\u0006\u0002\u00109J\b\u0010:\u001a\u00020\u001cH\u0002J\b\u0010;\u001a\u00020\u001cH\u0002J\u0010\u0010<\u001a\u00020\u001c2\b\u0010=\u001a\u0004\u0018\u00010\u0010R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0013\u001a\u00020\u00148BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0017\u0010\u0018\u001a\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006?"}, d2 = {"Lio/agora/scene/voice/spatial/ui/activity/ChatroomLiveActivity;", "Lio/agora/voice/common/ui/BaseUiActivity;", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialActivityChatroomBinding;", "Lpub/devrel/easypermissions/EasyPermissions$PermissionCallbacks;", "Lpub/devrel/easypermissions/EasyPermissions$RationaleCallbacks;", "Lio/agora/scene/voice/spatial/service/VoiceRoomSubscribeDelegate;", "()V", "isRoomOwnerLeave", "", "roomKitBean", "Lio/agora/scene/voice/spatial/model/RoomKitBean;", "roomLivingViewModel", "Lio/agora/scene/voice/spatial/viewmodel/VoiceRoomLivingViewModel;", "roomObservableDelegate", "Lio/agora/scene/voice/spatial/ui/RoomObservableViewDelegate;", "spatialSeatInfo", "Lio/agora/scene/voice/spatial/model/SeatPositionInfo;", "spatialTimer", "Ljava/util/Timer;", "voiceRoomModel", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "getVoiceRoomModel", "()Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "voiceRoomModel$delegate", "Lkotlin/Lazy;", "voiceServiceProtocol", "Lio/agora/scene/voice/spatial/service/VoiceServiceProtocol;", "checkFocus", "", "focus", "finish", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "initData", "initListeners", "initView", "onBackPressed", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onPermissionGrant", "onPermissionsDenied", "requestCode", "", "perms", "", "", "onPermissionsGranted", "onRationaleAccepted", "onRationaleDenied", "onRequestPermissionsResult", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "requestAudioPermission", "reset", "setSpatialSeatInfo", "info", "Companion", "voice_spatial_release"})
public final class ChatroomLiveActivity extends io.agora.voice.common.ui.BaseUiActivity<io.agora.scene.voice.spatial.databinding.VoiceSpatialActivityChatroomBinding> implements pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks, pub.devrel.easypermissions.EasyPermissions.RationaleCallbacks, io.agora.scene.voice.spatial.service.VoiceRoomSubscribeDelegate {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.ui.activity.ChatroomLiveActivity.Companion Companion = null;
    public static final int RC_PERMISSIONS = 101;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_VOICE_ROOM_MODEL = "voice_chat_room_model";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "ChatroomLiveActivity";
    
    /**
     * room viewModel
     */
    private io.agora.scene.voice.spatial.viewmodel.VoiceRoomLivingViewModel roomLivingViewModel;
    private final io.agora.scene.voice.spatial.service.VoiceServiceProtocol voiceServiceProtocol = null;
    
    /**
     * 代理头部view以及麦位view
     */
    private io.agora.scene.voice.spatial.ui.RoomObservableViewDelegate roomObservableDelegate;
    
    /**
     * voice room info
     */
    private final kotlin.Lazy voiceRoomModel$delegate = null;
    
    /**
     * 房间基础
     */
    private final io.agora.scene.voice.spatial.model.RoomKitBean roomKitBean = null;
    private boolean isRoomOwnerLeave = false;
    
    /**
     * 空间位置信息
     */
    private io.agora.scene.voice.spatial.model.SeatPositionInfo spatialSeatInfo;
    
    /**
     * 空间位置同步timer
     */
    private java.util.Timer spatialTimer;
    
    public ChatroomLiveActivity() {
        super();
    }
    
    /**
     * voice room info
     */
    private final io.agora.scene.voice.spatial.model.VoiceRoomModel getVoiceRoomModel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected io.agora.scene.voice.spatial.databinding.VoiceSpatialActivityChatroomBinding getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater) {
        return null;
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initData() {
    }
    
    private final void initListeners() {
    }
    
    private final void initView() {
    }
    
    public final void setSpatialSeatInfo(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.model.SeatPositionInfo info) {
    }
    
    @java.lang.Override()
    public void onBackPressed() {
    }
    
    @java.lang.Override()
    public void finish() {
    }
    
    private final void requestAudioPermission() {
    }
    
    @java.lang.Override()
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull()
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull()
    int[] grantResults) {
    }
    
    private final void onPermissionGrant() {
    }
    
    @java.lang.Override()
    public void onPermissionsGranted(int requestCode, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> perms) {
    }
    
    @java.lang.Override()
    public void onPermissionsDenied(int requestCode, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> perms) {
    }
    
    @java.lang.Override()
    public void onRationaleAccepted(int requestCode) {
    }
    
    @java.lang.Override()
    public void onRationaleDenied(int requestCode) {
    }
    
    private final void reset() {
    }
    
    private final void checkFocus(boolean focus) {
    }
    
    /**
     * 聊天室公告更新
     * @param roomId 语聊房房间id
     * @param content 公告变化内容
     */
    public void onAnnouncementChanged(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.lang.String content) {
    }
    
    /**
     * 接收邀请消息
     * @param message IM消息对象
     */
    public void onReceiveSeatInvitation() {
    }
    
    /**
     * 接收拒绝邀请消息
     * @param userId
     */
    public void onReceiveSeatInvitationRejected(@org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
    }
    
    /**
     * 收到上麦申请消息
     * @param message 消息对象
     */
    public void onReceiveSeatRequest() {
    }
    
    /**
     * 收到取消上麦申请消息
     * @param userId 环信IM SDK 用户id
     */
    public void onReceiveSeatRequestRejected(@org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
    }
    
    public void onRobotUpdate(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.RobotSpatialAudioModel robotInfo) {
    }
    
    /**
     * 房间销毁
     */
    public void onRoomDestroyed(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId) {
    }
    
    /**
     * 聊天室自定义麦位属性发生变化
     * @param 语聊房房间id
     * @param attributeMap 变换的属性kv
     */
    public void onSeatUpdated(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> attributeMap) {
    }
    
    /**
     * 聊天室成员被踢出房间
     * @param roomId 语聊房房间id
     * @param reason 被踢出房间
     */
    public void onUserBeKicked(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.service.VoiceRoomServiceKickedReason reason) {
    }
    
    /**
     * 用户加入聊天室回调，带所有用户信息
     * @param roomId 语聊房房间id
     * @param user 用户数据
     */
    public void onUserJoinedRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceMemberModel user) {
    }
    
    /**
     * 用户离开房间
     * @param roomId 语聊房房间id
     * @param userId 离开的用户id
     */
    public void onUserLeftRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lio/agora/scene/voice/spatial/ui/activity/ChatroomLiveActivity$Companion;", "", "()V", "KEY_VOICE_ROOM_MODEL", "", "RC_PERMISSIONS", "", "TAG", "startActivity", "", "activity", "Landroid/app/Activity;", "voiceRoomModel", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "voice_spatial_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void startActivity(@org.jetbrains.annotations.NotNull()
        android.app.Activity activity, @org.jetbrains.annotations.NotNull()
        io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel) {
        }
    }
}