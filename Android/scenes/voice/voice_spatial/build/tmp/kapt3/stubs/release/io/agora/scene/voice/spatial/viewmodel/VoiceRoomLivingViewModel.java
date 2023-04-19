package io.agora.scene.voice.spatial.viewmodel;

import java.lang.System;

/**
 * 语聊房
 *
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010$\n\u0002\b\u0015\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0019\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u00101\u001a\u000202J\u0012\u00103\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u000504J\u001e\u00105\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b0\u000504J\u0012\u00106\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u000504J\u0012\u00107\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u000504J\u000e\u00108\u001a\u0002022\u0006\u00109\u001a\u00020\'J\u0012\u0010:\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u000e\u0010;\u001a\u0002022\u0006\u0010<\u001a\u00020\tJ\u0016\u0010=\u001a\u0002022\u0006\u0010>\u001a\u00020\t2\u0006\u0010?\u001a\u00020\tJ\u001e\u0010@\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00060\u000f0\u000504J\u0012\u0010A\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u0012\u0010B\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u0012\u0010C\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u0012\u0010D\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u0012\u0010E\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u000e\u0010F\u001a\u0002022\u0006\u0010G\u001a\u00020\nJ\u000e\u0010H\u001a\u0002022\u0006\u0010G\u001a\u00020\nJ\u000e\u0010I\u001a\u0002022\u0006\u0010G\u001a\u00020\nJ\u000e\u0010J\u001a\u0002022\u0006\u0010G\u001a\u00020\nJ\u000e\u0010K\u001a\u0002022\u0006\u0010G\u001a\u00020\nJ\u000e\u0010L\u001a\u0002022\u0006\u0010M\u001a\u00020NJ\u000e\u0010O\u001a\u0002022\u0006\u0010<\u001a\u00020\tJ\u0012\u0010P\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u000504J\u000e\u0010Q\u001a\u0002022\u0006\u0010R\u001a\u00020SJ\u0012\u0010T\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u0012\u0010U\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u000504J\u000e\u0010V\u001a\u0002022\u0006\u0010<\u001a\u00020\tJ\u000e\u0010W\u001a\u0002022\u0006\u0010<\u001a\u00020\tJ\u0012\u0010X\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u000504J\u0016\u0010Y\u001a\u0002022\u0006\u0010Z\u001a\u00020\'2\u0006\u0010[\u001a\u00020\nJ\u0012\u0010\\\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u000e\u0010]\u001a\u0002022\u0006\u0010<\u001a\u00020\tJ\u0012\u0010^\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u000504J\u000e\u0010_\u001a\u0002022\u0006\u0010<\u001a\u00020\tJ\u0012\u0010`\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u000504J\u0012\u0010a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u0012\u0010b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u0012\u0010c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u0012\u0010d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u0012\u0010e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u001e\u0010f\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b0\u000504J\u0006\u0010g\u001a\u000202J\u0012\u0010h\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u001e\u0010i\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b0\u000504J\u0012\u0010j\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0\u000504J\u001e\u0010k\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\'\u0012\u0004\u0012\u00020\n0\b0\u000504J\u0015\u0010l\u001a\u0002022\b\u0010<\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010mJ\u0012\u0010n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504J\u000e\u0010o\u001a\u0002022\u0006\u0010<\u001a\u00020\tJ\u000e\u0010p\u001a\u0002022\u0006\u0010<\u001a\u00020\tJ\u0012\u0010q\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u000504J\u000e\u0010r\u001a\u0002022\u0006\u0010s\u001a\u00020\'J\u000e\u0010t\u001a\u0002022\u0006\u0010u\u001a\u00020\tJ\u000e\u0010v\u001a\u0002022\u0006\u0010w\u001a\u00020\tJ\u000e\u0010x\u001a\u0002022\u0006\u0010u\u001a\u00020\tJ\u0012\u0010y\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000504R\u001a\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R&\u0010\u0007\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R&\u0010\u000e\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00060\u000f0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R&\u0010!\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\"\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R&\u0010#\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010$\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R&\u0010&\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\'\u0012\u0004\u0012\u00020\n0\b0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010(\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010*\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010+\u001a\u00020,8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b/\u00100\u001a\u0004\b-\u0010.\u00a8\u0006z"}, d2 = {"Lio/agora/scene/voice/spatial/viewmodel/VoiceRoomLivingViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "_acceptMicSeatInvitationObservable", "Lio/agora/voice/common/viewmodel/SingleSourceLiveData;", "Lio/agora/voice/common/net/Resource;", "Lio/agora/scene/voice/spatial/model/VoiceMicInfoModel;", "_blueRobotAttenuationObservable", "Lkotlin/Pair;", "", "", "_cancelForbidMicObservable", "_cancelLockMicObservable", "_cancelMicSeatApplyObservable", "_changeMicObservable", "", "_closeBlueBotAirAbsorbObservable", "_closeBlueBotBlurObservable", "_closeBotObservable", "_closeRedBotAirAbsorbObservable", "_closeRedBotBlurObservable", "_forbidMicObservable", "_joinObservable", "_kickMicObservable", "_leaveMicObservable", "_leaveSyncRoomObservable", "_lockMicObservable", "_muteMicObservable", "_openBlueBotAirAbsorbObservable", "_openBlueBotBlurObservable", "_openBotObservable", "_openRedBotAirAbsorbObservable", "_openRedBotBlurObservable", "_redRobotAttenuationObservable", "_rejectMicInvitationObservable", "_robotVolumeObservable", "_roomDetailsObservable", "Lio/agora/scene/voice/spatial/model/VoiceRoomInfo;", "_roomNoticeObservable", "", "_startMicSeatApplyObservable", "_unMuteMicObservable", "_updateRoomMemberObservable", "mRepository", "Lio/agora/scene/voice/spatial/viewmodel/repositories/VoiceRoomLivingRepository;", "getMRepository", "()Lio/agora/scene/voice/spatial/viewmodel/repositories/VoiceRoomLivingRepository;", "mRepository$delegate", "Lkotlin/Lazy;", "acceptMicSeatInvitation", "", "acceptMicSeatInvitationObservable", "Landroidx/lifecycle/LiveData;", "blueRobotAttenuationObservable", "cancelForbidMicObservable", "cancelLockMicObservable", "cancelMicSeatApply", "userId", "cancelMicSeatApplyObservable", "cancelMuteMic", "micIndex", "changeMic", "oldIndex", "newIndex", "changeMicObservable", "closeBlueBotAirAbsorbObservable", "closeBlueBotBlurObservable", "closeBotObservable", "closeRedBotAirAbsorbObservable", "closeRedBotBlurObservable", "enableBlueRobotAirAbsorb", "active", "enableBlueRobotBlur", "enableRedRobotAirAbsorb", "enableRedRobotBlur", "enableRobot", "fetchRoomDetail", "voiceRoomModel", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "forbidMic", "forbidMicObservable", "initSdkJoin", "roomKitBean", "Lio/agora/scene/voice/spatial/model/RoomKitBean;", "joinObservable", "kickMicObservable", "kickOff", "leaveMic", "leaveMicObservable", "leaveSyncManagerRoom", "roomId", "isRoomOwnerLeave", "leaveSyncRoomObservable", "lockMic", "lockMicObservable", "muteLocal", "muteMicObservable", "openBlueBotAirAbsorbObservable", "openBlueBotBlurObservable", "openBotObservable", "openRedBotAirAbsorbObservable", "openRedBotBlurObservable", "redRobotAttenuationObservable", "refuseInvite", "rejectMicInvitationObservable", "robotVolumeObservable", "roomDetailsObservable", "roomNoticeObservable", "startMicSeatApply", "(Ljava/lang/Integer;)V", "startMicSeatApplyObservable", "unLockMic", "unMuteLocal", "unMuteMicObservable", "updateAnnouncement", "notice", "updateBlueRoBotAttenuation", "attenuation", "updateBotVolume", "robotVolume", "updateRedRoBotAttenuation", "updateRoomMemberObservable", "voice_spatial_release"})
public final class VoiceRoomLivingViewModel extends androidx.lifecycle.ViewModel {
    private final kotlin.Lazy mRepository$delegate = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceRoomInfo>> _roomDetailsObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _joinObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.String, java.lang.Boolean>>> _roomNoticeObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _openBotObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _closeBotObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.Integer, java.lang.Boolean>>> _robotVolumeObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _openBlueBotAirAbsorbObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _closeBlueBotAirAbsorbObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _openRedBotAirAbsorbObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _closeRedBotAirAbsorbObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _openBlueBotBlurObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _closeBlueBotBlurObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _openRedBotBlurObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _closeRedBotBlurObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.Integer, java.lang.Boolean>>> _blueRobotAttenuationObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.Integer, java.lang.Boolean>>> _redRobotAttenuationObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> _muteMicObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> _unMuteMicObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> _leaveMicObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> _forbidMicObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> _cancelForbidMicObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> _kickMicObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _rejectMicInvitationObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> _lockMicObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> _cancelLockMicObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _startMicSeatApplyObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _cancelMicSeatApplyObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel>>> _changeMicObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> _acceptMicSeatInvitationObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _leaveSyncRoomObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _updateRoomMemberObservable = null;
    
    public VoiceRoomLivingViewModel() {
        super();
    }
    
    private final io.agora.scene.voice.spatial.viewmodel.repositories.VoiceRoomLivingRepository getMRepository() {
        return null;
    }
    
    /**
     * 房间详情
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceRoomInfo>> roomDetailsObservable() {
        return null;
    }
    
    /**
     * 加入im房间&&rtc 频道
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> joinObservable() {
        return null;
    }
    
    /**
     * 更新公告
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.String, java.lang.Boolean>>> roomNoticeObservable() {
        return null;
    }
    
    /**
     * 打开机器人
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> openBotObservable() {
        return null;
    }
    
    /**
     * 关闭机器人
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> closeBotObservable() {
        return null;
    }
    
    /**
     * 改变机器人音量
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.Integer, java.lang.Boolean>>> robotVolumeObservable() {
        return null;
    }
    
    /**
     * 打开蓝色机器人空气衰减
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> openBlueBotAirAbsorbObservable() {
        return null;
    }
    
    /**
     * 关闭蓝色机器人空气衰减
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> closeBlueBotAirAbsorbObservable() {
        return null;
    }
    
    /**
     * 打开红色机器人空气衰减
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> openRedBotAirAbsorbObservable() {
        return null;
    }
    
    /**
     * 关闭红色机器人空气衰减
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> closeRedBotAirAbsorbObservable() {
        return null;
    }
    
    /**
     * 打开蓝色机器人模糊
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> openBlueBotBlurObservable() {
        return null;
    }
    
    /**
     * 关闭蓝色机器人模糊
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> closeBlueBotBlurObservable() {
        return null;
    }
    
    /**
     * 打开红色机器人模糊
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> openRedBotBlurObservable() {
        return null;
    }
    
    /**
     * 关闭红色机器人模糊
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> closeRedBotBlurObservable() {
        return null;
    }
    
    /**
     * 蓝色机器人衰减系数
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.Integer, java.lang.Boolean>>> blueRobotAttenuationObservable() {
        return null;
    }
    
    /**
     * 红色机器人衰减系数
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<kotlin.Pair<java.lang.Integer, java.lang.Boolean>>> redRobotAttenuationObservable() {
        return null;
    }
    
    /**
     * 本地禁麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> muteMicObservable() {
        return null;
    }
    
    /**
     * 取消本地禁麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> unMuteMicObservable() {
        return null;
    }
    
    /**
     * 下麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> leaveMicObservable() {
        return null;
    }
    
    /**
     * 禁言指定麦位
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> forbidMicObservable() {
        return null;
    }
    
    /**
     * 取消禁言指定麦位
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> cancelForbidMicObservable() {
        return null;
    }
    
    /**
     * 踢用户下麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> kickMicObservable() {
        return null;
    }
    
    /**
     * 用户拒绝上麦申请
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> rejectMicInvitationObservable() {
        return null;
    }
    
    /**
     * 锁麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> lockMicObservable() {
        return null;
    }
    
    /**
     * 取消锁麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> cancelLockMicObservable() {
        return null;
    }
    
    /**
     * 申请上麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> startMicSeatApplyObservable() {
        return null;
    }
    
    /**
     * 取消申请
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> cancelMicSeatApplyObservable() {
        return null;
    }
    
    /**
     * 换麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel>>> changeMicObservable() {
        return null;
    }
    
    /**
     * 接受邀请
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>> acceptMicSeatInvitationObservable() {
        return null;
    }
    
    /**
     * 离开syncManager 房间
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> leaveSyncRoomObservable() {
        return null;
    }
    
    /**
     * 更新成员列表
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> updateRoomMemberObservable() {
        return null;
    }
    
    /**
     * 获取详情
     */
    public final void fetchRoomDetail(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel) {
    }
    
    public final void initSdkJoin(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.RoomKitBean roomKitBean) {
    }
    
    public final void enableRobot(boolean active) {
    }
    
    public final void updateBotVolume(int robotVolume) {
    }
    
    public final void enableBlueRobotAirAbsorb(boolean active) {
    }
    
    public final void enableRedRobotAirAbsorb(boolean active) {
    }
    
    public final void enableBlueRobotBlur(boolean active) {
    }
    
    public final void enableRedRobotBlur(boolean active) {
    }
    
    public final void updateBlueRoBotAttenuation(int attenuation) {
    }
    
    public final void updateRedRoBotAttenuation(int attenuation) {
    }
    
    public final void updateAnnouncement(@org.jetbrains.annotations.NotNull()
    java.lang.String notice) {
    }
    
    public final void muteLocal(int micIndex) {
    }
    
    public final void unMuteLocal(int micIndex) {
    }
    
    public final void leaveMic(int micIndex) {
    }
    
    public final void forbidMic(int micIndex) {
    }
    
    public final void cancelMuteMic(int micIndex) {
    }
    
    public final void kickOff(int micIndex) {
    }
    
    public final void acceptMicSeatInvitation() {
    }
    
    public final void refuseInvite() {
    }
    
    public final void lockMic(int micIndex) {
    }
    
    public final void unLockMic(int micIndex) {
    }
    
    public final void startMicSeatApply(@org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex) {
    }
    
    public final void cancelMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
    }
    
    public final void changeMic(int oldIndex, int newIndex) {
    }
    
    public final void leaveSyncManagerRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, boolean isRoomOwnerLeave) {
    }
}