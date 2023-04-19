package io.agora.scene.voice.imkit.manager;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0088\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010%\n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0002\b\u0016\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u0000 c2\u00020\u0001:\u0001cB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J-\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00032\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013\u00a2\u0006\u0002\u0010\u0015J-\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00032\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013\u00a2\u0006\u0002\u0010\u0015J(\u0010\u0017\u001a\u00020\u000e2\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192\u0012\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00190\u0013J\u0016\u0010\u001a\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u001bJ0\u0010\u001c\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u00112\u0006\u0010\u001e\u001a\u00020\u00112\u0018\u0010\u0012\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00140\u001f0\u0013J\u0012\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010\u0006H\u0002J\u0006\u0010#\u001a\u00020\u000eJ\u001c\u0010$\u001a\u00020\u000e2\u0006\u0010%\u001a\u00020!2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020!0\u0013J\u001a\u0010&\u001a\u00020\u000e2\u0012\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\'0\u00190\u0013J\f\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00060)J\u001c\u0010*\u001a\u00020\u000e2\u0006\u0010+\u001a\u00020,2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020-0\u0013J\f\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00060)J\f\u0010/\u001a\b\u0012\u0004\u0012\u00020\u00060)J\u001c\u00100\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013J\b\u00101\u001a\u00020\u0011H\u0002J\u001a\u00102\u001a\u00020\u000e2\u0012\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00190\u0013J\u0010\u00103\u001a\u00020\u00032\u0006\u00104\u001a\u00020\u0011H\u0002J\u0012\u00105\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u001406J\u0012\u00105\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u001c\u00107\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013J\u001a\u00108\u001a\u00020\u000e2\u0012\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00190\u0013J\u0006\u00109\u001a\u00020\u0006J\u001e\u0010:\u001a\u00020\u000e2\u0006\u0010;\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010<\u001a\u00020\u001bJ\'\u0010=\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00032\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0012\u001a\u00020\u001b\u00a2\u0006\u0002\u0010>J\u001c\u0010?\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013J\u001c\u0010@\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013J\u001c\u0010A\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013J\u001c\u0010B\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013J\u0016\u0010C\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u001bJ\u0006\u0010D\u001a\u00020\u000eJ>\u0010E\u001a\u00020\u000e2\u0006\u0010F\u001a\u00020!2\b\u0010\u000f\u001a\u0004\u0018\u00010\u00032\u0006\u0010G\u001a\u00020H2\u0012\u0010I\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0003062\u0006\u0010\u0012\u001a\u00020\u001bH\u0002J\u001f\u0010J\u001a\u00020\u000e2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0012\u001a\u00020\u001b\u00a2\u0006\u0002\u0010KJ\u001c\u0010L\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013J\u001c\u0010M\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013J\u001c\u0010N\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013J\u0016\u0010O\u001a\u00020\u000e2\u0006\u0010P\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u001bJ\u001e\u0010Q\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010R\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u001bJ:\u0010S\u001a\u00020\u000e2\n\b\u0002\u0010T\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010U\u001a\u00020\u00112\u0006\u0010V\u001a\u00020!2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013H\u0002J\u001a\u0010W\u001a\u00020\u000e2\u0012\u0010X\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u001fJ$\u0010Y\u001a\u00020\u000e2\u0006\u0010Z\u001a\u00020\u00142\u0006\u0010[\u001a\u00020\u00112\n\b\u0002\u0010\\\u001a\u0004\u0018\u00010\u0006H\u0002J\u001e\u0010]\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010^\u001a\u00020_2\u0006\u0010\u0012\u001a\u00020\u001bJ\u0016\u0010`\u001a\u00020\u000e2\u0006\u0010a\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u001bJ\u001c\u0010b\u001a\u00020\u000e2\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00060\u00192\u0006\u0010\u0012\u001a\u00020\u001bR\u001a\u0010\u0005\u001a\u00020\u0006X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006d"}, d2 = {"Lio/agora/scene/voice/imkit/manager/ChatroomProtocolDelegate;", "", "roomId", "", "(Ljava/lang/String;)V", "ownerBean", "Lio/agora/scene/voice/model/VoiceMemberModel;", "getOwnerBean", "()Lio/agora/scene/voice/model/VoiceMemberModel;", "setOwnerBean", "(Lio/agora/scene/voice/model/VoiceMemberModel;)V", "roomManager", "Lio/agora/chat/ChatRoomManager;", "acceptMicSeatApply", "", "chatUid", "micIndex", "", "callback", "Lio/agora/ValueCallBack;", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "(Ljava/lang/String;Ljava/lang/Integer;Lio/agora/ValueCallBack;)V", "acceptMicSeatInvitation", "addMemberListBySelf", "memberList", "", "cancelSubmitMic", "Lio/agora/CallBack;", "changeMic", "fromMicIndex", "toMicIndex", "", "checkMemberIsOnMic", "", "memberModel", "clearCache", "enableRobot", "enable", "fetchGiftContribute", "Lio/agora/scene/voice/model/VoiceRankUserModel;", "fetchRaisedList", "", "fetchRoomDetail", "voiceRoomModel", "Lio/agora/scene/voice/model/VoiceRoomModel;", "Lio/agora/scene/voice/model/VoiceRoomInfo;", "fetchRoomInviteMembers", "fetchRoomMembers", "forbidMic", "getFirstFreeMic", "getMemberFromServer", "getMicIndex", "index", "getMicInfo", "", "getMicInfoByIndexFromServer", "getMicInfoFromServer", "getMySelfModel", "initMicInfo", "roomType", "callBack", "invitationMic", "(Ljava/lang/String;Ljava/lang/Integer;Lio/agora/CallBack;)V", "kickOff", "leaveMic", "lockMic", "muteLocal", "refuseInviteToMic", "rejectSubmitMic", "sendChatroomEvent", "isSingle", "eventType", "Lio/agora/scene/voice/imkit/custorm/CustomMsgType;", "params", "startMicSeatApply", "(Ljava/lang/Integer;Lio/agora/CallBack;)V", "unForbidMic", "unLockMic", "unMuteLocal", "updateAnnouncement", "content", "updateGiftAmount", "newAmount", "updateMicByResult", "member", "clickAction", "isForced", "updateMicInfoCache", "kvMap", "updateMicStatusByAction", "micInfo", "action", "memberBean", "updateRankList", "giftBean", "Lio/agora/scene/voice/model/VoiceGiftModel;", "updateRobotVolume", "value", "updateRoomMember", "Companion", "voice_debug"})
public final class ChatroomProtocolDelegate {
    private final java.lang.String roomId = null;
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.imkit.manager.ChatroomProtocolDelegate.Companion Companion = null;
    private static final java.lang.String TAG = "ChatroomProtocolDelegate";
    private io.agora.chat.ChatRoomManager roomManager;
    public io.agora.scene.voice.model.VoiceMemberModel ownerBean;
    
    public ChatroomProtocolDelegate(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.VoiceMemberModel getOwnerBean() {
        return null;
    }
    
    public final void setOwnerBean(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceMemberModel p0) {
    }
    
    /**
     * 初始化麦位信息
     */
    public final void initMicInfo(int roomType, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceMemberModel ownerBean, @org.jetbrains.annotations.NotNull()
    io.agora.CallBack callBack) {
    }
    
    /**
     * 获取详情，kv 组装
     */
    public final void fetchRoomDetail(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceRoomModel voiceRoomModel, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceRoomInfo> callback) {
    }
    
    /**
     * 从服务端获取所有麦位信息
     */
    public final void getMicInfoFromServer(@org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel>> callback) {
    }
    
    /**
     * 从本地缓存获取所有麦位信息
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, io.agora.scene.voice.model.VoiceMicInfoModel> getMicInfo() {
        return null;
    }
    
    /**
     * 从本地获取指定麦位信息
     */
    private final io.agora.scene.voice.model.VoiceMicInfoModel getMicInfo(int micIndex) {
        return null;
    }
    
    /**
     * 从服务端获取指定麦位信息
     */
    public final void getMicInfoByIndexFromServer(int micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 下麦
     */
    public final void leaveMic(int micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 交换麦位
     */
    public final void changeMic(int fromMicIndex, int toMicIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel>> callback) {
    }
    
    /**
     * 关麦
     */
    public final void muteLocal(int micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 取消关麦
     */
    public final void unMuteLocal(int micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 禁言指定麦位
     */
    public final void forbidMic(int micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 取消指定麦位禁言
     */
    public final void unForbidMic(int micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 踢用户下麦
     */
    public final void kickOff(int micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 锁麦
     */
    public final void lockMic(int micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 取消锁麦
     */
    public final void unLockMic(int micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 获取上麦申请列表
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.model.VoiceMemberModel> fetchRaisedList() {
        return null;
    }
    
    /**
     * 申请上麦
     */
    public final void startMicSeatApply(@org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.CallBack callback) {
    }
    
    /**
     * 同意上麦申请
     */
    public final void acceptMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * check 用户是否在麦位上
     */
    private final boolean checkMemberIsOnMic(io.agora.scene.voice.model.VoiceMemberModel memberModel) {
        return false;
    }
    
    /**
     * 拒绝上麦申请
     */
    public final void rejectSubmitMic() {
    }
    
    /**
     * 撤销上麦申请
     */
    public final void cancelSubmitMic(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.NotNull()
    io.agora.CallBack callback) {
    }
    
    /**
     * 邀请上麦列表(过滤已在麦位的成员)
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.model.VoiceMemberModel> fetchRoomInviteMembers() {
        return null;
    }
    
    /**
     * 获取观众列表
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.model.VoiceMemberModel> fetchRoomMembers() {
        return null;
    }
    
    /**
     * 邀请上麦
     */
    public final void invitationMic(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.CallBack callback) {
    }
    
    /**
     * 用户拒绝上麦邀请
     */
    public final void refuseInviteToMic(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.NotNull()
    io.agora.CallBack callback) {
    }
    
    /**
     * 用户同意上麦邀请
     */
    public final void acceptMicSeatInvitation(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 更新公告
     */
    public final void updateAnnouncement(@org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    io.agora.CallBack callback) {
    }
    
    /**
     * 是否启用机器人
     * @param enable true 启动机器人，false 关闭机器人
     */
    public final void enableRobot(boolean enable, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<java.lang.Boolean> callback) {
    }
    
    /**
     * 更新机器人音量
     * @param value 音量
     */
    public final void updateRobotVolume(int value, @org.jetbrains.annotations.NotNull()
    io.agora.CallBack callback) {
    }
    
    /**
     * 更新指定麦位信息并返回更新成功的麦位信息
     * 0:正常状态 1:闭麦 2:禁言 3:锁麦 4:锁麦和禁言 -1:空闲 5:机器人专属激活状态 -2:机器人专属关闭状态
     */
    private final void updateMicByResult(io.agora.scene.voice.model.VoiceMemberModel member, int micIndex, @io.agora.scene.voice.model.annotation.MicClickAction()
    int clickAction, boolean isForced, io.agora.ValueCallBack<io.agora.scene.voice.model.VoiceMicInfoModel> callback) {
    }
    
    /**
     * 根据麦位原状态与action 更新麦位状态
     */
    private final void updateMicStatusByAction(io.agora.scene.voice.model.VoiceMicInfoModel micInfo, @io.agora.scene.voice.model.annotation.MicClickAction()
    int action, io.agora.scene.voice.model.VoiceMemberModel memberBean) {
    }
    
    /**
     * 更新榜单
     */
    public final void updateRankList(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceGiftModel giftBean, @org.jetbrains.annotations.NotNull()
    io.agora.CallBack callback) {
    }
    
    /**
     * 更新礼物总数
     */
    public final void updateGiftAmount(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, int newAmount, @org.jetbrains.annotations.NotNull()
    io.agora.CallBack callback) {
    }
    
    /**
     * 从服务端获取榜单
     */
    public final void fetchGiftContribute(@org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<java.util.List<io.agora.scene.voice.model.VoiceRankUserModel>> callback) {
    }
    
    /**
     * 从服务端获取成员列表
     */
    public final void getMemberFromServer(@org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<java.util.List<io.agora.scene.voice.model.VoiceMemberModel>> callback) {
    }
    
    /**
     * 获取当前用户实体信息
     */
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.model.VoiceMemberModel getMySelfModel() {
        return null;
    }
    
    /**
     * 向成员列表中添加自己(每个新加入房间的人需要调用一次)
     */
    public final void addMemberListBySelf(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.model.VoiceMemberModel> memberList, @org.jetbrains.annotations.NotNull()
    io.agora.ValueCallBack<java.util.List<io.agora.scene.voice.model.VoiceMemberModel>> callback) {
    }
    
    /**
     * 更新成员列表
     */
    public final void updateRoomMember(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.model.VoiceMemberModel> memberList, @org.jetbrains.annotations.NotNull()
    io.agora.CallBack callback) {
    }
    
    private final void sendChatroomEvent(boolean isSingle, java.lang.String chatUid, io.agora.scene.voice.imkit.custorm.CustomMsgType eventType, java.util.Map<java.lang.String, java.lang.String> params, io.agora.CallBack callback) {
    }
    
    /**
     * 按麦位顺序查询空麦位
     */
    private final int getFirstFreeMic() {
        return 0;
    }
    
    private final java.lang.String getMicIndex(int index) {
        return null;
    }
    
    public final void clearCache() {
    }
    
    public final void updateMicInfoCache(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> kvMap) {
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lio/agora/scene/voice/imkit/manager/ChatroomProtocolDelegate$Companion;", "", "()V", "TAG", "", "voice_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}