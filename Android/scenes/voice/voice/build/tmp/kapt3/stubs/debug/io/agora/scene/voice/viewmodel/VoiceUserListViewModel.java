package io.agora.scene.voice.viewmodel;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0010\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001bJ\u0012\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u001dJ\u0018\u0010\u001e\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00050\u001dJ\u0018\u0010\u001f\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\b0\u00050\u001dJ\u0006\u0010 \u001a\u00020\u0019J\u0006\u0010!\u001a\u00020\u0019J\u0006\u0010\"\u001a\u00020\u0019J\u0006\u0010#\u001a\u00020\u0019J\u0018\u0010$\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00050\u001dJ\u0016\u0010%\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010&\u001a\u00020\u000eJ\u0012\u0010\'\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\u00050\u001dJ\u0018\u0010(\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00050\u001dJ\u001d\u0010)\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010*\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\u0002\u0010+J\u0012\u0010,\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00050\u001dR\u001a\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u0007\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\n\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\b0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\f\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u000f\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0012\u001a\u00020\u00138BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0016\u0010\u0017\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006-"}, d2 = {"Lio/agora/scene/voice/viewmodel/VoiceUserListViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "_acceptMicSeatApplyObservable", "Lio/agora/voice/common/viewmodel/SingleSourceLiveData;", "Lio/agora/voice/common/net/Resource;", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "_applicantsListObservable", "", "Lio/agora/scene/voice/model/VoiceMemberModel;", "_contributeListObservable", "Lio/agora/scene/voice/model/VoiceRankUserModel;", "_inviteListObservable", "_kickMemberObservable", "", "_roomMemberObservable", "_startMicSeatInvitationObservable", "", "mRepository", "Lio/agora/scene/voice/viewmodel/repositories/VoiceUserListRepository;", "getMRepository", "()Lio/agora/scene/voice/viewmodel/repositories/VoiceUserListRepository;", "mRepository$delegate", "Lkotlin/Lazy;", "acceptMicSeatApply", "", "chatUid", "", "acceptMicSeatApplyObservable", "Landroidx/lifecycle/LiveData;", "applicantsListObservable", "contributeListObservable", "fetchApplicantsList", "fetchGiftContribute", "fetchInviteList", "fetchMemberList", "inviteListObservable", "kickMembersOutOfTheRoom", "index", "kickOffObservable", "memberListObservable", "startMicSeatInvitation", "micIndex", "(Ljava/lang/String;Ljava/lang/Integer;)V", "startMicSeatInvitationObservable", "voice_debug"})
public final class VoiceUserListViewModel extends androidx.lifecycle.ViewModel {
    private final kotlin.Lazy mRepository$delegate = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.model.VoiceMemberModel>>> _applicantsListObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.model.VoiceMemberModel>>> _inviteListObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.model.VoiceRankUserModel>>> _contributeListObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> _startMicSeatInvitationObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> _acceptMicSeatApplyObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.model.VoiceMemberModel>>> _roomMemberObservable = null;
    private final io.agora.voice.common.viewmodel.SingleSourceLiveData<io.agora.voice.common.net.Resource<java.lang.Integer>> _kickMemberObservable = null;
    
    public VoiceUserListViewModel() {
        super();
    }
    
    private final io.agora.scene.voice.viewmodel.repositories.VoiceUserListRepository getMRepository() {
        return null;
    }
    
    /**
     * 申请列表
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.model.VoiceMemberModel>>> applicantsListObservable() {
        return null;
    }
    
    /**
     * 邀请列表
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.model.VoiceMemberModel>>> inviteListObservable() {
        return null;
    }
    
    /**
     * 成员列表
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.model.VoiceMemberModel>>> memberListObservable() {
        return null;
    }
    
    /**
     * 榜单列表
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.util.List<io.agora.scene.voice.model.VoiceRankUserModel>>> contributeListObservable() {
        return null;
    }
    
    /**
     * 邀请用户上麦
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Boolean>> startMicSeatInvitationObservable() {
        return null;
    }
    
    /**
     * 同意上麦申请
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<io.agora.scene.voice.model.VoiceMicInfoModel>> acceptMicSeatApplyObservable() {
        return null;
    }
    
    /**
     * 踢出用户
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<io.agora.voice.common.net.Resource<java.lang.Integer>> kickOffObservable() {
        return null;
    }
    
    /**
     * 申请列表
     */
    public final void fetchApplicantsList() {
    }
    
    /**
     * 邀请列表
     */
    public final void fetchInviteList() {
    }
    
    /**
     * 贡献排行榜
     */
    public final void fetchGiftContribute() {
    }
    
    /**
     * 成员列表
     */
    public final void fetchMemberList() {
    }
    
    public final void startMicSeatInvitation(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex) {
    }
    
    public final void acceptMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid) {
    }
    
    public final void kickMembersOutOfTheRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, int index) {
    }
}