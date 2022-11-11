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
@import AgoraRtcKit;
@import AgoraLyricsScore;
@import YYCategories;
@import SDWebImage;

typedef void (^sendStreamSuccess)(BOOL ifSuccess);
typedef enum : NSUInteger {
    KTVSingRoleMainSinger,
    KTVSingRoleAudience
} KTVSingRole;
static NSInteger streamId = -1;
typedef void (^LyricCallback)(NSString* lyricUrl);
typedef void (^LoadMusicCallback)(void);

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
AgoraMusicContentCenterEventDelegate,
VLPopScoreViewDelegate
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
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;

@property (nonatomic, strong) VLPopScoreView *scoreView;

@property (nonatomic, strong) AgoraRtcConnection *mediaPlayerConnection;
@property (nonatomic, strong) NSString *mutedRemoteUserId;
@property (nonatomic, strong) NSString *currentPlayingSongNo;
@property (nonatomic, strong) NSMutableDictionary<NSString*, LyricCallback>* lyricCallbacks;
@property (nonatomic, strong) NSMutableDictionary<NSString*, LoadMusicCallback>* musicCallbacks;

@property (nonatomic, assign) BOOL isNowMicMuted;
@property (nonatomic, assign) BOOL isNowCameraMuted;
@property (nonatomic, assign) BOOL isPlayerPublish;
@property (nonatomic, assign) BOOL isOnMicSeat;

@property (nonatomic, strong) NSArray <VLRoomSelSongModel*>* selSongsArray;

@end

@implementation VLKTVViewController

#pragma mark view lifecycles
- (void)dealloc {
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
    VLKTVTopView *topView = [[VLKTVTopView alloc]initWithFrame:CGRectMake(0, kStatusBarHeight+10, SCREEN_WIDTH, 22+20+14) withDelegate:self];
    [self.view addSubview:topView];
    self.topView = topView;
    topView.listModel = self.roomModel;
    
    //MV视图(显示歌词...)
    self.MVView = [[VLKTVMVView alloc]initWithFrame:CGRectMake(15, topView.bottom+13, SCREEN_WIDTH-30, (SCREEN_WIDTH-30)*0.67) withDelegate:self];
    self.MVView.lrcView.delegate = self;
    [self.view addSubview:self.MVView];
    
    //房间麦位视图
    VLRoomPersonView *personView = [[VLRoomPersonView alloc]initWithFrame:CGRectMake(0, self.MVView.bottom+42, SCREEN_WIDTH, (VLREALVALUE_WIDTH(54)+20)*2+26) withDelegate:self withRTCkit:self.RTCkit];
    self.roomPersonView = personView;
    [self.roomPersonView setSeatsArray:self.seatsArray];
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
    
    
    self.isOnMicSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
    
    //处理背景
    [self prepareBgImage];
    [[UIApplication sharedApplication] setIdleTimerDisabled: YES];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [UIViewController popGestureClose:self];
    
    //请求已点歌曲
    VL(weakSelf);
    [self refreshChoosedSongList:^{
        //拿到当前歌的歌词去播放和同步歌词
        VLRoomSelSongModel *selSongModel = weakSelf.selSongsArray.firstObject;
        //请求歌词和歌曲
        [self loadAndPlaySong];
    }];
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
    self.rtcMediaPlayer = nil;

    if(self.mediaPlayerConnection) {
//        [self disableMediaChannel];
        self.mediaPlayerConnection = nil;
    }

    if(self.AgoraMcc) {
//        [self.AgoraMcc registerEventDelegate:nil];
        VLLog(@"Agora - unregisterEventHandler");
        [AgoraMusicContentCenter destroy];
        VLLog(@"Agora - destroy MCC");
        self.AgoraMcc = nil;
    }
    
    [AgoraRtcEngineKit destroy];
    VLLog(@"Agora - destroy RTCEngine");
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    [self.lyricCallbacks removeAllObjects];
    [self.musicCallbacks removeAllObjects];
}

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return true;
}
// 是否允许手动滑回 @return true 是、 false否
- (BOOL)forceEnableInteractivePopGestureRecognizer {
    return NO;
}

#pragma mark service handler
- (void)subscribeServiceEvent {
    VL(weakSelf);
    [[AppContext ktvServiceImp] subscribeUserListCountChangedWithBlock:^(NSUInteger count) {
        //TODO
        [weakSelf setRoomUsersCount:count];
    }];
    
    [[AppContext ktvServiceImp] subscribeSeatListChangedWithBlock:^(KTVSubscribe status, VLRoomSeatModel* seatModel) {
        
        VLRoomSeatModel* model = [self getUserSeatInfoWithIndex:seatModel.seatIndex];
        if (model == nil) {
            NSAssert(false, @"model == nil");
            return;
        }
        if (status == KTVSubscribeCreated || status == KTVSubscribeUpdated) {
            //上麦消息 / 是否打开视频 / 是否静音
            
            //TODO can be removed
            model.isMaster = seatModel.isMaster;
            model.headUrl = seatModel.headUrl;
            model.seatIndex = seatModel.seatIndex;
            model.name = seatModel.name;
            model.userNo = seatModel.userNo;
            model.isVideoMuted = seatModel.isVideoMuted;
            model.isSelfMuted = seatModel.isSelfMuted;
            //TODO ??
            model.rtcUid = seatModel.rtcUid;
            
            if ([VLUserCenter.user.userNo isEqualToString:model.userNo]) {
                BOOL isMainSinger = [weakSelf isMainSinger:model.userNo];
                model.isOwner = isMainSinger;
                
                weakSelf.isOnMicSeat = YES;
            }
            
            //TODO
            VLRoomSelSongModel *song = weakSelf.selSongsArray.firstObject;
            if (song.isChorus && [song.chorusNo isEqualToString:seatModel.userNo]) {
                model.isJoinedChorus = YES;
            }
            
            [weakSelf.roomPersonView updateSeatsByModel:model];
        } else if (status == KTVSubscribeDeleted) {
            // 下麦消息
            
            // 被下麦用户刷新UI
            //TODO 需要重新review
            if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {
                //当前的座位用户离开RTC通道
                VLRoomSelSongModel *song = weakSelf.selSongsArray.firstObject;
                [weakSelf.MVView updateUIWithUserOnSeat:NO song:song];
                
                //user leave seat, hide bottom bar
                weakSelf.isOnMicSeat = NO;
                
                // 取出对应模型、防止数组越界
                [weakSelf setSelfAudience];
                [weakSelf resetChorusStatus:model.userNo];
            }
            
            // 下麦重置占位模型
            [model resetLeaveSeat];
            [weakSelf.roomPersonView setSeatsArray:weakSelf.seatsArray];
        }
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
            //发送通知
            //TODO don't use notification center
//            [[NSNotificationCenter defaultCenter]postNotificationName:kExitRoomNotification object:nil];
            [weakSelf popForceLeaveRoom];
        }
    }];
    
    //callback if choose song list didchanged
    [[AppContext ktvServiceImp] subscribeChooseSongChangedWithBlock:^(KTVSubscribe status, VLRoomSelSongModel * songInfo) {
        //refresh first
        [weakSelf refreshChoosedSongList:nil];
        if (KTVSubscribeCreated == status || KTVSubscribeUpdated == status) {
            
            //TODO 逻辑要理一下
            if (KTVSubscribeUpdated == status) {
                //有人加入合唱
                if(songInfo.isChorus
                   && weakSelf.currentPlayingSongNo == nil
                   && songInfo.chorusNo != nil) {
                    [weakSelf.MVView setJoinInViewHidden];
                    [weakSelf setUserJoinChorus:songInfo.chorusNo];
                    [weakSelf markSongDidPlayWithModel:songInfo];
                    return;
                }
                
                [weakSelf loadAndPlaySong];
                
                //观众看到打分
                if (songInfo.status == 2) {
                    double voicePitch = songInfo.score;
                    [weakSelf.MVView setVoicePitch:@[@(voicePitch)]];
                    return;
                }
            }
        } else if (KTVSubscribeDeleted == status) {
            VLRoomSelSongModel *selSongModel = weakSelf.selSongsArray.firstObject;
            if (![selSongModel.songNo isEqualToString:songInfo.songNo]) {
                return;
            }
            
            //切换歌曲
            /*
             1. removed song is top song, play next
             2. waitting for play, play next
             */
            BOOL removedSongIsPlaying = [selSongModel.songNo isEqualToString:weakSelf.currentPlayingSongNo];
            BOOL isWaitingForPlay = [weakSelf.currentPlayingSongNo length] == 0;
            if (removedSongIsPlaying || isWaitingForPlay) {
                [weakSelf stopCurrentSong];
            }
        }
    }];
}

#pragma mark view helpers
- (void)prepareBgImage {
    if (self.roomModel.bgOption) {
        VLKTVSelBgModel *selBgModel = [VLKTVSelBgModel new];
        selBgModel.imageName = [NSString stringWithFormat:@"ktv_mvbg%d",(int)self.roomModel.bgOption];
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
                [weakSelf destroyMediaPlayer];
                [weakSelf leaveRTCChannel];
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
    [YGViewDisplayer dismiss:self.settingView completionHandler:^{}];
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
    if (speakers.count) {
        if([self isMainSinger:VLUserCenter.user.userNo]) {
            double voicePitch = (double)totalVolume;
            [self.MVView setVoicePitch:@[@(voicePitch)]];
            [[AppContext ktvServiceImp] updateSingingScoreWithTotalVolume:totalVolume];
        }
    }
}

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
          didChangedToState:(AgoraMediaPlayerState)state
                      error:(AgoraMediaPlayerError)error {
    dispatch_async(dispatch_get_main_queue(), ^{
        VLLog(@"AgoraMediaPlayerState---%ld\n",state);
        if (state == AgoraMediaPlayerStateOpenCompleted) {
            [playerKit play];
        } else if (state == AgoraMediaPlayerStatePlayBackCompleted) {
            VLLog(@"Playback Completed");
        } else if (state == AgoraMediaPlayerStatePlayBackAllLoopsCompleted) {
            dispatch_async(dispatch_get_main_queue(), ^{
                VLLog(@"Playback all loop completed");
                VLRoomSelSongModel *songModel = self.selSongsArray.firstObject;
                if([self isMainSinger:VLUserCenter.user.userNo]) {
                    [self showScoreViewWithScore:[self.MVView getAvgSongScore] song:songModel];
                }
                [self playNextSong:0];
            });
        } else if (state == AgoraMediaPlayerStateStopped) {
        }
    });
}

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
       didChangedToPosition:(NSInteger)position {
    //只有主唱才能发送消息
    if (self.selSongsArray.count > 0) {
        VLRoomSelSongModel *songModel = self.selSongsArray.firstObject;
            if ([songModel.userNo isEqualToString:VLUserCenter.user.userNo]) { //主唱
//                VLLog(@"didChangedToPosition-----%@,%ld",playerKit,position);
                NSDictionary *dict = @{
                    @"cmd":@"setLrcTime",
                    @"duration":@([self getTotalTime]),
                    @"time":@(position),
                };
                [self sendStremMessageWithDict:dict success:^(BOOL ifSuccess) {
                    if (ifSuccess) {
//                        VLLog(@"发送成功");
                    }else{
//                        VLLog(@"发送失败");
                    }
                }];
            }
    }
}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
receiveStreamMessageFromUid:(NSUInteger)uid
         streamId:(NSInteger)streamId
             data:(NSData * _Nonnull)data {    //接收到对方的RTC消息
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:data];
    if (![dict[@"cmd"] isEqualToString:@"setLrcTime"] && ![dict[@"cmd"] isEqualToString:@"testDelay"]) {
        VLLog(@"receiveStreamMessageFromUid::%ld---message::%@",uid, dict);
    }
    VLLog(@"返回数据:%@,streamID:%d,uid:%d",dict,(int)streamId,(int)uid);
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
        if(self.currentPlayingSongNo == nil) {
            [self.MVView receiveCountDown:leftSecond
                                   onSeat:self.isOnMicSeat
                              currentSong:song];
        }
        VLLog(@"收到倒计时剩余:%d秒",(int)leftSecond);
    }
//    else if([dict[@"cmd"] isEqualToString:@"TrackMode"]) {
//        [self.rtcMediaPlayer selectAudioTrack:[dict[@"value"] intValue]];
//    }
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

#pragma mark - content center
- (void)onLyricResult:(nonnull NSString *)requestId
             lyricUrl:(nonnull NSString *)lyricUrl {
    LyricCallback callback = [self.lyricCallbacks objectForKey:requestId];
    if(!callback) {
        return;
    }
    [self.lyricCallbacks removeObjectForKey:requestId];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        callback(lyricUrl);
    });
}

- (void)onMusicChartsResult:(nonnull NSString *)requestId
                     status:(AgoraMusicContentCenterStatusCode)status
                     result:(nonnull NSArray<AgoraMusicChartInfo *> *)result {
    VLLog(@"Music charts - ");
}

- (void)onMusicCollectionResult:(nonnull NSString *)requestId
                         status:(AgoraMusicContentCenterStatusCode)status
                         result:(nonnull AgoraMusicCollection *)result {
}

- (void)onPreLoadEvent:(NSInteger)songCode
               percent:(NSInteger)percent
                status:(AgoraMusicContentCenterPreloadStatus)status
                   msg:(nonnull NSString *)msg
              lyricUrl:(nonnull NSString *)lyricUrl {
    if (status == AgoraMusicContentCenterPreloadStatusOK) {
        VLLog(@"Agora - onPreLoadEvent, playMusic");
        NSString* sSongCode = [NSString stringWithFormat:@"%ld", songCode];
        LoadMusicCallback block = [self.musicCallbacks objectForKey:sSongCode];
        if(!block) {
            return;
        }
        [self.musicCallbacks removeObjectForKey:sSongCode];
        dispatch_async(dispatch_get_main_queue(), ^{
            block();
        });
    } else if(status == AgoraMusicContentCenterPreloadStatusPreloading) {
        // Do nothing.
    } else {
        dispatch_main_async_safe(^{
            [VLToast toast:KTVLocalizedString(@"加载歌曲失败")];
        });
    }
}

#pragma mark - action utils / business
- (void)muteLocalAudio:(BOOL)mute {
    if (mute) {
        self.isNowMicMuted = YES;
    } else{
        self.isNowMicMuted = NO;
    }
    [self.RTCkit updateChannelWithMediaOptions:[self channelMediaOptions]];
    
    [[AppContext ktvServiceImp] openAudioStatusWithStatus:!mute
                                               completion:^(NSError * error) {
    }];
}

- (void)muteLocalVideo:(BOOL)mute {
    if (!mute) {
        [self.RTCkit enableLocalVideo:YES];
        self.isNowCameraMuted = NO;
    } else {
        [self.RTCkit enableLocalVideo:NO];
        self.isNowCameraMuted = YES;
    }
    [self.RTCkit updateChannelWithMediaOptions:[self channelMediaOptions]];
    [[AppContext ktvServiceImp] openVideoStatusWithStatus:!mute
                                               completion:^(NSError * error) {
    }];
}

- (void)toggleLocalAudio {
    [self muteLocalAudio:!self.isNowMicMuted];
}

- (void)toggleLocalVideo {
    [self muteLocalVideo:!self.isNowCameraMuted];
}

- (void)leaveRoom {
    VL(weakSelf);
    [[AppContext ktvServiceImp] leaveRoomWithCompletion:^(NSError * error) {
        if (error != nil) {
            return;
        }
        
//        [[NSNotificationCenter defaultCenter]postNotificationName:kExitRoomNotification object:nil];
        [weakSelf destroyMediaPlayer];
        for (BaseViewController *vc in weakSelf.navigationController.childViewControllers) {
            if ([vc isKindOfClass:[VLOnLineListVC class]]) {
                [weakSelf.navigationController popToViewController:vc animated:YES];
            }
        }
    }];
}

- (void)loadAndPlaySong {
    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
    if (model == nil
        || [model waittingForChorus]
        || model.status != 2) {
        return;
    }
    
    [self loadAndPlaySongWithModel:model withRole:[model isSongOwner] ? KTVSingRoleMainSinger : KTVSingRoleAudience];
}

- (void)loadAndPlaySongWithModel:(VLRoomSelSongModel*)model withRole:(KTVSingRole)role
{
    if([model.songNo isEqualToString:self.currentPlayingSongNo]) {
        return;
    }
    
    VL(weakSelf);
    if(role == KTVSingRoleMainSinger) {
        [self loadLyric:[model.songNo integerValue] withCallback:^(NSString *lyricUrl) {
            model.lyric = lyricUrl;
            [weakSelf.MVView loadLrcURL:lyricUrl];
            [weakSelf loadMusic:model.songNo withCallback:^{
                [weakSelf openMusicWithSongCode:[model.songNo integerValue]];
                weakSelf.isPlayerPublish = YES;
                [weakSelf.RTCkit updateChannelWithMediaOptions:[self channelMediaOptions]];
                
                [weakSelf.MVView start];
                [weakSelf.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
                //TODO why always NO?
                [self.MVView updateUIWithUserOnSeat:NO
                                               song:model];
                
                //owner to update
                [self markSongDidPlayWithModel:model];
            }];
        }];
    } else if(role == KTVSingRoleAudience) {
        [self loadLyric:[model.songNo integerValue] withCallback:^(NSString *lyricUrl) {
            model.lyric = lyricUrl;
            [weakSelf.MVView loadLrcURL:lyricUrl];
            
            weakSelf.isPlayerPublish = NO;
            [weakSelf.RTCkit updateChannelWithMediaOptions:[self channelMediaOptions]];
            
            [weakSelf.MVView start];
            [weakSelf.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
            //TODO why always NO?
            [self.MVView updateUIWithUserOnSeat:NO
                                           song:model];
        }];
    }
}

// TODO
// Play song locally and update UI
- (void)loadLyric:(NSInteger)songNo withCallback:(void (^ _Nullable)(NSString* lyricUrl))block {
    NSString* requestId = [self.AgoraMcc getLyricWithSongCode:songNo lyricType:0];
    [self.lyricCallbacks setObject:block forKey:requestId];
}

- (void)loadMusic:(NSString *)songCode withCallback:(void (^ _Nullable)(void))block {
    NSInteger songCodeIntValue = [songCode integerValue];
    NSInteger error = [self.AgoraMcc isPreloadedWithSongCode:songCodeIntValue];
    if(error == 0) {
        if(block) {
            [self.musicCallbacks removeObjectForKey:songCode];
            block();
        }
    }
    else {
        error = [self.AgoraMcc preloadWithSongCode:songCodeIntValue jsonOption:nil];
        [self.musicCallbacks setObject:block forKey:songCode];
    }
    VLLog(@"_rtcMediaPlayer--------是否静音:%d",[_rtcMediaPlayer getMute]);
}

- (void)openMusicWithSongCode:(NSInteger )songCode {
    VLLog(@"Agora - MediaPlayer playing %ld", songCode);
    if(self.rtcMediaPlayer != nil) {
        self.currentPlayingSongNo = [NSString stringWithFormat:@"%ld", songCode];
        [self.rtcMediaPlayer openMediaWithSongCode:songCode startPos:0];
    }
}

- (void)stopCurrentSong {
    self.currentTime = 0;
    self.currentPlayingSongNo = nil;
    [self.MVView stop];
    [self.MVView reset];
    [self.MVView cleanMusicText];
    [self.rtcMediaPlayer stop];
    [self resetPlayer];
}

- (void)playNextSong:(int)isMasterInterrupt {
    [self stopCurrentSong];
    [self deleteSongEvent:self.selSongsArray.firstObject
        isMasterInterrupt:isMasterInterrupt];
    VLLog(@"RTC media player stop");
}

- (void)requestOnSeatWithIndex:(NSInteger)index {
    
    KTVOnSeatInputModel* inputModel = [KTVOnSeatInputModel new];
    inputModel.seatIndex = index;
//    VL(weakSelf);
    [[AppContext ktvServiceImp] onSeatWithInput:inputModel
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
    [[AppContext ktvServiceImp] outSeatWithInput:inputModel
                                      completion:^(NSError * error) {
        if (completion) {
            completion(error);
        }
    }];
}

#pragma mark - rtc utils
//发送流消息
- (void)sendStremMessageWithDict:(NSDictionary *)dict
                         success:(sendStreamSuccess)success {
    NSLog(@"sendStremMessageWithDict:::%@",dict);
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

/// 销毁播放器
- (void)destroyMediaPlayer {
    [self.rtcMediaPlayer stop];
    VLLog(@"Agora - RTCMediaPlayer stop");
    [self.RTCkit destroyMediaPlayer:self.rtcMediaPlayer];
    VLLog(@"Agora - Destroy media player");
}

- (void)joinRTCChannel {
    [self.RTCkit leaveChannel:nil];
    [AgoraRtcEngineKit destroy];
    
    self.RTCkit = [AgoraRtcEngineKit sharedEngineWithAppId:[AppContext.shared appId] delegate:self];
    [self.RTCkit setChannelProfile:AgoraChannelProfileLiveBroadcasting];
    /// 开启唱歌评分功能
    int code = [self.RTCkit enableAudioVolumeIndication:3000 smooth:3 reportVad:YES];
    if (code == 0) {
        VLLog(@"评分回调开启成功\n");
    } else {
        VLLog(@"评分回调开启失败：%d\n",code);
    }
    
    [self.RTCkit enableVideo];
    [self.RTCkit enableLocalVideo:NO];
    [self.RTCkit enableAudio];
    
    self.isNowMicMuted = ![self isBroadcaster];
    self.isNowCameraMuted = YES;
    
    AgoraVideoEncoderConfiguration *encoderConfiguration =
    [[AgoraVideoEncoderConfiguration alloc] initWithSize:CGSizeMake(100, 100)
                                               frameRate:AgoraVideoFrameRateFps7
                                                 bitrate:20
                                         orientationMode:AgoraVideoOutputOrientationModeFixedLandscape
                                              mirrorMode:AgoraVideoMirrorModeAuto];
    [self.RTCkit setVideoEncoderConfiguration:encoderConfiguration];
    
    
    VLLog(@"Agora - joining RTC channel with token: %@, for roomNo: %@, with uid: %@", VLUserCenter.user.agoraRTCToken, self.roomModel.roomNo, VLUserCenter.user.id);
    [self.RTCkit joinChannelByToken:VLUserCenter.user.agoraRTCToken
                          channelId:self.roomModel.roomNo
                                uid:[VLUserCenter.user.id integerValue]
                       mediaOptions:[self channelMediaOptions]
                        joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        VLLog(@"Agora - 加入RTC成功");
       
//        [self setUpUI];
//        [AgoraRtm updateDelegate:self];
    }];
    [self.RTCkit setEnableSpeakerphone:YES];
    
    VLLog(@"Agora - Creating MCC with RTM token: %@", VLUserCenter.user.agoraRTMToken);
    AgoraMusicContentCenterConfig *contentCenterConfiguration = [[AgoraMusicContentCenterConfig alloc] init];
    contentCenterConfiguration.rtcEngine = self.RTCkit;
    contentCenterConfiguration.appId = [[AppContext shared] appId];
    contentCenterConfiguration.mccUid = [VLUserCenter.user.id integerValue];
    contentCenterConfiguration.rtmToken = VLUserCenter.user.agoraRTMToken;
    VLLog(@"AgoraMcc: %@, %@\n", contentCenterConfiguration.appId, contentCenterConfiguration.rtmToken);
    self.AgoraMcc = [AgoraMusicContentCenter sharedContentCenterWithConfig:contentCenterConfiguration];
    self.lyricCallbacks = [NSMutableDictionary new];
}

- (void)setSelfAudience {
    [self.RTCkit muteLocalVideoStream:YES];
    [self.RTCkit muteLocalAudioStream:YES];
    [self.RTCkit setClientRole:AgoraClientRoleAudience];
    [self resetPlayer];
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
    [option setPublishMicrophoneTrack:!self.isNowMicMuted];
    [option setPublishCustomAudioTrack:NO];
    [option setAutoSubscribeAudio:YES];
    [option setAutoSubscribeVideo:YES];
    [option setPublishMediaPlayerId:[self.rtcMediaPlayer getMediaPlayerId]];
    [option setPublishMediaPlayerAudioTrack:self.isPlayerPublish];
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
- (void)onVLKTVMoreSelView:(VLPopMoreSelView *)view btnTapped:(id)sender withValue:(VLKTVMoreBtnClickType)typeValue {
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
- (void)onVLKTVBottomView:(VLKTVBottomView *)view btnTapped:(id)sender withValues:(VLKTVBottomBtnClickType)typeValue {
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
            [self toggleLocalAudio];
            break;
        case VLKTVBottomBtnClickTypeVideo:
            [self toggleLocalVideo];
            break;
        default:
            break;
    }
}

#pragma mark - VLRoomPersonViewDelegate
- (void)onVLRoomPersonView:(VLRoomPersonView *)view seatItemTappedWithModel:(VLRoomSeatModel *)model atIndex:(NSInteger)seatIndex {
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
                [self requestOnSeatWithIndex:seatIndex];
            }
        }
    }
}

#pragma mark - VLPopSelBgViewDelegate
-(void)onVLPopSelBgView:(VLPopSelBgView *)view tappedWithAction:(VLKTVSelBgModel *)selBgModel atIndex:(NSInteger)index {
    KTVChangeMVCoverInputModel* inputModel = [KTVChangeMVCoverInputModel new];
//    inputModel.roomNo = self.roomModel.roomNo;
    inputModel.mvIndex = index;
//    inputModel.userNo = VLUserCenter.user.userNo;
    VL(weakSelf);
    [[AppContext ktvServiceImp] changeMVCoverWithInput:inputModel
                                            completion:^(NSError * error) {
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }
        
        [[LSTPopView getPopViewWithCustomView:view] dismiss];
        weakSelf.choosedBgModel = selBgModel;
    }];
}

#pragma mark - VLChooseBelcantoViewDelegate
- (void)onVLChooseBelcantoView:(VLChooseBelcantoView *)view itemTapped:(VLBelcantoModel *)model withIndex:(NSInteger)index {
    self.selBelcantoModel = model;
    [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality
                        scenario:AgoraAudioScenarioGameStreaming];
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
-(NSTimeInterval)getTotalTime{
    NSTimeInterval time = [_rtcMediaPlayer getDuration];
    return time;
}

- (NSTimeInterval)getPlayerCurrentTime
{
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
        [self.rtcMediaPlayer resume];
        //        [self.rtcMediaPlayer play];
        [self.MVView start];
        //发送继续播放的消息
        [self sendPauseOrResumeMessage:0];
    } else if (type == VLKTVMVViewActionTypeMVPause) { //暂停
        [self.rtcMediaPlayer pause];
        [self.MVView stop];
        //发送暂停的消息
        [self sendPauseOrResumeMessage:-1];
    } else if (type == VLKTVMVViewActionTypeMVNext) { //切换
        VL(weakSelf);
        [LEEAlert popSwitchSongDialogWithCancelBlock:nil
                                       withDoneBlock:^{
            if (weakSelf.selSongsArray.count >= 1) {
                if([weakSelf isRoomOwner]
                   && [weakSelf isMainSinger:VLUserCenter.user.userNo] == NO) {
                    [weakSelf playNextSong:1];
                } else {
                    [weakSelf playNextSong:0];
                }
                
                VLLog(@"---Change song---");
            }
        }];
    } else if (type == VLKTVMVViewActionTypeSingOrigin) { // 原唱
        [self.rtcMediaPlayer selectAudioTrack:0];
        [self sendTrackModeMessage:0];
    } else if (type == VLKTVMVViewActionTypeSingAcc) { // 伴奏
        [self.rtcMediaPlayer selectAudioTrack:1];
        [self sendTrackModeMessage:1];
    } else if (type == VLKTVMVViewActionTypeExit) {
        [self playNextSong:0];
    }
}

//合唱的倒计时事件
- (void)onKTVMVView:(VLKTVMVView *)view timerCountDown:(NSInteger)countDownSecond {
    if (!(self.selSongsArray.count > 0)) {
        return;
    }
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
        [self becomeSolo];
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
        [self setIsEarOn: setting.soundOn];
        
        if(self.isNowMicMuted) {
            [self.RTCkit enableInEarMonitoring:NO];
        } else {
            [self.RTCkit enableInEarMonitoring:setting.soundOn];
        }
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
        [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:3 param2:4];
    } else if (effectType == VLKTVSoundEffectTypeXiaoDiao){
        [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:2 param2:4];
    } else if (effectType == VLKTVSoundEffectTypeDaDiao){
        [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:1 param2:4];
    } else if (effectType == VLKTVSoundEffectTypeNone) {
        [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:0 param2:4];
    }
    VLLog(@"Agora - Setting effect type to %lu", effectType);
}

#pragma mark --
//- (void)startSingingIfNeed {
//    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
//    if(!selSongModel.isChorus
//       && ![self.currentPlayingSongNo isEqualToString:selSongModel.songNo] ) {
//        [self startSinging];
//    }
//}
//
//- (void)startSinging {
//    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
//
//    if(self.mediaPlayerConnection) {
//        [self disableMediaChannel];
//        self.mediaPlayerConnection = nil;
//    }
//
//    if([self ifMainSinger:VLUserCenter.user.userNo]) {
//        self.mediaPlayerConnection = [self enableMediaChannel:YES];
//    } else if([self ifChorusSinger:VLUserCenter.user.userNo]) {
//        self.mediaPlayerConnection = [self enableMediaChannel:NO];
//    }
//
//    VLLog(@"Agora - Song is chorus: %d, I am chorus singer: %d",
//          [self isCurrentSongChorus],
//          [self ifChorusSinger:VLUserCenter.user.userNo]);
//
//    if ([self isCurrentSongChorus] && [self ifChorusSinger:VLUserCenter.user.userNo]) {
//        self.mutedRemoteUserId = selSongModel.userId;//[self getMainSingerUserNo];
//        if(self.mutedRemoteUserId != nil) {
//            [self updateRemoteUserMuteStatus:self.mutedRemoteUserId doMute:YES];
//        }
//    } else if ([self ifMainSinger:VLUserCenter.user.userNo]) {
//        self.mutedRemoteUserId = VLUserCenter.user.id;
//        if(self.mutedRemoteUserId != nil) {
//            [self updateRemoteUserMuteStatus:self.mutedRemoteUserId doMute:YES];
//        }
//    }
//
//    self.currentPlayingSongNo = selSongModel.songNo;
//
//    self.mccRequestId =
//    [self.AgoraMcc getLyricWithSongCode:[selSongModel.songNo integerValue] lyricType:0];
//}

- (void)refreshChoosedSongList:(void (^ _Nullable)(void))block{
    VL(weakSelf);
    [[AppContext ktvServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        
        weakSelf.selSongsArray = songArray;
        //TODO should be removed
//        [self loadAndPlaySong];
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
    [self sendStremMessageWithDict:dict
                           success:^(BOOL ifSuccess) {
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

- (void)deleteSongEvent:(VLRoomSelSongModel *)model isMasterInterrupt:(int)isMasterInterrupt {
    if(model == nil
       || self.roomModel == nil
       || self.roomModel.roomNo == nil
       || model.songNo == nil
       || model.sort == nil) {
        return;
    }
    
    KTVRemoveSongInputModel* inputModel = [KTVRemoveSongInputModel new];
    inputModel.songNo = model.songNo;
    inputModel.sort = model.sort;
    inputModel.objectId = model.objectId;
    [[AppContext ktvServiceImp] removeSongWithInput:inputModel
                                         completion:^(NSError * error) {
    }];
}

//发送独唱的消息
- (void)becomeSolo {
    [[AppContext ktvServiceImp] becomeSolo];
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
- (BOOL)isMainSinger:(NSString *)userNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    if (selSongModel != nil && [selSongModel.userNo isEqualToString:userNo]) {
        return YES;
    }
    
    return NO;
}

- (BOOL)ifChorusSinger:(NSString *)userNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    VLLog(@"Agora - Song chorusNo: %@, userNo: %@", selSongModel.chorusNo, userNo);
    if(selSongModel != nil && selSongModel.isChorus && [selSongModel.chorusNo isEqualToString:userNo]) {
        return YES;
    }
    
    return NO;
}

- (NSString *)getMainSingerUserNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    if(selSongModel != nil) {
        return selSongModel.userNo;
    }
    
    return nil;
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
- (void)setSelfChorusUserNo:(NSString *)userNo
{
    VLRoomSelSongModel *song = self.selSongsArray.firstObject;
    if(song != nil) {
        if(userNo == nil) {
            song.isChorus = NO;
        }
        song.chorusNo = userNo;
    }
}

- (void)setUserJoinChorus:(NSString *)userNo
{
    [self setSelfChorusUserNo:userNo];
    
    for (VLRoomSeatModel *seat in self.seatsArray) {
        if ([seat.userNo isEqualToString:userNo]) {
            seat.isJoinedChorus = YES;
            break;
        }
    }
    [self.roomPersonView setSeatsArray:self.seatsArray];
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

- (void)setAgoraMcc:(AgoraMusicContentCenter *)AgoraMcc {
    [_AgoraMcc registerEventDelegate:nil];
    [[AppContext shared] unregisterEventDelegate:self];
//    [AgoraMusicContentCenter destroy];
    _AgoraMcc = AgoraMcc;
    if (_AgoraMcc != nil) {
        [[AppContext shared] registerEventDelegate:self];
        [_AgoraMcc registerEventDelegate:[AppContext shared]];
    }
    [[AppContext shared] setAgoraMcc:AgoraMcc];
}

- (void)setSeatsArray:(NSArray<VLRoomSeatModel *> *)seatsArray
{
    _seatsArray = seatsArray;
    //update booleans
    self.isOnMicSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
}

- (void)setIsOnMicSeat:(BOOL)isOnMicSeat {
    _isOnMicSeat = isOnMicSeat;
    
    self.bottomView.hidden = !_isOnMicSeat;
    self.requestOnLineView.hidden = !self.bottomView.hidden;
}

- (void)setIsNowMicMuted:(BOOL)isNowMicMuted
{
    _isNowMicMuted = isNowMicMuted;
    [self.bottomView updateAudioBtn:isNowMicMuted];
}

- (void)setIsNowCameraMuted:(BOOL)isNowCameraMuted
{
    _isNowCameraMuted = isNowCameraMuted;
    [self.bottomView updateVideoBtn:isNowCameraMuted];
}

- (void)setIsEarOn:(BOOL)isEarOn
{
    [self.settingView setIsEarOn:isEarOn];
}

- (void)setSelSongsArray:(NSArray<VLRoomSelSongModel *> *)selSongsArray
{
    _selSongsArray = selSongsArray;
    
    if (self.chooseSongView) {
        self.chooseSongView.selSongsArray = selSongsArray; //刷新已点歌曲UI
    }
    //刷新MV里的视图
    [self.MVView updateUIWithSong:self.selSongsArray.firstObject
                               onSeat:self.isOnMicSeat];
    
    [self.roomPersonView updateSingBtnWithChoosedSongArray:self.selSongsArray];
}

#pragma mark - lazy getter
- (id<AgoraRtcMediaPlayerProtocol>)rtcMediaPlayer {
    if (!_rtcMediaPlayer) {
        _rtcMediaPlayer = [self.AgoraMcc createMusicPlayerWithDelegate:self];
        // 调节本地播放音量。0-100
         [_rtcMediaPlayer adjustPlayoutVolume:200];
//         调节远端用户听到的音量。0-400
         [_rtcMediaPlayer adjustPublishSignalVolume:200];
    }
    return _rtcMediaPlayer;
}

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
