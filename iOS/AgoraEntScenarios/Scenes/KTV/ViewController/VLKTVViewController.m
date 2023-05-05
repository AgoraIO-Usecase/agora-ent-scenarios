//
//  VLKTVViewController.m
//  VoiceOnLine
//

#import "VLKTVViewController.h"
#import "VLKTVTopView.h"
#import "VLKTVMVView.h"
#import "VLMicSeatList.h"
#import "VLKTVBottomToolbar.h"
#import "VLAudienceIndicator.h"
#import "VLKTVMVIdleView.h"
#import "VLOnLineListVC.h"

#import "VLKTVSettingView.h"
//model
#import "VLSongItmModel.h"
#import "VLRoomListModel.h"
#import "VLRoomSeatModel.h"
#import "VLRoomSelSongModel.h"
#import "VLKTVSelBgModel.h"
#import "UIViewController+VL.h"
#import "VLPopScoreView.h"
#import "VLUserCenter.h"
#import "VLMacroDefine.h"
#import "VLGlobalHelper.h"
#import "VLURLPathConfig.h"
#import "VLToast.h"
#import "VLKTVMVView.h"
#import "UIView+VL.h"
#import "AppContext+KTV.h"
#import "KTVMacro.h"
#import "LSTPopView+KTVModal.h"
#import "HWWeakTimer.h"
#import "VLAlert.h"
#import "VLKTVAlert.h"
#import "KTVDebugManager.h"
@import AgoraRtcKit;
@import AgoraLyricsScore;
@import YYCategories;
@import SDWebImage;


NSInteger ktvApiStreamId = -1;
NSInteger ktvStreamId = -1;

@interface VLKTVViewController ()<
VLKTVTopViewDelegate,
VLKTVMVViewDelegate,
VLMicSeatListDelegate,
VLKTVBottomToolbarDelegate,
VLPopSelBgViewDelegate,
VLPopMoreSelViewDelegate,
VLDropOnLineViewDelegate,
VLAudienceIndicatorDelegate,
VLAudioEffectPickerDelegate,
VLPopSongListDelegate,
VLEffectViewDelegate,
VLKTVSettingViewDelegate,
VLBadNetWorkViewDelegate,
AgoraRtcMediaPlayerDelegate,
AgoraRtcEngineDelegate,
VLPopScoreViewDelegate,
KTVLrcControlDelegate,
KTVApiEventHandlerDelegate,
IMusicLoadStateListener
>

typedef void (^CompletionBlock)(BOOL isSuccess, NSInteger songCode);
@property (nonatomic, strong) VLKTVMVView *MVView;
@property (nonatomic, strong) VLKTVSelBgModel *choosedBgModel;
@property (nonatomic, strong) VLKTVBottomToolbar *bottomView;
@property (nonatomic, strong) VLBelcantoModel *selBelcantoModel;
@property (nonatomic, strong) VLKTVMVIdleView *noBodyOnLineView; // mv空页面
@property (nonatomic, strong) VLKTVTopView *topView;
@property (nonatomic, strong) VLKTVSettingView *settingView;
@property (nonatomic, strong) VLMicSeatList *roomPersonView; //房间麦位视图
@property (nonatomic, strong) VLAudienceIndicator *requestOnLineView;//空位上麦
@property (nonatomic, strong) VLPopSongList *chooseSongView; //点歌视图
@property (nonatomic, strong) VLEffectView *effectView; // 音效视图

@property (nonatomic, strong) VLSongItmModel *choosedSongModel; //点的歌曲
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;

@property (nonatomic, strong) VLPopScoreView *scoreView;

@property (nonatomic, assign) BOOL isNowMicMuted;
@property (nonatomic, assign) BOOL isNowCameraMuted;
@property (nonatomic, assign) BOOL isOnMicSeat;
@property (nonatomic, assign) NSUInteger chorusNum;    //合唱人数
@property (nonatomic, assign) KTVSingRole singRole;    //角色
@property (nonatomic, assign) BOOL isEarOn;
@property (nonatomic, assign) int playoutVolume;
@property (nonatomic, assign) KTVPlayerTrackMode trackMode;  //合唱/伴奏

@property (nonatomic, strong) NSArray <VLRoomSelSongModel*>* selSongsArray;
@property (nonatomic, strong) KTVApiImpl* ktvApi;

@property (nonatomic, strong) LyricModel *lyricModel;
@property (nonatomic, strong) KTVLrcControl *lrcControl;
@property (nonatomic, copy, nullable) CompletionBlock loadMusicCallBack;
@property (nonatomic, assign) NSInteger selectedEffectIndex;
@property (nonatomic, assign) BOOL isPause;
@property (nonatomic, assign) NSInteger retryCount;
@property (nonatomic, assign) BOOL isJoinChorus;
@property (nonatomic, assign) NSInteger coSingerDegree;
@end

@implementation VLKTVViewController

#pragma mark view lifecycles
- (void)dealloc {
    NSLog(@"dealloc:%s",__FUNCTION__);
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = UIColor.blackColor;

    [self subscribeServiceEvent];
    
    // setup view
    [self setBackgroundImage:@"ktv_temp_mainbg"];
    
    UIView *bgView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)];
    bgView.backgroundColor = UIColorMakeWithRGBA(0, 0, 0, 0.6);
    [self.view addSubview:bgView];
    //头部视图
    VLKTVTopView *topView = [[VLKTVTopView alloc]initWithFrame:CGRectMake(0, kStatusBarHeight, SCREEN_WIDTH, 60) withDelegate:self];
    [self.view addSubview:topView];
    self.topView = topView;
    topView.listModel = self.roomModel;
    
    //底部按钮视图
    VLKTVBottomToolbar *bottomView = [[VLKTVBottomToolbar alloc] initWithFrame:CGRectMake(0, SCREEN_HEIGHT-50-kSafeAreaBottomHeight, SCREEN_WIDTH, 50) withDelegate:self withRoomNo:self.roomModel.roomNo withData:self.seatsArray];
    bottomView.backgroundColor = [UIColor clearColor];
    self.bottomView = bottomView;
    [self.view addSubview:bottomView];
    
    //去掉首尾的高度
    CGFloat musicHeight = SCREEN_HEIGHT -50 - kSafeAreaBottomHeight - kStatusBarHeight - 60;
    
    //MV视图(显示歌词...)
    CGFloat mvViewTop = topView.bottom;
    self.MVView = [[VLKTVMVView alloc]initWithFrame:CGRectMake(15, mvViewTop, SCREEN_WIDTH - 30, musicHeight * 0.5) withDelegate:self];
    [self.view addSubview:self.MVView];
    
    //房间麦位视图
    VLMicSeatList *personView = [[VLMicSeatList alloc] initWithFrame:CGRectMake(0, self.MVView.bottom + 20, SCREEN_WIDTH, musicHeight * 0.5) withDelegate:self withRTCkit:self.RTCkit];
    self.roomPersonView = personView;
    self.roomPersonView.roomSeatsArray = self.seatsArray;
    [self.view addSubview:personView];

    //空位上麦视图
    VLAudienceIndicator *requestOnLineView = [[VLAudienceIndicator alloc]initWithFrame:CGRectMake(0, SCREEN_HEIGHT-kSafeAreaBottomHeight-56-VLREALVALUE_WIDTH(30), SCREEN_WIDTH, 56) withDelegate:self];
    self.requestOnLineView = requestOnLineView;
    [self.view addSubview:requestOnLineView];
    
    //start join
    [self joinRTCChannel];
    
    self.isOnMicSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
    
    //处理背景
    [self prepareBgImage];
    [[UIApplication sharedApplication] setIdleTimerDisabled: YES];
    
    //add debug
    [self.topView addGestureRecognizer:[KTVDebugManager createStartGesture]];
    
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [UIViewController popGestureClose:self];
    
    
    //请求已点歌曲
    [self refreshChoosedSongList:nil];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [UIViewController popGestureOpen:self];
    [self leaveRTCChannel];
    [[UIApplication sharedApplication] setIdleTimerDisabled: NO];
}

- (void)viewDidDisappear:(BOOL)animated
{
    KTVLogInfo(@"Agora - destroy RTCEngine");
    [AgoraRtcEngineKit destroy];
}

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return YES;
}
// 是否允许手动滑回 @return true 是、 false否
- (BOOL)forceEnableInteractivePopGestuzreRecognizer {
    return NO;
}

#pragma mark service handler
- (void)subscribeServiceEvent {
    VL(weakSelf);
    [[AppContext ktvServiceImp] unsubscribeAll];
    [[AppContext ktvServiceImp] subscribeUserListCountChangedWithBlock:^(NSUInteger count) {
        //TODO
        [weakSelf setRoomUsersCount:count];
    }];
    
    [[AppContext ktvServiceImp] subscribeSeatListChangedWithBlock:^(KTVSubscribe status, VLRoomSeatModel* seatModel) {
        VLRoomSeatModel* model = [self getUserSeatInfoWithIndex:seatModel.seatIndex];
        if (model == nil) {
            NSAssert(NO, @"model == nil");
            return;
        }
        
        if (status == KTVSubscribeCreated || status == KTVSubscribeUpdated) {
            //上麦消息 / 是否打开视频 / 是否静音
            [model resetWithInfo:seatModel];
            [weakSelf setSeatsArray:weakSelf.seatsArray];
        } else if (status == KTVSubscribeDeleted) {
            // 下麦消息
            
            // 下麦重置占位模型
            [model resetWithInfo:nil];
            [weakSelf setSeatsArray:weakSelf.seatsArray];
        }
        
        VLRoomSelSongModel *song = weakSelf.selSongsArray.firstObject;
        [weakSelf.MVView updateUIWithSong:song role:weakSelf.singRole];
        [weakSelf.roomPersonView reloadSeatIndex:model.seatIndex];
        
        [weakSelf onSeatFull];

    }];
    
    [[AppContext ktvServiceImp] subscribeRoomStatusChangedWithBlock:^(KTVSubscribe status, VLRoomListModel * roomInfo) {
        if (KTVSubscribeUpdated == status) {
            //切换背景
            
            //mv bg / room member count did changed
            VLKTVSelBgModel* selBgModel = [VLKTVSelBgModel new];
            selBgModel.imageName = [NSString stringWithFormat:@"ktv_mvbg%ld", roomInfo.bgOption];
            selBgModel.isSelect = YES;
            weakSelf.choosedBgModel = selBgModel;
        } else if (status == KTVSubscribeDeleted) {
            //房主关闭房间
            if ([roomInfo.creatorNo isEqualToString:VLUserCenter.user.id]) {
                NSString *mes = @"连接超时，房间已解散";
                [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sceneImageWithName:@"empty"] message:mes buttonTitle:KTVLocalizedString(@"确定") completion:^(bool flag, NSString * _Nullable text) {
                    [[VLKTVAlert shared]dismiss];
                    [weakSelf leaveRoom];
                }];
                return;
            }
            
            [weakSelf popForceLeaveRoom];
        }
    }];
    
    //callback if choose song list did changed
    [[AppContext ktvServiceImp] subscribeChooseSongChangedWithBlock:^(KTVSubscribe status, VLRoomSelSongModel * songInfo, NSArray<VLRoomSelSongModel*>* songArray) {
        // update in-ear monitoring
        [weakSelf _checkInEarMonitoring];
        
        if (KTVSubscribeDeleted == status) {
            if(weakSelf.singRole == KTVSingRoleCoSinger){
                [weakSelf showScoreViewWithScore:[weakSelf.lrcControl getAvgScore]];
            }
            BOOL success = [weakSelf removeSelSongWithSongNo:[songInfo.songNo integerValue] sync:NO];
            if (!success) {
                weakSelf.selSongsArray = songArray;
                KTVLogInfo(@"removeSelSongWithSongNo fail, reload it");
            }
            //清除合唱者总分
            weakSelf.coSingerDegree = 0;
        } else {
            VLRoomSelSongModel* song = [weakSelf selSongWithSongNo:songInfo.songNo];
            //add new song
            KTVLogInfo(@"song did updated: %@ status: %ld", song.name, songInfo.status);
            weakSelf.selSongsArray = [NSMutableArray arrayWithArray:songArray];
        }
    }];
    
    [[AppContext ktvServiceImp] subscribeNetworkStatusChangedWithBlock:^(KTVServiceNetworkStatus status) {
        if (status != KTVServiceNetworkStatusOpen) {
//            [VLToast toast:[NSString stringWithFormat:@"network changed: %ld", status]];
            return;
        }
        [weakSelf subscribeServiceEvent];
        [weakSelf _fetchServiceAllData];
    }];
    
    [[AppContext ktvServiceImp] subscribeRoomWillExpire:^{
        bool isOwner = [weakSelf.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
        NSString *mes = isOwner ? @"您已体验超过20分钟，当前房间已过期，请退出重新创建房间" : @"当前房间已过期,请退出";
        [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sceneImageWithName:@"empty"] message:mes buttonTitle:KTVLocalizedString(@"确定") completion:^(bool flag, NSString * _Nullable text) {
            [[VLKTVAlert shared]dismiss];
            [weakSelf leaveRoom];
        }];
    }];
}

#pragma mark view helpers
- (void)prepareBgImage {
    if (self.roomModel.bgOption) {
        VLKTVSelBgModel *selBgModel = [VLKTVSelBgModel new];
        selBgModel.imageName = [NSString stringWithFormat:@"ktv_mvbg%ld", self.roomModel.bgOption];
        selBgModel.isSelect = YES;
        self.choosedBgModel = selBgModel;
    }
}

//更换MV背景
- (void)popSelMVBgView {
    [LSTPopView popSelMVBgViewWithParentView:self.view
                                     bgModel:self.choosedBgModel
                                withDelegate:self];
}

//弹出更多
- (void)popSelMoreView {
    [LSTPopView popSelMoreViewWithParentView:self.view
                                withDelegate:self];
}

//弹出下麦视图
- (void)popDropLineViewWithSeatModel:(VLRoomSeatModel *)seatModel {
    [LSTPopView popDropLineViewWithParentView:self.view
                                withSeatModel:seatModel
                                 withDelegate:self];
}

//弹出美声视图
- (void)popBelcantoView {
    [LSTPopView popBelcantoViewWithParentView:self.view
                            withBelcantoModel:self.selBelcantoModel
                                 withDelegate:self];
}

//弹出点歌视图
- (void)popUpChooseSongView:(BOOL)ifChorus {
    LSTPopView* popChooseSongView =
    [LSTPopView popUpChooseSongViewWithParentView:self.view
                                         isChorus:ifChorus
                                  chooseSongArray:self.selSongsArray
                                       withRoomNo:self.roomModel.roomNo
                                     withDelegate:self];
    
    self.chooseSongView = (VLPopSongList*)popChooseSongView.currCustomView;
}

//弹出音效
- (void)popSetSoundEffectView {
    LSTPopView* popView = 
    [LSTPopView popSetSoundEffectViewWithParentView:self.view
                                          soundView:self.effectView
                                       withDelegate:self];
    self.effectView = (VLEffectView*)popView.currCustomView;
    [self.effectView setSelectedIndex:self.selectedEffectIndex];
}

//网络差视图
- (void)popBadNetWrokTipView {
    [LSTPopView popBadNetWrokTipViewWithParentView:self.view
                                      withDelegate:self];
}

//用户弹框离开房间
- (void)popForceLeaveRoom {
    VL(weakSelf);
    [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sceneImageWithName:@"empty"] message:KTVLocalizedString(@"房主已解散房间,请确认离开房间") buttonTitle:KTVLocalizedString(@"确定") completion:^(bool flag, NSString * _Nullable text) {
        for (BaseViewController *vc in weakSelf.navigationController.childViewControllers) {
            if ([vc isKindOfClass:[VLOnLineListVC class]]) {
//                [weakSelf destroyMediaPlayer];
//                [weakSelf leaveRTCChannel];
                [weakSelf.navigationController popToViewController:vc animated:YES];
            }
        }
        [[VLKTVAlert shared] dismiss];
    }];
}

- (void)showSettingView {
    LSTPopView* popView = [LSTPopView popSettingViewWithParentView:self.view
                                                       settingView:self.settingView
                                                      withDelegate:self];
    
    self.settingView = (VLKTVSettingView*)popView.currCustomView;
    [self.settingView setIspause:self.isPause];
}

- (void)showScoreViewWithScore:(NSInteger)score {
                        //  song:(VLRoomSelSongModel *)song {
    if (score < 0) return;
    if(_scoreView == nil) {
        _scoreView = [[VLPopScoreView alloc] initWithFrame:self.view.bounds withDelegate:self];
        [self.view addSubview:_scoreView];
    }
    KTVLogInfo(@"Avg score for the song: %ld", (long)score);
    [_scoreView configScore:score];
    [self.view bringSubviewToFront:_scoreView];
    self.scoreView.hidden = NO;
}

- (void)popScoreViewDidClickConfirm
{
    KTVLogInfo(@"Using as score view hidding");
    self.scoreView = nil;
}

#pragma mark - rtc callbacks
- (void)rtcEngine:(AgoraRtcEngineKit *)engine didJoinedOfUid:(NSUInteger)uid elapsed:(NSInteger)elapsed
{
    KTVLogInfo(@"didJoinedOfUid: %ld", uid);
//    [self.ktvApi mainRtcEngine:engine didJoinedOfUid:uid elapsed:elapsed];
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine
reportAudioVolumeIndicationOfSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers
      totalVolume:(NSInteger)totalVolume {
    [self.ktvApi didKTVAPIReceiveAudioVolumeIndicationWith:speakers totalVolume:totalVolume];
}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
receiveStreamMessageFromUid:(NSUInteger)uid
         streamId:(NSInteger)streamId
             data:(NSData * _Nonnull)data {    //接收到对方的RTC消息
    
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:data];
//    KTVLogInfo(@"receiveStreamMessageFromUid:%@,streamID:%d,uid:%d",dict,(int)streamId,(int)uid);
    [self.ktvApi didKTVAPIReceiveStreamMessageFromUid:uid streamId:streamId data:data];
    if ([dict[@"cmd"] isEqualToString:@"SingingScore"]) {
        //伴唱显示自己的分数，观众显示主唱的分数
        if(self.singRole == KTVSingRoleCoSinger){
            return;
        }
        int score = [dict[@"score"] intValue];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self showScoreViewWithScore:score];
        });
        
        KTVLogInfo(@"score: %ds",score);
        return;
    } else if([dict[@"cmd"] isEqualToString:@"singleLineScore"]) {//观众接收主唱的分数
        NSLog(@"index: %li", [dict[@"index"] integerValue]);
        if(self.singRole != KTVSingRoleAudience){
            return;
        }
        //观众使用主唱的分数来显示
        NSInteger index = [dict[@"index"] integerValue];
        NSInteger score = [dict[@"score"] integerValue];
        NSInteger cumulativeScore = [dict[@"cumulativeScore"] integerValue];
        NSInteger total = [dict[@"total"] integerValue];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.MVView.lineScoreView showScoreViewWithScore:score];
            [self.MVView.gradeView setScoreWithCumulativeScore:cumulativeScore totalScore:total];
            [self.MVView.incentiveView showWithScore:score];
        });
        NSLog(@"index: %li, score: %li, cumulativeScore: %li, total: %li", index, score, cumulativeScore, total);
    }
}

// Network quality callbacks
- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
   networkQuality:(NSUInteger)uid
        txQuality:(AgoraNetworkQuality)txQuality
        rxQuality:(AgoraNetworkQuality)rxQuality
{
    //    VLLog(@"Agora - network quality : %lu", txQuality);
    if(uid == [VLUserCenter.user.id intValue]) {
        if(txQuality == AgoraNetworkQualityExcellent || txQuality == AgoraNetworkQualityGood) {
            // Good quality
            [self.topView setNetworkQuality:0];
        } else if(txQuality == AgoraNetworkQualityPoor || txQuality == AgoraNetworkQualityBad) {
            // Bad quality
            [self.topView setNetworkQuality:1];
        } else if(txQuality == AgoraNetworkQualityVBad || txQuality == AgoraNetworkQualityDown) {
            // Barely usable
            [self.topView setNetworkQuality:2];
        } else {
            // Unknown or detecting
            [self.topView setNetworkQuality:3];
        }
    }
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine tokenPrivilegeWillExpire:(NSString *)token {
    KTVLogInfo(@"tokenPrivilegeWillExpire: %@", token);
    [[NetworkManager shared] generateTokenWithChannelName:self.roomModel.roomNo
                                                      uid:VLUserCenter.user.id
                                                tokenType:TokenGeneratorTypeToken006
                                                     type:AgoraTokenTypeRtc
                                                  success:^(NSString * token) {
        KTVLogInfo(@"tokenPrivilegeWillExpire rtc renewToken: %@", token);
        [self.RTCkit renewToken:token];
    }];
    
    //TODO: mcc missing token expire callback
    [[NetworkManager shared] generateTokenWithChannelName:self.roomModel.roomNo
                                                      uid:VLUserCenter.user.id
                                                tokenType:TokenGeneratorTypeToken006
                                                     type:AgoraTokenTypeRtm
                                                  success:^(NSString * token) {
        KTVLogInfo(@"tokenPrivilegeWillExpire rtm renewToken: %@", token);
        //TODO(chenpan): mcc missing
//        [self.AgoraMcc renewToken:token];
    }];
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine contentInspectResult:(AgoraContentInspectResult)result {
    KTVLogInfo(@"contentInspectResult: %ld", result);
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine localAudioStats:(AgoraRtcLocalAudioStats *)stats {
    [self.ktvApi didKTVAPILocalAudioStatsWithStats:stats];
}

#pragma mark - action utils / business
- (void)stopPlaySong {
    self.isPause = false;
    self.MVView.joinCoSingerState = KTVJoinCoSingerStateWaitingForJoin;
    [self.ktvApi switchSingerRoleWithNewRole:KTVSingRoleAudience
                           onSwitchRoleState:^(KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
    }];
}

- (void)loadAndPlaySong{
    //清空分数
    [self.MVView.gradeView reset];
    
    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
    
    //TODO: fix score view visible problem while owner reopen the room
    [self.MVView updateUIWithSong:model role:self.singRole];
    [self setCoSingerStateWith:self.singRole];
    if(!model){
        return;
    }
    [self markSongPlaying:model];
    
    //TODO: will remove ktv api adjust playout volume method
    [self setPlayoutVolume:50];
    
//    self.retryCount = 0;
    
    
    KTVSingRole role = [self getUserSingRole];
    KTVSongConfiguration* songConfig = [[KTVSongConfiguration alloc] init];
    songConfig.autoPlay = (role == KTVSingRoleAudience || role == KTVSingRoleCoSinger) ? NO : YES ;
    songConfig.mode = (role == KTVSingRoleAudience || role == KTVSingRoleCoSinger) ? KTVLoadMusicModeLoadLrcOnly : KTVLoadMusicModeLoadMusicAndLrc;
    songConfig.mainSingerUid = [model.userNo integerValue];
    songConfig.songIdentifier = model.songNo;
    
    self.MVView.loadingType = VLKTVMVViewStateLoading;
    [self.MVView setBotViewHidden:true];
    VL(weakSelf);
    self.loadMusicCallBack = ^(BOOL isSuccess, NSInteger songCode) {
        if (!isSuccess) {
            return;
        }
        [weakSelf.MVView setBotViewHidden:false];
        [weakSelf.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
        if(role == KTVSingRoleCoSinger){
            [weakSelf.ktvApi startSingWithSongCode:songCode startPos:0];
        }
    };
    
    [self.lrcControl resetShowOnce];
    [self.ktvApi loadMusicWithSongCode:[model.songNo integerValue] config:songConfig onMusicLoadStateListener:self];

    [weakSelf.ktvApi switchSingerRoleWithNewRole:role
                           onSwitchRoleState:^( KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
        if(state != KTVSwitchRoleStateSuccess) {
            //TODO(chenpan): error toast and retry?
            KTVLogError(@"switchSingerRole error: %ld", reason);
            return;
        }
    }];
    
}

- (void)enterSeatWithIndex:(NSInteger)index completion:(void(^)(NSError*))completion {
    
    KTVOnSeatInputModel* inputModel = [KTVOnSeatInputModel new];
    inputModel.seatIndex = index;
//    VL(weakSelf);
    [[AppContext ktvServiceImp] enterSeatWithInput:inputModel
                                        completion:completion];
}

- (void)leaveSeatWithSeatModel:(VLRoomSeatModel * __nonnull)seatModel
                 withCompletion:(void(^ __nullable)(NSError*))completion {
    if(seatModel.rtcUid == VLUserCenter.user.id) {
        if(seatModel.isVideoMuted == 1) {
            [self.RTCkit stopPreview];
        }
    }
    
    KTVOutSeatInputModel* inputModel = [KTVOutSeatInputModel new];
    inputModel.userNo = seatModel.userNo;
    inputModel.userId = seatModel.rtcUid;
    inputModel.userName = seatModel.name;
    inputModel.userHeadUrl = seatModel.headUrl;
    inputModel.seatIndex = seatModel.seatIndex;
    [[AppContext ktvServiceImp] leaveSeatWithInput:inputModel
                                        completion:completion];
}

- (void)refreshChoosedSongList:(void (^ _Nullable)(void))block{
    VL(weakSelf);
    [[AppContext ktvServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        
        weakSelf.selSongsArray = songArray;
        if(block) {
            block();
        }
    }];
}

- (void)markSongPlaying:(VLRoomSelSongModel *)model {
    if (model.status == VLSongPlayStatusPlaying) {
        return;
    }
    [[AppContext ktvServiceImp] markSongDidPlayWithInput:model
                                              completion:^(NSError * error) {
    }];
}

- (void)syncChoruScore:(NSInteger)score {
    NSDictionary *dict = @{
        @"cmd":@"SingingScore",
        @"score":@(score)
    };
    [self sendStreamMessageWithDict:dict success:nil];
}

- (void)joinChorus {
    
    [self.MVView.gradeView reset];

    if([self getOnMicUserCount] == 8 && !_isOnMicSeat){
        [VLToast toast:@"“麦位已满，请在他人下麦后重试"];
        return;
    }
    
    if(self.RTCkit.getConnectionState != AgoraConnectionStateConnected){
        self.MVView.joinCoSingerState = KTVJoinCoSingerStateWaitingForJoin;
        [VLToast toast:@"加入合唱失败，reson:连接已断开"];
        return;
    }
    
    if (![self getJoinChorusEnable]) {
        KTVLogInfo(@"getJoinChorusEnable false");
        self.MVView.joinCoSingerState = KTVJoinCoSingerStateWaitingForJoin;
        return;
    }
    
    //没有上麦需要先上麦
    if ([self getCurrentUserSeatInfo] == nil) {
        for (int i = 1; i < self.seatsArray.count; i++) {
            VLRoomSeatModel* seat = self.seatsArray[i];
            
            if (seat.rtcUid == 0) {
                VL(weakSelf);
                KTVLogError(@"before enterSeat error");
                self.isJoinChorus = true;
                [self enterSeatWithIndex:i completion:^(NSError *error) {
                    if(error){
                        KTVLogError(@"enterSeat error:%@", error.description);
                        weakSelf.MVView.joinCoSingerState = KTVJoinCoSingerStateWaitingForJoin;
                        weakSelf.isJoinChorus = false;
                        return;
                    }
                    [weakSelf _joinChorus];
                }];
                return;
            }
        }
        
        //TODO(chenpan):没有空麦位，show error
        [VLToast toast:@"麦位已满，请在他人下麦后重试"];
        return;
    }
    
    [self _joinChorus];
}

- (void)_joinChorus {
    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
    KTVSingRole role = KTVSingRoleCoSinger;
    KTVSongConfiguration* songConfig = [[KTVSongConfiguration alloc] init];
    songConfig.autoPlay = NO;
    songConfig.mode = KTVLoadMusicModeLoadMusicOnly;
    songConfig.mainSingerUid = [model.userNo integerValue];
    songConfig.songIdentifier = model.songNo;
    
    VL(weakSelf);
    self.loadMusicCallBack = ^(BOOL isSuccess, NSInteger songCode) {
        if (!isSuccess) {
            weakSelf.isJoinChorus = false;
            return;
        }
        
        [weakSelf.ktvApi startSingWithSongCode:songCode startPos:0];
        NSLog(@"before switch role, load music success");
        [weakSelf.ktvApi switchSingerRoleWithNewRole:role
                                   onSwitchRoleState:^( KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
            if (state == KTVSwitchRoleStateFail && reason != KTVSwitchRoleFailReasonNoPermission) {
                weakSelf.MVView.joinCoSingerState = KTVJoinCoSingerStateWaitingForJoin;
                [VLToast toast:[NSString stringWithFormat:@"join chorus fail: %ld", reason]];
                weakSelf.isJoinChorus = false;
                KTVLogInfo(@"join chorus fail");
                //TODO: error toast?
                return;
            }

            weakSelf.MVView.joinCoSingerState = KTVJoinCoSingerStateWaitingForLeave;
            weakSelf.isJoinChorus = false;
            
            weakSelf.isNowMicMuted = role == KTVSingRoleAudience;

            VLRoomSelSongModel *selSongModel = weakSelf.selSongsArray.firstObject;
            KTVJoinChorusInputModel* inputModel = [KTVJoinChorusInputModel new];
            inputModel.isChorus = YES;
            inputModel.songNo = selSongModel.songNo;
            [[AppContext ktvServiceImp] joinChorusWithInput:inputModel
                                                 completion:^(NSError * error) {
            }];
            [weakSelf.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
            
            //开麦
            [[AppContext ktvServiceImp] updateSeatAudioMuteStatusWithMuted:NO
                                                                completion:^(NSError * error) {
            }];
        }];
    };
    KTVLogInfo(@"before songCode:%li", [model.songNo integerValue]);
    [self.ktvApi loadMusicWithSongCode:[model.songNo integerValue] config:songConfig onMusicLoadStateListener:self];
}

- (void)removeCurrentSongWithSync:(BOOL)sync
{
    VLRoomSelSongModel* top = [self.selSongsArray firstObject];
    if(top && top.songNo.length != 0) {
        [self removeSelSongWithSongNo:[top.songNo integerValue] sync:sync];
    }
}

- (BOOL)removeSelSongWithSongNo:(NSInteger)songNo sync:(BOOL)sync {
    __block VLRoomSelSongModel* removed;
    BOOL isTopSong = [self.selSongsArray.firstObject.songNo integerValue] == songNo;
    
    if (isTopSong) {
        [self stopPlaySong];
    }
    
    NSMutableArray<VLRoomSelSongModel*> *updatedList = [NSMutableArray arrayWithArray:[self.selSongsArray filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(VLRoomSelSongModel*  _Nullable evaluatedObject, NSDictionary<NSString *,id> * _Nullable bindings) {
        if([evaluatedObject.songNo integerValue] == songNo) {
            removed = evaluatedObject;
            return NO;
        }
        return YES;
    }]]];
    
    if(removed != nil) {
        //did remove
        self.selSongsArray = updatedList;

        if(sync) {
            KTVRemoveSongInputModel* inputModel = [KTVRemoveSongInputModel new];
            inputModel.songNo = removed.songNo;
            inputModel.objectId = removed.objectId;
            [[AppContext ktvServiceImp] removeSongWithInput:inputModel
                                                 completion:^(NSError * error) {
                if (error) {
                    KTVLogInfo(@"deleteSongEvent fail: %@ %ld", removed.songName, error.code);
                }
            }];
        }
        
        return YES;
    } else {
        return NO;
    }
}

//- (void)replaceSelSongWithInfo:(VLRoomSelSongModel*)songInfo {
//    self.selSongsArray = [KTVSyncManagerServiceImp sortChooseSongWithSongList:self.selSongsArray];
//}

- (void)leaveRoom {
    VL(weakSelf);
    [[AppContext ktvServiceImp] leaveRoomWithCompletion:^(NSError * error) {
        if (error != nil) {
            return;
        }
        
        for (BaseViewController *vc in weakSelf.navigationController.childViewControllers) {
            if ([vc isKindOfClass:[VLOnLineListVC class]]) {
                [weakSelf.navigationController popToViewController:vc animated:YES];
            }
        }
    }];
}

#pragma mark - rtc utils
- (void)setupContentInspectConfig {
    AgoraContentInspectConfig* config = [AgoraContentInspectConfig new];
    NSDictionary* dic = @{
        @"userNo": [VLUserCenter user].id ? : @"unknown",
        @"sceneName": @"ktv"
    };
    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:dic options:0 error:nil];
    NSString* jsonStr = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    config.extraInfo = jsonStr;
    AgoraContentInspectModule* module = [AgoraContentInspectModule new];
    module.interval = 30;
    module.type = AgoraContentInspectTypeModeration;
    config.modules = @[module];
    [self.RTCkit enableContentInspect:YES config:config];
    
    //添加音频鉴黄接口
    [[NetworkManager shared] voiceIdentifyWithChannelName:self.roomModel.roomNo
                                              channelType:1
                                                sceneType:SceneTypeKtv
                                                  success:^(NSString * msg) {
        KTVLogInfo(@"voiceIdentify success: %@", msg);
    }];
}

- (void)joinRTCChannel {
    self.RTCkit = [AgoraRtcEngineKit sharedEngineWithAppId:[AppContext.shared appId] delegate:self];
    //setup private param
//    [self.RTCkit setParameters:@"{\"rtc.debug.enable\": true}"];
//    [self.RTCkit setParameters:@"{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}"];
    
    //use game streaming in so mode, chrous profile in chrous mode
    [self.RTCkit setAudioScenario:AgoraAudioScenarioGameStreaming];
    [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
    [self.RTCkit setChannelProfile:AgoraChannelProfileLiveBroadcasting];
    
    /// 开启唱歌评分功能
    int code = [self.RTCkit enableAudioVolumeIndication:50 smooth:10 reportVad:true];
    
    if (code == 0) {
        KTVLogInfo(@"评分回调开启成功\n");
    } else {
        KTVLogInfo(@"评分回调开启失败：%d\n",code);
    }
    
    [self.RTCkit enableVideo];
    [self.RTCkit enableAudio];
    
    [self setupContentInspectConfig];
    
    VLRoomSeatModel* myseat = [self.seatsArray objectAtIndex:0];
    
    self.isNowMicMuted = myseat.isAudioMuted;
    self.isNowCameraMuted = myseat.isVideoMuted;
    self.trackMode = KTVPlayerTrackModeAcc;
    self.singRole = KTVSingRoleAudience;
    
    AgoraVideoEncoderConfiguration *encoderConfiguration =
    [[AgoraVideoEncoderConfiguration alloc] initWithSize:CGSizeMake(100, 100)
                                               frameRate:AgoraVideoFrameRateFps7
                                                 bitrate:20
                                         orientationMode:AgoraVideoOutputOrientationModeFixedLandscape
                                              mirrorMode:AgoraVideoMirrorModeAuto];
    [self.RTCkit setVideoEncoderConfiguration:encoderConfiguration];
    
    
    [self.RTCkit setEnableSpeakerphone:YES];
    
    AgoraDataStreamConfig *config = [AgoraDataStreamConfig new];
    config.ordered = NO;
    config.syncWithAudio = NO;
    [self.RTCkit createDataStream:&ktvStreamId
                           config:config];
    
    NSString* exChannelToken = VLUserCenter.user.agoraPlayerRTCToken;
    KTVApiConfig* apiConfig = [[KTVApiConfig alloc] initWithAppId:[[AppContext shared] appId]
                                                         rtmToken:VLUserCenter.user.agoraRTMToken
                                                           engine:self.RTCkit
                                                      channelName:self.roomModel.roomNo
                                                         localUid:[VLUserCenter.user.id integerValue]
                                                        chorusChannelName:[NSString stringWithFormat:@"%@_ex", self.roomModel.roomNo] chorusChannelToken:exChannelToken
    ];
    self.ktvApi = [[KTVApiImpl alloc] initWithConfig: apiConfig];
    KTVLrcControl* lrcControl = [[KTVLrcControl alloc] initWithLrcView:self.MVView.karaokeView];
    [self.ktvApi setLrcViewWithView:lrcControl];
    self.lrcControl = lrcControl;
    self.lrcControl.delegate = self;
    VL(weakSelf);
    lrcControl.skipCallBack = ^(NSInteger time, BOOL flag) {
        NSInteger seekTime = flag ? [weakSelf.ktvApi getMediaPlayer].getDuration - 800 : time;
        [weakSelf.ktvApi seekSingWithTime:seekTime];
    };
    [self.ktvApi setMicStatusWithIsOnMicOpen:!self.isNowMicMuted];
    [self.ktvApi addEventHandlerWithKtvApiEventHandler:self];
//    VL(weakSelf);
    KTVLogInfo(@"Agora - joining RTC channel with token: %@, for roomNo: %@, with uid: %@", VLUserCenter.user.agoraRTCToken, self.roomModel.roomNo, VLUserCenter.user.id);
    int ret =
    [self.RTCkit joinChannelByToken:VLUserCenter.user.agoraRTCToken
                          channelId:self.roomModel.roomNo
                                uid:[VLUserCenter.user.id integerValue]
                       mediaOptions:[self channelMediaOptions]
                        joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        KTVLogInfo(@"Agora - 加入RTC成功");
//        [weakSelf.RTCkit setParameters: @"{\"che.audio.enable.md \": false}"];sin
    }];
    if (ret != 0) {
        KTVLogError(@"joinChannelByToken fail: %d, uid: %ld, token: %@", ret, [VLUserCenter.user.id integerValue], VLUserCenter.user.agoraRTCToken);
    }
}

- (void)leaveRTCChannel {
    [self.ktvApi removeEventHandlerWithKtvApiEventHandler:self];
    [self.ktvApi cleanCache];
    self.ktvApi = nil;
    self.loadMusicCallBack = nil;
    [self.RTCkit leaveChannel:^(AgoraChannelStats * _Nonnull stat) {
        KTVLogInfo(@"Agora - Leave RTC channel");
    }];
}

- (AgoraRtcChannelMediaOptions*)channelMediaOptions {
    AgoraRtcChannelMediaOptions *option = [AgoraRtcChannelMediaOptions new];
    [option setClientRoleType:[self isBroadcaster] ? AgoraClientRoleBroadcaster : AgoraClientRoleAudience];
    [option setPublishCameraTrack:!self.isNowCameraMuted];
    // use audio volume to control mic on/off, so that mic is always on when broadcaster
    [option setPublishMicrophoneTrack:[self isBroadcaster]];
    [option setPublishCustomAudioTrack:NO];
    [option setChannelProfile:AgoraChannelProfileLiveBroadcasting];
    [option setAutoSubscribeAudio:YES];
    [option setAutoSubscribeVideo:YES];
    [option setPublishMediaPlayerId:[[self.ktvApi getMediaPlayer] getMediaPlayerId]];
    [option setEnableAudioRecordingOrPlayout:YES];
    return option;
}

- (void)sendStreamMessageWithDict:(NSDictionary *)dict
                         success:(void (^ _Nullable)(BOOL))success {
//    VLLog(@"sendStremMessageWithDict:::%@",dict);
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    
    int code = [self.RTCkit sendStreamMessage:ktvStreamId
                                         data:messageData];
    if (code == 0 && success) {
        success(YES);
    } else{
//        VLLog(@"发送失败-streamId:%ld\n",streamId);
    };
}

#pragma mark -- VLKTVAPIDelegate

- (void)didLrcViewDragedToPos:(NSInteger)pos score:(NSInteger)score totalScore:(NSInteger)totalScore{
    [self.ktvApi.getMediaPlayer seekToPosition:pos];
    [self.MVView.gradeView setScoreWithCumulativeScore:score totalScore:totalScore];
}

- (void)didLrcViewScorllFinishedWith:(NSInteger)score totalScore:(NSInteger)totalScore lineScore:(NSInteger)lineScore lineIndex:(NSInteger)lineIndex{
    if(self.singRole == KTVSingRoleAudience){
        return;
    }
    
    NSInteger realScore = self.singRole == KTVSingRoleCoSinger ? self.coSingerDegree + lineScore : score;
    [self.MVView.lineScoreView showScoreViewWithScore:lineScore];
    [self.MVView.gradeView setScoreWithCumulativeScore:realScore totalScore:totalScore];
    [self.MVView.incentiveView showWithScore:lineScore];
    //将主唱的分数同步给观众
    if(self.singRole == KTVSingRoleSoloSinger || self.singRole == KTVSingRoleLeadSinger){
        [self sendMainSingerLineScoreToAudienceWith:score totalScore:totalScore lineScore:lineScore lineIndex:lineIndex];
    } else {
        self.coSingerDegree += lineScore;
    }
}

-(void)sendMainSingerLineScoreToAudienceWith:(NSInteger)cumulativeScore totalScore:(NSInteger)totalScore lineScore:(NSInteger)lineScore lineIndex:(NSInteger)lineIndex{
    NSDictionary *dict = @{
        @"cmd":@"singleLineScore",
        @"score":@(lineScore),
        @"index":@(lineIndex),
        @"cumulativeScore":@(cumulativeScore),
        @"total":@(totalScore),
        
    };
    [self sendStreamMessageWithDict:dict success:nil];
    NSLog(@"index: %li, score: %li, cumulativeScore: %li, total: %li", lineIndex, lineScore, cumulativeScore, totalScore);
}

- (void)didSongLoadedWith:(LyricModel *)model{
    self.lyricModel = model;
}

- (void)didJoinChours {
    //加入合唱
    self.MVView.joinCoSingerState = KTVJoinCoSingerStateJoinNow;
    [self joinChorus];
}

-(void)didLeaveChours {
    //退出合唱
    [[AppContext ktvServiceImp] coSingerLeaveChorusWithCompletion:^(NSError * error) {
    }];
    [self stopPlaySong];
    self.isNowMicMuted = true;
    [self.MVView.gradeView reset];
    [[AppContext ktvServiceImp] updateSeatAudioMuteStatusWithMuted:YES
                                                        completion:^(NSError * error) {
    }];
}

#pragma mark -- VLKTVTopViewDelegate
- (void)onVLKTVTopView:(VLKTVTopView *)view closeBtnTapped:(id)sender {
    VL(weakSelf);
    NSString *title = VLUserCenter.user.ifMaster ? KTVLocalizedString(@"解散房间") : KTVLocalizedString(@"退出房间");
    NSString *message = VLUserCenter.user.ifMaster ? KTVLocalizedString(@"确定解散该房间吗？") : KTVLocalizedString(@"确定退出该房间吗？");
    NSArray *array = [[NSArray alloc]initWithObjects:KTVLocalizedString(@"取消"),KTVLocalizedString(@"确定"), nil];
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:title message:message placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == YES){
            [weakSelf leaveRoom];
        }
        [[VLAlert shared] dismiss];
    }];
}

#pragma mark - VLPopMoreSelViewDelegate
- (void)onVLKTVMoreSelView:(VLPopMoreSelView *)view
                 btnTapped:(id)sender
                 withValue:(VLKTVMoreBtnClickType)typeValue {
    [[LSTPopView getPopViewWithCustomView:view] dismiss];
    switch (typeValue) {
//        case VLKTVMoreBtnClickTypeBelcanto:
//            [self popBelcantoView];
//            break;
        case VLKTVMoreBtnClickTypeSound:
            [self popSetSoundEffectView];
            break;
        case VLKTVMoreBtnClickTypeMV:
            [self popSelMVBgView];
            break;
        default:
            break;
    }
}

#pragma mark - VLKTVBottomViewDelegate
- (void)onVLKTVBottomView:(VLKTVBottomToolbar *)view
                btnTapped:(id)sender
               withValues:(VLKTVBottomBtnClickType)typeValue {
    switch (typeValue) {
        case VLKTVBottomBtnClickTypeMore:  //更多
//            [self popSelMVBgView];
            [self popSelMoreView];
            break;
        case VLKTVBottomBtnClickTypeJoinChorus:
            [self popUpChooseSongView:YES];
            break;
        case VLKTVBottomBtnClickTypeChoose:
            [self popUpChooseSongView:NO];
            break;
        case VLKTVBottomBtnClickTypeAudio:
            self.isNowMicMuted = !self.isNowMicMuted;
            [[AppContext ktvServiceImp] updateSeatAudioMuteStatusWithMuted:self.isNowMicMuted
                                                                completion:^(NSError * error) {
            }];
            break;
        case VLKTVBottomBtnClickTypeVideo:
            self.isNowCameraMuted = !self.isNowCameraMuted;
            [[AppContext ktvServiceImp] updateSeatVideoMuteStatusWithMuted:self.isNowCameraMuted
                                                                completion:^(NSError * error) {
            }];
            break;
        default:
            break;
    }
}

#pragma mark - VLRoomPersonViewDelegate
- (void)onVLRoomPersonView:(VLMicSeatList *)view
   seatItemTappedWithModel:(VLRoomSeatModel *)model
                   atIndex:(NSInteger)seatIndex {
    if(VLUserCenter.user.ifMaster) {
        //is owner
        if ([model.userNo isEqualToString:VLUserCenter.user.id]) {
            //self, return
            return;
        }
        if (model.userNo.length > 0) {
            return [self popDropLineViewWithSeatModel:model];
        }
    } else {
        if (model.userNo.length > 0) {
            //occupied
            if ([model.userNo isEqualToString:VLUserCenter.user.id]) {//点击的是自己
                return [self popDropLineViewWithSeatModel:model];
            }
        } else{
            //empty
            BOOL isOnSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
            if (!isOnSeat) {
                //not yet seated
                [self enterSeatWithIndex:seatIndex completion:^(NSError *error) {
                }];
            }
        }
    }
}

- (void)onVLRoomPersonView:(VLMicSeatList *)view onRenderVideo:(VLRoomSeatModel *)model inView:(UIView *)videoView atIndex:(NSInteger)seatIndex
{
    AgoraRtcVideoCanvas *videoCanvas = [[AgoraRtcVideoCanvas alloc] init];
    videoCanvas.uid = [model.rtcUid unsignedIntegerValue];
    videoCanvas.view = videoView;
    videoCanvas.renderMode = AgoraVideoRenderModeHidden;
    if([model.userNo isEqual:VLUserCenter.user.id]) {
        //is self
        [self.RTCkit setupLocalVideo:videoCanvas];
    } else {
        [self.RTCkit setupRemoteVideo:videoCanvas];
    }
}

#pragma mark - VLPopSelBgViewDelegate
-(void)onVLPopSelBgView:(VLPopSelBgView *)view
       tappedWithAction:(VLKTVSelBgModel *)selBgModel
                atIndex:(NSInteger)index {
    KTVChangeMVCoverInputModel* inputModel = [KTVChangeMVCoverInputModel new];
//    inputModel.roomNo = self.roomModel.roomNo;
    inputModel.mvIndex = index;
//    inputModel.userNo = VLUserCenter.user.id;
    VL(weakSelf);
    [[AppContext ktvServiceImp] changeMVCoverWithParams:inputModel
                                             completion:^(NSError * error) {
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }
        
        [[LSTPopView getPopViewWithCustomView:view] dismiss];
        weakSelf.choosedBgModel = selBgModel;
    }];
}

#pragma mark VLPopChooseSongViewDelegate
- (void)chooseSongView:(VLPopSongList*)view tabbarDidClick:(NSUInteger)tabIndex {
    if (tabIndex != 1) {
        return;
    }
    
    [self refreshChoosedSongList:nil];
}

#pragma mark - VLChooseBelcantoViewDelegate
- (void)onVLChooseBelcantoView:(VLAudioEffectPicker *)view
                    itemTapped:(VLBelcantoModel *)model
                     withIndex:(NSInteger)index {
    self.selBelcantoModel = model;
    if (index == 0) {
        [self.RTCkit setVoiceBeautifierPreset:AgoraVoiceBeautifierPresetOff];
    }else if (index == 1){
        [self.RTCkit setVoiceBeautifierPreset:AgoraVoiceBeautifierPresetChatBeautifierMagnetic];
    }else if (index == 2){
        [self.RTCkit setVoiceBeautifierPreset:AgoraVoiceBeautifierPresetChatBeautifierFresh];
    }else if (index == 3){
        [self.RTCkit setVoiceBeautifierPreset:AgoraVoiceBeautifierPresetChatBeautifierVitality];
    }else if (index == 4){
        [self.RTCkit setVoiceBeautifierPreset:AgoraVoiceBeautifierPresetChatBeautifierVitality];
    }
}

#pragma mark VLDropOnLineViewDelegate
- (void)onVLDropOnLineView:(VLDropOnLineView *)view action:(VLRoomSeatModel *)seatModel {
    [self leaveSeatWithSeatModel:seatModel withCompletion:^(NSError *error) {
        [[LSTPopView getPopViewWithCustomView:view] dismiss];
    }];
}

#pragma mark VLTouristOnLineViewDelegate
//上麦方式
- (void)requestOnlineAction {
}
#pragma mark - MVViewDelegate
// 打分实时回调
- (void)onKTVMVView:(VLKTVMVView *)view scoreDidUpdate:(int)score {
}

- (void)onKTVMVView:(VLKTVMVView *)view btnTappedWithActionType:(VLKTVMVViewActionType)type {
    if (type == VLKTVMVViewActionTypeSetParam) {
        [self showSettingView];
    } else if (type == VLKTVMVViewActionTypeMVPlay) { //播放
        [self.ktvApi resumeSing];
        self.isPause = false;
    } else if (type == VLKTVMVViewActionTypeMVPause) { //暂停
        [self.ktvApi pauseSing];
        self.isPause = true;
    } else if (type == VLKTVMVViewActionTypeMVNext) { //切换
        
        if(self.RTCkit.getConnectionState != AgoraConnectionStateConnected){
            [VLToast toast:@"切歌失败，reson:连接已断开"];
            return;
        }
        
        VL(weakSelf);

        NSString *title = KTVLocalizedString(@"切换歌曲");
        NSString *message = KTVLocalizedString(@"切换下一首歌歌曲？");
        NSArray *array = [[NSArray alloc]initWithObjects:KTVLocalizedString(@"取消"),KTVLocalizedString(@"确定"), nil];
        [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:title message:message placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
            if(flag == YES){
                if (weakSelf.selSongsArray.count >= 1) {
                    [weakSelf stopPlaySong];
                    [weakSelf removeCurrentSongWithSync:YES];
                }
            }
            [[VLAlert shared] dismiss];
        }];
    } else if (type == VLKTVMVViewActionTypeSingOrigin) { // 原唱
        self.trackMode = KTVPlayerTrackModeOrigin;
    } else if (type == VLKTVMVViewActionTypeSingAcc) { // 伴奏
        self.trackMode = KTVPlayerTrackModeAcc;
    } else if (type == VLKTVMVViewActionTypeRetryLrc) {  //歌词重试
        [self reloadMusic];
    }
}

- (void)onKTVMView:(VLKTVMVView *)view lrcViewDidScrolled:(NSInteger)position {
    [[self.ktvApi getMediaPlayer] seekToPosition:position];
}

- (void)reloadMusic{
    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
    KTVSongConfiguration* songConfig = [[KTVSongConfiguration alloc] init];
    songConfig.autoPlay = YES;
    songConfig.mode = KTVLoadMusicModeLoadLrcOnly;
    songConfig.mainSingerUid = [model.userNo integerValue];
    songConfig.songIdentifier = model.songNo;
    
    self.MVView.loadingType = VLKTVMVViewStateLoading;
    [self.MVView setBotViewHidden:true];
    VL(weakSelf);
    self.loadMusicCallBack = ^(BOOL isSuccess, NSInteger songCode) {
        if (!isSuccess) {
            return;
        }
        [weakSelf.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
    };
    
    [self.ktvApi loadMusicWithSongCode:[model.songNo integerValue] config:songConfig onMusicLoadStateListener:self];
}

#pragma mark - VLKTVSettingViewDelegate
- (void)settingViewSettingChanged:(VLKTVSettingModel *)setting
              valueDidChangedType:(VLKTVValueDidChangedType)type {
    if (type == VLKTVValueDidChangedTypeEar) { // 耳返设置
        // 用户必须使用有线耳机才能听到耳返效果
        // 1、不在耳返中添加audiofilter
        // AgoraEarMonitoringFilterNone
        // 2: 在耳返中添加人声效果 audio filter。如果你实现了美声、音效等功能，用户可以在耳返中听到添加效果后的声音。
        // AgoraEarMonitoringFilterBuiltInAudioFilters
        // 4: 在耳返中添加降噪 audio filter。
        // AgoraEarMonitoringFilterNoiseSuppression
        // [self.RTCkit enableInEarMonitoring:setting.soundOn includeAudioFilters:AgoraEarMonitoringFilterBuiltInAudioFilters | AgoraEarMonitoringFilterNoiseSuppression];
        self.isEarOn = setting.soundOn;
    } else if (type == VLKTVValueDidChangedTypeMV) { // MV
        
    } else if (type == VLKTVValueDidChangedRiseFall) { // 升降调
        // 调整当前播放的媒体资源的音调
        // 按半音音阶调整本地播放的音乐文件的音调，默认值为 0，即不调整音调。取值范围为 [-12,12]，每相邻两个值的音高距离相差半音。取值的绝对值越大，音调升高或降低得越多
        NSInteger value = setting.toneValue * 2 - 12;
        [[self.ktvApi getMediaPlayer] setAudioPitch:value];
    } else if (type == VLKTVValueDidChangedTypeSound) { // 音量
        // 调节音频采集信号音量、取值范围为 [0,400]
        // 0、静音 100、默认原始音量 400、原始音量的4倍、自带溢出保护
        [self.RTCkit adjustRecordingSignalVolume:setting.soundValue * 100];
        if(setting.soundOn) {
            [self.RTCkit setInEarMonitoringVolume:setting.soundValue * 100];
        }
    } else if (type == VLKTVValueDidChangedTypeAcc) { // 伴奏
        int value = setting.accValue * 100;
        self.playoutVolume = value;
    } else if (type == VLKTVValueDidChangedTypeListItem) {
        AgoraAudioEffectPreset preset = [self audioEffectPreset:setting.kindIndex];
        [self.RTCkit setAudioEffectPreset:preset];
    } else if (type == VLKTVValueDidChangedTypeRemoteValue) {
//        [self.ktvApi adjustChorusRemoteUserPlaybackVoulme:setting.remoteVolume];
        [self.RTCkit adjustPlaybackSignalVolume:setting.remoteVolume];
    }
}

- (AgoraAudioEffectPreset)audioEffectPreset:(NSInteger)index {
    NSArray* audioEffectPresets = @[
        @(AgoraAudioEffectPresetOff),
        @(AgoraAudioEffectPresetRoomAcousticsKTV),
        @(AgoraAudioEffectPresetRoomAcousVocalConcer),
        @(AgoraAudioEffectPresetRoomAcousStudio),
        @(AgoraAudioEffectPresetRoomAcousPhonograph),
        @(AgoraAudioEffectPresetRoomAcousSpatial),
        @(AgoraAudioEffectPresetRoomAcousEthereal),
        @(AgoraAudioEffectPresetStyleTransformationPopular),
        @(AgoraAudioEffectPresetStyleTransformationRnb),
    ];
    if (audioEffectPresets.count <= index) {
        return AgoraAudioEffectPresetOff;
    }
    
    return [[audioEffectPresets objectAtIndex:index] integerValue];
}

//音效设置
- (void)effectItemClickAction:(NSInteger)effect {
    self.selectedEffectIndex = effect;
    NSArray *effects = @[@(AgoraAudioEffectPresetOff),
                         @(AgoraAudioEffectPresetRoomAcousticsKTV),
                         @(AgoraAudioEffectPresetRoomAcousVocalConcer),
                         @(AgoraAudioEffectPresetRoomAcousStudio),
                         @(AgoraAudioEffectPresetRoomAcousPhonograph),
                         @(AgoraAudioEffectPresetRoomAcousSpatial),
                         @(AgoraAudioEffectPresetRoomAcousEthereal),
                         @(AgoraAudioEffectPresetStyleTransformationPopular),
                         @(AgoraAudioEffectPresetStyleTransformationRnb)];
  [self.RTCkit setAudioEffectPreset: [effects[effect] integerValue]];
    
}
//- (void)soundEffectItemClickAction:(VLKTVSoundEffectType)effectType {
//    if (effectType == VLKTVSoundEffectTypeHeFeng) {
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:3 param2:4];
//    } else if (effectType == VLKTVSoundEffectTypeXiaoDiao){
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:2 param2:4];
//    } else if (effectType == VLKTVSoundEffectTypeDaDiao){
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:1 param2:4];
//    } else if (effectType == VLKTVSoundEffectTypeNone) {
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:0 param2:4];
//    }
//    KTVLogInfo(@"Agora - Setting effect type to %lu", effectType);
//}


#pragma mark --
- (void)_fetchServiceAllData {
    //请求已点歌曲
    VL(weakSelf);
    [self refreshChoosedSongList:^{
        //请求歌词和歌曲
        [weakSelf loadAndPlaySong];
    }];
}

#pragma mark - getter/handy utils
- (BOOL)isCurrentSongMainSinger:(NSString *)userNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    return [selSongModel.userNo isEqualToString:userNo];
}

- (BOOL)isRoomOwner {
    return [self.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
}

- (BOOL)isBroadcaster {
    return [self isRoomOwner] || self.isOnMicSeat;
}

- (VLRoomSelSongModel*)selSongWithSongNo:(NSString*)songNo {
    __block VLRoomSelSongModel* song = nil;
    [self.selSongsArray enumerateObjectsUsingBlock:^(VLRoomSelSongModel * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([obj.songNo isEqualToString:songNo]) {
            song = obj;
            *stop = YES;
        }
    }];
    
    return song;
}

/// 获取当前用户的麦位
- (VLRoomSeatModel*)getCurrentUserSeatInfo {
    for (VLRoomSeatModel *model in self.seatsArray) {
        if ([model.userNo isEqualToString:VLUserCenter.user.id]) {
            return model;
        }
    }
    
    return nil;
}

/// 根据麦位索引获取麦位
/// @param seatIndex <#seatIndex description#>
- (VLRoomSeatModel*)getUserSeatInfoWithIndex:(NSUInteger)seatIndex {
    for (VLRoomSeatModel *model in self.seatsArray) {
        if (model.seatIndex == seatIndex) {
            return model;
        }
    }
    
    return nil;
}

/// 计算当前歌曲用户的演唱角色
- (KTVSingRole)getUserSingRole {
    VLRoomSelSongModel* songModel = [[self selSongsArray] firstObject];
    BOOL currentSongIsJoinSing = [[self getCurrentUserSeatInfo].chorusSongCode isEqualToString:songModel.chorusSongId];
    BOOL currentSongIsSongOwner = [songModel isSongOwner];
    BOOL currentSongIsChorus = [self getChorusNumWithSeatArray:self.seatsArray] > 0;
    if (currentSongIsSongOwner) {
        return currentSongIsChorus ? KTVSingRoleLeadSinger : KTVSingRoleSoloSinger;
    } else if (currentSongIsJoinSing) {
        return KTVSingRoleCoSinger;
    } else {
        return KTVSingRoleAudience;
    }
}


/// 计算合唱者数量
/// @param seatArray <#seatArray description#>
- (NSUInteger)getChorusNumWithSeatArray:(NSArray*)seatArray {
    NSUInteger chorusNum = 0;
    VLRoomSelSongModel* topSong = [self.selSongsArray firstObject];
    for(VLRoomSeatModel* seat in seatArray) {
        //TODO: validate songCode
        if([seat.chorusSongCode isEqualToString:[topSong chorusSongId]]) {
            chorusNum += 1;
        }
       // else if ([seat.chorusSongCode length] > 0) {
//            KTVLogError(@"calc seat chorus status fail! chorusSongCode: %@, playSongCode: %@", seat.chorusSongCode, topSong.songNo);
//        }
//        if ([seat.chorusSongCode length] > 0) {
//            chorusNum += 1;
//        }
    }
    
    return chorusNum;
}

- (BOOL)getJoinChorusEnable {
    //不是观众不允许加入
    if ([self getUserSingRole] != KTVSingRoleAudience) {
        return NO;
    }
    
    VLRoomSelSongModel* topSong = [[self selSongsArray] firstObject];
    //TODO: 不在播放不允许加入
    if (topSong.status != VLSongPlayStatusPlaying) {
        return NO;
    }
    
    return YES;
}

//获取已经非麦下观众的总数
-(NSInteger)getOnMicUserCount{
    NSInteger num = 0;
    if(self.seatsArray){
        for(VLRoomSeatModel *model in self.seatsArray){
            if(model.rtcUid){
                num++;
            }
        }
    }
    return num;
}

#pragma mark - setter
- (void)setKtvApi:(KTVApiImpl *)ktvApi {
    _ktvApi = ktvApi;
    [[AppContext shared] setKtvAPI:ktvApi];
}

- (void)setRoomUsersCount:(NSUInteger)userCount {
    self.roomModel.roomPeopleNum = [NSString stringWithFormat:@"%ld", userCount];
    self.topView.listModel = self.roomModel;
}

- (void)setChoosedBgModel:(VLKTVSelBgModel *)choosedBgModel {
    _choosedBgModel = choosedBgModel;
    [self.MVView changeBgViewByModel:choosedBgModel];
}

- (void)setSeatsArray:(NSArray<VLRoomSeatModel *> *)seatsArray {
    _seatsArray = seatsArray;
    
    //update booleans
    self.isOnMicSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
    
    self.roomPersonView.roomSeatsArray = self.seatsArray;
    [self.roomPersonView updateSingBtnWithChoosedSongArray:_selSongsArray];
    self.chorusNum = [self getChorusNumWithSeatArray:seatsArray];
    [self onSeatFull];
}

-(void)onSeatFull{
    if(self.singRole != KTVSingRoleAudience){
        return;
    }
    NSInteger count = [self getOnMicUserCount];
    if(!_isOnMicSeat && count >=8){
        self.MVView.joinCoSingerState = KTVJoinCoSingerStateIdle;
    } else {
        if(!self.isJoinChorus){
            self.MVView.joinCoSingerState = KTVJoinCoSingerStateWaitingForJoin;
        }
    }
}

- (void)setChorusNum:(NSUInteger)chorusNum {
    NSUInteger origChorusNum = _chorusNum;
    _chorusNum = chorusNum;
    if (origChorusNum != chorusNum) {
        //主唱<->独唱切换，非歌曲owner不需要调用
        if(![self isCurrentSongMainSinger:VLUserCenter.user.id]) {
            return;
        }
        KTVLogInfo(@"seat array update chorusNum %ld->%ld", origChorusNum, chorusNum);
        //lead singer <-> solo
        KTVSingRole role = [self getUserSingRole];
        [self.ktvApi switchSingerRoleWithNewRole:role
                               onSwitchRoleState:^(KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
        }];
    }
}

- (void)setIsOnMicSeat:(BOOL)isOnMicSeat {
    BOOL onMicSeatStatusDidChanged = _isOnMicSeat != isOnMicSeat;
    _isOnMicSeat = isOnMicSeat;
    
    if (onMicSeatStatusDidChanged) {
        if (!isOnMicSeat) {
            [self stopPlaySong];
        }
        
        //start mic once enter seat
        AgoraRtcChannelMediaOptions *option = [AgoraRtcChannelMediaOptions new];
        [option setClientRoleType:[self isBroadcaster] ? AgoraClientRoleBroadcaster : AgoraClientRoleAudience];
        // use audio volume to control mic on/off, so that mic is always on when broadcaster
        [option setPublishMicrophoneTrack:[self isBroadcaster]];
        [self.RTCkit updateChannelWithMediaOptions:option];
    }
    
//    [self.RTCkit enableLocalAudio:isOnMicSeat];
//    [self.RTCkit muteLocalAudioStream:!isOnMicSeat];
    
    VLRoomSeatModel* info = [self getCurrentUserSeatInfo];
    self.isNowMicMuted = info.isAudioMuted;
    self.isNowCameraMuted = info.isVideoMuted;
    
    self.bottomView.hidden = !_isOnMicSeat;
    self.requestOnLineView.hidden = !self.bottomView.hidden;
}

- (void)setIsNowMicMuted:(BOOL)isNowMicMuted {
    BOOL oldValue = _isNowMicMuted;
    _isNowMicMuted = isNowMicMuted;
    
    [self.ktvApi setMicStatusWithIsOnMicOpen:!isNowMicMuted];
    [self.RTCkit adjustRecordingSignalVolume:isNowMicMuted ? 0 : 100];
    if(oldValue != isNowMicMuted) {
        [self.bottomView updateAudioBtn:isNowMicMuted];
    }
}

- (void)setIsNowCameraMuted:(BOOL)isNowCameraMuted {
    BOOL oldValue = _isNowCameraMuted;
    _isNowCameraMuted = isNowCameraMuted;
    
    [self.RTCkit enableLocalVideo:!isNowCameraMuted];
    AgoraRtcChannelMediaOptions *option = [AgoraRtcChannelMediaOptions new];
    [option setPublishCameraTrack:!self.isNowCameraMuted];
    [self.RTCkit updateChannelWithMediaOptions:option];
    if(oldValue != isNowCameraMuted) {
        [self.bottomView updateVideoBtn:isNowCameraMuted];
    }
}

- (void)setIsEarOn:(BOOL)isEarOn {
    _isEarOn = isEarOn;
    [self _checkInEarMonitoring];
    NSAssert(self.settingView != nil, @"self.settingView == nil");
    [self.settingView setIsEarOn:isEarOn];
}

- (void)setPlayoutVolume:(int)playoutVolume {
    _playoutVolume = playoutVolume;
    
    // 官方文档是100 ？ SDK 是 400？？？？
    // 调节本地播放音量 取值范围为 [0,100]
    // 0、无声。 100、（默认）媒体文件的原始播放音量
//    [self.ktvApi adjustPlayoutVolume:playoutVolume];
    [[self.ktvApi getMediaPlayer] adjustPlayoutVolume:playoutVolume];
    
    // 调节远端用户听到的音量 取值范围[0、400]
    // 100: （默认）媒体文件的原始音量。400: 原始音量的四倍（自带溢出保护）
    [[self.ktvApi getMediaPlayer] adjustPublishSignalVolume:playoutVolume];
    
    //update ui
    [self.settingView setAccValue: (float)playoutVolume / 100.0];
}

- (void)_checkInEarMonitoring {
//    if([self isCurrentSongMainSinger:VLUserCenter.user.id]) {
//        [self.RTCkit enableInEarMonitoring:_isEarOn includeAudioFilters:AgoraEarMonitoringFilterBuiltInAudioFilters];
//    } else {
//        [self.RTCkit enableInEarMonitoring:NO includeAudioFilters:AgoraEarMonitoringFilterBuiltInAudioFilters];
//    }
    if(self.singRole != KTVSingRoleAudience){//主唱伴唱都能开启耳返
        [self.RTCkit enableInEarMonitoring:_isEarOn includeAudioFilters:AgoraEarMonitoringFilterNone];
    }
}

- (void)setSelSongsArray:(NSArray<VLRoomSelSongModel *> *)selSongsArray {
    NSArray<VLRoomSelSongModel*> *oldSongsArray = _selSongsArray;
    _selSongsArray = [NSMutableArray arrayWithArray:selSongsArray];
    self.chorusNum = [self getChorusNumWithSeatArray:self.seatsArray];
    if (self.chooseSongView) {
        self.chooseSongView.selSongsArray = _selSongsArray; //刷新已点歌曲UI
    }
    
    [self.roomPersonView updateSingBtnWithChoosedSongArray:_selSongsArray];
    VLRoomSelSongModel* originalTopSong = [oldSongsArray firstObject];
    VLRoomSelSongModel* updatedTopSong = [selSongsArray firstObject];
    KTVLogInfo(@"setSelSongsArray current top: songName: %@, status: %ld",
               updatedTopSong.songName, updatedTopSong.status);
    KTVLogInfo(@"setSelSongsArray orig top: songName: %@, status: %ld",
               originalTopSong.songName, originalTopSong.status);
    if(![updatedTopSong.songNo isEqualToString:originalTopSong.songNo]){
        [self.MVView reset];
        [self.lrcControl resetLrc];
        //song changes
        [self stopPlaySong];
        [self loadAndPlaySong];
    }
}

- (void)setTrackMode:(KTVPlayerTrackMode)trackMode {
    KTVLogInfo(@"setTrackMode: %ld", trackMode);
    _trackMode = trackMode;
    [[self.ktvApi getMediaPlayer] selectAudioTrack:self.trackMode == KTVPlayerTrackModeOrigin ? 0 : 1];
    
    [self.MVView setOriginBtnState: trackMode == KTVPlayerTrackModeOrigin ? VLKTVMVViewActionTypeSingOrigin : VLKTVMVViewActionTypeSingAcc];
}

- (void)setSingRole:(KTVSingRole)singRole {
    _singRole = singRole;
    self.lrcControl.lrcView.lyricsView.draggable = false;
    self.lrcControl.isMainSinger = (_singRole == KTVSingRoleSoloSinger || _singRole == KTVSingRoleLeadSinger);
    KTVLogInfo(@"setSingRole: %ld", singRole);
    
    VLRoomSelSongModel *song = self.selSongsArray.firstObject;
    [self.MVView updateUIWithSong:song role:singRole];
    [self setCoSingerStateWith:singRole];

}

-(void)setCoSingerStateWith:(KTVSingRole)role {
    switch (role) {
        case KTVSingRoleSoloSinger:
        case KTVSingRoleLeadSinger: {
            self.MVView.joinCoSingerState = KTVJoinCoSingerStateIdle;
        } break;
        case KTVSingRoleCoSinger: {
//        case KTVSingRoleFollowSinger:
            self.MVView.joinCoSingerState = KTVJoinCoSingerStateWaitingForLeave;
        } break;
        case KTVSingRoleAudience:
        default: {
            //self.MVView.joinCoSingerState = KTVJoinCoSingerStateWaitingForJoin;
            [self onSeatFull];
        } break;
    }
}

#pragma mark KTVApiEventHandlerDelegate
- (void)onMusicPlayerStateChangedWithState:(AgoraMediaPlayerState)state error:(AgoraMediaPlayerError)error isLocal:(BOOL)isLocal {
    dispatch_async(dispatch_get_main_queue(), ^{
        if(state == AgoraMediaPlayerStatePlaying) {
            if(isLocal) {
                //track has to be selected after loaded
                self.trackMode = self.trackMode;
            }
            [self.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
            //显示跳过前奏
            if(self.singRole == KTVSingRoleSoloSinger || self.singRole == KTVSingRoleLeadSinger){
                [self.lrcControl showPreludeEnd];
            }
        } else if(state == AgoraMediaPlayerStatePaused) {
            [self.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPause];
            [self.lrcControl hideSkipViewWithFlag:true];
        } else if(state == AgoraMediaPlayerStateStopped) {

        } else if(state == AgoraMediaPlayerStatePlayBackAllLoopsCompleted || state == AgoraMediaPlayerStatePlayBackCompleted) {
            if(isLocal) {
                KTVLogInfo(@"Playback all loop completed");
                //if([self isCurrentSongMainSinger:VLUserCenter.user.id]) {
                if(self.singRole != KTVSingRoleAudience){
                    //伴唱和房主都用自己的分数
                    if(self.singRole == KTVSingRoleLeadSinger || self.singRole == KTVSingRoleSoloSinger){
                        [self syncChoruScore:[self.lrcControl getAvgScore]];
                    }
                    [self showScoreViewWithScore: [self.lrcControl getAvgScore]];
                }
                [self removeCurrentSongWithSync:YES];
            }
        }
        
        //判断伴唱是否是暂停状态
        if(self.singRole == KTVSingRoleCoSinger){
            self.isPause = (isLocal && state == AgoraMediaPlayerStatePaused);
        }
    });
}

- (void)onSingerRoleChangedWithOldRole:(enum KTVSingRole)oldRole newRole:(enum KTVSingRole)newRole {
    if(oldRole == newRole){
        KTVLogInfo(@"old role:%li is equal to new role", oldRole);
    }
    self.singRole = newRole;
}

- (void)onSingingScoreResultWithScore:(float)score {
}

#pragma mark KTVMusicLoadStateListener

- (void)onMusicLoadProgressWithSongCode:(NSInteger)songCode
                                percent:(NSInteger)percent
                                 status:(AgoraMusicContentCenterPreloadStatus)status
                                    msg:(NSString *)msg
                               lyricUrl:(NSString *)lyricUrl {
    KTVLogInfo(@"load: %li, %li", status, percent);
    dispatch_async_on_main_queue(^{
        
        if(status == AgoraMusicContentCenterPreloadStatusError){
            [VLToast toast:@"加载歌曲失败，请切歌"];
            [self.MVView setBotViewHidden:false];
            self.MVView.loadingType = VLKTVMVViewStateIdle;
            return;
        }
        
        if (status == AgoraMusicContentCenterPreloadStatusOK){
            self.MVView.loadingType = VLKTVMVViewStateIdle;
        }
        self.MVView.loadingProgress = percent;
    });
}

- (void)onMusicLoadFailWithSongCode:(NSInteger)songCode reason:(enum KTVLoadSongFailReason)reason{
    
    dispatch_async_on_main_queue(^{
        if(self.loadMusicCallBack) {
            self.loadMusicCallBack(NO, songCode);
            self.loadMusicCallBack = nil;
        }
        if (reason == KTVLoadSongFailReasonNoLyricUrl) {
            self.MVView.loadingType = VLKTVMVViewStateLoadFail;
        } else {
            self.MVView.loadingType = VLKTVMVViewStateIdle;
//            if(reason == KTVLoadSongFailReasonMusicPreloadFail){
//                if(self.retryCount < 3){
//                    self.retryCount++;
//                    [VLToast toast:@"歌曲加载失败"];
//                    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
//                    KTVSongConfiguration* songConfig = [[KTVSongConfiguration alloc] init];
//                    songConfig.autoPlay = YES;
//                    songConfig.songCode = [model.songNo integerValue];
//                    songConfig.mainSingerUid = [model.userNo integerValue];
//                    [self.ktvApi loadMusicWithConfig:songConfig mode:KTVLoadMusicModeLoadMusicAndLrc onMusicLoadStateListener:self];
//                } else {
//                    [VLToast toast:@"已尝试3次，请自动切歌"];
//                }
//            }
        }
        KTVLogError(@"onMusicLoadFail songCode: %ld error: %ld", songCode, reason);
    });
}

- (void)onMusicLoadSuccessWithSongCode:(NSInteger)songCode lyricUrl:(NSString * _Nonnull)lyricUrl {
    dispatch_async_on_main_queue(^{
        if(self.loadMusicCallBack){
            self.loadMusicCallBack(YES, songCode);
            self.loadMusicCallBack = nil;
        }
        self.MVView.loadingType = VLKTVMVViewStateIdle;
        if(lyricUrl.length > 0){
            KTVLogInfo(@"onMusicLoadSuccessWithSongCode: %ld", self.singRole);
        }
        self.retryCount = 0;
    });
}

@end



