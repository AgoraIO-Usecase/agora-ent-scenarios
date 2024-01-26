//
//  VLSRViewController.m
//  VoiceOnLine
//

#import "VLSRViewController.h"
#import "VLSRTopView.h"
#import "VLSRMicSeatList.h"
#import "VLSRBottomToolbar.h"
#import "VLSRAudienceIndicator.h"
#import "VLSRMVIdleView.h"
#import "VLSROnLineListVC.h"
#import "AgoraEntScenarios-Swift.h"
#import "VLSRSettingView.h"
#import "VLSRSongItmModel.h"
#import "VLSRSelBgModel.h"
#import "UIViewController+VL.h"
#import "VLSRPopScoreView.h"
#import "VLGlobalHelper.h"
#import "VLURLPathConfig.h"
#import "UIView+VL.h"
#import "AppContext+SR.h"
#import "LSTPopView+SRModal.h"
//#import "HWWeakTimer.h"
#import "SRDebugManager.h"
#import "SRDebugInfo.h"
@import AgoraRtcKit;
@import AgoraLyricsScore;
@import YYCategories;
@import SDWebImage;
@import AgoraCommon;

NSInteger srApiStreamId = -1;
NSInteger srStreamId = -1;

@interface VLSRViewController ()<
VLSRTopViewDelegate,
VLSRMicSeatListDelegate,
VLSRBottomToolbarDelegate,
VLSRPopSelBgViewDelegate,
VLSRPopMoreSelViewDelegate,
VLSRDropOnLineViewDelegate,
VLSRAudienceIndicatorDelegate,
VLSRAudioEffectPickerDelegate,
VLSRPopSongListDelegate,
VLSRSettingViewDelegate,
VLSRBadNetWorkViewDelegate,
AgoraRtcMediaPlayerDelegate,
AgoraRtcEngineDelegate,
VLSRPopScoreViewDelegate,
KTVApiEventHandlerDelegate,
VLSRStatusViewDelegate,
VLSRLrcViewDelegate,
IMusicLoadStateListener,
VLSRDebugViewDelegate
>

typedef void (^CompletionBlock)(BOOL isSuccess, NSInteger songCode);
@property (nonatomic, assign) BOOL isEnterSeatNotFirst;
@property (nonatomic, strong) VLSRSelBgModel *choosedBgModel;
@property (nonatomic, strong) VLSRBottomToolbar *bottomView;
@property (nonatomic, strong) VLSRBelcantoModel *selBelcantoModel;
@property (nonatomic, strong) VLSRMVIdleView *noBodyOnLineView; // mv空页面
@property (nonatomic, strong) VLSRTopView *topView;
@property (nonatomic, strong) VLSRSettingView *settingView;
@property (nonatomic, strong) VLSRMicSeatList *roomPersonView; //房间麦位视图
@property (nonatomic, strong) VLSRAudienceIndicator *requestOnLineView;//空位上麦
@property (nonatomic, strong) VLSRPopSongList *chooseSongView; //点歌视图

@property (nonatomic, strong) VLSRSongItmModel *choosedSongModel; //点的歌曲
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;

@property (nonatomic, strong) VLSRPopScoreView *scoreView;

@property (nonatomic, assign) BOOL isNowMicMuted;
@property (nonatomic, assign) BOOL isNowCameraMuted;
@property (nonatomic, assign) BOOL isOnMicSeat;
@property (nonatomic, assign) NSUInteger chorusNum;    //合唱人数
@property (nonatomic, assign) KTVSingRole singRole;    //角色
@property (nonatomic, assign) BOOL isEarOn;
@property (nonatomic, assign) int playoutVolume;
@property (nonatomic, assign) KTVPlayerTrackMode trackMode;  //合唱/伴奏

@property (nonatomic, strong) NSArray <VLSRRoomSelSongModel*>* selSongsArray;
@property (nonatomic, strong) KTVApiImpl* SRApi;

@property (nonatomic, strong) LyricModel *lyricModel;
@property (nonatomic, strong) SRLrcControl *lrcControl;
@property (nonatomic, copy, nullable) CompletionBlock loadMusicCallBack;
@property (nonatomic, assign) int soundVolume;
@property (nonatomic, assign) NSInteger selectedEffectIndex;
@property (nonatomic, assign) BOOL isPause;
@property (nonatomic, assign) NSInteger retryCount;
@property (nonatomic, assign) BOOL isJoinChorus;
@property (nonatomic, assign) NSInteger coSingerDegree;
@property (nonatomic, strong) VLSRStatusView *statusView;
@property (nonatomic, strong) SingRelayModel *gameModel;
@property (nonatomic, strong) NSMutableArray *scoreArray;//保存分数信息
//@property (nonatomic, assign) BOOL hasReady;
@property (nonatomic, assign) BOOL indexReturned;
@property (nonatomic, strong) NSString *nextWinNo; //下一个抢唱的用户
@property (nonatomic, strong) NSString *currentUserNo; //当前抢唱的用户
@property (nonatomic, strong) NSMutableArray<NSNumber *> *chooseArray;
@property (nonatomic, assign) NSInteger currentIndex;
@property (nonatomic, assign) NSInteger segmentScore;
@property (nonatomic, assign) NSInteger segmentCount;
@property (nonatomic, assign) NSInteger sumScore;
@property (nonatomic, assign) NSInteger cosingerLoadCount; //合唱歌曲加载成功的个数
@property (nonatomic, assign) BOOL MainSingerPlayFlag; //主唱歌曲加载成功
@property (nonatomic, assign) BOOL hasCountDown; //已经倒计时过
@property (nonatomic, assign) BOOL isDumpMode;
@end

@implementation VLSRViewController

#pragma mark view lifecycles
- (void)dealloc {
    NSLog(@"dealloc:%s",__FUNCTION__);
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = UIColor.blackColor;
    [[NSUserDefaults standardUserDefaults] setObject:nil forKey:@"MICOWNERINDEX"];
    [self subscribeServiceEvent];
    
    // setup view
    [self setBackgroundImage:@"sr_main_back" bundleName:@"SRResource"];
    //头部视图
    VLSRTopView *topView = [[VLSRTopView alloc]initWithFrame:CGRectMake(0, kStatusBarHeight, SCREEN_WIDTH, 60) withDelegate:self];
    [self.view addSubview:topView];
    self.topView = topView;
    topView.listModel = self.roomModel;
    
    //底部按钮视图
    VLSRBottomToolbar *bottomView = [[VLSRBottomToolbar alloc] initWithFrame:CGRectMake(0, SCREEN_HEIGHT-64-kSafeAreaBottomHeight, SCREEN_WIDTH, 64) withDelegate:self withRoomNo:self.roomModel.roomNo withData:self.seatsArray];
    bottomView.backgroundColor = [UIColor clearColor];
    self.bottomView = bottomView;
    [self.view addSubview:bottomView];
    
    //去掉首尾的高度
    CGFloat musicHeight = SCREEN_HEIGHT -64 - kSafeAreaBottomHeight - kStatusBarHeight - 60 - 20;
    
    //MV视图(显示歌词...)
    CGFloat mvViewTop = topView.bottom;
    
    self.statusView = [[VLSRStatusView alloc]initWithFrame:CGRectMake(0, mvViewTop, SCREEN_WIDTH, musicHeight * 0.5)];
    self.statusView.state = [self isRoomOwner] ? SRStateOwnerOrderMusic : SRStateAudienceWating;
    self.statusView.delegate = self;
    self.statusView.lrcView.delegate = self;
    [self.view addSubview:self.statusView];
    
    //房间麦位视图
    VLSRMicSeatList *personView = [[VLSRMicSeatList alloc] initWithFrame:CGRectMake(0, self.statusView.bottom + 20, SCREEN_WIDTH, musicHeight * 0.5) withDelegate:self withRTCkit:self.RTCkit];
    self.roomPersonView = personView;
    self.roomPersonView.roomSeatsArray = self.seatsArray;
    [self.view addSubview:personView];
    
    //空位上麦视图
    VLSRAudienceIndicator *requestOnLineView = [[VLSRAudienceIndicator alloc]initWithFrame:CGRectMake(0, SCREEN_HEIGHT-kSafeAreaBottomHeight-56-VLREALVALUE_WIDTH(30), SCREEN_WIDTH, 56) withDelegate:self];
    self.requestOnLineView = requestOnLineView;
    [self.view addSubview:requestOnLineView];
    
    //start join
    [self joinRTCChannel];
    
    if(AppContext.shared.isDebugMode){
        //如果开启了debug模式
        UIButton *debugBtn = [[UIButton alloc]initWithFrame:CGRectMake(SCREEN_WIDTH - 100, SCREEN_HEIGHT - 200, 80, 80)];
        [debugBtn setBackgroundColor:[UIColor blueColor]];
        debugBtn.layer.cornerRadius = 40;
        debugBtn.layer.masksToBounds = true;
        [debugBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [debugBtn setTitle:@"Debug" forState:UIControlStateNormal];
        [debugBtn addTarget:self action:@selector(showDebug) forControlEvents:UIControlEventTouchUpInside];
        [self.view addSubview:debugBtn];
    }
    
    self.isOnMicSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
    
    //处理背景
    [self prepareBgImage];
    [[UIApplication sharedApplication] setIdleTimerDisabled: YES];
    
    //add debug
    [self.topView addGestureRecognizer:[SRDebugManager createStartGesture]];
    
    self.chooseArray = [NSMutableArray arrayWithObjects:@(NO), @(NO), @(NO), @(NO), @(NO), nil];
    self.currentIndex = 1;
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
    SRLogInfo(@"Agora - destroy RTCEngine");
    [AgoraRtcEngineKit destroy];
}

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return YES;
}
- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}
// 是否允许手动滑回 @return true 是、 false否
- (BOOL)forceEnableInteractivePopGestuzreRecognizer {
    return NO;
}

-(void)showDebug {
    [LSTPopView popSRDebugViewWithParentView:self.view channelName:self.roomModel.roomNo sdkVer:[AgoraRtcEngineKit getSdkVersion]  isDebugMode:self.isDumpMode withDelegate:self];
}

#pragma mark service handler
- (void)subscribeServiceEvent {
    VL(weakSelf);
    [[AppContext srServiceImp] unsubscribeAll];
    [[AppContext srServiceImp] subscribeUserListCountChangedWith:^(NSUInteger count) {
        //TODO
        [weakSelf setRoomUsersCount:count];
    }];
    
    /**
     1.任何人进入房间都需要查询抢唱
     2.如果是房主查询不到状态，房主需要创建抢唱状态
     */
    [[AppContext srServiceImp] innerSingRelayInfo:^(NSError * error, SingRelayModel * model) {
        if(error == nil){
            if(model.status == SingRelayStatusStarted){
                if(self.gameModel.status == SingRelayStatusStarted){
                    return;
                }
                NSString *mes = SRLocalizedString(@"sr_game_isOn");
                [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sr_sceneImageWithName:@"empty" ] message:mes buttonTitle:SRLocalizedString(@"sr_confirm") completion:^(bool flag, NSString * _Nullable text) {
                    [[VLKTVAlert shared]dismiss];
                    [weakSelf leaveRoom];
                }];
                return;
            }
            
            if(model){
                weakSelf.gameModel = model;
            } else {
                if([weakSelf isRoomOwner]){
                    SingRelayModel *model = [[SingRelayModel alloc]init];
                    model.status = SingRelayStatusWaiting;
                    [[AppContext srServiceImp] innerAddSingRelayInfo:model completion:^(NSError * error) {
                        if(error) {
                            SRLogInfo(@"owner add sbg state error");
                        } else {
                            weakSelf.gameModel = model;
                        }
                    }];
                }
            }
        } else {
            
        }
    }];
    
    [[AppContext srServiceImp] subscribeSeatListChangedWith:^(SRSubscribe status, VLSRRoomSeatModel* seatModel) {
        [AgoraEntAuthorizedManager checkMediaAuthorizedWithParent:self completion:^(BOOL granted) {
            if (!granted) { return; }
            VLSRRoomSeatModel* model = [self getUserSeatInfoWithIndex:seatModel.seatIndex];
            if (model == nil) {
                NSAssert(NO, @"model == nil");
                return;
            }
            
            if (status == SRSubscribeCreated || status == SRSubscribeUpdated) {
                //上麦消息 / 是否打开视频 / 是否静音
                [model resetWith:seatModel];
                [weakSelf setSeatsArray:weakSelf.seatsArray];
            } else if (status == SRSubscribeDeleted) {
                // 下麦消息
                
                // 下麦重置占位模型
                [model resetWith:nil];
                [weakSelf setSeatsArray:weakSelf.seatsArray];
            }
            
            VLSRRoomSelSongModel *song = weakSelf.selSongsArray.firstObject;
           // [weakSelf.MVView updateUIWithSong:song role:weakSelf.singRole];
            VLSRRoomSeatModel *owner = self.seatsArray.firstObject;
            NSString *msg = [NSString stringWithFormat:@"owner:%@---%@, updater:%@---%@", owner.name, owner.headUrl, seatModel.name, seatModel.headUrl];
            SRLogInfo(@"%@", msg);
            [weakSelf.roomPersonView reloadSeatIndex:model.seatIndex];
            
            [weakSelf onSeatFull];
        }];
    }];
    
    [[AppContext srServiceImp] subscribeRoomStatusChangedWith:^(SRSubscribe status, VLSRRoomListModel * roomInfo) {
        if (SRSubscribeUpdated == status) {
            //切换背景
            
            //mv bg / room member count did changed
            VLSRSelBgModel* selBgModel = [VLSRSelBgModel new];
            selBgModel.imageName = [NSString stringWithFormat:@"SR_mvbg%ld", roomInfo.bgOption];
            selBgModel.isSelect = YES;
            weakSelf.choosedBgModel = selBgModel;
        } else if (status == SRSubscribeDeleted) {
            //房主关闭房间
            if ([roomInfo.creatorNo isEqualToString:VLUserCenter.user.id]) {
                NSString *mes = SRLocalizedString(@"sr_room_exit");
                [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sr_sceneImageWithName:@"empty" ] message:mes buttonTitle:SRLocalizedString(@"sr_confirm") completion:^(bool flag, NSString * _Nullable text) {
                    [[VLKTVAlert shared]dismiss];
                    [weakSelf leaveRoom];
                }];
                return;
            }
            
            [weakSelf popForceLeaveRoom];
        }
    }];
    
    //callback if choose song list did changed
    [[AppContext srServiceImp] subscribeChooseSongChangedWith:^(SRSubscribe status, VLSRRoomSelSongModel * songInfo, NSArray<VLSRRoomSelSongModel*>* songArray) {
        // update in-ear monitoring
        [weakSelf _checkInEarMonitoring];
        NSLog(@"update:%@歌曲被删掉了", songInfo.songName);
        if (SRSubscribeDeleted == status) {
            BOOL success = [weakSelf removeSelSongWithSongNo:[songInfo.songNo integerValue] sync:NO];
            if (!success) {
                weakSelf.selSongsArray = songArray;
                SRLogInfo(@"removeSelSongWithSongNo fail, reload it");
            }
            //歌曲删除关闭耳返
            weakSelf.isEarOn = false;
            [self.RTCkit enableInEarMonitoring:false includeAudioFilters:AgoraEarMonitoringFilterNone];
        } else {
            VLSRRoomSelSongModel* song = [weakSelf selSongWithSongNo:songInfo.songNo];
            //add new song
            SRLogInfo(@"song did updated: %@ status: %ld", song.name, songInfo.status);
            weakSelf.selSongsArray = [NSMutableArray arrayWithArray:songArray];
            
            if(status == SRSubscribeUpdated && ![songInfo.winnerNo isEqualToString:@""]){
                //主线程刷新UI
                dispatch_async(dispatch_get_main_queue(), ^{
                    //展示谁抢到麦的视图
                    [self.statusView hideSRBtn];

                    if([self isOnMicSeat]){
                        [self.statusView setMicOwnerWith:songInfo.name url:songInfo.imageUrl];
                        [self.statusView showNextMicOwnerWith:songInfo.name url:songInfo.imageUrl];
                    }
                });
                //延迟三秒执行下面的逻辑
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    [self.statusView hideMicOwner];
                    NSString *realWinNo = [songInfo.winnerNo componentsSeparatedByString:@"_"][0];
                    self.nextWinNo = realWinNo;
                    [self.statusView hideSRBtn];
                });
                
            }
        }
    }];
    
    [[AppContext srServiceImp] subscribeNetworkStatusChangedWith:^(SRServiceNetworkStatus status) {
        if (status != SRServiceNetworkStatusOpen) {
//            [VLToast toast:[NSString stringWithFormat:@"network changed: %ld", status]];
            return;
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            // 在主线程中执行的代码
            [weakSelf subscribeServiceEvent];
            [weakSelf _fetchServiceAllData];
        });
        
    }];
    [[AppContext srServiceImp] subscribeRoomWillExpireWith:^{
        bool isOwner = [weakSelf.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
        NSString *mes = isOwner ? SRLocalizedString(@"sr_room_timeout") : SRLocalizedString(@"sr_room_offline");
        [[VLKTVAlert shared] showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sr_sceneImageWithName:@"empty" ]  message:mes buttonTitle:SRLocalizedString(@"sr_confirm") completion:^(bool flag, NSString * _Nullable text) {
            [[VLKTVAlert shared]dismiss];
            [weakSelf leaveRoom];
        }];
    }];
    
    [[AppContext srServiceImp] innerSubscribeSingRelayInfoWithCompletion:^(SRSubscribe status, SingRelayModel * model, NSError * error) {
        if(error == nil){
            weakSelf.gameModel = model;
            NSLog(@"sync State:%li", model.status);
            if(![weakSelf isOnMicSeat]){
                [weakSelf.requestOnLineView setTipHidden:model.status == SingRelayStatusStarted];
            }
        }
    }];
}

#pragma mark view helpers
- (void)prepareBgImage {
    if (self.roomModel.bgOption) {
        VLSRSelBgModel *selBgModel = [VLSRSelBgModel new];
        selBgModel.imageName = [NSString stringWithFormat:@"SR_mvbg%ld", self.roomModel.bgOption];
        selBgModel.isSelect = YES;
        self.choosedBgModel = selBgModel;
    }
}

//更换MV背景
- (void)popSelMVBgView {
    [LSTPopView popSRSelMVBgViewWithParentView:self.view
                                     bgModel:self.choosedBgModel
                                withDelegate:self];
}

//弹出更多
- (void)popSelMoreView {
    [LSTPopView popSRSelMoreViewWithParentView:self.view
                                withDelegate:self];
}

//弹出下麦视图
- (void)popDropLineViewWithSeatModel:(VLSRRoomSeatModel *)seatModel {
    [LSTPopView popSRDropLineViewWithParentView:self.view
                                withSeatModel:seatModel
                                 withDelegate:self];
}

//弹出美声视图
- (void)popBelcantoView {
    [LSTPopView popSRBelcantoViewWithParentView:self.view
                            withBelcantoModel:self.selBelcantoModel
                                 withDelegate:self];
}

//弹出点歌视图
- (void)popUpChooseSongView:(BOOL)ifChorus {
    LSTPopView* popChooseSongView =
    [LSTPopView popSRUpChooseSongViewWithParentView:self.view
                                         isChorus:ifChorus
                                  chooseSongArray:self.selSongsArray
                                       withRoomNo:self.roomModel.roomNo
                                     withDelegate:self];
    
    self.chooseSongView = (VLSRPopSongList*)popChooseSongView.currCustomView;
}

//网络差视图
- (void)popBadNetWrokTipView {
    [LSTPopView popSRBadNetWrokTipViewWithParentView:self.view
                                      withDelegate:self];
}

#pragma mark - VLDebugViewDelegate
- (void)didExportLogWith:(NSString *)path {
    UIActivityViewController *activityController = [[UIActivityViewController alloc] initWithActivityItems:@[[NSURL fileURLWithPath:path isDirectory:YES]] applicationActivities:nil];
    activityController.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:activityController animated:YES completion:nil];
}

- (void)didDumpModeChanged:(BOOL)enable {
    self.isDumpMode = enable;
    NSString* key = @"dump enable";
    [SRDebugInfo setSelectedStatus:enable forKey:key];
    [SRDebugManager reLoadParamAll];
}

-(void)didParamsSetWith:(NSString *)key value:(NSString *)value{
    if([value.lowercaseString isEqualToString:@"true"] || [value.lowercaseString isEqualToString:@"false"] || [value.lowercaseString isEqualToString:@"yes"] || [value.lowercaseString isEqualToString:@"no"]){
        BOOL flag = [value.lowercaseString isEqualToString:@"true"] || [value.lowercaseString isEqualToString:@"yes"];
        NSString *params = @"";
        if(flag){
            params = [NSString stringWithFormat:@"{\"%@\":true", key];
        } else {
            params = [NSString stringWithFormat:@"{\"%@\":false", key];
        }
        [self.RTCkit setParameters:params];
    }else if([self isPureNumberString:value]){
        NSInteger num = [value integerValue];
        NSString *params = [NSString stringWithFormat:@"{\"%@\":%li", key, (long)num];
        [self.RTCkit setParameters:params];
    } else {
        NSString *params = [NSString stringWithFormat:@"{\"%@\":\"%@\"", key, value];
        [self.RTCkit setParameters:params];
    }
}

- (BOOL)isPureNumberString:(NSString *)string {
    NSCharacterSet *numberSet = [NSCharacterSet characterSetWithCharactersInString:@"0123456789"];
    NSRange range = [string rangeOfCharacterFromSet:numberSet.invertedSet];
    return (range.location == NSNotFound);
}

//用户弹框离开房间
- (void)popForceLeaveRoom {
    VL(weakSelf);
    [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sr_sceneImageWithName:@"empty" ] message:SRLocalizedString(@"room_has_close") buttonTitle:SRLocalizedString(@"confirm") completion:^(bool flag, NSString * _Nullable text) {
        for (BaseViewController *vc in weakSelf.navigationController.childViewControllers) {
            if ([vc isKindOfClass:[VLSROnLineListVC class]]) {
//                [weakSelf destroyMediaPlayer];
//                [weakSelf leaveRTCChannel];
                [weakSelf.navigationController popToViewController:vc animated:YES];
            }
        }
        [[VLKTVAlert shared] dismiss];
    }];
}

- (void)showSettingView {
    LSTPopView* popView = [LSTPopView popSRSettingViewWithParentView:self.view
                                                       settingView:self.settingView
                                                      withDelegate:self];
    
    self.settingView = (VLSRSettingView*)popView.currCustomView;
}

- (void)showScoreViewWithScore:(NSInteger)score {
                        //  song:(VLRoomSelSongModel *)song {
    if (score < 0) return;
    if(_scoreView == nil) {
        _scoreView = [[VLSRPopScoreView alloc] initWithFrame:self.view.bounds withDelegate:self];
        [self.view addSubview:_scoreView];
    }
    SRLogInfo(@"Avg score for the song: %ld", (long)score);
    [_scoreView configScore:(int)score];
    [self.view bringSubviewToFront:_scoreView];
    self.scoreView.hidden = NO;
}

- (void)popScoreViewDidClickConfirm
{
    SRLogInfo(@"Using as score view hidding");
    self.scoreView = nil;
}

#pragma mark - rtc callbacks
- (void)rtcEngine:(AgoraRtcEngineKit *)engine didJoinedOfUid:(NSUInteger)uid elapsed:(NSInteger)elapsed
{
    SRLogInfo(@"didJoinedOfUid: %ld", uid);
//    [self.SRApi mainRtcEngine:engine didJoinedOfUid:uid elapsed:elapsed];
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine
reportAudioVolumeIndicationOfSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers
      totalVolume:(NSInteger)totalVolume {
    if(speakers.count == 0){
        return;
    }
}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
receiveStreamMessageFromUid:(NSUInteger)uid
         streamId:(NSInteger)streamId
             data:(NSData * _Nonnull)data {    //接收到对方的RTC消息
    
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:data];
   if([dict[@"cmd"] isEqualToString:@"singleLineScore"]) {//主唱和合唱都可能发分数
        //观众使用主唱的分数来显示
        NSInteger index = [dict[@"index"] integerValue];
        NSInteger score = [dict[@"score"] integerValue];
        NSInteger cumulativeScore = [dict[@"cumulativeScore"] integerValue];
        NSInteger total = [dict[@"total"] integerValue];
       self.sumScore = cumulativeScore;
       self.segmentScore += score;
       self.segmentCount += 1;
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.statusView.lrcView updateScoreWith:score cumulativeScore:cumulativeScore totalScore:total];
            NSLog(@"score:%li--%li---%li---%li", index, score, cumulativeScore, total);
        });
   } else if([dict[@"cmd"] isEqualToString:@"SingingScore"]) {
//       int score = [dict[@"score"] intValue];
//       NSString *userName = dict[@"userName"];
//       NSString *userId = dict[@"userId"];
//       NSString *poster = dict[@"poster"];
//       //把用户信息存进数组，需要在这里合并数据
//       SubRankModel *model = [[SubRankModel alloc]init];
//       model.userName = userName;
//       model.poster = poster;
//       model.score = score;
//       model.songNum = 1;
//       model.userId = userId;
//       [self.scoreArray addObject:model];
   } else if([dict[@"cmd"] isEqualToString:@"CoSingerLoadSuccess"]) {
       if([self isRoomOwner]) {
           self.cosingerLoadCount++;
           NSLog(@"coCount:%li---%li", [self getOnMicUserCount], self.cosingerLoadCount);
           if(self.cosingerLoadCount == [self getOnMicUserCount] - 1 && self.MainSingerPlayFlag == true){//如果所有合唱者都加载好了，主唱也加载好了，主唱开始
               VLSRRoomSelSongModel *model = self.selSongsArray.firstObject;
               [self.SRApi switchSingerRoleWithNewRole:KTVSingRoleLeadSinger
                                         onSwitchRoleState:^( KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
//                   if(state != SRSwitchRoleStateSuccess) {
//                       SRLogError(@"switchSingerRole error: %ld", reason);
//                       return;
//                   }
                   [self.SRApi startSingWithSongCode:[model.songNo integerValue] startPos:0];
               }];
           }
       }
       
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

-(NSMutableArray *)scoreArray {
    if(!_scoreArray){
       _scoreArray = [NSMutableArray array];
    }
    return _scoreArray;
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine tokenPrivilegeWillExpire:(NSString *)token {
    SRLogInfo(@"tokenPrivilegeWillExpire: %@", token);
    [[NetworkManager shared] generateTokenWithChannelName:self.roomModel.roomNo
                                                      uid:VLUserCenter.user.id
                                                tokenType:TokenGeneratorTypeToken006
                                                     type:AgoraTokenTypeRtc
                                                   expire:1500
                                                  success:^(NSString * token) {
        SRLogInfo(@"tokenPrivilegeWillExpire rtc renewToken: %@", token);
        [self.RTCkit renewToken:token];
    }];
    
    //TODO: mcc missing token expire callback
    [[NetworkManager shared] generateTokenWithChannelName:self.roomModel.roomNo
                                                      uid:VLUserCenter.user.id
                                                tokenType:TokenGeneratorTypeToken006
                                                     type:AgoraTokenTypeRtm
                                                   expire:1500
                                                  success:^(NSString * token) {
        SRLogInfo(@"tokenPrivilegeWillExpire rtm renewToken: %@", token);
        //TODO(chenpan): mcc missing
//        [self.AgoraMcc renewToken:token];
    }];
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine contentInspectResult:(AgoraContentInspectResult)result {
    SRLogInfo(@"contentInspectResult: %ld", result);
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine localAudioStats:(AgoraRtcLocalAudioStats *)stats {
}

#pragma mark - action utils / business
- (void)stopPlaySong {
    self.isPause = false;
   // self.MVView.joinCoSingerState = SRJoinCoSingerStateWaitingForJoin;
//    [self.SRApi switchSingerRoleWithNewRole:SRSingRoleAudience
//                           onSwitchRoleState:^(SRSwitchRoleState state, SRSwitchRoleFailReason reason) {
//    }];
}

-(void)loadAndPlaySRSong{
    VLSRRoomSelSongModel* model = [[self selSongsArray] firstObject];
    /*
     1.如果是房主切换到leaderSinger
     2.合唱上麦
     3.观众loadmusic
     */
    
    KTVSingRole role = KTVSingRoleAudience;
    if([self isRoomOwner]){
        role = KTVSingRoleLeadSinger;
    } else if (![self isRoomOwner] && self.isOnMicSeat) {
        role = KTVSingRoleCoSinger;
    } else {
        role = KTVSingRoleAudience;
    }
    self.singRole = role;
    
    if(!model){
        return;
    }
    [self markSongPlaying:model];

    [self setPlayoutVolume:50];
    if([self isRoomOwner]){
        self.isNowMicMuted = false;
        [self.RTCkit adjustRecordingSignalVolume:(role == KTVSingRoleLeadSinger || role == KTVSingRoleSoloSinger) ? 100 : 0];
    } else {
        self.isNowMicMuted = true;
        [self.RTCkit muteLocalAudioStream:self.isNowMicMuted];
    }

    KTVSongConfiguration* songConfig = [[KTVSongConfiguration alloc] init];
    songConfig.autoPlay = NO;
    songConfig.mode = (role == KTVSingRoleAudience) ? KTVLoadMusicModeLoadLrcOnly : KTVLoadMusicModeLoadMusicAndLrc;
    songConfig.mainSingerUid = [model.userNo integerValue];
    songConfig.songIdentifier = model.songNo;
    
    VL(weakSelf);
    self.loadMusicCallBack = ^(BOOL isSuccess, NSInteger songCode) {
        if (!isSuccess) {
            return;
        }
//        if(role == SRSingRoleCoSinger){
//            [weakSelf.SRApi startSingWithSongCode:songCode startPos:0];
//        }
    };

    [self.SRApi loadMusicWithSongCode:[model.songNo integerValue] config:songConfig onMusicLoadStateListener:self];

}

- (void)enterSeatWithIndex:(NSInteger)index completion:(void(^)(NSError*))completion {
    
    SROnSeatInputModel* inputModel = [SROnSeatInputModel new];
    inputModel.seatIndex = index;
//    VL(weakSelf);
    [[AppContext srServiceImp] enterSeatWith:inputModel
                                        completion:completion];
    [self _checkEnterSeatAudioAuthorized];
}

- (void)_checkEnterSeatAudioAuthorized {
    if (self.isEnterSeatNotFirst) {
        return;
    }
    
    self.isEnterSeatNotFirst = YES;
    [AgoraEntAuthorizedManager checkAudioAuthorizedWithParent:self completion:nil];
}

- (void)leaveSeatWithSeatModel:(VLSRRoomSeatModel * __nonnull)seatModel
                 withCompletion:(void(^ __nullable)(NSError*))completion {
    if(seatModel.rtcUid == VLUserCenter.user.id) {
        if(seatModel.isVideoMuted == 1) {
            [self.RTCkit stopPreview];
        }
    }
    
    SROutSeatInputModel* inputModel = [SROutSeatInputModel new];
    inputModel.userNo = seatModel.userNo;
    inputModel.userId = seatModel.rtcUid;
    inputModel.userName = seatModel.name;
    inputModel.userHeadUrl = seatModel.headUrl;
    inputModel.seatIndex = seatModel.seatIndex;
    [[AppContext srServiceImp] leaveSeatWith:inputModel
                                        completion:completion];
}

- (void)refreshChoosedSongList:(void (^ _Nullable)(void))block{
    VL(weakSelf);
    [[AppContext srServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLSRRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        
        weakSelf.selSongsArray = songArray;
        if(block) {
            block();
        }
    }];
}

- (void)markSongPlaying:(VLSRRoomSelSongModel *)model {
    if (model.status == VLSRSongPlayStatusPlaying) {
        return;
    }
    [[AppContext srServiceImp] markSongDidPlayWith:model
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
    
   // [self.MVView.gradeView reset];

    if([self getOnMicUserCount] == 8 && !_isOnMicSeat){
        [VLToast toast:SRLocalizedString(@"sr_mic_full")];
        return;
    }
    
    if(self.RTCkit.getConnectionState != AgoraConnectionStateConnected){
      //  self.MVView.joinCoSingerState = SRJoinCoSingerStateWaitingForJoin;
        [VLToast toast:SRLocalizedString(@"sr_join_chorus_failed")];
        return;
    }
    
    if (![self getJoinChorusEnable]) {
        SRLogInfo(@"getJoinChorusEnable false");
      //  self.MVView.joinCoSingerState = SRJoinCoSingerStateWaitingForJoin;
        return;
    }
    
    //没有上麦需要先上麦
    if ([self getCurrentUserSeatInfo] == nil) {
        for (int i = 1; i < self.seatsArray.count; i++) {
            VLSRRoomSeatModel* seat = self.seatsArray[i];
            
            if (seat.rtcUid == 0) {
                VL(weakSelf);
                SRLogError(@"before enterSeat error");
                self.isJoinChorus = true;
                [self enterSeatWithIndex:i completion:^(NSError *error) {
                    if(error){
                        SRLogError(@"enterSeat error:%@", error.description);
                   //     weakSelf.MVView.joinCoSingerState = SRJoinCoSingerStateWaitingForJoin;
                        weakSelf.isJoinChorus = false;
                        return;
                    }
                    [weakSelf _joinChorus];
                }];
                return;
            }
        }
        
        //TODO(chenpan):没有空麦位，show error
        [VLToast toast:SRLocalizedString(@"sr_mic_full")];
        return;
    }
    
    [self _joinChorus];
}

- (void)_joinChorus {
    VLSRRoomSelSongModel* model = [[self selSongsArray] firstObject];
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
        
        [weakSelf.SRApi startSingWithSongCode:songCode startPos:0];
        NSLog(@"before switch role, load music success");
        [weakSelf.SRApi switchSingerRoleWithNewRole:role
                                   onSwitchRoleState:^( KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
            if (state == KTVSwitchRoleStateFail && reason != KTVSwitchRoleFailReasonNoPermission) {
           //     weakSelf.MVView.joinCoSingerState = SRJoinCoSingerStateWaitingForJoin;
                [VLToast toast:[NSString stringWithFormat:@"join chorus fail: %ld", reason]];
                weakSelf.isJoinChorus = false;
                SRLogInfo(@"join chorus fail");
                //TODO: error toast?
                return;
            }

          //  weakSelf.MVView.joinCoSingerState = SRJoinCoSingerStateWaitingForLeave;
            weakSelf.isJoinChorus = false;
            
            weakSelf.isNowMicMuted = role == KTVSingRoleAudience;

            VLSRRoomSelSongModel *selSongModel = weakSelf.selSongsArray.firstObject;
            SRJoinChorusInputModel* inputModel = [SRJoinChorusInputModel new];
            inputModel.isChorus = YES;
            inputModel.songNo = selSongModel.songNo;
            [[AppContext srServiceImp] joinChorusWith:inputModel
                                                 completion:^(NSError * error) {
            }];
            
            //开麦
            [[AppContext srServiceImp] updateSeatAudioMuteStatusWith:NO
                                                                completion:^(NSError * error) {
            }];
        }];
    };
    SRLogInfo(@"before songCode:%li", [model.songNo integerValue]);
    [self.SRApi loadMusicWithSongCode:[model.songNo integerValue] config:songConfig onMusicLoadStateListener:self];
}

- (void)removeCurrentSongWithSync:(BOOL)sync
{
    VLSRRoomSelSongModel* top = [self.selSongsArray firstObject];
    if(top && top.songNo.length != 0) {
        [self removeSelSongWithSongNo:[top.songNo integerValue] sync:sync];
    }
}

- (BOOL)removeSelSongWithSongNo:(NSInteger)songNo sync:(BOOL)sync {
    __block VLSRRoomSelSongModel* removed;
    BOOL isTopSong = [self.selSongsArray.firstObject.songNo integerValue] == songNo;
    
    if (isTopSong) {
        [self stopPlaySong];
    }
    
    NSMutableArray<VLSRRoomSelSongModel*> *updatedList = [NSMutableArray arrayWithArray:[self.selSongsArray filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(VLSRRoomSelSongModel*  _Nullable evaluatedObject, NSDictionary<NSString *,id> * _Nullable bindings) {
        if([evaluatedObject.songNo integerValue] == songNo) {
            removed = evaluatedObject;
            return NO;
        }
        return YES;
    }]]];
    
    if (removed != nil) {
        // 已删除
        self.selSongsArray = updatedList;
        
        if (sync) {
            SRRemoveSongInputModel* inputModel = [SRRemoveSongInputModel new];
            inputModel.songNo = removed.songNo;
            inputModel.objectId = removed.objectId;
            
            [[AppContext srServiceImp] removeSongWith:inputModel completion:^(NSError * error) {
                if (error) {
                    SRLogInfo(@"deleteSongEvent fail: %@ %ld", removed.songName, (long)error.code);
                }
            }];
        }
        return YES;
    } else {
        return NO;
    }
}

//- (void)replaceSelSongWithInfo:(VLRoomSelSongModel*)songInfo {
//    self.selSongsArray = [SRSyncManagerServiceImp sortChooseSongWithSongList:self.selSongsArray];
//}

- (void)leaveRoom {
    VL(weakSelf);
    [[AppContext srServiceImp] leaveRoomWithCompletion:^(NSError * error) {
        if (error != nil) {
            return;
        }
        
        for (BaseViewController *vc in weakSelf.navigationController.childViewControllers) {
            if ([vc isKindOfClass:[VLSROnLineListVC class]]) {
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
        @"sceneName": @"SR",
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
        SRLogInfo(@"voiceIdentify success: %@", msg);
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
        SRLogInfo(@"评分回调开启成功\n");
    } else {
        SRLogInfo(@"评分回调开启失败：%d\n",code);
    }
    
    [self.RTCkit enableVideo];
    [self.RTCkit enableAudio];
    
    [self setupContentInspectConfig];
    
    VLSRRoomSeatModel* myseat = [self.seatsArray objectAtIndex:0];
    
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
    [self.RTCkit createDataStream:&srStreamId
                           config:config];
    
    NSString* exChannelToken = VLUserCenter.user.agoraPlayerRTCToken;
    KTVApiConfig *apiConfig = [[KTVApiConfig alloc] initWithAppId:[[AppContext shared] appId]
                                                        rtmToken:VLUserCenter.user.agoraRTMToken
                                                        engine:self.RTCkit
                                                        channelName:self.roomModel.roomNo
                                                        localUid:[VLUserCenter.user.id integerValue]
                                                        chorusChannelName:[NSString stringWithFormat:@"%@_ex", self.roomModel.roomNo]
                                                        chorusChannelToken:@""
                                                        type:KTVTypeSingRelay
                                                        maxCacheSize:10
                                                        musicType:loadMusicTypeMcc
                                                        isDebugMode:false];
    
    self.SRApi = [[KTVApiImpl alloc] initWithConfig: apiConfig];
    [self.SRApi renewInnerDataStreamId];
//    SRLrcControl* lrcControl = [[SRLrcControl alloc] initWithLrcView:self.MVView.karaokeView];
//    [self.SRApi setLrcViewWithView:lrcControl];
//    self.lrcControl = lrcControl;
//    self.lrcControl.delegate = self;
    [self.SRApi setLrcViewWithView:self.statusView.lrcView];
    [self.SRApi muteMicWithMuteStatus:self.isNowMicMuted];
    [self.SRApi addEventHandlerWithKtvApiEventHandler:self];
//    VL(weakSelf);
    SRLogInfo(@"Agora - joining RTC channel with token: %@, for roomNo: %@, with uid: %@", VLUserCenter.user.agoraRTCToken, self.roomModel.roomNo, VLUserCenter.user.id);
    int ret =
    [self.RTCkit joinChannelByToken:VLUserCenter.user.agoraRTCToken
                          channelId:self.roomModel.roomNo
                                uid:[VLUserCenter.user.id integerValue]
                       mediaOptions:[self channelMediaOptions]
                        joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        SRLogInfo(@"Agora - 加入RTC成功");
//        [weakSelf.RTCkit setParameters: @"{\"che.audio.enable.md \": false}"];sin
    }];
    if (ret != 0) {
        SRLogError(@"joinChannelByToken fail: %d, uid: %ld, token: %@", ret, [VLUserCenter.user.id integerValue], VLUserCenter.user.agoraRTCToken);
    }
    
    VLSRRoomSeatModel* info = [self getCurrentUserSeatInfo];
    if (info) {
        [self _checkEnterSeatAudioAuthorized];
        
        if (!info.isVideoMuted) {
            [AgoraEntAuthorizedManager checkCameraAuthorizedWithParent:self completion:nil];
        }
        self.isNowMicMuted = info.isAudioMuted;
        self.isNowCameraMuted = info.isVideoMuted;
    } else {
        self.isNowMicMuted = YES;
        self.isNowCameraMuted = YES;
    }
}

- (void)leaveRTCChannel {
    [self.SRApi removeEventHandlerWithKtvApiEventHandler:self];
    [self.SRApi cleanCache];
    self.SRApi = nil;
    self.loadMusicCallBack = nil;
    [self.RTCkit leaveChannel:^(AgoraChannelStats * _Nonnull stat) {
        SRLogInfo(@"Agora - Leave RTC channel");
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
    [option setPublishMediaPlayerId:[[self.SRApi getMusicPlayer] getMediaPlayerId]];
    [option setEnableAudioRecordingOrPlayout:YES];
    return option;
}

- (void)sendStreamMessageWithDict:(NSDictionary *)dict
                         success:(void (^ _Nullable)(BOOL))success {
//    VLLog(@"sendStremMessageWithDict:::%@",dict);
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    
    int code = [self.RTCkit sendStreamMessage:srStreamId
                                         data:messageData];
    if (code == 0 && success) {
        success(YES);
    } else{
//        VLLog(@"发送失败-streamId:%ld\n",streamId);
    };
}

#pragma mark -- VLSRAPIDelegate

- (void)onChorusChannelAudioVolumeIndicationWithSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers totalVolume:(NSInteger)totalVolume{
    
}

- (void)didDragTo:(NSInteger)progress{
    [self.SRApi seekSingWithTime:progress];
}

- (void)didLrcViewScorllFinishedWith:(NSInteger)score totalScore:(NSInteger)totalScore lineScore:(NSInteger)lineScore lineIndex:(NSInteger)lineIndex{
    
    if([self.currentUserNo isEqualToString:VLUserCenter.user.id]){
        self.sumScore += lineScore;
        self.segmentScore += lineScore;
        self.segmentCount += 1;
        [self.statusView.lrcView updateScoreWith:lineScore cumulativeScore:self.sumScore totalScore:totalScore];
        
        //将分数同步给他人
        [self sendMainSingerLineScoreToAudienceWith:self.sumScore totalScore:totalScore lineScore:lineScore lineIndex:lineIndex];
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
}

- (void)didSongLoadedWith:(LyricModel *)model{
    self.lyricModel = model;
}

- (void)didJoinChours {
    //加入合唱
  //  self.MVView.joinCoSingerState = SRJoinCoSingerStateJoinNow;
    [self joinChorus];
}

-(void)didLeaveChours {
    //退出合唱
    [[AppContext srServiceImp] coSingerLeaveChorusWithCompletion:^(NSError * error) {
    }];
    [self stopPlaySong];
    self.isNowMicMuted = true;
   // [self.MVView.gradeView reset];
    [[AppContext srServiceImp] updateSeatAudioMuteStatusWith:YES
                                                        completion:^(NSError * error) {
    }];
}

#pragma mark -- VLSRTopViewDelegate
- (void)onVLSRTopView:(VLSRTopView *)view closeBtnTapped:(id)sender {
    VL(weakSelf);
    NSString *title = VLUserCenter.user.ifMaster ? SRLocalizedString(@"sr_disband_room") : SRLocalizedString(@"sr_exit_room");
    NSString *message = VLUserCenter.user.ifMaster ? SRLocalizedString(@"sr_confirm_disband_room") : SRLocalizedString(@"sr_confirm_exit_room");
    NSArray *array = [[NSArray alloc]initWithObjects:SRLocalizedString(@"sr_cancel"),SRLocalizedString(@"sr_confirm"), nil];
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:title message:message placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == YES){
            [weakSelf leaveRoom];
        }
        [[VLAlert shared] dismiss];
    }];
}

- (void)onVLSRTopView:(VLSRTopView *)view moreBtnTapped:(id)sender {
    AUiMoreDialog* dialog = [[AUiMoreDialog alloc] initWithFrame:self.view.bounds];
    [self.view addSubview:dialog];
    [dialog show];
}

#pragma mark - VLPopMoreSelViewDelegate
- (void)onVLSRMoreSelView:(VLSRPopMoreSelView *)view
                 btnTapped:(id)sender
                 withValue:(VLSRMoreBtnClickType)typeValue {
    [[LSTPopView getSRPopViewWithCustomView:view] dismiss];
    switch (typeValue) {
//        case VLSRMoreBtnClickTypeBelcanto:
//            [self popBelcantoView];
//            break;
        case VLSRMoreBtnClickTypeSound:
            break;
        case VLSRMoreBtnClickTypeMV:
            [self popSelMVBgView];
            break;
        default:
            break;
    }
}

#pragma mark - VLSRStatusViewDelegate
- (void)didSrActionChanged:(enum SRClickAction)action{
    switch(action) {
        case SRClickActionStartGame:
            //开始游戏后 卖上观众切换为合唱，房主切换成leadSinger
            [self startGame];
            break;
        case SRClickActionSbg:
            //抢唱操作
            [self startSBGGrapWith:(int)self.currentIndex];
            break;
        case SRClickActionAgain:
            [self renewGame];
            break;
        case SRClickActionRetryLrc:
            self.statusView.retryBtn.hidden = true;
            [self loadAndPlaySRSong];
            break;
        default:
            break;
    }
}

- (void)didLrcViewActionChangedWithState:(enum SRClickAction)state{
    switch(state) {
        case SRClickActionNextSong:
            //切歌
            if(self.statusView.srBtn.isEnabled){
                [self changeToNextSong];
            }
            break;
        case SRClickActionAac:
            //伴奏
            self.trackMode = KTVPlayerTrackModeAcc;
            break;
        case SRClickActionEffect:
            //调音
            [self showSettingView];
            break;
        case SRClickActionOrigin:
            //原唱
            self.trackMode = KTVPlayerTrackModeOrigin;
           // [self.SRApi.getMusicPlayer selectAudioTrack:0];
            break;
        case SRClickActionAgain:
            break;
        default:
            break;
    }
}

- (void)didLrcProgressChanged:(NSInteger)progress{
    [self handleProgress:progress];
}

-(void)handleProgress:(CGFloat)progress {
    VLSRRoomSelSongModel * model = self.selSongsArray.firstObject;
    NSArray *array = nil;
    for(SRChooseSongInputModel *chooseModel in [self getChooseSongArray]){
        if([chooseModel.songNo isEqualToString:model.songNo]){
            array = chooseModel.playCounts;
        }
    }

    for (NSInteger i = 0; i < array.count; i++) {
        NSInteger lowerBound = [array[i] integerValue];
        NSInteger upperBound = (i < (array.count - 1)) ? [array[i+1] integerValue] : NSIntegerMax;
        
        //每段开始前3秒需要提示即将开始抢唱
        if(lowerBound - progress >= 2000 && lowerBound - progress <= 3000){
            if(i < array.count - 1){
                if([self isOnMicSeat]){
                    [self.statusView showContentViewWith: i < (array.count - 2) ? SRLocalizedString(@"sr_next_ready") : SRLocalizedString(@"sr_next_start")];
                    if(self.nextWinNo){
                        for(int i=0;i<self.seatsArray.count;i++) {
                            if([self.seatsArray[i].userNo isEqualToString:self.nextWinNo]){
                                [[NSUserDefaults standardUserDefaults]setObject:@(i) forKey:@"MICOWNERINDEX"];
                                [[NSUserDefaults standardUserDefaults]synchronize];
                            }
                        }
                    } else {
                        [[NSUserDefaults standardUserDefaults]setObject:@(0) forKey:@"MICOWNERINDEX"];
                        [[NSUserDefaults standardUserDefaults]synchronize];
                    }
                }
            }
        }
        
        if(self.scoreArray.count == 5){//超过五个的数据不需要了。这个时候的progerss是无用数据
            [[NSUserDefaults standardUserDefaults]setObject:nil forKey:@"MICOWNERINDEX"];
            [[NSUserDefaults standardUserDefaults]synchronize];
            return;
        }
        
        if (progress >= lowerBound && progress < upperBound) {
            NSUInteger index = i + 1;
            [self performBusinessLogicWithIndex:index pro:progress];
        }
    }
}

-(void)performBusinessLogicWithIndex:(NSInteger)index pro:(NSInteger)pro {
    // 在这里处理对应的业务逻辑，使用传入的index
    if (index > 0 && [self.chooseArray[index - 1] isEqualToNumber:@(NO)]) {
        self.chooseArray[index - 1] = @(YES);
        self.currentIndex = index + 1;

        NSLog(@"index:%li, pro:%li, score:%li", (long)index, (long)pro, self.segmentScore);
        [self.statusView hideContentView];
        if([self isOnMicSeat]){
            [self.statusView hideNextMicOwner];
        }
        [self sendScoreToServiceWith:self.currentUserNo];
        self.segmentScore = 0;
        self.segmentCount = 0;
        self.currentUserNo = self.nextWinNo ? self.nextWinNo : self.seatsArray.firstObject.userNo;
        self.isNowMicMuted = ![self.currentUserNo isEqualToString:VLUserCenter.user.id];
        
        //开麦
        [[AppContext srServiceImp] updateSeatAudioMuteStatusWith:self.isNowMicMuted
                                                            completion:^(NSError * error) {
        }];
        if(index < 4){
            [self updatePlayerStatus];
        } else {
            if (index == 4) {
                [self updateLastStatus];
            }
            [self.statusView hideSRBtn];
        }
        
        if(index < 5){
            self.statusView.numStr = [NSString stringWithFormat:@"%li/5", index + 1];
            self.nextWinNo = nil;
        }
        [self.roomPersonView.personCollectionView reloadData];
    }
}

-(void)sendScoreToServiceWith:(NSString *)userNo{
    //发送分段数据到服务器
    VLSRRoomSeatModel *seatModel = nil;
    for(VLSRRoomSeatModel *model in self.seatsArray) {
        if([model.userNo isEqualToString:userNo]){
            seatModel = model;
        }
    }
    
    if(seatModel){
        SRSubRankModel *model = [[SRSubRankModel alloc]init];
        model.userName = seatModel.name;
        model.poster = seatModel.headUrl;
        model.score = self.segmentScore;
        model.songNum = 1;
        model.lines = self.segmentCount;
        model.userId = seatModel.userNo;
        NSLog(@"index:seatModel:%@--%@---%li---%li---%li", seatModel.name, seatModel.userNo, model.score, self.sumScore, self.segmentCount);
        [self.scoreArray addObject:model];
    }
}

-(void)updateMicStatus {
    if(self.nextWinNo == nil || [self.nextWinNo isEqualToString:@""]){
        //表示无人抢唱 那么还是房主唱
        VLSRRoomSeatModel *model = self.seatsArray.firstObject;
        self.nextWinNo = model.userNo;
    }
    self.isNowMicMuted = ![self.currentUserNo isEqualToString:VLUserCenter.user.id];
}

-(void)updatePlayerStatus{
    [self updateMicStatus];
    [self updateSBGCountDown];
}

-(void)updateLastStatus {
    [self updateMicStatus];
    if([self isOnMicSeat]){
        if([self isRoomOwner]){
            if([self.currentUserNo isEqualToString:VLUserCenter.user.id]){
                self.statusView.state = SRStateOwnerSredAndPlaying;
            } else {
                self.statusView.state = SRStateOwnerUnSredAndPlaying;
            }
        } else {
            if([self.currentUserNo isEqualToString:VLUserCenter.user.id]){
                self.statusView.state = SRStatePlayerSredAndPlaying;
            } else {
                self.statusView.state = SRStatePlayerUnsredAndPlaying;
            }
        }
    }
}

- (void)changeToNextSong {
    NSString *title = SRLocalizedString(@"sr_change_song");
    NSString *message = SRLocalizedString(@"sr_change_next_song");
    NSArray *array = [[NSArray alloc]initWithObjects:SRLocalizedString(@"sr_cancel"),SRLocalizedString(@"sr_confirm"), nil];
    kWeakSelf(self);
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:title message:message placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == YES){
            [weakself.SRApi stopSing];
            [weakself removeCurrentSongWithSync:YES];
            [weakself reNewAllData];
            self.gameModel.status = SingRelayStatusWaiting;
            self.statusView.state = [self isRoomOwner] ? SRStateOwnerOrderMusic : SRStateAudienceWating;
            [[AppContext srServiceImp] innerUpdateSingRelayInfo:self.gameModel completion:^(NSError * error) {
                
            }];
//            [[NSUserDefaults standardUserDefaults] setObject:nil forKey:@"MICOWNERINDEX"];
//            [[NSUserDefaults standardUserDefaults] synchronize];
//            weakself.isNowMicMuted = true;
//            [[AppContext srServiceImp] updateSeatAudioMuteStatusWith:weakself.isNowMicMuted completion:^(NSError * err) {
//
//            }];
//
//            weakself.gameModel.status = SingRelayStatusEnded;
//            if([weakself isRoomOwner]){
//////                //房主把分数给到服务端
////                weakself.gameModel.rank = [weakself convertScoreArrayToRank];
////                NSLog(@"model: %@", weakself.gameModel.rank);
//                [[AppContext srServiceImp] innerUpdateSingRelayInfo:weakself.gameModel completion:^(NSError * err) {
//
//                }];
//            }
        }
        [[VLAlert shared] dismiss];
    }];
}

-(NSDictionary *)convertScoreArrayToRank {
    NSMutableDictionary *muDict = [NSMutableDictionary dictionary];
    //先进性数组合并
    NSArray *mergeModels = mergeSRModelsWithSameUserIds(self.scoreArray);
    NSInteger count = mergeModels.count;
    if(count == 0){
        return muDict;
    }
    for(SubRankModel *model in mergeModels){
        RankModel *model1 = [[RankModel alloc]init];
        model1.userName = model.userName;
        model1.poster = model.poster;
        model1.score = model.score;
        model1.songNum = model.songNum;
        [muDict setValue:model1 forKey:model.userId];
    }
    return muDict;
}

-(void)reNewAllData {

  [[NSUserDefaults standardUserDefaults] setObject:nil forKey:@"MICOWNERINDEX"];
  [[NSUserDefaults standardUserDefaults] synchronize];
  self.isNowMicMuted = true;
  [[AppContext srServiceImp] updateSeatAudioMuteStatusWith:self.isNowMicMuted completion:^(NSError * err) {
      
  }];

  [_bottomView setAudioBtnEnabled:true];
  self.chooseArray = [NSMutableArray arrayWithObjects:@(NO), @(NO), @(NO), @(NO), @(NO), nil];
  self.currentUserNo = self.seatsArray.firstObject.userNo;
  self.nextWinNo = nil;
  self.segmentScore = 0;
  self.segmentCount = 0;
  self.sumScore = 0;
  self.cosingerLoadCount = 0;
  self.MainSingerPlayFlag = false;
  self.hasCountDown = false;
  self.currentIndex = 1;
  [self.SRApi stopSing];
  [self.statusView resetLrcView];
  [self.SRApi switchSingerRoleWithNewRole:KTVSingRoleAudience onSwitchRoleState:^(KTVSwitchRoleState state, KTVSwitchRoleFailReason reason)     {

  }];
  dispatch_async(dispatch_get_main_queue(), ^{
      [self.statusView hideNextMicOwner];
      [self.scoreArray removeAllObjects];
  });
}

-(void)startGame{//开始游戏 随机选歌
    SRChooseSongInputModel *model = [self getRandomSongModel];
    [[AppContext srServiceImp] chooseSongWith:model completion:^(NSError * err) {
        
    }];
    self.gameModel.status = SingRelayStatusStarted;
    [[AppContext srServiceImp] innerUpdateSingRelayInfo:self.gameModel completion:^(NSError * error) {
        
    }];
}

-(void)renewGame{//再来一轮
    self.gameModel.status = SingRelayStatusWaiting;
    self.statusView.state = [self isRoomOwner] ? SRStateOwnerOrderMusic : SRStateAudienceWating;
    [[AppContext srServiceImp] innerUpdateSingRelayInfo:self.gameModel completion:^(NSError * error) {
        
    }];
}

-(void)startSBGGrapWith:(int)index {
    VLSRRoomSelSongModel* model = [[self selSongsArray] firstObject];
    kWeakSelf(self);
    [[NetworkManager shared] startSongGrab:[AppContext.shared appId] sceneId:@"sing_battle_game_info" roomId:_roomModel.roomNo headUrl:@"12345" userId:VLUserCenter.user.id userName:VLUserCenter.user.name songCode:model.songNo success:^(BOOL flag) {
        if(flag){
            //抢唱成功
            NSLog(@"抢唱成功");
            VLSRRoomSelSongModel *model = weakself.selSongsArray.firstObject;
            model.winnerNo = [NSString stringWithFormat:@"%@_%i", VLUserCenter.user.id,index];
            model.name = VLUserCenter.user.name;
            model.imageUrl = VLUserCenter.user.headUrl;
            [[AppContext srServiceImp] updateChooseSongWithSongInfo:model finished:^(NSError * error) {
                
            }];
        }
    }];
}

// 合并相同 user id 的模型，并累加分数
NSArray<SRSubRankModel *> *mergeSRModelsWithSameUserIds(NSArray<SRSubRankModel *> *models) {
    NSMutableDictionary *userDict = [NSMutableDictionary dictionary];
    for (SRSubRankModel *model in models) {
        NSLog(@"结果:%li---%li", model.score, model.songNum);
        SRSubRankModel *existModel = [userDict objectForKey:model.userId];
        if (!existModel) { // 新用户
            [userDict setObject:model forKey:model.userId];
        } else { // 已有该用户，累加分数
            existModel.score += model.score;
            existModel.lines += model.lines;
            existModel.songNum += model.songNum;
        }
    }
    
    NSMutableArray *finaModels = [NSMutableArray array];
    for(SRSubRankModel *model in [userDict allValues]){
        SRSubRankModel *newModel = model;
        newModel.score = (int)model.score / model.lines;
        [finaModels addObject:newModel];
    }
    return finaModels;
}

-(void)onKaraokeViewWithScore:(NSInteger)score totalScore:(NSInteger)totalScore lineScore:(NSInteger)lineScore lineIndex:(NSInteger)lineIndex {
    if(self.singRole == KTVSingRoleAudience){
        return;
    }

   [self sendMainSingerLineScoreToAudienceWith:score totalScore:totalScore lineScore:lineScore lineIndex:lineIndex];
   [self.statusView.lrcView updateScoreWith:lineScore cumulativeScore:score totalScore:totalScore];
}

#pragma mark - VLSRBottomViewDelegate
- (void)onVLSRBottomView:(VLSRBottomToolbar *)view
                btnTapped:(id)sender
               withValues:(VLSRBottomBtnClickType)typeValue {
    switch (typeValue) {
        case VLSRBottomBtnClickTypeMore:  //更多
//            [self popSelMVBgView];
            [self popSelMoreView];
            break;
        case VLSRBottomBtnClickTypeJoinChorus:
            [self popUpChooseSongView:YES];
            break;
        case VLSRBottomBtnClickTypeChoose:
            [self popUpChooseSongView:NO];
            break;
        case VLSRBottomBtnClickTypeAudio:
            if (self.isNowMicMuted) {
                [AgoraEntAuthorizedManager checkAudioAuthorizedWithParent:self completion:nil];
            }
            self.isNowMicMuted = !self.isNowMicMuted;
            [[AppContext srServiceImp] updateSeatAudioMuteStatusWith:self.isNowMicMuted
                                                                completion:^(NSError * error) {
            }];
            break;
        case VLSRBottomBtnClickTypeVideo:
            if (self.isNowCameraMuted) {
                [AgoraEntAuthorizedManager checkCameraAuthorizedWithParent:self completion:nil];
            }
            self.isNowCameraMuted = !self.isNowCameraMuted;
            [[AppContext srServiceImp] updateSeatVideoMuteStatusWith:self.isNowCameraMuted
                                                                completion:^(NSError * error) {
            }];
            break;
        default:
            break;
    }
}

#pragma mark - VLRoomPersonViewDelegate
- (void)onVLRoomPersonView:(VLSRMicSeatList *)view
   seatItemTappedWithModel:(VLSRRoomSeatModel *)model
                   atIndex:(NSInteger)seatIndex {
    if(VLUserCenter.user.ifMaster) {
        //is owner
        if(self.gameModel.status == SingRelayStatusStarted){
            if(seatIndex != 0){
                [VLToast toast:SRLocalizedString(@"sr_game_start_cant_make_down")];
            } else {
                [VLToast toast:SRLocalizedString(@"sr_game_start_cant_down")];
            }
            return;
        }
        if ([model.userNo isEqualToString:VLUserCenter.user.id]) {
            //self, return
            return;
        }
        if (model.userNo.length > 0) {
            return [self popDropLineViewWithSeatModel:model];
        }
    } else {
        
        if(self.gameModel.status == SingRelayStatusStarted){
            if([self isOnMicSeat]){
                [VLToast toast:SRLocalizedString(@"sr_game_start_cant_down")];
            } else {
                [VLToast toast:SRLocalizedString(@"sr_game_start_join_before")];
            }
            return;
        }
        
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

- (void)onVLRoomPersonView:(VLSRMicSeatList *)view onRenderVideo:(VLSRRoomSeatModel *)model inView:(UIView *)videoView atIndex:(NSInteger)seatIndex
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
-(void)onVLPopSelBgView:(VLSRPopSelBgView *)view
       tappedWithAction:(VLSRSelBgModel *)selBgModel
                atIndex:(NSInteger)index {
    SRChangeMVCoverInputModel* inputModel = [SRChangeMVCoverInputModel new];
//    inputModel.roomNo = self.roomModel.roomNo;
    inputModel.mvIndex = index;
//    inputModel.userNo = VLUserCenter.user.id;
    VL(weakSelf);
    [[AppContext srServiceImp] changeMVCoverWith:inputModel
                                             completion:^(NSError * error) {
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }
        
        [[LSTPopView getSRPopViewWithCustomView:view] dismiss];
        weakSelf.choosedBgModel = selBgModel;
    }];
}

#pragma mark VLPopChooseSongViewDelegate
- (void)chooseSongView:(VLSRPopSongList*)view tabbarDidClick:(NSUInteger)tabIndex {
    if (tabIndex != 1) {
        return;
    }
    
    [self refreshChoosedSongList:nil];
}

#pragma mark - VLChooseBelcantoViewDelegate
- (void)onVLChooseBelcantoView:(VLSRAudioEffectPicker *)view
                    itemTapped:(VLSRBelcantoModel *)model
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
- (void)onVLDropOnLineView:(VLSRDropOnLineView *)view action:(VLSRRoomSeatModel *)seatModel {
    [self leaveSeatWithSeatModel:seatModel withCompletion:^(NSError *error) {
        if(seatModel.userNo == VLUserCenter.user.id){
            self.statusView.state = SRStateAudienceWating;
        }

        [[LSTPopView getSRPopViewWithCustomView:view] dismiss];
    }];
}

#pragma mark VLTouristOnLineViewDelegate

- (void)reloadMusic{
    VLSRRoomSelSongModel* model = [[self selSongsArray] firstObject];
    KTVSongConfiguration* songConfig = [[KTVSongConfiguration alloc] init];
    songConfig.autoPlay = YES;
    songConfig.mode = KTVLoadMusicModeLoadLrcOnly;
    songConfig.mainSingerUid = [model.userNo integerValue];
    songConfig.songIdentifier = model.songNo;
    
    //self.MVView.loadingType = VLSRMVViewStateLoading;
  //  [self.MVView setBotViewHidden:true];
    VL(weakSelf);
    self.loadMusicCallBack = ^(BOOL isSuccess, NSInteger songCode) {
        if (!isSuccess) {
            return;
        }
       // [weakSelf.MVView updateMVPlayerState:VLSRMVViewActionTypeMVPlay];
    };
    
    [self.SRApi loadMusicWithSongCode:[model.songNo integerValue] config:songConfig onMusicLoadStateListener:self];
}

#pragma mark - VLSRSettingViewDelegate
- (void)settingViewSettingChanged:(VLSRSettingModel *)setting
              valueDidChangedType:(VLSRValueDidChangedType)type {
    if (type == VLSRValueDidChangedTypeEar) { // 耳返设置
        // 用户必须使用有线耳机才能听到耳返效果
        // 1、不在耳返中添加audiofilter
        // AgoraEarMonitoringFilterNone
        // 2: 在耳返中添加人声效果 audio filter。如果你实现了美声、音效等功能，用户可以在耳返中听到添加效果后的声音。
        // AgoraEarMonitoringFilterBuiltInAudioFilters
        // 4: 在耳返中添加降噪 audio filter。
        // AgoraEarMonitoringFilterNoiseSuppression
        // [self.RTCkit enableInEarMonitoring:setting.soundOn includeAudioFilters:AgoraEarMonitoringFilterBuiltInAudioFilters | AgoraEarMonitoringFilterNoiseSuppression];
        self.isEarOn = setting.soundOn;
    } else if (type == VLSRValueDidChangedTypeMV) { // MV
        
    } else if (type == VLSRValueDidChangedRiseFall) { // 升降调
        // 调整当前播放的媒体资源的音调
        // 按半音音阶调整本地播放的音乐文件的音调，默认值为 0，即不调整音调。取值范围为 [-12,12]，每相邻两个值的音高距离相差半音。取值的绝对值越大，音调升高或降低得越多
        NSInteger value = setting.toneValue * 2 - 12;
        [[self.SRApi getMusicPlayer] setAudioPitch:value];
    } else if (type == VLSRValueDidChangedTypeSound) { // 音量
        // 调节音频采集信号音量、取值范围为 [0,400]
        // 0、静音 100、默认原始音量 400、原始音量的4倍、自带溢出保护
        if(self.soundVolume != setting.soundValue * 100){
            [self.RTCkit adjustRecordingSignalVolume:setting.soundValue * 100];
            if(setting.soundOn) {
                [self.RTCkit setInEarMonitoringVolume:setting.soundValue * 100];
            }
            self.soundVolume = setting.soundValue * 100;
        }
    } else if (type == VLSRValueDidChangedTypeAcc) { // 伴奏
        int value = setting.accValue * 100;
        if(self.playoutVolume != value){
            self.playoutVolume = value;
        }
    } else if (type == VLSRValueDidChangedTypeListItem) {
        AgoraAudioEffectPreset preset = [self audioEffectPreset:setting.kindIndex];
        [self.RTCkit setAudioEffectPreset:preset];
    } else if (type == VLSRValueDidChangedTypeRemoteValue) {
//        [self.SRApi adjustChorusRemoteUserPlaybackVoulme:setting.remoteVolume];
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
//- (void)soundEffectItemClickAction:(VLSRSoundEffectType)effectType {
//    if (effectType == VLSRSoundEffectTypeHeFeng) {
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:3 param2:4];
//    } else if (effectType == VLSRSoundEffectTypeXiaoDiao){
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:2 param2:4];
//    } else if (effectType == VLSRSoundEffectTypeDaDiao){
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:1 param2:4];
//    } else if (effectType == VLSRSoundEffectTypeNone) {
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:0 param2:4];
//    }
//    SRLogInfo(@"Agora - Setting effect type to %lu", effectType);
//}


#pragma mark --
- (void)_fetchServiceAllData {
    //请求已点歌曲
    VL(weakSelf);
    [self refreshChoosedSongList:^{
        //请求歌词和歌曲
      //  [weakSelf loadAndPlaySong];
    }];
}

#pragma mark - getter/handy utils
- (BOOL)isCurrentSongMainSinger:(NSString *)userNo {
    VLSRRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    return [selSongModel.userNo isEqualToString:userNo];
}

- (BOOL)isRoomOwner {
    return [self.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
}

- (BOOL)isBroadcaster {
    return [self isRoomOwner] || self.isOnMicSeat;
}

- (VLSRRoomSelSongModel*)selSongWithSongNo:(NSString*)songNo {
    __block VLSRRoomSelSongModel* song = nil;
    [self.selSongsArray enumerateObjectsUsingBlock:^(VLSRRoomSelSongModel * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([obj.songNo isEqualToString:songNo]) {
            song = obj;
            *stop = YES;
        }
    }];
    
    return song;
}

/// 获取当前用户的麦位
- (VLSRRoomSeatModel*)getCurrentUserSeatInfo {
    for (VLSRRoomSeatModel *model in self.seatsArray) {
        if ([model.userNo isEqualToString:VLUserCenter.user.id]) {
            return model;
        }
    }
    
    return nil;
}

/// 根据麦位索引获取麦位
/// @param seatIndex <#seatIndex description#>
- (VLSRRoomSeatModel*)getUserSeatInfoWithIndex:(NSUInteger)seatIndex {
    for (VLSRRoomSeatModel *model in self.seatsArray) {
        if (model.seatIndex == seatIndex) {
            return model;
        }
    }
    
    return nil;
}

/// 计算当前歌曲用户的演唱角色
- (KTVSingRole)getUserSingRole {
    VLSRRoomSelSongModel* songModel = [[self selSongsArray] firstObject];
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
    VLSRRoomSelSongModel* topSong = [self.selSongsArray firstObject];
    for(VLSRRoomSeatModel* seat in seatArray) {
        //TODO: validate songCode
        if([seat.chorusSongCode isEqualToString:[topSong chorusSongId]]) {
            chorusNum += 1;
        }
       // else if ([seat.chorusSongCode length] > 0) {
//            SRLogError(@"calc seat chorus status fail! chorusSongCode: %@, playSongCode: %@", seat.chorusSongCode, topSong.songNo);
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
    
    VLSRRoomSelSongModel* topSong = [[self selSongsArray] firstObject];
    //TODO: 不在播放不允许加入
    if (topSong.status != VLSRSongPlayStatusPlaying) {
        return NO;
    }
    
    return YES;
}

//获取已经非麦下观众的总数
-(NSInteger)getOnMicUserCount{
    NSInteger num = 0;
    if(self.seatsArray){
        for(VLSRRoomSeatModel *model in self.seatsArray){
            if(model.rtcUid){
                num++;
            }
        }
    }
    return num;
}

#pragma mark - setter
- (void)setSRApi:(KTVApiImpl *)SRApi {
    _SRApi = SRApi;
    [[AppContext shared] setSrAPI:SRApi];
}

- (void)setRoomUsersCount:(NSUInteger)userCount {
    self.roomModel.roomPeopleNum = [NSString stringWithFormat:@"%ld", userCount];
    self.topView.listModel = self.roomModel;
}

- (void)setChoosedBgModel:(VLSRSelBgModel *)choosedBgModel {
    _choosedBgModel = choosedBgModel;
   // [self.MVView changeBgViewByModel:choosedBgModel];
}

- (void)setSeatsArray:(NSArray<VLSRRoomSeatModel *> *)seatsArray {
    //判断需要显示谁为主唱
    _seatsArray = seatsArray;

    //update booleans
    self.isOnMicSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
    [self updateMediaOptionWithStatus];
    
    self.roomPersonView.roomSeatsArray = self.seatsArray;
    [self.roomPersonView updateSingBtnWithChoosedSongArray:_selSongsArray];
    self.chorusNum = [self getChorusNumWithSeatArray:seatsArray];
    [self onSeatFull];
    
}

-(void)updateMediaOptionWithStatus{
    AgoraRtcChannelMediaOptions *options = [[AgoraRtcChannelMediaOptions alloc]init];
    options.publishCameraTrack = self.isOnMicSeat;
    options.clientRoleType = self.isOnMicSeat ? AgoraClientRoleBroadcaster : AgoraClientRoleAudience;
}

-(void)setGameModel:(SingRelayModel *)gameModel {
    _gameModel = gameModel;
    kWeakSelf(self);
    if(gameModel.status == SingRelayStatusWaiting){
        /**
         1.房主是选歌状态
         2.观众是等待状态
         */
        if(![self isRoomOwner]){
            [self reNewAllData];
        }
        self.statusView.state = [self isRoomOwner] ? SRStateOwnerOrderMusic : SRStateAudienceWating;
    } else if(gameModel.status == SingRelayStatusStarted){
        /**
         1.嗨唱开始
         2.所有人开始倒计时
         3.倒计时结束后，所有人开始抢唱
         4.抢唱时间为10S，时间内有人抢唱则变成抢唱中
         5.无人抢唱显示无人抢唱状态
         */
        [self updateSBGUI];
        //关闭卖位 等待解锁
        self.isNowMicMuted = ![self isRoomOwner];
        [_bottomView setAudioBtnEnabled:false];
        [[NSUserDefaults standardUserDefaults] setObject:@(0) forKey:@"MICOWNERINDEX"];
        [[AppContext srServiceImp] updateSeatAudioMuteStatusWith:self.isNowMicMuted
                                                            completion:^(NSError * error) {
        }];
    } else if(gameModel.status == SingRelayStatusEnded){
        [[NSUserDefaults standardUserDefaults] setObject:nil forKey:@"MICOWNERINDEX"];
        [[NSUserDefaults standardUserDefaults] synchronize];
        [_bottomView setAudioBtnEnabled:true];
        self.chooseArray = [NSMutableArray arrayWithObjects:@(NO), @(NO), @(NO), @(NO), @(NO), nil];
        self.currentUserNo = self.seatsArray.firstObject.userNo;
        self.nextWinNo = nil;
        self.segmentScore = 0;
        self.segmentCount = 0;
        self.sumScore = 0;
        self.cosingerLoadCount = 0;
        self.MainSingerPlayFlag = false;
        self.hasCountDown = false;
        self.currentIndex = 1;
        [self.SRApi stopSing];
        [self.statusView resetLrcView];
        [self.SRApi switchSingerRoleWithNewRole:KTVSingRoleAudience onSwitchRoleState:^(KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {

        }];
        
        NSMutableArray *res = [NSMutableArray array];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.statusView hideNextMicOwner];
            NSDictionary *subDict = gameModel.rank;
            NSMutableArray *res = [[NSMutableArray alloc] init]; // 确保res已经初始化

            for (NSString *key in subDict) {
                NSDictionary *value = subDict[key];
                if (![value isKindOfClass:[NSDictionary class]]) {
                    continue;
                }

                NSLog(@"Key: %@, Value: %@", key, value);
                NSString *image = value[@"poster"];
                NSString *name = value[@"userName"];

                int score = [value[@"score"] isKindOfClass:[NSNumber class]] ? [value[@"score"] intValue] : 0;
                int songNum = [value[@"songNum"] isKindOfClass:[NSNumber class]] ? [value[@"songNum"] intValue] : 0;
                
                VLSRRoomSelSongModel *topSong = self.selSongsArray.firstObject;
                SRSubRankModel *model = [[SRSubRankModel alloc] init];
                model.userName = name;
                model.poster = image;
                model.score = score;
                model.songNum = songNum;
                model.userId = key;
                [res addObject:model];
            }
         //   NSArray *mergeModels = mergeSRModelsWithSameUserIds(res);
            self.statusView.dataSource = sortSRModels(res, NO);
            self.statusView.state = [self isRoomOwner] ? SRStateResultOwner : SRStateResultAudience;
            [self.roomPersonView.personCollectionView reloadData];
            [self.scoreArray removeAllObjects];
        });

    }
}

// 组合：合并相同 user id 的模型 -> 按分数排序 -> 分配索引
NSArray<SRSubRankModel *> *sortSRModels(NSArray<SRSubRankModel *> *models, BOOL ascending) {
    NSArray<SRSubRankModel *> *sortedByCountModels = sortSRModelsByCountAndScore(models, ascending);
    NSArray<SRSubRankModel *> *resultModels = assignIndexesToSRModelsInArray(sortedByCountModels);
    return resultModels;
}

NSArray<SRSubRankModel *> *sortSRModelsByCountAndScore(NSArray<SRSubRankModel *> *models, BOOL ascending) {
    NSSortDescriptor *countSorter = [NSSortDescriptor sortDescriptorWithKey:@"songNum" ascending:ascending];
    NSSortDescriptor *scoreSorter = [NSSortDescriptor sortDescriptorWithKey:@"score" ascending:ascending];
    NSArray *sortedArray = [models sortedArrayUsingDescriptors:@[scoreSorter, countSorter]];
    
    return sortedArray;
}

// 对数组中每个元素进行索引赋值，并返回结果数组
NSArray<SRSubRankModel *> *assignIndexesToSRModelsInArray(NSArray<SRSubRankModel *> *array) {
    if (!array || array.count == 0) return nil;
    NSMutableArray<SRSubRankModel *> *resultArray = [NSMutableArray arrayWithCapacity:array.count];
    for (NSInteger i = 0; i < array.count; i++) {
        SRSubRankModel *model = array[i];
        model.index = i + 1; // 索引从1开始计算
        [resultArray addObject:model];
    }
    return resultArray;
}

- (void)updateSBGUI {
    NSLog(@"loadAndPlaySRSongupdateSBGUI");
    int count = 5;
   // if(!self.hasReady){
        for (NSInteger i = count; i >= 0; i--) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)((count - i) * 1.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                // 在此处更新 UI
                NSLog(@"count:%li", (long)i);
                //self.hasReady = true;
                self.currentUserNo = self.seatsArray.firstObject.userNo;
                if(i >= 0){
                    if(i == 5){
                        self.statusView.state = ([self isRoomOwner] || self.isOnMicSeat) ? SRStateOwnerPrepare : SRStateAudiencePrepare;
                    } else {
                        if(i > 1){
                            self.statusView.contentStr = [NSString stringWithFormat:@"%li", i - 1];
                        } else if (i == 1) {
                            self.statusView.contentStr = @"Go";
                        } else {
                            NSLog(@"loadAndPlaySRSong%li", i);
                            if(!self.MainSingerPlayFlag){
                                [self loadAndPlaySRSong];
                            }
                            [self updateWatingUI];
                        }
                    }
                }
            });
        }
//    } else {
//        [self loadAndPlaySRSong];
//        [self updateWatingUI];
//    }
}

-(void)updateWatingUI{
    self.statusView.state = SRStatePlayerUnsredAndPlaying;
    [self.statusView hideSRBtn];
}


-(void)onSeatFull{
    if(self.singRole != KTVSingRoleAudience){
        return;
    }
    NSInteger count = [self getOnMicUserCount];
    if(!_isOnMicSeat && count >=8){
      //  self.MVView.joinCoSingerState = SRJoinCoSingerStateIdle;
    } else {
        if(!self.isJoinChorus){
           // self.MVView.joinCoSingerState = SRJoinCoSingerStateWaitingForJoin;
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
        SRLogInfo(@"seat array update chorusNum %ld->%ld", origChorusNum, chorusNum);
        //lead singer <-> solo
        KTVSingRole role = [self getUserSingRole];
        [self.SRApi switchSingerRoleWithNewRole:role
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
    
    VLSRRoomSeatModel* info = [self getCurrentUserSeatInfo];
    self.isNowMicMuted = info.isAudioMuted;
    self.isNowCameraMuted = info.isVideoMuted;
    
    self.bottomView.hidden = !_isOnMicSeat;
    
    self.requestOnLineView.hidden = !self.bottomView.hidden;
    
    if(![self isOnMicSeat]){
        [self.requestOnLineView setTipHidden:self.gameModel.status == SingRelayStatusStarted];
    }
}

- (void)setIsNowMicMuted:(BOOL)isNowMicMuted {
    BOOL oldValue = _isNowMicMuted;
    _isNowMicMuted = isNowMicMuted;
    [self.SRApi muteMicWithMuteStatus:isNowMicMuted];
    if([self isRoomOwner]){
        [self.RTCkit adjustRecordingSignalVolume:isNowMicMuted ? 0 : 100];
    } else {
        [self.RTCkit muteLocalAudioStream:isNowMicMuted];
    }
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

    [self.settingView setIsEarOn:isEarOn];
}

- (void)setPlayoutVolume:(int)playoutVolume {
    _playoutVolume = playoutVolume;
    
    // 官方文档是100 ？ SDK 是 400？？？？
    // 调节本地播放音量 取值范围为 [0,100]
    // 0、无声。 100、（默认）媒体文件的原始播放音量
//    [self.SRApi adjustPlayoutVolume:playoutVolume];
    [[self.SRApi getMusicPlayer] adjustPlayoutVolume:playoutVolume];
    
    // 调节远端用户听到的音量 取值范围[0、400]
    // 100: （默认）媒体文件的原始音量。400: 原始音量的四倍（自带溢出保护）
    [[self.SRApi getMusicPlayer] adjustPublishSignalVolume:playoutVolume];
    
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

- (void)setSelSongsArray:(NSArray<VLSRRoomSelSongModel *> *)selSongsArray {
    NSArray<VLSRRoomSelSongModel*> *oldSongsArray = _selSongsArray;
    _selSongsArray = [NSMutableArray arrayWithArray:selSongsArray];
    self.chorusNum = [self getChorusNumWithSeatArray:self.seatsArray];
    if (self.chooseSongView) {
        self.chooseSongView.selSongsArray = _selSongsArray; //刷新已点歌曲UI
    }
    
    [self.roomPersonView updateSingBtnWithChoosedSongArray:_selSongsArray];
    VLSRRoomSelSongModel* originalTopSong = [oldSongsArray firstObject];
    VLSRRoomSelSongModel* updatedTopSong = [selSongsArray firstObject];
    SRLogInfo(@"setSelSongsArray current top: songName: %@, status: %ld",
               updatedTopSong.songName, updatedTopSong.status);
    SRLogInfo(@"setSelSongsArray orig top: songName: %@, status: %ld",
               originalTopSong.songName, originalTopSong.status);
    if(![updatedTopSong.songNo isEqualToString:originalTopSong.songNo]){
      //  [self.MVView reset];
        [self.lrcControl resetLrc];
        //song changes
        [self stopPlaySong];
        //[self loadAndPlaySong];
    }
}

- (void)setTrackMode:(KTVPlayerTrackMode)trackMode {
    SRLogInfo(@"setTrackMode: %ld", trackMode);
    _trackMode = trackMode;
    if([self isOnMicSeat]){
        if([self isRoomOwner]){
            [[self.SRApi getMusicPlayer] selectMultiAudioTrack:trackMode == KTVPlayerTrackModeAcc ? 1 : 0 publishTrackIndex: 1 ];
        } else {
            [[self.SRApi getMusicPlayer] selectAudioTrack:self.trackMode == KTVPlayerTrackModeOrigin ? 0 : 1];
        }
    }
    
    
   // [self.MVView setOriginBtnState: trackMode == SRPlayerTrackModeOrigin ? VLSRMVViewActionTypeSingOrigin : VLSRMVViewActionTypeSingAcc];
}

- (void)setSingRole:(KTVSingRole)singRole {
    _singRole = singRole;
    self.lrcControl.lrcView.lyricsView.draggable = false;
    self.lrcControl.isMainSinger = (_singRole == KTVSingRoleSoloSinger || _singRole == KTVSingRoleLeadSinger);
    SRLogInfo(@"setSingRole: %ld", singRole);
    
    VLSRRoomSelSongModel *song = self.selSongsArray.firstObject;
   // [self.MVView updateUIWithSong:song role:singRole];
    [self setCoSingerStateWith:singRole];

}

-(void)setCoSingerStateWith:(KTVSingRole)role {
    switch (role) {
        case KTVSingRoleSoloSinger:
        case KTVSingRoleLeadSinger: {
        //    self.MVView.joinCoSingerState = SRJoinCoSingerStateIdle;
        } break;
        case KTVSingRoleCoSinger: {
//        case SRSingRoleFollowSinger:
            //self.MVView.joinCoSingerState = SRJoinCoSingerStateWaitingForLeave;
        } break;
        case KTVSingRoleAudience:
        default: {
            //self.MVView.joinCoSingerState = SRJoinCoSingerStateWaitingForJoin;
            [self onSeatFull];
        } break;
    }
}

#pragma mark SRApiEventHandlerDelegate
- (void)onMusicPlayerStateChangedWithState:(AgoraMediaPlayerState)state error:(AgoraMediaPlayerError)error isLocal:(BOOL)isLocal {
    dispatch_async(dispatch_get_main_queue(), ^{
        if(state == AgoraMediaPlayerStatePlaying) {
            if(isLocal) {
                //track has to be selected after loaded
                self.trackMode = self.trackMode;
            }
            
        } else if(state == AgoraMediaPlayerStatePaused) {
           // [self.MVView updateMVPlayerState:VLSRMVViewActionTypeMVPause];
            [self.lrcControl hideSkipViewWithFlag:true];
        } else if(state == AgoraMediaPlayerStateStopped) {

        } else if(state == AgoraMediaPlayerStatePlayBackAllLoopsCompleted || state == AgoraMediaPlayerStatePlayBackCompleted) {
            if(isLocal) {
                SRLogInfo(@"Playback all loop completed");

                    if(self.singRole == KTVSingRoleLeadSinger || self.singRole == KTVSingRoleSoloSinger){
                        [self.SRApi stopSing];
                        [self removeCurrentSongWithSync:YES];
                        
                        self.gameModel.status = SingRelayStatusEnded;
                        self.gameModel.rank = [self convertScoreArrayToRank];
                        [[AppContext srServiceImp] innerUpdateSingRelayInfo:self.gameModel completion:^(NSError * error) {
                                        
                        }];
                    }
//                    if([self.currentUserNo isEqualToString: VLUserCenter.user.id]){
//                        NSLog(@"index:5, score:%li",self.segmentScore);
//                        SubRankModel *model = [[SubRankModel alloc]init];
//                        VLSRRoomSelSongModel *currentSong = self.selSongsArray.firstObject;
//                        model.userName = VLUserCenter.user.name;
//                        model.poster = currentSong.imageUrl;
//                        model.score = self.segmentScore;
//                        model.songNum = 1;
//                        model.userId = VLUserCenter.user.id;
//                        [self.scoreArray addObject:model];
//
//                        self.gameModel.status = SingRelayStatusEnded;
//                        self.gameModel.rank = [self convertScoreArrayToRank];
//                        [[AppContext srServiceImp] innerUpdateSingRelayInfo:self.gameModel completion:^(NSError * err) {
//
//                        }];
//                    }
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
        SRLogInfo(@"old role:%li is equal to new role", oldRole);
    }
    self.singRole = newRole;
}

- (void)onSingingScoreResultWithScore:(float)score {
}

- (void)onTokenPrivilegeWillExpire {
    
}

- (void)onMusicPlayerProgressChangedWith:(NSInteger)progress{
    if(!self.hasCountDown) {
        [self updateSBGCountDown];
        self.hasCountDown = true;
    }
}

#pragma mark SRMusicLoadStateListener

- (void)onMusicLoadProgressWithSongCode:(NSInteger)songCode
                                percent:(NSInteger)percent
                                 status:(AgoraMusicContentCenterPreloadStatus)status
                                    msg:(NSString *)msg
                               lyricUrl:(NSString *)lyricUrl {
    SRLogInfo(@"load: %li, %li", status, percent);
    dispatch_async_on_main_queue(^{
        [self.statusView updateLoadingViewWith:percent];
        if(status == AgoraMusicContentCenterPreloadStatusError){
            [VLToast toast:SRLocalizedString(@"sr_load_failed_and_change")];
//            [self.MVView setBotViewHidden:false];
//            self.MVView.loadingType = VLSRMVViewStateIdle;
            self.statusView.state = SRStateOwnerUnSredAndPlaying;
            return;
        }
        
        if (status == AgoraMusicContentCenterPreloadStatusOK){
           // self.MVView.loadingType = VLSRMVViewStateIdle;
        }
       // self.MVView.loadingProgress = percent;
    });
}

- (void)onMusicLoadFailWithSongCode:(NSInteger)songCode reason:(enum KTVLoadSongFailReason)reason{
    [self.statusView updateLoadingViewWith:100];
    dispatch_async_on_main_queue(^{
        if(self.loadMusicCallBack) {
            self.loadMusicCallBack(NO, songCode);
            self.loadMusicCallBack = nil;
        }
        if (reason == KTVLoadSongFailReasonNoLyricUrl) {
            
            self.statusView.retryBtn.hidden = false;
            [self.statusView bringSubviewToFront:self.statusView.retryBtn];
        } else {
            self.statusView.state = [self isRoomOwner] ? SRStateOwnerFailed : SRStatePlayerFailed;
           // self.MVView.loadingType = VLSRMVViewStateIdle;
//            if(reason == SRLoadSongFailReasonMusicPreloadFail){
//                if(self.retryCount < 3){
//                    self.retryCount++;
//                    [VLToast toast:@"歌曲加载失败"];
//                    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
//                    SRSongConfiguration* songConfig = [[SRSongConfiguration alloc] init];
//                    songConfig.autoPlay = YES;
//                    songConfig.songCode = [model.songNo integerValue];
//                    songConfig.mainSingerUid = [model.userNo integerValue];
//                    [self.SRApi loadMusicWithConfig:songConfig mode:SRLoadMusicModeLoadMusicAndLrc onMusicLoadStateListener:self];
//                } else {
//                    [VLToast toast:@"已尝试3次，请自动切歌"];
//                }
//            }
        }
        SRLogInfo(@"onMusicLoadFail songCode: %ld error: %ld", songCode, reason);
    });
}

- (void)onMusicLoadSuccessWithSongCode:(NSInteger)songCode lyricUrl:(NSString * _Nonnull)lyricUrl {
    [self.statusView updateLoadingViewWith:100];
    self.trackMode = KTVPlayerTrackModeAcc;//切换为伴奏
    
    dispatch_async_on_main_queue(^{
        if(self.loadMusicCallBack){
            self.loadMusicCallBack(YES, songCode);
            self.loadMusicCallBack = nil;
        }
        
        if(lyricUrl.length > 0){
            SRLogInfo(@"onMusicLoadSuccessWithSongCode: %ld", self.singRole);
        }
        self.retryCount = 0;
        //        VLSRRoomSelSongModel *model = self.selSongsArray.firstObject;
        //        if([model.winnerNo isEqualToString:@""] || model.winnerNo == nil){
        //            NSLog(@"加载成功的歌曲为:%@---%@", model.songName, model.winnerNo);
        //            //如果是主唱歌曲加载成功 发送ds告诉观众同步进度
        if([self isRoomOwner]){
            self.MainSingerPlayFlag = true;
            NSLog(@"coCount:%li---%li", [self getOnMicUserCount], self.cosingerLoadCount);
            if(self.cosingerLoadCount == [self getOnMicUserCount] - 1){
                VLSRRoomSelSongModel *model = self.selSongsArray.firstObject;
                [self.SRApi switchSingerRoleWithNewRole:KTVSingRoleLeadSinger
                                          onSwitchRoleState:^( KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
//                    if(state != SRSwitchRoleStateSuccess) {
//                        SRLogError(@"switchSingerRole error: %ld", reason);
//                        return;
//                    }
                    [self.SRApi startSingWithSongCode:[model.songNo integerValue] startPos:0];
                }];
            }
        } else if(self.singRole == KTVSingRoleCoSinger) {
            NSDictionary *dict = @{
                @"cmd":@"CoSingerLoadSuccess"
            };
           [self sendStreamMessageWithDict:dict success:nil];
            //如果是合唱 需要swithRole
            if([self isOnMicSeat] && ![self isRoomOwner]){
                [self.SRApi switchSingerRoleWithNewRole:KTVSingRoleCoSinger onSwitchRoleState:^( KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
                    if(state != KTVSwitchRoleStateSuccess) {
                        SRLogError(@"switchSingerRole error: %ld", reason);
                        return;
                    }
                }];
            }
        }
        
        if(self.singRole == KTVSingRoleAudience){
            self.statusView.state = SRStateAudiencePlaying;
        }
        self.statusView.numStr = @"1/5";
       // }
    });
}

-(void)updateSBGCountDown {
    int count = 3;
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        for (int i = count; i >= 0; i--) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)((count - i) * 1.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                if(self.gameModel.status == SingRelayStatusEnded){
                    return;
                }
                // 在此处更新 UI
                if (i == 0) {
                    NSLog(@"%@", [NSString stringWithFormat:@"compare:%@--%@", self.seatsArray.firstObject.userNo, VLUserCenter.user.id]);
                    if([self isOnMicSeat]){
                        if([self isRoomOwner]){
                            if([self.currentUserNo isEqualToString:VLUserCenter.user.id]){
                                self.statusView.state = SRStateOwnerSredAndPlaying;
                            } else {
                                self.statusView.state = SRStateOwnerUnSredAndPlaying;
                            }
                        } else {
                            if([self.currentUserNo isEqualToString:VLUserCenter.user.id]){
                                self.statusView.state = SRStatePlayerSredAndPlaying;
                            } else {
                                self.statusView.state = SRStatePlayerUnsredAndPlaying;
                            }
                        }
                    }
                } else {
                    if([self isOnMicSeat]){
                        if([self isRoomOwner]){
                            if([self.currentUserNo isEqualToString:VLUserCenter.user.id]){
                                self.statusView.state = SRStateTimedownOwnersred;
                            } else {
                                self.statusView.state = SRStateTimedownOwnerunsred;
                            }
                        } else {
                            if([self.currentUserNo isEqualToString:VLUserCenter.user.id]){
                                self.statusView.state = SRStateTimedownPlayersred;
                            } else {
                                self.statusView.state = SRStateTimedownPlayerunsred;
                            }
                        }
                    } else {
                        self.statusView.state = SRStateAudiencePlaying;
                    }
                    self.statusView.countTime = i;
                }
            });
        }
    });
}

-(NSMutableArray *)getChooseSongArray{
    
    NSString *bundlePath = [[NSBundle mainBundle] pathForResource:@"SRResource" ofType:@"bundle"];
    NSBundle *yourBundle = [NSBundle bundleWithPath:bundlePath];

    NSString *filePath = [yourBundle pathForResource:@"SRMusicSource" ofType:@"json"];
    NSData *jsonData = [NSData dataWithContentsOfFile:filePath];
    NSMutableArray<SRChooseSongInputModel *> *models = [NSMutableArray array];
    if (jsonData) {
        NSError *error;
        NSArray *jsonArray = [NSJSONSerialization JSONObjectWithData:jsonData options:kNilOptions error:&error];
        
        if (jsonArray && [jsonArray isKindOfClass:[NSArray class]]) {

            for (NSDictionary *dict in jsonArray) {
                SRChooseSongInputModel *model = [[SRChooseSongInputModel alloc] init];
                model.isChorus = [dict[@"isChorus"] boolValue];
                model.songName = dict[@"songName"];
                model.songNo = dict[@"songNo"];
                model.singer = dict[@"singer"];
                model.imageUrl = dict[@"imageUrl"];
                model.playCounts = dict[@"playCounts"];
                
                [models addObject:model];
            }
        } else {
            NSLog(@"Invalid JSON format.");
        }
    } else {
           NSLog(@"Failed to read JSON file.");
    }
    
    return models;
}

-(SRChooseSongInputModel *)getRandomSongModel{
    NSArray *array = [self getChooseSongArray];
    int index = arc4random() % array.count;
    return array[index];
}
@end



