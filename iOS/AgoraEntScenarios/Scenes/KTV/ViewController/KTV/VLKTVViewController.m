//
//  VLKTVViewController.m
//  VoiceOnLine
//

#import "VLKTVViewController.h"
#import "VLKTVTopView.h"
#import "VLKTVMVView.h"
#import "VLRoomPersonView.h"
#import "VLKTVBottomView.h"
#import "VLTouristOnLineView.h"
#import "VLNoBodyOnLineView.h"
#import "VLOnLineListVC.h"

#import "VLKTVSettingView.h"
#import "YGViewDisplayer.h"
//model
#import "VLSongItmModel.h"
#import "VLRoomListModel.h"
#import "VLRoomSeatModel.h"
#import "VLRoomSelSongModel.h"
#import "VLKTVSelBgModel.h"
#import "RtcMusicLrcMessage.h"
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
#import "LEEAlert+KTVModal.h"
#import "LSTPopView+KTVModal.h"
#import "KTVSoloController.h"
@import AgoraRtcKit;
@import AgoraLyricsScore;
@import YYCategories;
@import SDWebImage;

typedef void (^sendStreamSuccess)(BOOL ifSuccess);
static NSInteger streamId = -1;
typedef void (^LyricCallback)(NSString* lyricUrl);
typedef void (^LoadMusicCallback)(AgoraMusicContentCenterPreloadStatus);

@interface VLKTVViewController ()<
VLKTVTopViewDelegate,
VLKTVMVViewDelegate,
VLRoomPersonViewDelegate,
VLKTVBottomViewDelegate,
VLPopSelBgViewDelegate,
VLPopMoreSelViewDelegate,
VLDropOnLineViewDelegate,
VLTouristOnLineViewDelegate,
VLChooseBelcantoViewDelegate,
VLPopChooseSongViewDelegate,
VLsoundEffectViewDelegate,
VLKTVSettingViewDelegate,
VLBadNetWorkViewDelegate,
AgoraLrcViewDelegate,
AgoraRtcMediaPlayerDelegate,
AgoraRtcEngineDelegate,
VLPopScoreViewDelegate,
KTVSoloControllerDelegate
>

@property (nonatomic, strong) VLKTVMVView *MVView;
@property (nonatomic, strong) VLKTVSelBgModel *choosedBgModel;
@property (nonatomic, strong) VLKTVBottomView *bottomView;
@property (nonatomic, strong) VLBelcantoModel *selBelcantoModel;
@property (nonatomic, strong) VLNoBodyOnLineView *noBodyOnLineView; // mv空页面
@property (nonatomic, strong) VLKTVTopView *topView;
@property (nonatomic, strong) VLKTVSettingView *settingView;
@property (nonatomic, strong) VLRoomPersonView *roomPersonView; //房间麦位视图
@property (nonatomic, strong) VLTouristOnLineView *requestOnLineView;//空位上麦
@property (nonatomic, strong) VLPopChooseSongView *chooseSongView; //点歌视图
@property (nonatomic, strong) VLsoundEffectView *soundEffectView; // 音效视图

@property (nonatomic, strong) id<AgoraMusicPlayerProtocol> rtcMediaPlayer;
@property (nonatomic, strong) AgoraMusicContentCenter *AgoraMcc;
@property (nonatomic, strong) VLSongItmModel *choosedSongModel; //点的歌曲
@property (nonatomic, assign) float currentTime;
@property (nonatomic, assign) float currentDuration;
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;

@property (nonatomic, strong) VLPopScoreView *scoreView;

@property (nonatomic, strong) AgoraRtcConnection *mediaPlayerConnection;
@property (nonatomic, strong) NSString *mutedRemoteUserId;
@property (nonatomic, strong) NSMutableDictionary<NSString*, LyricCallback>* lyricCallbacks;

@property (nonatomic, assign) BOOL isNowMicMuted;
@property (nonatomic, assign) BOOL isNowCameraMuted;
@property (nonatomic, assign) BOOL isPlayerPublish;
@property (nonatomic, assign) BOOL isOnMicSeat;
@property (nonatomic, assign) BOOL isEarOn;
@property (nonatomic, assign) double currentVoicePitch;

@property (nonatomic, strong) NSArray <VLRoomSelSongModel*>* selSongsArray;
@property (nonatomic, strong) KTVSoloController* soloControl;

@end

@implementation VLKTVViewController

#pragma mark view lifecycles
- (void)dealloc {
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = UIColor.blackColor;
    self.lyricCallbacks = [NSMutableDictionary dictionary];

    [self subscribeServiceEvent];
    
    // setup view
    [self setBackgroundImage:@"ktv_temp_mainbg"];
    
    UIView *bgView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)];
    bgView.backgroundColor = UIColorMakeWithRGBA(0, 0, 0, 0.6);
    [self.view addSubview:bgView];
    //头部视图
    VLKTVTopView *topView = [[VLKTVTopView alloc]initWithFrame:CGRectMake(0, kStatusBarHeight+10, SCREEN_WIDTH, 22+20+14) withDelegate:self];
    [self.view addSubview:topView];
    self.topView = topView;
    topView.listModel = self.roomModel;
    
    //MV视图(显示歌词...)
    self.MVView = [[VLKTVMVView alloc]initWithFrame:CGRectMake(15, topView.bottom+13, SCREEN_WIDTH-30, (SCREEN_WIDTH-30)*0.67) withDelegate:self];
    [self.view addSubview:self.MVView];
    
    //房间麦位视图
    VLRoomPersonView *personView = [[VLRoomPersonView alloc]initWithFrame:CGRectMake(0, self.MVView.bottom+42, SCREEN_WIDTH, (VLREALVALUE_WIDTH(54)+20)*2+26) withDelegate:self withRTCkit:self.RTCkit];
    self.roomPersonView = personView;
    self.roomPersonView.roomSeatsArray = self.seatsArray;
    [self.view addSubview:personView];
    
    //底部按钮视图
    VLKTVBottomView *bottomView = [[VLKTVBottomView alloc]initWithFrame:CGRectMake(0, SCREEN_HEIGHT-40-kSafeAreaBottomHeight-VLREALVALUE_WIDTH(35), SCREEN_WIDTH, 40) withDelegate:self withRoomNo:self.roomModel.roomNo withData:self.seatsArray];
    self.bottomView = bottomView;
    bottomView.backgroundColor = UIColorClear;
    [self.view addSubview:bottomView];
    
    //空位上麦视图
    VLTouristOnLineView *requestOnLineView = [[VLTouristOnLineView alloc]initWithFrame:CGRectMake(0, SCREEN_HEIGHT-kSafeAreaBottomHeight-56-VLREALVALUE_WIDTH(30), SCREEN_WIDTH, 56) withDelegate:self];
    self.requestOnLineView = requestOnLineView;
    [self.view addSubview:requestOnLineView];
    
    //start join
    [self joinRTCChannel];
    self.soloControl = [[KTVSoloController alloc] initWithRtcEngine:self.RTCkit musicCenter:self.AgoraMcc player:self.rtcMediaPlayer delegate:self];
    
    self.isOnMicSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
    
    //处理背景
    [self prepareBgImage];
    [[UIApplication sharedApplication] setIdleTimerDisabled: YES];
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
    streamId = -1;
    
    [self.AgoraMcc registerEventDelegate:nil];
    [[AppContext shared] setAgoraMcc:nil];
    [AgoraMusicContentCenter destroy];
    
    [self.rtcMediaPlayer stop];
    [[AppContext shared] setAgoraRtcMediaPlayer:nil];
    [self.RTCkit destroyMediaPlayer:self.rtcMediaPlayer];
    
    [AgoraRtcEngineKit destroy];
    VLLog(@"Agora - destroy RTCEngine");
    
    self.soloControl = nil;
    
    [self.lyricCallbacks removeAllObjects];
}

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return true;
}
// 是否允许手动滑回 @return true 是、 false否
- (BOOL)forceEnableInteractivePopGestuzreRecognizer {
    return NO;
}

#pragma mark service handler
- (void)subscribeServiceEvent {
    VL(weakSelf);
    [[AppContext ktvServiceImp] subscribeUserListCountChangedWithBlock:^(NSUInteger count) {
        //TODO
        [weakSelf setRoomUsersCount:count];
    }];
    
    [[AppContext ktvServiceImp] subscribeSingingScoreChangedWithBlock:^(double score) {
        if(![self isCurrentSongMainSinger:VLUserCenter.user.userNo]) {
            //audience use sync to update pitch value, main singer don't
            weakSelf.currentVoicePitch = score;
        }
    }];
    
    [[AppContext ktvServiceImp] subscribeSeatListChangedWithBlock:^(KTVSubscribe status, VLRoomSeatModel* seatModel) {
        
        VLRoomSeatModel* model = [self getUserSeatInfoWithIndex:seatModel.seatIndex];
        if (model == nil) {
            NSAssert(false, @"model == nil");
            return;
        }
        
        if (status == KTVSubscribeCreated || status == KTVSubscribeUpdated) {
            //上麦消息 / 是否打开视频 / 是否静音
            
            [model resetWithInfo:seatModel];
            model.isJoinedChorus = [weakSelf isJoinedChorusWithUserNo:seatModel.userNo];
            
            [weakSelf setSeatsArray:weakSelf.seatsArray];
        } else if (status == KTVSubscribeDeleted) {
            // 下麦消息
            
            // 被下麦用户刷新UI
            if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {
                //当前的座位用户离开RTC通道
                VLRoomSelSongModel *song = weakSelf.selSongsArray.firstObject;
                [weakSelf.MVView updateUIWithUserOnSeat:NO song:song];
                [weakSelf resetChorusStatus:model.userNo];
            }
            
            // 下麦重置占位模型
            [model resetWithInfo:nil];
            [weakSelf setSeatsArray:weakSelf.seatsArray];
        }
        
        //update my seat status
        weakSelf.isOnMicSeat = [weakSelf getCurrentUserSeatInfo] ? YES : NO;
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
            if ([roomInfo.creator isEqualToString:VLUserCenter.user.userNo]) {
                return;
            }
            
            [weakSelf popForceLeaveRoom];
        }
    }];
    
    //callback if choose song list didchanged
    [[AppContext ktvServiceImp] subscribeChooseSongChangedWithBlock:^(KTVSubscribe status, VLRoomSelSongModel * songInfo) {
        // update in-ear monitoring
        [weakSelf _checkInEarMonitoring];
        
        if (KTVSubscribeDeleted == status) {
            [weakSelf removeSelSongWithSongNo:[songInfo.songNo integerValue] sync:NO];
        } else {
            VLRoomSelSongModel* song = [weakSelf selSongWithSongNo:songInfo.songNo];
            //add new song
            if (song == nil) {
                NSMutableArray* selSongsArray = [NSMutableArray arrayWithArray:weakSelf.selSongsArray];
                [selSongsArray appendObject:songInfo];
                weakSelf.selSongsArray = selSongsArray;
                return;
            }
            
            [weakSelf replaceSelSongWithInfo:songInfo];
            
            //有人加入合唱
            //TODO
//            if(songInfo.isChorus
//               && weakSelf.currentPlayingSongNo == nil
//               && songInfo.chorusNo != nil) {
//                [weakSelf.MVView setJoinInViewHidden];
//                [weakSelf setUserJoinChorus:songInfo.chorusNo];
//                return;
//            }
            
            //pin
//            [weakSelf refreshChoosedSongList:nil];
        }
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
    
    self.chooseSongView = (VLPopChooseSongView*)popChooseSongView.currCustomView;
}

//弹出音效
- (void)popSetSoundEffectView {
    [LSTPopView popSetSoundEffectViewWithParentView:self.view
                                       withDelegate:self];
}

//网络差视图
- (void)popBadNetWrokTipView {
    [LSTPopView popBadNetWrokTipViewWithParentView:self.view
                                      withDelegate:self];
}

//用户弹框离开房间
- (void)popForceLeaveRoom {
    VL(weakSelf);
    [LEEAlert popForceLeaveRoomDialogWithCompletion:^{
        for (BaseViewController *vc in weakSelf.navigationController.childViewControllers) {
            if ([vc isKindOfClass:[VLOnLineListVC class]]) {
//                [weakSelf destroyMediaPlayer];
//                [weakSelf leaveRTCChannel];
                [weakSelf.navigationController popToViewController:vc animated:YES];
            }
        }
    }];
}

- (void)showSettingView {
    [YGViewDisplayer popupBottom:self.settingView setupBlock:^(YGViewDisplayOptions * _Nonnull options) {
        options.screenInteraction = YGViewDisplayOptionsUserInteractionDismiss;
        options.safeArea = YGViewDisplayOptionsSafeAreaOverridden;
        options.backgroundColor = [UIColor clearColor];
        options.screenBackgroundColor = [UIColor colorWithWhite:0 alpha:0.5];
    }];
}

- (void)dismissSettingView {
    [YGViewDisplayer dismiss:self.settingView completionHandler:^{
    }];
}

- (void)showScoreViewWithScore:(int)score
                          song:(VLRoomSelSongModel *)song {
    if (score < 0) return;
    if(_scoreView == nil) {
        _scoreView = [[VLPopScoreView alloc] initWithFrame:self.view.bounds withDelegate:self];
        [self.view addSubview:_scoreView];
    }
    VLLog(@"Avg score for the song: %d", score);
    [_scoreView configScore:score];
    [self.view bringSubviewToFront:_scoreView];
    self.scoreView.hidden = NO;
}

- (void)popScoreViewDidClickConfirm
{
    VLLog(@"Using as score view hidding");
    self.scoreView = nil;
}

#pragma mark - rtc callbacks
- (void)rtcEngine:(AgoraRtcEngineKit *)engine
reportAudioVolumeIndicationOfSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers
      totalVolume:(NSInteger)totalVolume {
    AgoraRtcAudioVolumeInfo* speaker = [speakers firstObject];
    if (speaker == nil || ![self isCurrentSongMainSinger:VLUserCenter.user.userNo]) {
        return;
    }
    
    self.currentVoicePitch = speaker.voicePitch;
    [[AppContext ktvServiceImp] updateSingingScoreWithScore:speaker.voicePitch];
}

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
       didChangedToPosition:(NSInteger)position {
    dispatch_async(dispatch_get_main_queue(), ^{
        //只有主唱才能发送消息
        VLRoomSelSongModel *songModel = self.selSongsArray.firstObject;
        if ([songModel.userNo isEqualToString:VLUserCenter.user.userNo]) { //主唱
//            VLLog(@"didChangedToPosition-----%@,%ld",playerKit,position);
            NSDictionary *dict = @{
                @"cmd":@"setLrcTime",
                @"duration":@([self getTotalTime]),
                @"time":@(position),
            };
            [self sendStremMessageWithDict:dict success:^(BOOL success) {
            }];
            
            //check invalid
            //TODO
//            if (![self.currentPlayingSongNo isEqualToString:self.selSongsArray.firstObject.songNo]) {
//                KTVLogInfo(@"play fail, current playing songNo: %@, topSongNo: %@", self.currentPlayingSongNo, self.selSongsArray.firstObject.songNo);
//                [self stopCurrentSong];
//                [self loadAndPlaySong];
//            }
        }
    });
}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
receiveStreamMessageFromUid:(NSUInteger)uid
         streamId:(NSInteger)streamId
             data:(NSData * _Nonnull)data {    //接收到对方的RTC消息
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:data];
    if (![dict[@"cmd"] isEqualToString:@"setLrcTime"] && ![dict[@"cmd"] isEqualToString:@"testDelay"]) {
        VLLog(@"receiveStreamMessageFromUid::%ld---message::%@",uid, dict);
    }
    VLLog(@"recv message: %@, streamID: %d, uid: %d",dict,(int)streamId,(int)uid);
    if ([dict[@"cmd"] isEqualToString:@"setLrcTime"]) {  //同步歌词
        long type = [dict[@"time"] longValue];
        if(type == 0) {
            if (self.rtcMediaPlayer.getPlayerState == AgoraMediaPlayerStatePaused) {
                [self.rtcMediaPlayer resume];
            }
        } else if(type == -1) {
            if (self.rtcMediaPlayer.getPlayerState == AgoraMediaPlayerStatePlaying) {
                [self.rtcMediaPlayer pause];
            }
        } else {
            RtcMusicLrcMessage *musicLrcMessage = [RtcMusicLrcMessage vj_modelWithDictionary:dict];
            float postion = musicLrcMessage.time;
            self.currentTime = postion;
            self.currentDuration = [dict[@"duration"] longValue];
//            KTVLogInfo(@"setLrcTime: %.2f/%.2f, songNo: %@", self.currentTime, self.currentDuration, self.currentPlayingSongNo);
            [_MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
            if (!_MVView.lrcView.isStart) {
                [_MVView start];
            }
            
            NSInteger currentPos = [self.rtcMediaPlayer getPosition];
            if(labs(musicLrcMessage.time - currentPos) > 1000) {
                [self.rtcMediaPlayer seekToPosition:musicLrcMessage.time];
            }
        }
    } else if([dict[@"cmd"] isEqualToString:@"countdown"]) {  //倒计时
        int leftSecond = [dict[@"time"] intValue];
        VLRoomSelSongModel *song = self.selSongsArray.firstObject;
        //TODO
//        if(self.currentPlayingSongNo == nil) {
//            [self.MVView receiveCountDown:leftSecond
//                                   onSeat:self.isOnMicSeat
//                              currentSong:song];
//        }
        VLLog(@"收到倒计时剩余:%d秒",(int)leftSecond);
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

#pragma mark - AgoraLrcDownloadDelegate
- (void)downloadLrcFinishedWithUrl:(NSString *)url {
    KTVLogInfo(@"download lrc finished %@",url);

    LyricCallback callback = [self.lyricCallbacks objectForKey:url];
    if(!callback) {
        return;
    }
    [self.lyricCallbacks removeObjectForKey:url];

    callback(url);
}

- (void)downloadLrcErrorWithUrl:(NSString *)url error:(NSError *)error {
    KTVLogInfo(@"download lrc fail %@: %@",url,error);

    LyricCallback callback = [self.lyricCallbacks objectForKey:url];
    if(!callback) {
        return;
    }
    [self.lyricCallbacks removeObjectForKey:url];

    callback(nil);
}

#pragma mark - action utils / business
- (void)leaveRoom {
    VL(weakSelf);
    [[AppContext ktvServiceImp] leaveRoomWithCompletion:^(NSError * error) {
        if (error != nil) {
            return;
        }
        
//        [weakSelf destroyMediaPlayer];
        for (BaseViewController *vc in weakSelf.navigationController.childViewControllers) {
            if ([vc isKindOfClass:[VLOnLineListVC class]]) {
                [weakSelf.navigationController popToViewController:vc animated:YES];
            }
        }
    }];
}

- (void)loadAndPlaySong {
    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
//    if (model == nil || [model waittingForChorus]) {
//        VLLog(@"skip load, song %@ waitting for chorus", model.songNo);
//        //刷新MV里的视图
//        return;
//    }
    if (model.status != 2 && [model readyToPlay]) {
        [self markSongDidPlayWithModel:model];
        VLLog(@"skip load, song %@ need to mark to playing", model.songNo);
        return;
    }
    
    [self.MVView updateUIWithSong:model onSeat:self.isOnMicSeat];
    
    KTVSingRole role = [model isSongOwner] ? KTVSingRoleMainSinger : KTVSingRoleAudience;
    VL(weakSelf);
    [self.soloControl loadSong:[[model songNo] integerValue] asRole:role withCallback:^(NSInteger songCode, NSString* lyricUrl, KTVSingRole role, KTVLoadSongState state) {
        if(state == KTVLoadSongStateOK) {
            [weakSelf setLrcLyric:lyricUrl withCallback:^(NSString *lyricUrl) {
                if(lyricUrl) {
                    [weakSelf.soloControl playSong:[[model songNo] integerValue] asRole:role];
                }
            }];
        }
    }];
}

- (void)enterSeatWithIndex:(NSInteger)index {
    
    KTVOnSeatInputModel* inputModel = [KTVOnSeatInputModel new];
    inputModel.seatIndex = index;
//    VL(weakSelf);
    [[AppContext ktvServiceImp] enterSeatWithInput:inputModel
                                        completion:^(NSError * error) {
    }];
}

#pragma mark private method
- (void)_leaveSeatWithSeatModel:(VLRoomSeatModel * __nonnull)seatModel
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

#pragma mark - rtc utils
//发送流消息
- (void)sendStremMessageWithDict:(NSDictionary *)dict
                         success:(sendStreamSuccess)success {
    VLLog(@"sendStremMessageWithDict:::%@",dict);
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    if (streamId == -1) {
        AgoraDataStreamConfig *config = [AgoraDataStreamConfig new];
        config.ordered = false;
        config.syncWithAudio = false;
        [self.RTCkit createDataStream:&streamId
                               config:config];
    }
    
    int code = [self.RTCkit sendStreamMessage:streamId
                                         data:messageData];
    if (code == 0) {
        success(YES);
    } else{
//        VLLog(@"发送失败-streamId:%ld\n",streamId);
    };
}

- (void)joinRTCChannel {
    self.RTCkit = [AgoraRtcEngineKit sharedEngineWithAppId:[AppContext.shared appId] delegate:self];
    //use game streaming in solo mode, chrous profile in chrous mode
    [self.RTCkit setAudioScenario:AgoraAudioScenarioGameStreaming];
    
    [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
    [self.RTCkit setAudioScenario:AgoraAudioScenarioGameStreaming];
    [self.RTCkit setParameters:@"{\"che.audio.custom_bitrate\":128000}"];
    [self.RTCkit setParameters:@"{\"che.audio.custom_payload_type\":78}"];
    [self.RTCkit setChannelProfile:AgoraChannelProfileLiveBroadcasting];
    /// 开启唱歌评分功能
    int code = [self.RTCkit enableAudioVolumeIndication:250 smooth:3 reportVad:YES];
    if (code == 0) {
        VLLog(@"评分回调开启成功\n");
    } else {
        VLLog(@"评分回调开启失败：%d\n",code);
    }
    
    [self.RTCkit enableVideo];
    [self.RTCkit enableAudio];
    
    VLRoomSeatModel* myseat = [self.seatsArray objectAtIndex:0];
    
    self.isNowMicMuted = myseat.isAudioMuted;
    self.isNowCameraMuted = myseat.isVideoMuted;
    
    AgoraVideoEncoderConfiguration *encoderConfiguration =
    [[AgoraVideoEncoderConfiguration alloc] initWithSize:CGSizeMake(100, 100)
                                               frameRate:AgoraVideoFrameRateFps7
                                                 bitrate:20
                                         orientationMode:AgoraVideoOutputOrientationModeFixedLandscape
                                              mirrorMode:AgoraVideoMirrorModeAuto];
    [self.RTCkit setVideoEncoderConfiguration:encoderConfiguration];
    
    
    VLLog(@"Agora - joining RTC channel with token: %@, for roomNo: %@, with uid: %@", VLUserCenter.user.agoraRTCToken, self.roomModel.roomNo, VLUserCenter.user.id);
    
    KTVLogInfo(@"Agora - joining RTC channel with token: %@, for roomNo: %@, with uid: %@", VLUserCenter.user.agoraRTCToken, self.roomModel.roomNo, VLUserCenter.user.id);
    [self.RTCkit joinChannelByToken:VLUserCenter.user.agoraRTCToken
                          channelId:self.roomModel.roomNo
                                uid:[VLUserCenter.user.id integerValue]
                       mediaOptions:[self channelMediaOptions]
                        joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        VLLog(@"Agora - 加入RTC成功");
       
    }];
    [self.RTCkit setEnableSpeakerphone:YES];
    
    //prepare content center
    AgoraMusicContentCenterConfig *contentCenterConfiguration = [[AgoraMusicContentCenterConfig alloc] init];
    contentCenterConfiguration.rtcEngine = self.RTCkit;
    contentCenterConfiguration.appId = [[AppContext shared] appId];
    contentCenterConfiguration.mccUid = [VLUserCenter.user.id integerValue];
    contentCenterConfiguration.token = VLUserCenter.user.agoraRTMToken;
    self.AgoraMcc = [AgoraMusicContentCenter sharedContentCenterWithConfig:contentCenterConfiguration];
    [self.AgoraMcc registerEventDelegate:[AppContext shared]];
    [[AppContext shared] setAgoraMcc:self.AgoraMcc];
    [self.AgoraMcc enableMainQueueDispatch:YES];
    
    self.rtcMediaPlayer = [self.AgoraMcc createMusicPlayerWithDelegate:[AppContext shared]];
    // 调节本地播放音量。0-100
    [self.rtcMediaPlayer adjustPlayoutVolume:200];
    // 调节远端用户听到的音量。0-400
    [self.rtcMediaPlayer adjustPublishSignalVolume:200];
}

- (void)leaveRTCChannel {
    [self.RTCkit leaveChannel:^(AgoraChannelStats * _Nonnull stat) {
        VLLog(@"Agora - Leave RTC channel");
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
    [option setPublishMediaPlayerId:[self.rtcMediaPlayer getMediaPlayerId]];
    [option setPublishMediaPlayerAudioTrack:self.isPlayerPublish];
    [option setEnableAudioRecordingOrPlayout:YES];
    return option;
}

- (void)updateRemoteUserMuteStatus:(NSString *)userId
                            doMute:(BOOL)doMute {
    VLLog(@"Agora - updating UID: %@ to %d", userId, doMute);
    [self.RTCkit muteRemoteAudioStream:[VLGlobalHelper getAgoraPlayerUserId:userId] mute:doMute];
}

- (void)resetPlayer
{
    VLLog(@"Agora - updating unmute all user");
    self.mutedRemoteUserId = nil;
    [self.RTCkit muteAllRemoteAudioStreams:NO];
}

#pragma mark - KTVSoloController
- (void)controller:(KTVSoloController *)controller song:(NSInteger)songCode didChangedToState:(AgoraMediaPlayerState)state
{
    dispatch_async(dispatch_get_main_queue(), ^{
        VLRoomSelSongModel* model = [[self.selSongsArray filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(VLRoomSelSongModel*  _Nullable evaluatedObject, NSDictionary<NSString *,id> * _Nullable bindings) {
            return [[evaluatedObject songNo] integerValue] == songCode;
        }]] firstObject];
        if(state == AgoraMediaPlayerStatePlaying) {
            [self.MVView start];
            [self.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
            [self.MVView updateUIWithUserOnSeat:NO song:model];
        } else if(state == AgoraMediaPlayerStatePaused) {
            [self.MVView stop];
            [self.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPause];
            [self.MVView updateUIWithUserOnSeat:NO song:model];
        } else if(state == AgoraMediaPlayerStateStopped) {
            self.currentTime = 0;
            [self.MVView reset];
        } else if(state == AgoraMediaPlayerStatePlayBackAllLoopsCompleted) {
            VLLog(@"Playback all loop completed");
            VLRoomSelSongModel *songModel = self.selSongsArray.firstObject;
            if([self isCurrentSongMainSinger:VLUserCenter.user.userNo]) {
                [self showScoreViewWithScore:[self.MVView getAvgSongScore] song:songModel];
            }
            [self removeCurrentSongWithSync:YES];
        }
    });
}

#pragma mark -- VLKTVTopViewDelegate
- (void)onVLKTVTopView:(VLKTVTopView *)view closeBtnTapped:(id)sender {
    VL(weakSelf);
    if (VLUserCenter.user.ifMaster) { //自己是房主关闭房间
        [LEEAlert popRemoveRoomDialogWithCancelBlock:nil
                                       withDoneBlock:^{
            [weakSelf leaveRoom];
        }];
    } else {
        [LEEAlert popLeaveRoomDialogWithCancelBlock:nil
                                      withDoneBlock:^{
            [weakSelf resetChorusStatus:VLUserCenter.user.userNo];
            [weakSelf leaveRoom];
        }];
    }
}

#pragma mark - VLPopMoreSelViewDelegate
- (void)onVLKTVMoreSelView:(VLPopMoreSelView *)view
                 btnTapped:(id)sender
                 withValue:(VLKTVMoreBtnClickType)typeValue {
    [[LSTPopView getPopViewWithCustomView:view] dismiss];
    switch (typeValue) {
        case VLKTVMoreBtnClickTypeBelcanto:
            [self popBelcantoView];
            break;
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
- (void)onVLKTVBottomView:(VLKTVBottomView *)view
                btnTapped:(id)sender
               withValues:(VLKTVBottomBtnClickType)typeValue {
    switch (typeValue) {
        case VLKTVBottomBtnClickTypeMore:  //更多
//            [self popSelMVBgView];
            [self popSelMoreView];
            break;
        case VLKTVBottomBtnClickTypeChorus:
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
- (void)onVLRoomPersonView:(VLRoomPersonView *)view
   seatItemTappedWithModel:(VLRoomSeatModel *)model
                   atIndex:(NSInteger)seatIndex {
    if(VLUserCenter.user.ifMaster) {
        //is owner
        if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {
            //self, return
            return;
        }
        if (model.userNo.length > 0) {
            return [self popDropLineViewWithSeatModel:model];
        }
    } else {
        if (model.userNo.length > 0) {
            //occupied
            if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {//点击的是自己
                return [self popDropLineViewWithSeatModel:model];
            }
        } else{
            //empty
            BOOL isOnSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
            if (!isOnSeat) {
                //not yet seated
                [self enterSeatWithIndex:seatIndex];
            }
        }
    }
}

- (void)onVLRoomPersonView:(VLRoomPersonView *)view onRenderVideo:(VLRoomSeatModel *)model inView:(UIView *)videoView atIndex:(NSInteger)seatIndex
{
    AgoraRtcVideoCanvas *videoCanvas = [[AgoraRtcVideoCanvas alloc] init];
    videoCanvas.uid = [model.rtcUid unsignedIntegerValue];
    videoCanvas.view = videoView;
    videoCanvas.renderMode = AgoraVideoRenderModeHidden;
    if([model.userNo isEqual:VLUserCenter.user.userNo]) {
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
//    inputModel.userNo = VLUserCenter.user.userNo;
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
- (void)chooseSongView:(VLPopChooseSongView*)view tabbarDidClick:(NSUInteger)tabIndex {
    if (tabIndex != 1) {
        return;
    }
    
    [self refreshChoosedSongList:nil];
}

#pragma mark - VLChooseBelcantoViewDelegate
- (void)onVLChooseBelcantoView:(VLChooseBelcantoView *)view
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

// Reset chorus to audience
- (void)resetChorusStatus:(NSString *)userNo {
//    if([self ifChorusSinger:userNo]) {
//        [self setSelfChorusUserNo:nil];
//        if([userNo isEqualToString:VLUserCenter.user.userNo]) {
//            if(self.rtcMediaPlayer != nil) {
//                [self.rtcMediaPlayer stop];
//            }
//            [self resetPlayer];
//            [self disableMediaChannel];
//        }
//    } else if(userNo == nil) {
//        [self setSelfChorusUserNo:nil];
//    }
}

#pragma mark VLDropOnLineViewDelegate
- (void)onVLDropOnLineView:(VLDropOnLineView *)view action:(VLRoomSeatModel *)seatModel {
    [self _leaveSeatWithSeatModel:seatModel withCompletion:^(NSError *error) {
        [[LSTPopView getPopViewWithCustomView:view] dismiss];
    }];
}

#pragma mark VLTouristOnLineViewDelegate
//上麦方式
- (void)requestOnlineAction {
}

#pragma mark - AgoraLrcViewDelegate
-(NSTimeInterval)getTotalTime {
    VLRoomSelSongModel *model = self.selSongsArray.firstObject;
    if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {
        NSTimeInterval time = [_rtcMediaPlayer getDuration];
        return time;
    }
    return self.currentDuration;
}

- (NSTimeInterval)getPlayerCurrentTime {
    VLRoomSelSongModel *model = self.selSongsArray.firstObject;
    if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {
        NSTimeInterval time = [_rtcMediaPlayer getPosition];
        return time;
    }
    
    return self.currentTime;
}

#pragma mark - MVViewDelegate
// 打分实时回调
- (void)onKTVMVView:(VLKTVMVView *)view scoreDidUpdate:(int)score {
}

- (void)onKTVMVView:(VLKTVMVView *)view btnTappedWithActionType:(VLKTVMVViewActionType)type {
    if (type == VLKTVMVViewActionTypeSetParam) {
        [self showSettingView];
    } else if (type == VLKTVMVViewActionTypeMVPlay) { //播放
        [self.soloControl resumePlay];
//        [self.rtcMediaPlayer resume];
//        //发送继续播放的消息
//        [self sendPauseOrResumeMessage:0];
        
        //failed to load lyric/music, try again
//        [self loadAndPlaySong];
    } else if (type == VLKTVMVViewActionTypeMVPause) { //暂停
        [self.soloControl pausePlay];
//        [self.rtcMediaPlayer pause];
//        [self.MVView stop];
//        //发送暂停的消息
//        [self sendPauseOrResumeMessage:-1];
    } else if (type == VLKTVMVViewActionTypeMVNext) { //切换
        VL(weakSelf);
        [LEEAlert popSwitchSongDialogWithCancelBlock:nil
                                       withDoneBlock:^{
            if (weakSelf.selSongsArray.count >= 1) {
                [weakSelf.soloControl stopSong];
                [weakSelf removeCurrentSongWithSync:YES];
            }
        }];
    } else if (type == VLKTVMVViewActionTypeSingOrigin) { // 原唱
        [self.rtcMediaPlayer selectAudioTrack:0];
        [self sendTrackModeMessage:0];
    } else if (type == VLKTVMVViewActionTypeSingAcc) { // 伴奏
        [self.rtcMediaPlayer selectAudioTrack:1];
        [self sendTrackModeMessage:1];
    }
}

//合唱的倒计时事件
- (void)onKTVMVView:(VLKTVMVView *)view timerCountDown:(NSInteger)countDownSecond {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    if ([selSongModel.userNo isEqualToString:VLUserCenter.user.userNo]) {
        NSDictionary *dict = @{
            @"cmd":@"countdown",
            @"time":@(countDownSecond)
        };
        [self sendStremMessageWithDict:dict
                               success:^(BOOL ifSuccess) {
            if (ifSuccess) {
                VLLog(@"倒计时发送成功");
            }
        }];
    }
}

- (void)onKTVMVView:(VLKTVMVView *)view startSingType:(VLKTVMVViewSingActionType)singType {
    if (singType == VLKTVMVViewSingActionTypeSolo) { // 独唱
        //发送独唱的消息
        [self enterSoloMode];
    } else if (singType == VLKTVMVViewSingActionTypeJoinChorus) { // 加入合唱
        if(!self.isOnMicSeat) {
            [VLToast toast:KTVLocalizedString(@"请先上坐")];
        } else {
            [self joinChorus]; //发送加入合唱的消息
        }
    }
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
        [self.rtcMediaPlayer setAudioPitch:value];
    } else if (type == VLKTVValueDidChangedTypeSound) { // 音量
        // 调节音频采集信号音量、取值范围为 [0,400]
        // 0、静音 100、默认原始音量 400、原始音量的4倍、自带溢出保护
        [self.RTCkit adjustRecordingSignalVolume:setting.soundValue * 400];
        if(setting.soundOn) {
            [self.RTCkit setInEarMonitoringVolume:setting.soundValue * 400];
        }
    } else if (type == VLKTVValueDidChangedTypeAcc) { // 伴奏
        int value = setting.accValue * 400;
        // 官方文档是100 ？ SDK 是 400？？？？
        // 调节本地播放音量 取值范围为 [0,100]
        // 0、无声。 100、（默认）媒体文件的原始播放音量
        [self.rtcMediaPlayer adjustPlayoutVolume:value];
        
        // 调节远端用户听到的音量 取值范围[0、400]
        // 100: （默认）媒体文件的原始音量。400: 原始音量的四倍（自带溢出保护）
        [self.rtcMediaPlayer adjustPublishSignalVolume:value];
    } else if (type == VLKTVValueDidChangedTypeListItem) {
        AgoraAudioEffectPreset preset = [self audioEffectPreset:setting.kindIndex];
        [self.RTCkit setAudioEffectPreset:preset];
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
- (void)soundEffectItemClickAction:(VLKTVSoundEffectType)effectType {
    if (effectType == VLKTVSoundEffectTypeHeFeng) {
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:3 param2:4];
    } else if (effectType == VLKTVSoundEffectTypeXiaoDiao){
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:2 param2:4];
    } else if (effectType == VLKTVSoundEffectTypeDaDiao){
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:1 param2:4];
    } else if (effectType == VLKTVSoundEffectTypeNone) {
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:0 param2:4];
    }
    VLLog(@"Agora - Setting effect type to %lu", effectType);
}

#pragma mark --
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

//主唱告诉后台当前播放的歌曲
- (void)markSongDidPlayWithModel:(VLRoomSelSongModel *)model {
    if ([model waittingForChorus] || model.status == 2) {
        return;
    }
    [[AppContext ktvServiceImp] markSongDidPlayWithInput:model
                                              completion:^(NSError * error) {
    }];
}

- (void)sendPauseOrResumeMessage:(NSInteger)type {
    NSDictionary *dict;
    if (type == 0) {
        dict = @{
            @"cmd":@"setLrcTime",
            @"time":@"0"
        };
    } else if (type == -1) {
        dict = @{
            @"cmd":@"setLrcTime",
            @"time":@"-1"
        };
    }
    [self sendStremMessageWithDict:dict success:^(BOOL ifSuccess) {
    }];
}

- (void)sendTrackModeMessage:(NSInteger)type {
    NSDictionary *dict;
    if (type == 0) {
        dict = @{
            @"cmd":@"TrackMode",
            @"value":@"0"
        };
    }else if (type == 1){
        dict = @{
            @"cmd":@"TrackMode",
            @"value":@"1"
        };
    }
    [self sendStremMessageWithDict:dict success:^(BOOL ifSuccess) {
    }];
}

//发送独唱的消息
- (void)enterSoloMode {
    [[AppContext ktvServiceImp] enterSoloMode];
}

- (void)joinChorus {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    
    KTVJoinChorusInputModel* inputModel = [KTVJoinChorusInputModel new];
    inputModel.isChorus = @"1";
    inputModel.songNo = selSongModel.songNo;
    [[AppContext ktvServiceImp] joinChorusWithInput:inputModel
                                         completion:^(NSError * error) {
    }];
}

#pragma mark - Util functions to check user character for current song.
- (BOOL)isCurrentSongMainSinger:(NSString *)userNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    return [selSongModel.userNo isEqualToString:userNo];
}

- (BOOL)isJoinedChorusWithUserNo:(NSString*)userNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    return selSongModel.isChorus && [selSongModel.chorusNo isEqualToString:userNo];
}

- (BOOL)ifChorusSinger:(NSString *)userNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    VLLog(@"Agora - Song chorusNo: %@, userNo: %@", selSongModel.chorusNo, userNo);
    if(selSongModel != nil && selSongModel.isChorus && [selSongModel.chorusNo isEqualToString:userNo]) {
        return YES;
    }
    
    return NO;
}

- (NSString *)getChrousSingerUserNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    if(selSongModel != nil && selSongModel.isChorus && selSongModel.chorusNo != nil) {
        return selSongModel.chorusNo;
    }
    
    return nil;
}

- (BOOL)isCurrentSongChorus {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    if(selSongModel != nil) {
        return selSongModel.isChorus;
    }
    
    return NO;
}

#pragma mark other
- (void)setLrcLyric:(NSString*)url withCallback:(void (^ _Nullable)(NSString* lyricUrl))block
{
    BOOL taskExits = [self.lyricCallbacks objectForKey:url] != nil;
    if(!taskExits){
        //overwrite existing callback and use new
        [self.lyricCallbacks setObject:block forKey:url];
    }
    [self.MVView.lrcView setLrcUrlWithUrl:url];
}

- (void)setSelfChorusUserNo:(NSString *)userNo {
    VLRoomSelSongModel *song = self.selSongsArray.firstObject;
    if(song != nil) {
        if(userNo == nil) {
            song.isChorus = NO;
        }
        song.chorusNo = userNo;
    }
}

- (void)setUserJoinChorus:(NSString *)userNo {
    [self setSelfChorusUserNo:userNo];
    
    for (VLRoomSeatModel *seat in self.seatsArray) {
        if ([seat.userNo isEqualToString:userNo]) {
            seat.isJoinedChorus = YES;
            break;
        }
    }
    self.roomPersonView.roomSeatsArray = self.seatsArray;
}

- (void)removeCurrentSongWithSync:(BOOL)sync
{
    VLRoomSelSongModel* top = [self.selSongsArray firstObject];
    if(top && top.songNo.length != 0) {
        [self removeSelSongWithSongNo:[top.songNo integerValue] sync:sync];
    }
}

- (void)removeSelSongWithSongNo:(NSInteger)songNo sync:(BOOL)sync {
    __block VLRoomSelSongModel* removed;
    NSMutableArray<VLRoomSelSongModel*> *updatedList = [NSMutableArray arrayWithArray:[self.selSongsArray filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(VLRoomSelSongModel*  _Nullable evaluatedObject, NSDictionary<NSString *,id> * _Nullable bindings) {
        if([evaluatedObject.songNo integerValue] == songNo) {
            removed = evaluatedObject;
            return false;
        }
        return true;
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
    }
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

- (void)replaceSelSongWithInfo:(VLRoomSelSongModel*)songInfo {
    NSMutableArray* selSongsArray = [NSMutableArray array];
    [self.selSongsArray enumerateObjectsUsingBlock:^(VLRoomSelSongModel * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([obj.songNo isEqualToString:songInfo.songNo]) {
            [selSongsArray addObject:songInfo];
            return;
        }
        [selSongsArray addObject:obj];
    }];
    
    self.selSongsArray = selSongsArray;
}

#pragma mark - getter/handy utils
- (BOOL)isRoomOwner {
    return [self.roomModel.creatorNo isEqualToString:VLUserCenter.user.userNo];
}

- (BOOL)isBroadcaster {
    return [self isRoomOwner] || self.isOnMicSeat;
}

- (VLRoomSeatModel*)getCurrentUserSeatInfo {
    for (VLRoomSeatModel *model in self.seatsArray) {
        if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {
            return model;
        }
    }
    
    return nil;
}

- (VLRoomSeatModel*)getUserSeatInfoWithIndex:(NSUInteger)seatIndex {
    for (VLRoomSeatModel *model in self.seatsArray) {
        if (model.seatIndex == seatIndex) {
            return model;
        }
    }
    
    return nil;
}

#pragma mark - setter
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
}

- (void)setIsOnMicSeat:(BOOL)isOnMicSeat {
    _isOnMicSeat = isOnMicSeat;
    
    //start mic once enter seat
    if(isOnMicSeat) {
        [self.RTCkit setClientRole:AgoraClientRoleBroadcaster];
    } else {
        [self.RTCkit setClientRole:AgoraClientRoleAudience];
        [self resetPlayer];
    }
    [self.RTCkit enableLocalAudio:isOnMicSeat];
    [self.RTCkit muteLocalAudioStream:!isOnMicSeat];
    
    
    VLRoomSeatModel* info = [self getCurrentUserSeatInfo];
    self.isNowMicMuted = info.isAudioMuted;
    self.isNowCameraMuted = info.isVideoMuted;
    
    self.bottomView.hidden = !_isOnMicSeat;
    self.requestOnLineView.hidden = !self.bottomView.hidden;
}

- (void)setIsNowMicMuted:(BOOL)isNowMicMuted {
    BOOL oldValue = _isNowMicMuted;
    _isNowMicMuted = isNowMicMuted;
    
    [self.RTCkit adjustRecordingSignalVolume:isNowMicMuted ? 0 : 100];
    if(oldValue != isNowMicMuted) {
        [self.bottomView updateAudioBtn:isNowMicMuted];
    }
}

- (void)setIsNowCameraMuted:(BOOL)isNowCameraMuted {
    BOOL oldValue = _isNowCameraMuted;
    _isNowCameraMuted = isNowCameraMuted;
    
    [self.RTCkit enableLocalVideo:!isNowCameraMuted];
    [self.RTCkit updateChannelWithMediaOptions:[self channelMediaOptions]];
    if(oldValue != isNowCameraMuted) {
        [self.bottomView updateVideoBtn:isNowCameraMuted];
    }
}

- (void)setIsEarOn:(BOOL)isEarOn {
    _isEarOn = isEarOn;
    [self _checkInEarMonitoring];
    [self.settingView setIsEarOn:isEarOn];
}

- (void)_checkInEarMonitoring {
    if([self isCurrentSongMainSinger:VLUserCenter.user.userNo]) {
        [self.RTCkit enableInEarMonitoring:_isEarOn];
    } else {
        [self.RTCkit enableInEarMonitoring:NO];
    }
}

- (void)setSelSongsArray:(NSArray<VLRoomSelSongModel *> *)selSongsArray {
    NSArray<VLRoomSelSongModel*> *oldSongsArray = self.selSongsArray;
    _selSongsArray = selSongsArray;
    
    if (self.chooseSongView) {
        self.chooseSongView.selSongsArray = selSongsArray; //刷新已点歌曲UI
    }
    
    [self.roomPersonView updateSingBtnWithChoosedSongArray:self.selSongsArray];
    
    if([[selSongsArray firstObject] songNo] != [[oldSongsArray firstObject] songNo]) {
        //if top songno changes, try to load and play new song
        [self loadAndPlaySong];
    }
}

- (void)setCurrentVoicePitch:(double)currentVoicePitch {
    [self.MVView setVoicePitch:@[@(currentVoicePitch)]];
}

#pragma mark - lazy getter
- (VLKTVSettingView *)settingView {
    if (!_settingView) {
        _settingView = [[VLKTVSettingView alloc] initWithSetting:nil];
        _settingView.backgroundColor = UIColorMakeWithHex(@"#152164");
        [_settingView vl_radius:20 corner:UIRectCornerTopLeft | UIRectCornerTopRight];
        _settingView.delegate = self;
    }
    return _settingView;
}

@end
