package io.agora.scene.voice.spatial.ui;

import java.lang.System;

/**
 * @author create by zhangwei03
 *
 * 房间头部 && 麦位置数据变化代理
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0098\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010$\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u001e\n\u0002\b\u0004\u0018\u0000 \\2\u00020\u0001:\u0001\\B5\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0002\u0010\u000eJ\b\u0010\"\u001a\u00020#H\u0002J\u0006\u0010$\u001a\u00020#J\u000e\u0010$\u001a\u00020#2\u0006\u0010%\u001a\u00020\u0017J\u001c\u0010&\u001a\u00020#2\u0012\u0010\'\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00140(H\u0002J\u0006\u0010)\u001a\u00020#J\u0010\u0010*\u001a\u00020\u00172\u0006\u0010+\u001a\u00020\u0017H\u0002J\u000e\u0010,\u001a\u00020#2\u0006\u0010%\u001a\u00020\u0017J\b\u0010-\u001a\u00020\u0017H\u0002J\u000e\u0010.\u001a\u00020#2\u0006\u0010/\u001a\u00020\u0012J\u0014\u00100\u001a\u00020#2\f\u00101\u001a\b\u0012\u0004\u0012\u00020#02J\u000e\u00103\u001a\u00020#2\u0006\u00104\u001a\u000205J\u0006\u00106\u001a\u00020#J\u0006\u00107\u001a\u00020#J\u0006\u00108\u001a\u00020#J\u0010\u00109\u001a\u00020#2\b\b\u0002\u0010:\u001a\u00020\u0017J$\u0010;\u001a\u00020#2\u0006\u0010<\u001a\u0002052\u0006\u00104\u001a\u0002052\f\u00101\u001a\b\u0012\u0004\u0012\u00020#02J\u000e\u0010=\u001a\u00020#2\u0006\u0010\u0018\u001a\u00020\u0019J\u000e\u0010>\u001a\u00020#2\u0006\u0010?\u001a\u00020@J\u000e\u0010A\u001a\u00020#2\u0006\u0010 \u001a\u00020!J\u001a\u0010B\u001a\u00020#2\u0012\u0010C\u001a\u000e\u0012\u0004\u0012\u000205\u0012\u0004\u0012\u0002050(J\u0006\u0010D\u001a\u00020#J\u001c\u0010E\u001a\u00020#2\u0006\u00104\u001a\u0002052\f\u00101\u001a\b\u0012\u0004\u0012\u00020#02J\u000e\u0010F\u001a\u00020#2\u0006\u0010G\u001a\u00020\u0014J\u000e\u0010H\u001a\u00020#2\u0006\u0010I\u001a\u00020\u0017J\u0016\u0010J\u001a\u00020#2\u0006\u0010K\u001a\u0002052\u0006\u0010L\u001a\u00020\u0017J\u001a\u0010M\u001a\u00020#2\u0012\u0010N\u001a\u000e\u0012\u0004\u0012\u000205\u0012\u0004\u0012\u0002050\u0016J\u0018\u0010O\u001a\u00020#2\u0006\u00104\u001a\u0002052\u0006\u0010P\u001a\u00020QH\u0002J\u000e\u0010R\u001a\u00020#2\u0006\u0010L\u001a\u00020\u0017J\u000e\u0010S\u001a\u00020#2\u0006\u0010L\u001a\u00020\u0017J\u0006\u0010T\u001a\u00020#J\u0010\u0010U\u001a\u00020#2\b\u0010V\u001a\u0004\u0018\u000105J\u0016\u0010W\u001a\u00020#2\f\u0010X\u001a\b\u0012\u0004\u0012\u00020\u00140YH\u0002J\u001c\u0010Z\u001a\u00020#2\u0012\u0010[\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00140(H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00170\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u001a\u001a\u0004\u0018\u00010\u001bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020!X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006]"}, d2 = {"Lio/agora/scene/voice/spatial/ui/RoomObservableViewDelegate;", "Lio/agora/voice/common/ui/IParserSource;", "activity", "Landroidx/fragment/app/FragmentActivity;", "roomLivingViewModel", "Lio/agora/scene/voice/spatial/viewmodel/VoiceRoomLivingViewModel;", "roomKitBean", "Lio/agora/scene/voice/spatial/model/RoomKitBean;", "iRoomTopView", "Lio/agora/scene/voice/spatial/ui/widget/top/IRoomLiveTopView;", "iRoomMicView", "Lio/agora/scene/voice/spatial/ui/widget/mic/IRoomMicView;", "chatPrimaryMenuView", "Lio/agora/scene/voice/spatial/ui/widget/primary/ChatPrimaryMenuView;", "(Landroidx/fragment/app/FragmentActivity;Lio/agora/scene/voice/spatial/viewmodel/VoiceRoomLivingViewModel;Lio/agora/scene/voice/spatial/model/RoomKitBean;Lio/agora/scene/voice/spatial/ui/widget/top/IRoomLiveTopView;Lio/agora/scene/voice/spatial/ui/widget/mic/IRoomMicView;Lio/agora/scene/voice/spatial/ui/widget/primary/ChatPrimaryMenuView;)V", "handsDialog", "Lio/agora/scene/voice/spatial/ui/dialog/ChatroomHandsDialog;", "isRequesting", "", "localUserMicInfo", "Lio/agora/scene/voice/spatial/model/VoiceMicInfoModel;", "micMap", "", "", "robotInfo", "Lio/agora/scene/voice/spatial/model/RobotSpatialAudioModel;", "roomAudioSettingDialog", "Lio/agora/scene/voice/spatial/ui/dialog/RoomAudioSettingsSheetDialog;", "getRoomAudioSettingDialog", "()Lio/agora/scene/voice/spatial/ui/dialog/RoomAudioSettingsSheetDialog;", "setRoomAudioSettingDialog", "(Lio/agora/scene/voice/spatial/ui/dialog/RoomAudioSettingsSheetDialog;)V", "voiceRoomModel", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "activeRobotSound", "", "checkUserLeaveMic", "index", "dealMicDataMap", "updateMap", "", "destroy", "findIndexByRtcUid", "rtcUid", "handsUpdate", "localUserIndex", "muteLocalAudio", "mute", "onAudioSettingsDialog", "finishBack", "Lkotlin/Function0;", "onBotMicClick", "content", "", "onClickBottomHandUp", "onClickBottomMic", "onClickNotice", "onClickRank", "currentItem", "onExitRoom", "title", "onRobotUpdated", "onRoomDetails", "voiceRoomInfo", "Lio/agora/scene/voice/spatial/model/VoiceRoomInfo;", "onRoomModel", "onSeatUpdated", "attributeMap", "onSpatialDialog", "onTimeUpExitRoom", "onUserMicClick", "micInfo", "onVoiceChangerDialog", "mode", "receiveInviteSite", "roomId", "micIndex", "receiveSystem", "ext", "showAlertDialog", "onClickListener", "Lio/agora/scene/voice/spatial/ui/dialog/common/CommonSheetAlertDialog$OnClickBottomListener;", "showMemberHandsDialog", "showOwnerHandsDialog", "showRoom3DWelcomeSheetDialog", "updateAnnouncement", "announcement", "updateSpatialPosition", "models", "", "updateViewByMicMap", "newMicMap", "Companion", "voice_spatial_debug"})
public final class RoomObservableViewDelegate implements io.agora.voice.common.ui.IParserSource {
    private final androidx.fragment.app.FragmentActivity activity = null;
    private final io.agora.scene.voice.spatial.viewmodel.VoiceRoomLivingViewModel roomLivingViewModel = null;
    private final io.agora.scene.voice.spatial.model.RoomKitBean roomKitBean = null;
    private final io.agora.scene.voice.spatial.ui.widget.top.IRoomLiveTopView iRoomTopView = null;
    private final io.agora.scene.voice.spatial.ui.widget.mic.IRoomMicView iRoomMicView = null;
    private final io.agora.scene.voice.spatial.ui.widget.primary.ChatPrimaryMenuView chatPrimaryMenuView = null;
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.ui.RoomObservableViewDelegate.Companion Companion = null;
    private static final java.lang.String TAG = "RoomObservableDelegate";
    
    /**
     * 麦位信息，index,rtcUid
     */
    private final java.util.Map<java.lang.Integer, java.lang.Integer> micMap = null;
    private io.agora.scene.voice.spatial.model.VoiceMicInfoModel localUserMicInfo;
    
    /**
     * 举手dialog
     */
    private io.agora.scene.voice.spatial.ui.dialog.ChatroomHandsDialog handsDialog;
    
    /**
     * 申请上麦标志
     */
    private boolean isRequesting = false;
    private io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel;
    private io.agora.scene.voice.spatial.model.RobotSpatialAudioModel robotInfo;
    @org.jetbrains.annotations.Nullable()
    private io.agora.scene.voice.spatial.ui.dialog.RoomAudioSettingsSheetDialog roomAudioSettingDialog;
    
    public RoomObservableViewDelegate(@org.jetbrains.annotations.NotNull()
    androidx.fragment.app.FragmentActivity activity, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.viewmodel.VoiceRoomLivingViewModel roomLivingViewModel, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.RoomKitBean roomKitBean, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.ui.widget.top.IRoomLiveTopView iRoomTopView, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.ui.widget.mic.IRoomMicView iRoomMicView, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.ui.widget.primary.ChatPrimaryMenuView chatPrimaryMenuView) {
        super();
    }
    
    private final int localUserIndex() {
        return 0;
    }
    
    private final int findIndexByRtcUid(int rtcUid) {
        return 0;
    }
    
    /**
     * 房间概要
     */
    public final void onRoomModel(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel) {
    }
    
    /**
     * 房间详情
     */
    public final void onRoomDetails(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceRoomInfo voiceRoomInfo) {
    }
    
    /**
     * 排行榜
     */
    public final void onClickRank(int currentItem) {
    }
    
    /**
     * 展示3D空间音频欢迎页
     */
    public final void showRoom3DWelcomeSheetDialog() {
    }
    
    /**
     * 公告
     */
    public final void onClickNotice() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.spatial.ui.dialog.RoomAudioSettingsSheetDialog getRoomAudioSettingDialog() {
        return null;
    }
    
    public final void setRoomAudioSettingDialog(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.spatial.ui.dialog.RoomAudioSettingsSheetDialog p0) {
    }
    
    /**
     * 音效设置
     */
    public final void onAudioSettingsDialog(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> finishBack) {
    }
    
    /**
     * 变声器弹框
     */
    public final void onVoiceChangerDialog(int mode) {
    }
    
    /**
     * 空间音频弹框
     */
    public final void onSpatialDialog() {
    }
    
    /**
     * 退出房间
     */
    public final void onExitRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> finishBack) {
    }
    
    /**
     * 超时退出房间
     */
    public final void onTimeUpExitRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> finishBack) {
    }
    
    /**
     * 点击麦位
     */
    public final void onUserMicClick(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceMicInfoModel micInfo) {
    }
    
    /**
     * 点击机器人
     */
    public final void onBotMicClick(@org.jetbrains.annotations.NotNull()
    java.lang.String content) {
    }
    
    private final void showAlertDialog(java.lang.String content, io.agora.scene.voice.spatial.ui.dialog.common.CommonSheetAlertDialog.OnClickBottomListener onClickListener) {
    }
    
    /**
     * 自己关麦
     */
    public final void muteLocalAudio(boolean mute) {
    }
    
    /**
     * 收到邀请上麦消息
     */
    public final void receiveInviteSite(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, int micIndex) {
    }
    
    /**
     * 接受系统消息
     */
    public final void receiveSystem(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> ext) {
    }
    
    public final void destroy() {
    }
    
    /**
     * 房主举手弹框
     */
    public final void showOwnerHandsDialog(int micIndex) {
    }
    
    /**
     * 用户举手举手
     */
    public final void showMemberHandsDialog(int micIndex) {
    }
    
    public final void handsUpdate(int index) {
    }
    
    public final void onClickBottomMic() {
    }
    
    public final void onClickBottomHandUp() {
    }
    
    public final void updateAnnouncement(@org.jetbrains.annotations.Nullable()
    java.lang.String announcement) {
    }
    
    public final void onRobotUpdated(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.RobotSpatialAudioModel robotInfo) {
    }
    
    public final void onSeatUpdated(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> attributeMap) {
    }
    
    /**
     * 处理麦位数据
     */
    private final void dealMicDataMap(java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel> updateMap) {
    }
    
    private final void activeRobotSound() {
    }
    
    /**
     * 根据麦位数据更新ui
     */
    private final void updateViewByMicMap(java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel> newMicMap) {
    }
    
    private final void updateSpatialPosition(java.util.Collection<io.agora.scene.voice.spatial.model.VoiceMicInfoModel> models) {
    }
    
    public final void checkUserLeaveMic() {
    }
    
    public final void checkUserLeaveMic(int index) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lio/agora/scene/voice/spatial/ui/RoomObservableViewDelegate$Companion;", "", "()V", "TAG", "", "voice_spatial_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}