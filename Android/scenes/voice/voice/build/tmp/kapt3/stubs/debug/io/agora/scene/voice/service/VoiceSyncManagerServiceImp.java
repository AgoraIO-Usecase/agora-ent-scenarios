package io.agora.scene.voice.service;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u00a8\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010$\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b)\u0018\u00002\u00020\u0001B+\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u001c\u0010\u0004\u001a\u0018\u0012\f\u0012\n\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u0012\u0004\u0012\u00020\b\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\tJJ\u0010!\u001a\u00020\b2\u0006\u0010\"\u001a\u00020\u001028\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010(\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JB\u0010*\u001a\u00020\b28\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010(\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JH\u0010+\u001a\u00020\b2\u0006\u0010\"\u001a\u00020\u001026\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001d\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016J^\u0010,\u001a\u00020\b2\u0006\u0010-\u001a\u00020\u000b2\u0006\u0010.\u001a\u00020\u000b2D\u0010#\u001a@\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012!\u0012\u001f\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020(\u0018\u00010/\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JH\u00100\u001a\u00020\b2\u0006\u00101\u001a\u00020226\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u0014\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JH\u00103\u001a\u00020\b2\u0006\u00104\u001a\u00020\u001d26\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001d\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JF\u00105\u001a\u00020\b2<\u0010#\u001a8\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020706\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JH\u00108\u001a\u00020\b2>\u0010#\u001a:\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u001b\u0012\u0019\u0012\u0004\u0012\u000209\u0018\u000106\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JJ\u0010:\u001a\u00020\b2\u0006\u0010;\u001a\u00020\u001428\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010<\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JF\u0010=\u001a\u00020\b2<\u0010#\u001a8\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020706\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JN\u0010>\u001a\u00020\b2\u0006\u0010?\u001a\u00020\u000b2<\u0010#\u001a8\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020\u001406\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JF\u0010@\u001a\u00020\b2<\u0010#\u001a8\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020706\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JJ\u0010A\u001a\u00020\b2\u0006\u0010B\u001a\u00020\u000b28\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010(\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016J\u000e\u0010C\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016H\u0016J\u0016\u0010D\u001a\u00020\b2\f\u0010E\u001a\b\u0012\u0004\u0012\u00020\b0\u001bH\u0002JJ\u0010F\u001a\u00020\b2\u0006\u0010G\u001a\u00020\u001028\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010\u0014\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JN\u0010H\u001a\u00020\b2\f\u0010I\u001a\b\u0012\u0004\u0012\u00020\u00100\u001626\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001d\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JJ\u0010J\u001a\u00020\b2\u0006\u0010B\u001a\u00020\u000b28\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010(\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JJ\u0010K\u001a\u00020\b2\u0006\u0010B\u001a\u00020\u000b28\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010(\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JP\u0010L\u001a\u00020\b2\u0006\u0010G\u001a\u00020\u00102\u0006\u0010M\u001a\u00020\u001d26\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001d\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JJ\u0010N\u001a\u00020\b2\u0006\u0010B\u001a\u00020\u000b28\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010(\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JJ\u0010O\u001a\u00020\b2\u0006\u0010B\u001a\u00020\u000b28\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010(\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016J@\u0010P\u001a\u00020\b26\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001d\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016J\b\u0010Q\u001a\u00020\bH\u0016J\u001a\u0010R\u001a\u00020\b2\u0006\u0010G\u001a\u00020\u00102\b\b\u0002\u0010S\u001a\u00020\u001dH\u0002JO\u0010T\u001a\u00020\b2\b\u0010B\u001a\u0004\u0018\u00010\u000b26\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001d\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016\u00a2\u0006\u0002\u0010UJW\u0010V\u001a\u00020\b2\u0006\u0010\"\u001a\u00020\u00102\b\u0010B\u001a\u0004\u0018\u00010\u000b26\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001d\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016\u00a2\u0006\u0002\u0010WJ\u0010\u0010X\u001a\u00020\b2\u0006\u0010Y\u001a\u00020\u0017H\u0016J\u0016\u0010Z\u001a\u00020\b2\f\u0010[\u001a\b\u0012\u0004\u0012\u00020\b0\u001bH\u0016JJ\u0010\\\u001a\u00020\b2\u0006\u0010B\u001a\u00020\u000b28\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010(\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JJ\u0010]\u001a\u00020\b2\u0006\u0010B\u001a\u00020\u000b28\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010(\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JJ\u0010^\u001a\u00020\b2\u0006\u0010B\u001a\u00020\u000b28\u0010#\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0015\u0012\u0013\u0018\u00010(\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016J\b\u0010_\u001a\u00020\bH\u0016JH\u0010`\u001a\u00020\b2\u0006\u0010a\u001a\u00020\u001026\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001d\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016JH\u0010b\u001a\u00020\b2\u0006\u0010c\u001a\u00020\u000b26\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001d\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016J@\u0010d\u001a\u00020\b26\u0010#\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u001d\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0004\u0012\u00020\b0$H\u0016R\u000e\u0010\n\u001a\u00020\u000bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R$\u0010\u0004\u001a\u0018\u0012\f\u0012\n\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u0012\u0004\u0012\u00020\b\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00140\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u001a\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u001fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u0010X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006e"}, d2 = {"Lio/agora/scene/voice/service/VoiceSyncManagerServiceImp;", "Lio/agora/scene/voice/service/VoiceServiceProtocol;", "context", "Landroid/content/Context;", "errorHandler", "Lkotlin/Function1;", "Ljava/lang/Exception;", "Lkotlin/Exception;", "", "(Landroid/content/Context;Lkotlin/jvm/functions/Function1;)V", "ROOM_AVAILABLE_DURATION", "", "mSceneReference", "Lio/agora/syncmanager/rtm/SceneReference;", "objIdOfRoomNo", "", "", "roomChecker", "Lio/agora/scene/voice/service/RoomChecker;", "roomMap", "Lio/agora/scene/voice/model/VoiceRoomModel;", "roomServiceSubscribeDelegates", "", "Lio/agora/scene/voice/service/VoiceRoomSubscribeDelegate;", "roomSubscribeListener", "Lio/agora/syncmanager/rtm/Sync$EventListener;", "roomTimeUpSubscriber", "Lkotlin/Function0;", "syncUtilsInit", "", "timerRoomEndRun", "Ljava/lang/Runnable;", "voiceSceneId", "acceptMicSeatApply", "chatUid", "completion", "Lkotlin/Function2;", "Lkotlin/ParameterName;", "name", "error", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "result", "acceptMicSeatInvitation", "cancelMicSeatApply", "changeMic", "oldIndex", "newIndex", "", "createRoom", "inputModel", "Lio/agora/scene/voice/model/VoiceCreateRoomModel;", "enableRobot", "enable", "fetchApplicantsList", "", "Lio/agora/scene/voice/model/VoiceMemberModel;", "fetchGiftContribute", "Lio/agora/scene/voice/model/VoiceRankUserModel;", "fetchRoomDetail", "voiceRoomModel", "Lio/agora/scene/voice/model/VoiceRoomInfo;", "fetchRoomInvitedMembers", "fetchRoomList", "page", "fetchRoomMembers", "forbidMic", "micIndex", "getSubscribeDelegates", "initScene", "complete", "joinRoom", "roomId", "kickMemberOutOfRoom", "chatUidList", "kickOff", "leaveMic", "leaveRoom", "isRoomOwnerLeave", "lockMic", "muteLocal", "refuseInvite", "reset", "resetCacheInfo", "isRoomDestroyed", "startMicSeatApply", "(Ljava/lang/Integer;Lkotlin/jvm/functions/Function2;)V", "startMicSeatInvitation", "(Ljava/lang/String;Ljava/lang/Integer;Lkotlin/jvm/functions/Function2;)V", "subscribeEvent", "delegate", "subscribeRoomTimeUp", "onRoomTimeUp", "unForbidMic", "unLockMic", "unMuteLocal", "unsubscribeEvent", "updateAnnouncement", "content", "updateRobotVolume", "value", "updateRoomMembers", "voice_debug"})
public final class VoiceSyncManagerServiceImp implements io.agora.scene.voice.service.VoiceServiceProtocol {
    private final android.content.Context context = null;
    private final kotlin.jvm.functions.Function1<java.lang.Exception, kotlin.Unit> errorHandler = null;
    private final java.lang.String voiceSceneId = "scene_chatRoom";
    private final io.agora.scene.voice.service.RoomChecker roomChecker = null;
    @kotlin.jvm.Volatile()
    private volatile boolean syncUtilsInit = false;
    private io.agora.syncmanager.rtm.SceneReference mSceneReference;
    private final java.util.Map<java.lang.String, io.agora.scene.voice.model.VoiceRoomModel> roomMap = null;
    private final java.util.Map<java.lang.String, java.lang.String> objIdOfRoomNo = null;
    private final java.util.List<io.agora.syncmanager.rtm.Sync.EventListener> roomSubscribeListener = null;
    private final java.util.List<io.agora.scene.voice.service.VoiceRoomSubscribeDelegate> roomServiceSubscribeDelegates = null;
    private kotlin.jvm.functions.Function0<kotlin.Unit> roomTimeUpSubscriber;
    private final int ROOM_AVAILABLE_DURATION = 1200000;
    private final java.lang.Runnable timerRoomEndRun = null;
    
    public VoiceSyncManagerServiceImp(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> errorHandler) {
        super();
    }
    
    /**
     * 注册订阅
     * @param delegate 聊天室内IM回调处理
     */
    @java.lang.Override()
    public void subscribeEvent(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.service.VoiceRoomSubscribeDelegate delegate) {
    }
    
    /**
     * 取消订阅
     */
    @java.lang.Override()
    public void unsubscribeEvent() {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.util.List<io.agora.scene.voice.service.VoiceRoomSubscribeDelegate> getSubscribeDelegates() {
        return null;
    }
    
    @java.lang.Override()
    public void reset() {
    }
    
    /**
     * 获取房间列表
     * @param page 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
     */
    @java.lang.Override()
    public void fetchRoomList(int page, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.model.VoiceRoomModel>, kotlin.Unit> completion) {
    }
    
    /**
     * 创建房间
     * @param inputModel 输入的房间信息
     */
    @java.lang.Override()
    public void createRoom(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceCreateRoomModel inputModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceRoomModel, kotlin.Unit> completion) {
    }
    
    /**
     * 加入房间
     * @param roomId 房间id
     */
    @java.lang.Override()
    public void joinRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceRoomModel, kotlin.Unit> completion) {
    }
    
    /**
     * 离开房间
     * @param roomId 房间id
     */
    @java.lang.Override()
    public void leaveRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, boolean isRoomOwnerLeave, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 获取房间详情
     * @param voiceRoomModel 房间概要
     */
    @java.lang.Override()
    public void fetchRoomDetail(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceRoomModel voiceRoomModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceRoomInfo, kotlin.Unit> completion) {
    }
    
    /**
     * 获取排行榜列表
     */
    @java.lang.Override()
    public void fetchGiftContribute(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.model.VoiceRankUserModel>, kotlin.Unit> completion) {
    }
    
    /**
     * 获取邀请列表（过滤已在麦位成员）
     */
    @java.lang.Override()
    public void fetchRoomInvitedMembers(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.model.VoiceMemberModel>, kotlin.Unit> completion) {
    }
    
    /**
     * 获取房间成员列表
     */
    @java.lang.Override()
    public void fetchRoomMembers(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.model.VoiceMemberModel>, kotlin.Unit> completion) {
    }
    
    @java.lang.Override()
    public void kickMemberOutOfRoom(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> chatUidList, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 更新用户列表
     */
    @java.lang.Override()
    public void updateRoomMembers(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 申请列表
     */
    @java.lang.Override()
    public void fetchApplicantsList(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.model.VoiceMemberModel>, kotlin.Unit> completion) {
    }
    
    /**
     * 申请上麦
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void startMicSeatApply(@org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 同意申请
     * @param chatUid 环信用户id
     */
    @java.lang.Override()
    public void acceptMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 取消上麦
     * @param chatUid im uid
     */
    @java.lang.Override()
    public void cancelMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 邀请用户上麦
     * @param chatUid im uid
     */
    @java.lang.Override()
    public void startMicSeatInvitation(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid, @org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 接受邀请
     */
    @java.lang.Override()
    public void acceptMicSeatInvitation(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 拒绝邀请
     */
    @java.lang.Override()
    public void refuseInvite(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * mute
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void muteLocal(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * unMute
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void unMuteLocal(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 禁言指定麦位置
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void forbidMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 取消禁言指定麦位置
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void unForbidMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 锁麦
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void lockMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 取消锁麦
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void unLockMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 踢用户下麦
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void kickOff(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 下麦
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void leaveMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 换麦
     * @param oldIndex 老麦位index
     * @param newIndex 新麦位index
     */
    @java.lang.Override()
    public void changeMic(int oldIndex, int newIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel>, kotlin.Unit> completion) {
    }
    
    /**
     * 更新公告
     * @param content 公告内容
     */
    @java.lang.Override()
    public void updateAnnouncement(@org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 是否启用机器人
     * @param enable true 启动机器人，false 关闭机器人
     */
    @java.lang.Override()
    public void enableRobot(boolean enable, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 更新机器人音量
     * @param value 音量
     */
    @java.lang.Override()
    public void updateRobotVolume(int value, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    @java.lang.Override()
    public void subscribeRoomTimeUp(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRoomTimeUp) {
    }
    
    private final void initScene(kotlin.jvm.functions.Function0<kotlin.Unit> complete) {
    }
    
    private final void resetCacheInfo(java.lang.String roomId, boolean isRoomDestroyed) {
    }
}