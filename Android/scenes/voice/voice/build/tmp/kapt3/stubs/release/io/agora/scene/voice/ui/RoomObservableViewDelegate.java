package io.agora.scene.voice.ui;

import java.lang.System;

/**
 * @author create by zhangwei03
 *
 * 房间头部 && 麦位置数据变化代理
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u00a0\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u0000 g2\u00020\u0001:\u0001gB5\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0002\u0010\u000eJ\u0006\u0010%\u001a\u00020&J\u000e\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020\u001aJ\u001c\u0010(\u001a\u00020&2\u0012\u0010)\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u00150*H\u0002J\u0006\u0010+\u001a\u00020&J\u0010\u0010,\u001a\u00020\u001a2\u0006\u0010-\u001a\u00020\u001aH\u0002J\u000e\u0010.\u001a\u00020&2\u0006\u0010\'\u001a\u00020\u001aJ\b\u0010/\u001a\u00020\u001aH\u0002J\u0018\u00100\u001a\u00020&2\u0006\u00101\u001a\u00020\u00122\b\b\u0002\u0010\'\u001a\u00020\u001aJ\u000e\u00102\u001a\u00020&2\u0006\u00103\u001a\u00020\u0012J\u000e\u00104\u001a\u00020&2\u0006\u00103\u001a\u00020\u0012J\u000e\u00105\u001a\u00020&2\u0006\u00106\u001a\u00020\u001aJ\u0014\u00107\u001a\u00020&2\f\u00108\u001a\b\u0012\u0004\u0012\u00020&09J\u001c\u0010:\u001a\u00020&2\u0006\u0010;\u001a\u00020<2\f\u00108\u001a\b\u0012\u0004\u0012\u00020&09J\u0006\u0010=\u001a\u00020&J\u0006\u0010>\u001a\u00020&J\u0006\u0010?\u001a\u00020&J\u0006\u0010@\u001a\u00020&J\u0010\u0010A\u001a\u00020&2\b\b\u0002\u0010B\u001a\u00020\u001aJ\u001c\u0010C\u001a\u00020&2\u0006\u0010D\u001a\u00020\u001a2\f\u00108\u001a\b\u0012\u0004\u0012\u00020&09J$\u0010E\u001a\u00020&2\u0006\u0010F\u001a\u00020<2\u0006\u0010;\u001a\u00020<2\f\u00108\u001a\b\u0012\u0004\u0012\u00020&09J\u0006\u0010G\u001a\u00020&J\u000e\u0010H\u001a\u00020&2\u0006\u0010I\u001a\u00020JJ\u000e\u0010K\u001a\u00020&2\u0006\u0010#\u001a\u00020$J\u001a\u0010L\u001a\u00020&2\u0012\u0010M\u001a\u000e\u0012\u0004\u0012\u00020<\u0012\u0004\u0012\u00020<0*J\u0018\u0010N\u001a\u00020&2\u0006\u0010O\u001a\u00020<2\b\u0010P\u001a\u0004\u0018\u00010QJ\u001c\u0010R\u001a\u00020&2\u0006\u0010D\u001a\u00020\u001a2\f\u00108\u001a\b\u0012\u0004\u0012\u00020&09J\u0006\u0010S\u001a\u00020&J\u001c\u0010T\u001a\u00020&2\u0006\u0010;\u001a\u00020<2\f\u00108\u001a\b\u0012\u0004\u0012\u00020&09J\u000e\u0010U\u001a\u00020&2\u0006\u0010V\u001a\u00020\u0015J\u000e\u0010W\u001a\u00020&2\u0006\u0010X\u001a\u00020\u001aJ\u0018\u0010Y\u001a\u00020&2\u0006\u0010O\u001a\u00020<2\b\u0010P\u001a\u0004\u0018\u00010QJ\u0016\u0010Z\u001a\u00020&2\u0006\u0010O\u001a\u00020<2\u0006\u0010[\u001a\u00020\u001aJ\u001a\u0010\\\u001a\u00020&2\u0012\u0010]\u001a\u000e\u0012\u0004\u0012\u00020<\u0012\u0004\u0012\u00020<0\u0019J\u0018\u0010^\u001a\u00020&2\u0006\u0010;\u001a\u00020<2\u0006\u0010_\u001a\u00020`H\u0002J\u000e\u0010a\u001a\u00020&2\u0006\u0010[\u001a\u00020\u001aJ\u0006\u0010b\u001a\u00020&J\u0010\u0010c\u001a\u00020&2\b\u0010d\u001a\u0004\u0018\u00010<J\u001c\u0010e\u001a\u00020&2\u0012\u0010f\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u00150*H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u001a0\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020$X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006h"}, d2 = {"Lio/agora/scene/voice/ui/RoomObservableViewDelegate;", "Lio/agora/voice/common/ui/IParserSource;", "activity", "Landroidx/fragment/app/FragmentActivity;", "roomLivingViewModel", "Lio/agora/scene/voice/viewmodel/VoiceRoomLivingViewModel;", "roomKitBean", "Lio/agora/scene/voice/model/RoomKitBean;", "iRoomTopView", "Lio/agora/scene/voice/ui/widget/top/IRoomLiveTopView;", "iRoomMicView", "Lio/agora/scene/voice/ui/widget/mic/IRoomMicView;", "chatPrimaryMenuView", "Lio/agora/scene/voice/ui/widget/primary/ChatPrimaryMenuView;", "(Landroidx/fragment/app/FragmentActivity;Lio/agora/scene/voice/viewmodel/VoiceRoomLivingViewModel;Lio/agora/scene/voice/model/RoomKitBean;Lio/agora/scene/voice/ui/widget/top/IRoomLiveTopView;Lio/agora/scene/voice/ui/widget/mic/IRoomMicView;Lio/agora/scene/voice/ui/widget/primary/ChatPrimaryMenuView;)V", "handsDialog", "Lio/agora/scene/voice/ui/dialog/ChatroomHandsDialog;", "isLocalAudioMute", "", "isRequesting", "localUserMicInfo", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "memberCountDialog", "Lio/agora/scene/voice/ui/dialog/RoomMemberCountDialog;", "micMap", "", "", "robotDialog", "Lio/agora/scene/voice/ui/dialog/RoomRobotEnableDialog;", "roomAudioSettingDialog", "Lio/agora/scene/voice/ui/dialog/RoomAudioSettingsSheetDialog;", "getRoomAudioSettingDialog", "()Lio/agora/scene/voice/ui/dialog/RoomAudioSettingsSheetDialog;", "setRoomAudioSettingDialog", "(Lio/agora/scene/voice/ui/dialog/RoomAudioSettingsSheetDialog;)V", "voiceRoomModel", "Lio/agora/scene/voice/model/VoiceRoomModel;", "checkUserLeaveMic", "", "index", "dealMicDataMap", "updateMap", "", "destroy", "findIndexByRtcUid", "rtcUid", "handsUpdate", "localUserIndex", "muteLocalAudio", "mute", "onAIAECDialog", "isOn", "onAIAGCDialog", "onAINSDialog", "ainsMode", "onAudioSettingsDialog", "finishBack", "Lkotlin/Function0;", "onBotMicClick", "content", "", "onClickBottomHandUp", "onClickBottomMic", "onClickMemberCount", "onClickNotice", "onClickRank", "currentItem", "onClickSoundSocial", "soundSelection", "onExitRoom", "title", "onMemberJoinRefresh", "onRoomDetails", "voiceRoomInfo", "Lio/agora/scene/voice/model/VoiceRoomInfo;", "onRoomModel", "onSeatUpdated", "attributeMap", "onSendGiftSuccess", "roomId", "message", "Lio/agora/scene/voice/imkit/bean/ChatMessageData;", "onSoundSelectionDialog", "onSpatialDialog", "onTimeUpExitRoom", "onUserMicClick", "micInfo", "onVoiceChangerDialog", "mode", "receiveGift", "receiveInviteSite", "micIndex", "receiveSystem", "ext", "showAlertDialog", "onClickListener", "Lio/agora/scene/voice/ui/dialog/common/CommonSheetAlertDialog$OnClickBottomListener;", "showMemberHandsDialog", "showOwnerHandsDialog", "updateAnnouncement", "announcement", "updateViewByMicMap", "newMicMap", "Companion", "voice_release"})
public final class RoomObservableViewDelegate implements io.agora.voice.common.ui.IParserSource {
    private final androidx.fragment.app.FragmentActivity activity = null;
    private final io.agora.scene.voice.viewmodel.VoiceRoomLivingViewModel roomLivingViewModel = null;
    private final io.agora.scene.voice.model.RoomKitBean roomKitBean = null;
    private final io.agora.scene.voice.ui.widget.top.IRoomLiveTopView iRoomTopView = null;
    private final io.agora.scene.voice.ui.widget.mic.IRoomMicView iRoomMicView = null;
    private final io.agora.scene.voice.ui.widget.primary.ChatPrimaryMenuView chatPrimaryMenuView = null;
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.RoomObservableViewDelegate.Companion Companion = null;
    private static final java.lang.String TAG = "RoomObservableDelegate";
    
    /**
     * 麦位信息，index,rtcUid
     */
    private final java.util.Map<java.lang.Integer, java.lang.Integer> micMap = null;
    private io.agora.scene.voice.model.VoiceMicInfoModel localUserMicInfo;
    
    /**
     * 举手dialog
     */
    private io.agora.scene.voice.ui.dialog.ChatroomHandsDialog handsDialog;
    
    /**
     * 申请上麦标志
     */
    private boolean isRequesting = false;
    
    /**
     * 本地 座位 mute标志
     */
    private boolean isLocalAudioMute = true;
    private io.agora.scene.voice.model.VoiceRoomModel voiceRoomModel;
    private io.agora.scene.voice.ui.dialog.RoomMemberCountDialog memberCountDialog;
    private io.agora.scene.voice.ui.dialog.RoomRobotEnableDialog robotDialog;
    @org.jetbrains.annotations.Nullable()
    private io.agora.scene.voice.ui.dialog.RoomAudioSettingsSheetDialog roomAudioSettingDialog;
    
    public RoomObservableViewDelegate(@org.jetbrains.annotations.NotNull()
    androidx.fragment.app.FragmentActivity activity, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.viewmodel.VoiceRoomLivingViewModel roomLivingViewModel, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.RoomKitBean roomKitBean, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.ui.widget.top.IRoomLiveTopView iRoomTopView, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.ui.widget.mic.IRoomMicView iRoomMicView, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.ui.widget.primary.ChatPrimaryMenuView chatPrimaryMenuView) {
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
    io.agora.scene.voice.model.VoiceRoomModel voiceRoomModel) {
    }
    
    /**
     * 房间详情
     */
    public final void onRoomDetails(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceRoomInfo voiceRoomInfo) {
    }
    
    /**
     * 排行榜
     */
    public final void onClickRank(int currentItem) {
    }
    
    /**
     * 成员数
     */
    public final void onClickMemberCount() {
    }
    
    /**
     * 成员加入刷新成员列表
     */
    public final void onMemberJoinRefresh() {
    }
    
    /**
     * 公告
     */
    public final void onClickNotice() {
    }
    
    /**
     * 音效
     */
    public final void onClickSoundSocial(int soundSelection, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> finishBack) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.ui.dialog.RoomAudioSettingsSheetDialog getRoomAudioSettingDialog() {
        return null;
    }
    
    public final void setRoomAudioSettingDialog(@org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.ui.dialog.RoomAudioSettingsSheetDialog p0) {
    }
    
    /**
     * 音效设置
     */
    public final void onAudioSettingsDialog(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> finishBack) {
    }
    
    /**
     * 最佳音效选择
     */
    public final void onSoundSelectionDialog(int soundSelection, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> finishBack) {
    }
    
    /**
     * AI降噪弹框
     */
    public final void onAINSDialog(int ainsMode) {
    }
    
    /**
     * 回声消除弹框
     */
    public final void onAIAECDialog(boolean isOn) {
    }
    
    /**
     * 人声增强弹框
     */
    public final void onAIAGCDialog(boolean isOn) {
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
    io.agora.scene.voice.model.VoiceMicInfoModel micInfo) {
    }
    
    /**
     * 点击机器人
     */
    public final void onBotMicClick(@org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> finishBack) {
    }
    
    private final void showAlertDialog(java.lang.String content, io.agora.scene.voice.ui.dialog.common.CommonSheetAlertDialog.OnClickBottomListener onClickListener) {
    }
    
    /**
     * 自己关麦
     */
    public final void muteLocalAudio(boolean mute, int index) {
    }
    
    public final void onSendGiftSuccess(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.imkit.bean.ChatMessageData message) {
    }
    
    public final void receiveGift(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.Nullable()
    io.agora.scene.voice.imkit.bean.ChatMessageData message) {
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
    public final void showOwnerHandsDialog() {
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
    
    public final void onSeatUpdated(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> attributeMap) {
    }
    
    /**
     * 处理麦位数据
     */
    private final void dealMicDataMap(java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel> updateMap) {
    }
    
    /**
     * 根据麦位数据更新ui
     */
    private final void updateViewByMicMap(java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel> newMicMap) {
    }
    
    public final void checkUserLeaveMic() {
    }
    
    public final void checkUserLeaveMic(int index) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lio/agora/scene/voice/ui/RoomObservableViewDelegate$Companion;", "", "()V", "TAG", "", "voice_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}