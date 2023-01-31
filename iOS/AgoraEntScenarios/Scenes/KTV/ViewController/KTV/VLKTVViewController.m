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
#import "KTVApi.h"
#import "HWWeakTimer.h"
#import "VLAlert.h"
#import "VLKTVAlert.h"

@import AgoraRtcKit;
@import AgoraLyricsScore;
@import YYCategories;
@import SDWebImage;

typedef enum : NSUInteger {
    KTVCoSingerWaitStopTimeOut,
    KTVCoSingerWaitStopCancel,
} KTVCoSingerWaitStopReason;

NSInteger ktvApiStreamId = -1;
NSInteger ktvStreamId = -1;

typedef void (^LyricCallback)(NSString* lyricUrl);
typedef void (^LoadMusicCallback)(AgoraMusicContentCenterPreloadStatus);
typedef void (^ChorusCallback)(KTVCoSingerWaitStopReason);

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
VLsoundEffectViewDelegate,
VLKTVSettingViewDelegate,
VLBadNetWorkViewDelegate,
AgoraRtcMediaPlayerDelegate,
AgoraRtcEngineDelegate,
VLPopScoreViewDelegate,
KTVApiDelegate
>

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
@property (nonatomic, strong) VLSoundEffectView *soundEffectView; // 音效视图

@property (nonatomic, strong) id<AgoraMusicPlayerProtocol> rtcMediaPlayer;
@property (nonatomic, strong) AgoraMusicContentCenter *AgoraMcc;
@property (nonatomic, strong) VLSongItmModel *choosedSongModel; //点的歌曲
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;
@property (nonatomic, strong) NSTimer *chorusMatchingTimer;
@property (nonatomic, strong) ChorusCallback chorusMatchingCallback;

@property (nonatomic, strong) VLPopScoreView *scoreView;
@property (nonatomic, strong) NSMutableDictionary<NSString*, LyricCallback>* lyricCallbacks;

@property (nonatomic, assign) BOOL isNowMicMuted;
@property (nonatomic, assign) BOOL isNowCameraMuted;
@property (nonatomic, assign) BOOL isOnMicSeat;
@property (nonatomic, assign) BOOL isEarOn;
@property (nonatomic, assign) KTVPlayerTrackMode trackMode;

@property (nonatomic, strong) NSArray <VLRoomSelSongModel*>* selSongsArray;
@property (nonatomic, strong) KTVApi* ktvApi;

@property (nonatomic, assign) NSUInteger retryCount;
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
    self.MVView = [[VLKTVMVView alloc]initWithFrame:CGRectMake(0, mvViewTop, SCREEN_WIDTH, musicHeight * 0.5) withDelegate:self];
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
    
    self.retryCount = 0;
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
    [self.AgoraMcc registerEventDelegate:nil];
    [[AppContext shared] setAgoraMcc:nil];
    [AgoraMusicContentCenter destroy];
    
    [self.rtcMediaPlayer stop];
    [[AppContext shared] setAgoraRtcMediaPlayer:nil];
    [self.RTCkit destroyMediaPlayer:self.rtcMediaPlayer];
    
    [AgoraRtcEngineKit destroy];
    KTVLogInfo(@"Agora - destroy RTCEngine");
    
    self.ktvApi = nil;
    
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
    [[AppContext ktvServiceImp] unsubscribeAll];
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
            [model resetWithInfo:seatModel];
            [weakSelf setSeatsArray:weakSelf.seatsArray];
        } else if (status == KTVSubscribeDeleted) {
            // 下麦消息
            
            // 被下麦用户刷新UI
            if ([model.userNo isEqualToString:VLUserCenter.user.id]) {
                //当前的座位用户离开RTC通道
                VLRoomSelSongModel *song = weakSelf.selSongsArray.firstObject;
                [weakSelf.MVView updateUIWithSong:song onSeat:NO];;
//                [weakSelf resetChorusStatus:model.userNo];
            }
            
            // 下麦重置占位模型
            [model resetWithInfo:nil];
            [weakSelf setSeatsArray:weakSelf.seatsArray];
        }
        [weakSelf.roomPersonView reloadSeatIndex:model.seatIndex];
        
        //update my seat status
//        weakSelf.isOnMicSeat = [weakSelf getCurrentUserSeatInfo] ? YES : NO;
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
    
    //callback if choose song list didchanged
    [[AppContext ktvServiceImp] subscribeChooseSongChangedWithBlock:^(KTVSubscribe status, VLRoomSelSongModel * songInfo) {
        // update in-ear monitoring
        [weakSelf _checkInEarMonitoring];
        
        if (KTVSubscribeDeleted == status) {
            [weakSelf removeSelSongWithSongNo:[songInfo.songNo integerValue] sync:NO];
        } else {
            VLRoomSelSongModel* song = [weakSelf selSongWithSongNo:songInfo.songNo];
            //add new song
            KTVLogInfo(@"song did updated: %@ ischorus: %d, status: %ld", song.name, songInfo.isChorus, songInfo.status);
            if (song == nil) {
                NSMutableArray* selSongsArray = [NSMutableArray arrayWithArray:weakSelf.selSongsArray];
                [selSongsArray appendObject:songInfo];
                weakSelf.selSongsArray = selSongsArray;
                return;
            }
            
            [weakSelf replaceSelSongWithInfo:songInfo];
        }
    }];
    
    [[AppContext ktvServiceImp] subscribeNetworkStatusChangedWithBlock:^(KTVServiceNetworkStatus status) {
        if (status != KTVServiceNetworkStatusOpen) {
            [VLToast toast:[NSString stringWithFormat:@"network changed: %ld", status]];
            return;
        }
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
                                          soundView:self.soundEffectView
                                       withDelegate:self];
    self.soundEffectView = (VLSoundEffectView*)popView.currCustomView;
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
        [[VLAlert shared] dismiss];
    }];
}

- (void)showSettingView {
    LSTPopView* popView = [LSTPopView popSettingViewWithParentView:self.view
                                                       settingView:self.settingView
                                                      withDelegate:self];
    
    self.settingView = (VLKTVSettingView*)popView.currCustomView;
}

- (void)showScoreViewWithScore:(int)score {
                        //  song:(VLRoomSelSongModel *)song {
    if (score < 0) return;
    if(_scoreView == nil) {
        _scoreView = [[VLPopScoreView alloc] initWithFrame:self.view.bounds withDelegate:self];
        [self.view addSubview:_scoreView];
    }
    KTVLogInfo(@"Avg score for the song: %d", score);
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
    [self.ktvApi mainRtcEngine:engine didJoinedOfUid:uid elapsed:elapsed];
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine
reportAudioVolumeIndicationOfSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers
      totalVolume:(NSInteger)totalVolume {
    [self.ktvApi mainRtcEngine:engine reportAudioVolumeIndicationOfSpeakers:speakers totalVolume:totalVolume];
}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
receiveStreamMessageFromUid:(NSUInteger)uid
         streamId:(NSInteger)streamId
             data:(NSData * _Nonnull)data {    //接收到对方的RTC消息
    
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:data];
    KTVLogInfo(@"receiveStreamMessageFromUid:%@,streamID:%d,uid:%d",dict,(int)streamId,(int)uid);
    if([dict[@"cmd"] isEqualToString:@"countdown"]) {  //倒计时
        int leftSecond = [dict[@"time"] intValue];
        [self.MVView setCoundDown:leftSecond];
        KTVLogInfo(@"count down: %ds",(int)leftSecond);
        
        return;
    } else if ([dict[@"cmd"] isEqualToString:@"SingingScore"]) {
        int score = [dict[@"score"] intValue];
        [self showScoreViewWithScore:score];
        KTVLogInfo(@"score: %ds",score);
        return;
    }
    
    [self.ktvApi mainRtcEngine:engine receiveStreamMessageFromUid:uid streamId:streamId data:data];
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
        [self.AgoraMcc renewToken:token];
    }];
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine contentInspectResult:(AgoraContentInspectResult)result {
    KTVLogInfo(@"contentInspectResult: %ld", result);
}

#pragma mark - action utils / business
- (void)loadAndPlaySong {
    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
    
    if(!model){
        [self.MVView updateUIWithSong:model onSeat:self.isOnMicSeat];
        return;
    }
    //TODO: fix score view visible problem while owner reopen the room
    [self.MVView updateUIWithSong:model onSeat:self.isOnMicSeat];
    [self markSongPlaying:model];
    
    KTVSingRole role = [model isSongOwner] ? KTVSingRoleMainSinger :
        [[model chorusNo] isEqualToString:VLUserCenter.user.id] ? KTVSingRoleCoSinger : KTVSingRoleAudience;
    KTVSongType type = [model isChorus] ? KTVSongTypeChorus : KTVSongTypeSolo;
    KTVSongConfiguration* config = [KTVSongConfiguration configWithSongCode:[[model songNo] integerValue]];
    
    config.type = type;
    config.role = role;
    config.mainSingerUid = [[model userNo] integerValue];
    config.coSingerUid = [[model chorusNo] integerValue];
    KTVLogInfo(@"loadSong name: %@, songNo: %@, type: %ld, role: %ld", model.songName, model.songNo, type, role);
    self.retryCount = 0;
    [self loadSongWithModel:model config:config];
    
}

- (void)loadSongWithModel:(VLRoomSelSongModel *)model config:(nonnull KTVSongConfiguration *)config {
    VL(weakSelf);
    [self.ktvApi loadSong:[[model songNo] integerValue] withConfig:config withCallback:^(NSInteger songCode, NSString * _Nonnull lyricUrl, KTVSingRole role, KTVLoadSongState state) {
        if(state == KTVLoadSongStateOK) {
            [weakSelf.MVView updateUIWithSong:model onSeat:weakSelf.isOnMicSeat];
            [weakSelf.ktvApi playSong:[[model songNo] integerValue]];
            [weakSelf.MVView showSkipView:true];
        } else if(state == KTVLoadSongStateNoLyricUrl) {
            [weakSelf.MVView showSkipView:true];
            //如果歌词加载失败进行三次重试
            if(weakSelf.retryCount < 2) {
                KTVLogInfo(@"songName: %@, songNo: %@, retryCount: %lu",model.songName, model.songNo,(unsigned long)weakSelf.retryCount);
                [weakSelf loadSongWithModel:model config:config];
                weakSelf.retryCount++;
            }
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

- (void)startChorusMatching
{
    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
    [self.MVView updateUIWithSong:model onSeat:self.isOnMicSeat];
    if(model.isChorus && model.status == VLSongPlayStatusIdle && model.chorusNo.length == 0) {
        // for new chorus song, need to wait till co-singer joins or force solo
        if([model isSongOwner]){
            //only song owner setup the timer, audience do nothing
            [self startCoSingerWaitForSeconds:20 withCallback:^(KTVCoSingerWaitStopReason reason) {
                KTVLogInfo(@"startCoSingerWaitForSeconds did stop: %ld", reason);
                if (reason == KTVCoSingerWaitStopCancel) {
                    return;
                }
                [[AppContext ktvServiceImp] enterSoloMode];
            }];
        }
        return;
    }
}

- (void)startCoSingerWaitForSeconds:(NSInteger)seconds withCallback:(void (^ _Nullable)(KTVCoSingerWaitStopReason))block
{
    if(self.chorusMatchingTimer) {
        //already waiting, ignore
        KTVLogInfo(@"already waiting for chorus, ignore call");
        return;
    }
    self.chorusMatchingCallback = block;
    __block NSInteger leftSecond = seconds;
    VL(weakSelf);
    self.chorusMatchingTimer = [HWWeakTimer scheduledTimerWithTimeInterval:1.0f block:^(id userInfo) {
        leftSecond -= 1;
        if (leftSecond == 0) {
            [weakSelf.MVView setChorusOptViewHidden];
            [weakSelf stopCoSingerWaitWithReason:KTVCoSingerWaitStopTimeOut];
        } else {
            [weakSelf.MVView setCoundDown:leftSecond];
            [weakSelf syncChorusMatchCountDown:leftSecond];
        }
    } userInfo:@"Fire" repeats:YES];
    [self.chorusMatchingTimer fire];
}

- (void)stopCoSingerWaitWithReason:(KTVCoSingerWaitStopReason)reason
{
    [self.chorusMatchingTimer invalidate];
    if (self.chorusMatchingCallback) {
        self.chorusMatchingCallback(reason);
    }
    self.chorusMatchingTimer = nil;
    self.chorusMatchingCallback = nil;
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
- (void)syncChorusMatchCountDown:(NSInteger)seconds {
    NSDictionary *dict = @{
        @"cmd":@"countdown",
        @"time":@(seconds)
    };
    [self sendStreamMessageWithDict:dict success:nil];
}

- (void)syncChoruScore:(NSInteger)score {
    NSDictionary *dict = @{
        @"cmd":@"SingingScore",
        @"score":@(score)
    };
    [self sendStreamMessageWithDict:dict success:nil];
}

- (void)joinChorus {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    
    KTVJoinChorusInputModel* inputModel = [KTVJoinChorusInputModel new];
    inputModel.isChorus = YES;
    inputModel.songNo = selSongModel.songNo;
    [[AppContext ktvServiceImp] joinChorusWithInput:inputModel
                                         completion:^(NSError * error) {
    }];
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
    BOOL isTopSong = [self.selSongsArray.firstObject.songNo integerValue] == songNo;
    
    if (isTopSong) {
        [self.ktvApi stopSong];
    }
    
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
    self.trackMode = KTVPlayerTrackAcc;
    
    AgoraVideoEncoderConfiguration *encoderConfiguration =
    [[AgoraVideoEncoderConfiguration alloc] initWithSize:CGSizeMake(100, 100)
                                               frameRate:AgoraVideoFrameRateFps7
                                                 bitrate:20
                                         orientationMode:AgoraVideoOutputOrientationModeFixedLandscape
                                              mirrorMode:AgoraVideoMirrorModeAuto];
    [self.RTCkit setVideoEncoderConfiguration:encoderConfiguration];
    
    
    [self.RTCkit setEnableSpeakerphone:YES];
    
    AgoraDataStreamConfig *config = [AgoraDataStreamConfig new];
    config.ordered = false;
    config.syncWithAudio = false;
    [self.RTCkit createDataStream:&ktvStreamId
                           config:config];
    [self.RTCkit createDataStream:&ktvApiStreamId
                           config:config];
    
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
    
    self.ktvApi = [[KTVApi alloc] initWithRtcEngine:self.RTCkit channel:self.roomModel.roomNo musicCenter:self.AgoraMcc player:self.rtcMediaPlayer dataStreamId:ktvApiStreamId delegate:self];
    self.ktvApi.lrcView = self.MVView.lrcView;
    
    KTVLogInfo(@"Agora - joining RTC channel with token: %@, for roomNo: %@, with uid: %@", VLUserCenter.user.agoraRTCToken, self.roomModel.roomNo, VLUserCenter.user.id);
    int ret =
    [self.RTCkit joinChannelByToken:VLUserCenter.user.agoraRTCToken
                          channelId:self.roomModel.roomNo
                                uid:[VLUserCenter.user.id integerValue]
                       mediaOptions:[self channelMediaOptions]
                        joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        KTVLogInfo(@"Agora - 加入RTC成功");
    }];
    if (ret != 0) {
        KTVLogError(@"joinChannelByToken fail: %d, uid: %ld, token: %@", ret, [VLUserCenter.user.id integerValue], VLUserCenter.user.agoraRTCToken);
    }
}

- (void)leaveRTCChannel {
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
    [option setPublishMediaPlayerId:[self.rtcMediaPlayer getMediaPlayerId]];
    [option setEnableAudioRecordingOrPlayout:YES];
    return option;
}

- (void)sendStreamMessageWithDict:(NSDictionary *)dict
                         success:(_Nullable sendStreamSuccess)success {
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

#pragma mark - KTVSoloController
- (void)controller:(KTVApi *)controller song:(NSInteger)songCode didChangedToState:(AgoraMediaPlayerState)state local:(BOOL)local
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if(state == AgoraMediaPlayerStatePlaying) {
            if(local) {
                //track has to be selected after loaded
                [self.ktvApi selectTrackMode:self.trackMode];
            }
            [self.MVView start];
            [self.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
        } else if(state == AgoraMediaPlayerStatePaused) {
            [self.MVView stop];
            [self.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPause];
        } else if(state == AgoraMediaPlayerStateStopped) {
            [self.MVView reset];
        } else if(state == AgoraMediaPlayerStatePlayBackAllLoopsCompleted) {
            if(local) {
                KTVLogInfo(@"Playback all loop completed");
                VLRoomSelSongModel *songModel = self.selSongsArray.firstObject;
                if([self isCurrentSongMainSinger:VLUserCenter.user.id]) {
                    //将房主实时的分数共享给所有人
                    [self syncChoruScore:[self.MVView getAvgSongScore]];
                    [self showScoreViewWithScore: [self.MVView getAvgSongScore]];
                }
                [self removeCurrentSongWithSync:YES];
            }
        }
    });
}

-(void)controller:(KTVApi *)controller song:(NSInteger)songCode config:(nonnull KTVSongConfiguration *)config didChangedToPosition:(NSInteger)position local:(BOOL)local
{
    
}

#pragma mark -- VLKTVTopViewDelegate
- (void)onVLKTVTopView:(VLKTVTopView *)view closeBtnTapped:(id)sender {
    VL(weakSelf);
    NSString *title = VLUserCenter.user.ifMaster ? KTVLocalizedString(@"解散房间") : KTVLocalizedString(@"退出房间");
    NSString *message = VLUserCenter.user.ifMaster ? KTVLocalizedString(@"确定解散该房间吗？") : KTVLocalizedString(@"确定退出该房间吗？");
    NSArray *array = [[NSArray alloc]initWithObjects:KTVLocalizedString(@"取消"),KTVLocalizedString(@"确定"), nil];
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:title message:message placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == true){
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
- (void)onVLKTVBottomView:(VLKTVBottomToolbar *)view
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
                [self enterSeatWithIndex:seatIndex];
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
        [self.ktvApi resumePlay];
        //failed to load lyric/music, try again
//        [self loadAndPlaySong];
    } else if (type == VLKTVMVViewActionTypeMVPause) { //暂停
        [self.ktvApi pausePlay];
    } else if (type == VLKTVMVViewActionTypeMVNext) { //切换
        VL(weakSelf);

        NSString *title = KTVLocalizedString(@"切换歌曲");
        NSString *message = KTVLocalizedString(@"切换下一首歌歌曲？");
        NSArray *array = [[NSArray alloc]initWithObjects:KTVLocalizedString(@"取消"),KTVLocalizedString(@"确定"), nil];
        [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:title message:message placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
            if(flag == true){
                if (weakSelf.selSongsArray.count >= 1) {
                    [weakSelf.ktvApi stopSong];
                    [weakSelf removeCurrentSongWithSync:YES];
                }
            }
            [[VLAlert shared] dismiss];
        }];
    } else if (type == VLKTVMVViewActionTypeSingOrigin) { // 原唱
        self.trackMode = KTVPlayerTrackOrigin;
    } else if (type == VLKTVMVViewActionTypeSingAcc) { // 伴奏
        self.trackMode = KTVPlayerTrackAcc;
    }
}

- (void)onKTVMVView:(VLKTVMVView *)view chorusSingAction:(VLKTVMVViewSingActionType)singType {
    if (singType == VLKTVMVViewSingActionTypeSolo) { // 独唱
        //发送独唱的消息
        [self.MVView setChorusOptViewHidden];
        [self stopCoSingerWaitWithReason:KTVCoSingerWaitStopTimeOut];
    } else if (singType == VLKTVMVViewSingActionTypeJoinChorus) { // 加入合唱
        if(!self.isOnMicSeat) {
            [VLToast toast:KTVLocalizedString(@"请先上坐")];
        } else {
            [self.MVView setChorusOptViewHidden];
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
    KTVLogInfo(@"Agora - Setting effect type to %lu", effectType);
}

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

- (VLRoomSeatModel*)getCurrentUserSeatInfo {
    for (VLRoomSeatModel *model in self.seatsArray) {
        if ([model.userNo isEqualToString:VLUserCenter.user.id]) {
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
    BOOL onMicSeatStatusDidChanged = _isOnMicSeat != isOnMicSeat;
    _isOnMicSeat = isOnMicSeat;
    
    if (onMicSeatStatusDidChanged) {
        VLRoomSelSongModel* song = [self.selSongsArray firstObject];
        if (!isOnMicSeat) {
            if (song.isChorus && song.chorusNo == VLUserCenter.user.id) {
                //co singer leave chorus
                [[AppContext ktvServiceImp] coSingerLeaveChorusWithCompletion:^(NSError * err) {
                }];
                [self.ktvApi stopSong];
                [self loadAndPlaySong];
            } else if (song.isSongOwner) {
                [self.ktvApi stopSong];
                [self removeCurrentSongWithSync:YES];
            }
        }
        
        //start mic once enter seat
        AgoraRtcChannelMediaOptions *option = [AgoraRtcChannelMediaOptions new];
        [option setClientRoleType:[self isBroadcaster] ? AgoraClientRoleBroadcaster : AgoraClientRoleAudience];
        // use audio volume to control mic on/off, so that mic is always on when broadcaster
        [option setPublishMicrophoneTrack:[self isBroadcaster]];
        [self.RTCkit updateChannelWithMediaOptions:option];
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

- (void)_checkInEarMonitoring {
    if([self isCurrentSongMainSinger:VLUserCenter.user.id]) {
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
    
    VLRoomSelSongModel* originalTopSong = [oldSongsArray firstObject];
    VLRoomSelSongModel* updatedTopSong = [selSongsArray firstObject];
    KTVLogInfo(@"setSelSongsArray: name: %@ isChorus: %d, status: %ld", updatedTopSong.name, updatedTopSong.isChorus, updatedTopSong.status);
    if(!updatedTopSong.isChorus) {
        //solo
        if(![updatedTopSong isEqual:originalTopSong]){
            //song changes
            [self.ktvApi stopSong];
            [self loadAndPlaySong];
        }
    } else {
        //chorus
        if(![updatedTopSong.songNo isEqualToString:originalTopSong.songNo]){
            if([updatedTopSong waittingForChorusMatch]) {
                [self startChorusMatching];
            }
        }
        if([updatedTopSong doneChorusMatch]) {
            [self stopCoSingerWaitWithReason:KTVCoSingerWaitStopCancel];
            [self loadAndPlaySong];
        }
    }
}

- (void)setTrackMode:(KTVPlayerTrackMode)trackMode
{
    _trackMode = trackMode;
    [self.ktvApi selectTrackMode:trackMode];
    
    [self.MVView setOriginBtnState: trackMode == KTVPlayerTrackOrigin ? VLKTVMVViewActionTypeSingOrigin : VLKTVMVViewActionTypeSingAcc];
}

@end
