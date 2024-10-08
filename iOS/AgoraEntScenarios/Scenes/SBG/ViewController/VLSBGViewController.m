//
//  VLSBGViewController.m
//  VoiceOnLine
//

#import "VLSBGViewController.h"
#import "VLSBGTopView.h"
#import "VLSBGMicSeatList.h"
#import "VLSBGBottomToolbar.h"
#import "VLSBGAudienceIndicator.h"
#import "VLSBGOnLineListVC.h"

#import "VLSBGSettingView.h"
//model
#import "VLSBGSongItmModel.h"
#import "VLSBGRoomListModel.h"
#import "VLSBGRoomSeatModel.h"
#import "VLSBGRoomSelSongModel.h"
#import "VLSBGSelBgModel.h"
#import "UIViewController+VL.h"
#import "VLSBGPopScoreView.h"
#import "VLUserCenter.h"
#import "VLMacroDefine.h"
#import "VLGlobalHelper.h"
#import "VLURLPathConfig.h"
#import "VLToast.h"
#import "UIView+VL.h"
#import "AppContext+SBG.h"
#import "SBGMacro.h"
#import "LSTPopView+SBGModal.h"
//#import "HWWeakTimer.h"
#import "VLAlert.h"
#import "VLKTVAlert.h"
#import "SBGDebugManager.h"
#import "VLSBGVoicePerShowView.h"
#import "SBGDebugInfo.h"
@import AgoraRtcKit;
@import AgoraLyricsScore;
@import YYCategories;
@import SDWebImage;


NSInteger SBGApiStreamId = -1;
NSInteger SBGStreamId = -1;

@interface VLSBGViewController ()<
VLSBGTopViewDelegate,
VLSBGMicSeatListDelegate,
VLSBGBottomToolbarDelegate,
VLSBGPopSelBgViewDelegate,
VLSBGPopMoreSelViewDelegate,
VLSBGDropOnLineViewDelegate,
VLSBGAudienceIndicatorDelegate,
VLSBGAudioEffectPickerDelegate,
VLSBGPopSongListDelegate,
VLSBGSettingViewDelegate,
VLSBGBadNetWorkViewDelegate,
AgoraRtcMediaPlayerDelegate,
AgoraRtcEngineDelegate,
VLSBGPopScoreViewDelegate,
SBGLrcControlDelegate,
KTVApiEventHandlerDelegate,
IMusicLoadStateListener,
VLSBGVoicePerShowViewDelegate,
VLSBGStatusViewDelegate,
VLSBGLrcViewDelegate,
VLSBGDebugViewDelegate
>

typedef void (^CompletionBlock)(BOOL isSuccess, NSInteger songCode);
typedef void (^CountDownBlock)(NSTimeInterval leftTimeInterval);
@property (nonatomic, strong) VLSBGSelBgModel *choosedBgModel;
@property (nonatomic, strong) VLSBGBottomToolbar *bottomView;
@property (nonatomic, strong) VLSBGBelcantoModel *selBelcantoModel;
@property (nonatomic, strong) VLSBGTopView *topView;
@property (nonatomic, strong) VLSBGSettingView *settingView;
@property (nonatomic, strong) VLSBGMicSeatList *roomPersonView; //房间麦位视图
@property (nonatomic, strong) VLSBGAudienceIndicator *requestOnLineView;//空位上麦
@property (nonatomic, strong) VLSBGPopSongList *chooseSongView; //点歌视图
@property (nonatomic, strong) VLSBGVoicePerShowView *voicePerShowView; //专业主播
@property (nonatomic, strong) VLSBGSongItmModel *choosedSongModel; //点的歌曲
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;

@property (nonatomic, strong) VLSBGPopScoreView *scoreView;

@property (nonatomic, assign) BOOL isNowMicMuted;
@property (nonatomic, assign) BOOL isNowCameraMuted;
@property (nonatomic, assign) BOOL isOnMicSeat;
//@property (nonatomic, assign) NSUInteger chorusNum;    //合唱人数
@property (nonatomic, assign) KTVSingRole singRole;    //角色
@property (nonatomic, assign) BOOL isEarOn;
@property (nonatomic, assign) int playoutVolume;
@property (nonatomic, assign) KTVPlayerTrackMode trackMode;  //合唱/伴奏

@property (nonatomic, strong) NSArray <VLSBGRoomSelSongModel*>* selSongsArray;
@property (nonatomic, strong) KTVApiImpl* SBGApi;

@property (nonatomic, strong) LyricModel *lyricModel;
@property (nonatomic, strong) SBGLrcControl *lrcControl;
@property (nonatomic, copy, nullable) CompletionBlock loadMusicCallBack;
@property (nonatomic, assign) NSInteger selectedEffectIndex;
@property (nonatomic, assign) NSInteger selectedVoiceShowIndex;
@property (nonatomic, assign) BOOL isProfessional;
@property (nonatomic, assign) BOOL isPause;
@property (nonatomic, assign) NSInteger retryCount;
@property (nonatomic, assign) BOOL isJoinChorus;
@property (nonatomic, assign) NSInteger coSingerDegree;
@property (nonatomic, assign) NSInteger currentSelectEffect;
@property (nonatomic, assign) BOOL isHighlightSinger;
@property (nonatomic, strong) VLSBGStatusView *statusView;
@property (nonatomic, strong) SingBattleGameModel *gameModel;
@property (nonatomic, assign) BOOL hasShowReady;
@property (nonatomic, strong) NSMutableArray *scoreArray;//保存分数信息
@property (nonatomic, assign) NSInteger maxCount;//此次选歌的最大数
@property (nonatomic, strong) VLSBGRoomSelSongModel *currentSelSong;
@property (nonatomic, assign) BOOL isWatingSbg;
@property (nonatomic, assign) NSInteger totalCount;
@property (nonatomic, assign) NSInteger hasPlayedCount;
@property (nonatomic, assign) BOOL isDumpMode;

@end

@implementation VLSBGViewController

#pragma mark view lifecycles
- (void)dealloc {
    NSLog(@"dealloc:%s",__FUNCTION__);
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = UIColor.blackColor;
    
    self.totalCount = -1;
    
    [self subscribeServiceEvent];
    
    // setup view
    [self setBackgroundImage:@"bg-main" bundleName:@"sbgResource"];
    
//    UIView *bgView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)];
//    bgView.backgroundColor = UIColorMakeWithRGBA(0, 0, 0, 0.6);
//    [self.view addSubview:bgView];
    //头部视图
    VLSBGTopView *topView = [[VLSBGTopView alloc]initWithFrame:CGRectMake(0, kStatusBarHeight, SCREEN_WIDTH, 60) withDelegate:self];
    [self.view addSubview:topView];
    self.topView = topView;
    topView.listModel = self.roomModel;
    
    //底部按钮视图
    VLSBGBottomToolbar *bottomView = [[VLSBGBottomToolbar alloc] initWithFrame:CGRectMake(0, SCREEN_HEIGHT-64-kSafeAreaBottomHeight, SCREEN_WIDTH, 64) withDelegate:self withRoomNo:self.roomModel.roomNo withData:self.seatsArray];
    bottomView.backgroundColor = [UIColor clearColor];
    self.bottomView = bottomView;
    [self.view addSubview:bottomView];
    
    //去掉首尾的高度
    CGFloat musicHeight = SCREEN_HEIGHT -64 - kSafeAreaBottomHeight - kStatusBarHeight - 60 - 20;
    
    //MV视图(显示歌词...)
    CGFloat mvViewTop = topView.bottom;
    self.statusView = [[VLSBGStatusView alloc]initWithFrame:CGRectMake(0, mvViewTop, SCREEN_WIDTH, musicHeight * 0.5)];
    self.statusView.state = [self isRoomOwner] ? SBGStateOwnerOrderMusic : SBGStateAudienceWating;
    self.statusView.delegate = self;
    self.statusView.lrcView.delegate = self;
    [self.view addSubview:self.statusView];
    
    //房间麦位视图
    VLSBGMicSeatList *personView = [[VLSBGMicSeatList alloc] initWithFrame:CGRectMake(0, self.statusView.bottom + 20, SCREEN_WIDTH, musicHeight * 0.5) withDelegate:self withRTCkit:self.RTCkit];
    self.roomPersonView = personView;
    self.roomPersonView.roomSeatsArray = self.seatsArray;
    [self.view addSubview:personView];

    //空位上麦视图
    VLSBGAudienceIndicator *requestOnLineView = [[VLSBGAudienceIndicator alloc]initWithFrame:CGRectMake(0, SCREEN_HEIGHT-kSafeAreaBottomHeight-56-VLREALVALUE_WIDTH(30), SCREEN_WIDTH, 56) withDelegate:self];
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
    [self.topView addGestureRecognizer:[SBGDebugManager createStartGesture]];
    
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
    SBGLogInfo(@"Agora - destroy RTCEngine");
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

-(void)showDebug {
    [LSTPopView popSBGDebugViewWithParentView:self.view channelName:self.roomModel.roomNo sdkVer:[AgoraRtcEngineKit getSdkVersion]  isDebugMode:self.isDumpMode withDelegate:self];
}

#pragma mark service handler
- (void)subscribeServiceEvent {
    VL(weakSelf);
    [[AppContext sbgServiceImp] unsubscribeAll];
    [[AppContext sbgServiceImp] subscribeUserListCountChangedWithBlock:^(NSUInteger count) {
        //TODO
        [weakSelf setRoomUsersCount:count];
    }];
    
    /**
     1.任何人进入房间都需要查询抢唱
     2.如果是房主查询不到状态，房主需要创建抢唱状态
     */
    [[AppContext sbgServiceImp] innerGetSingBattleGameInfo:^(NSError *error, SingBattleGameModel *model) {
        if(error == nil){
            if(model.status == SingBattleGameStatusStarted){
                if(self.gameModel.status == SingBattleGameStatusStarted){
                    return;
                }
                NSString *mes = SBGLocalizedString(@"sbg_game_isOn");
                [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sbg_sceneImageWithName:@"empty"] message:mes buttonTitle:SBGLocalizedString(@"sbg_confirm") completion:^(bool flag, NSString * _Nullable text) {
                    [[VLKTVAlert shared]dismiss];
                    [weakSelf leaveRoom];
                }];
                return;
            }
            
            if(model){
                weakSelf.gameModel = model;
            } else {
                if([weakSelf isRoomOwner]){
                    SingBattleGameModel *model = [[SingBattleGameModel alloc]init];
                    model.status = SingBattleGameStatusWaiting;
                    [[AppContext sbgServiceImp] innerAddSingBattleGameInfo:model completion:^(NSError * _Nullable err) {
                        if(error) {
                            SBGLogInfo(@"owner add sbg state error");
                        } else {
                            weakSelf.gameModel = model;
                        }
                    }];
                }
            }
        } else {
            
        }
    }];

    [[AppContext sbgServiceImp] subscribeSeatListChangedWithBlock:^(SBGSubscribe status, VLSBGRoomSeatModel* seatModel) {
        VLSBGRoomSeatModel* model = [self getUserSeatInfoWithIndex:seatModel.seatIndex];
        if (model == nil) {
            NSAssert(NO, @"model == nil");
            return;
        }
        
        if (status == SBGSubscribeCreated || status == SBGSubscribeUpdated) {
            //上麦消息 / 是否打开视频 / 是否静音
            [model resetWithInfo:seatModel];
            [weakSelf setSeatsArray:weakSelf.seatsArray];
        } else if (status == SBGSubscribeDeleted) {
            // 下麦消息
            
            // 下麦重置占位模型
            [model resetWithInfo:nil];
            [weakSelf setSeatsArray:weakSelf.seatsArray];
        }
        
        VLSBGRoomSelSongModel *song = weakSelf.selSongsArray.firstObject;
       // [weakSelf.MVView updateUIWithSong:song role:weakSelf.singRole];
        [weakSelf.roomPersonView reloadSeatIndex:model.seatIndex];
        VLSBGRoomSeatModel *owner = self.seatsArray.firstObject;
        NSString *msg = [NSString stringWithFormat:@"owner:%@---%@, updater:%@---%@", owner.name, owner.headUrl, seatModel.name, seatModel.headUrl];
        SBGLogInfo(@"%@", msg);
        [weakSelf onSeatFull];

    }];
    
    [[AppContext sbgServiceImp] subscribeRoomStatusChangedWithBlock:^(SBGSubscribe status, VLSBGRoomListModel * roomInfo) {
        if (SBGSubscribeUpdated == status) {
            //切换背景
            
            //mv bg / room member count did changed
            VLSBGSelBgModel* selBgModel = [VLSBGSelBgModel new];
            selBgModel.imageName = [NSString stringWithFormat:@"ktv_mvbg%ld", roomInfo.bgOption];
            selBgModel.isSelect = YES;
            weakSelf.choosedBgModel = selBgModel;
        } else if (status == SBGSubscribeDeleted) {
            //房主关闭房间
            if ([roomInfo.creatorNo isEqualToString:VLUserCenter.user.id]) {
                NSString *mes = @"sbg_room_exit";
                [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sbg_sceneImageWithName:@"empty"] message:mes buttonTitle:SBGLocalizedString(@"sbg_confirm") completion:^(bool flag, NSString * _Nullable text) {
                    [[VLKTVAlert shared]dismiss];
                    [weakSelf leaveRoom];
                }];
                return;
            }
            
            [weakSelf popForceLeaveRoom];
        }
    }];
    
    //callback if choose song list did changed
    [[AppContext sbgServiceImp] subscribeChooseSongChangedWithBlock:^(SBGSubscribe status, VLSBGRoomSelSongModel * songInfo, NSArray<VLSBGRoomSelSongModel*>* songArray) {
        // update in-ear monitoring
        [weakSelf _checkInEarMonitoring];
        
        if (SBGSubscribeDeleted == status) {
            BOOL success = [weakSelf removeSelSongWithSongNo:[songInfo.songNo integerValue] sync:NO];
            if (!success) {
                weakSelf.selSongsArray = songArray;
                SBGLogInfo(@"removeSelSongWithSongNo fail, reload it");
            }
            //歌曲删除关闭耳返
            weakSelf.isEarOn = false;
            [self.RTCkit enableInEarMonitoring:false includeAudioFilters:AgoraEarMonitoringFilterNone];
            SBGLogInfo(@"%@ has deleted", songInfo.songName);
        } else {
            VLSBGRoomSelSongModel* song = [weakSelf selSongWithSongNo:songInfo.songNo];
            SBGLogInfo(@"update:%@---%lu", songInfo.winnerNo, (unsigned long)status);
            if(([songInfo.winnerNo length] > 0 || songInfo.winnerNo != nil) && status == SBGSubscribeUpdated ){
                [weakSelf dealWithSbgEventWithUserNo:songInfo];
            }
            
            //add new song
            SBGLogInfo(@"song did updated: %@ status: %ld", song.name, songInfo.status);
            weakSelf.selSongsArray = [NSMutableArray arrayWithArray:songArray];
        }
    }];
    
    [[AppContext sbgServiceImp] subscribeNetworkStatusChangedWithBlock:^(SBGServiceNetworkStatus status) {
        if (status != SBGServiceNetworkStatusOpen) {
//            [VLToast toast:[NSString stringWithFormat:@"network changed: %ld", status]];
            return;
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            // 在主线程中执行的代码
            [weakSelf subscribeServiceEvent];
        });
        
       // [weakSelf _fetchServiceAllData];
    }];
    
    [[AppContext sbgServiceImp] subscribeRoomWillExpire:^{
        bool isOwner = [weakSelf.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
        NSString *mes = isOwner ? SBGLocalizedString(@"sbg_room_timeout") : SBGLocalizedString(@"sbg_room_offline");
        [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sbg_sceneImageWithName:@"empty"] message:mes buttonTitle:SBGLocalizedString(@"sbg_confirm") completion:^(bool flag, NSString * _Nullable text) {
            [[VLKTVAlert shared]dismiss];
            [weakSelf leaveRoom];
        }];
    }];
    
    [[AppContext sbgServiceImp] innerSubscribeSingBattleGameInfoWithCompletion:^(SBGSubscribe status, SingBattleGameModel * model, NSError * error) {
        if(error == nil){
            weakSelf.gameModel = model;
            if(![weakSelf isOnMicSeat]){
                [weakSelf.requestOnLineView setTipHidden:model.status == SingBattleGameStatusStarted];
            }
        }
    }];
}

-(void)dealWithSbgEventWithUserNo:(VLSBGRoomSelSongModel *)model{
    if([VLUserCenter.user.id isEqualToString:model.winnerNo]){
        self.isNowMicMuted = false;
        [[AppContext sbgServiceImp] updateSeatAudioMuteStatusWithMuted:self.isNowMicMuted
                                                            completion:^(NSError * error) {
        }];
    } else {
        self.isNowMicMuted = true;
    }
    if([self isRoomOwner]){
        [self.SBGApi stopSing];
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.statusView setMicOwnerWith:model.name url:model.imageUrl];
        self.statusView.state = SBGStateSbgSuccess;
    });
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW , (int64_t)(5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        SBGLogInfo(@"update:%@抢到麦%@", model.winnerNo, VLUserCenter.user.id);
        kWeakSelf(self);
            if([VLUserCenter.user.id isEqualToString:model.winnerNo]) {
                [self.SBGApi switchSingerRoleWithNewRole:KTVSingRoleSoloSinger onSwitchRoleState:^(enum KTVSwitchRoleState state, enum KTVSwitchRoleFailReason reason) {
                    weakself.statusView.state = SBGStateSingingBroadcaster;
                }];
            } else {
                [self.SBGApi switchSingerRoleWithNewRole:KTVSingRoleAudience onSwitchRoleState:^(enum KTVSwitchRoleState state, enum KTVSwitchRoleFailReason reason) {
                    weakself.statusView.state = SBGStateSingingAudience;
                }];
            }
            [self loadAndPlaySongWith:KTVPlayerTrackModeAcc];
    });
}

- (void)setAttributeWithOwner:(NSString *)owner icon:(NSString *)icon {
    NSMutableAttributedString *mutableAttributedString = [[NSMutableAttributedString alloc] initWithString:SBGLocalizedString(@"sbg_this_time")];
    
    NSURL *imageUrl = [NSURL URLWithString:icon];
    NSData *imageData = [NSData dataWithContentsOfURL:imageUrl];
    UIImage *image = [UIImage imageWithData:imageData];
    if (image != nil) {
        NSTextAttachment *textAttachment = [[NSTextAttachment alloc] init];
        CGFloat scaleFactor = (18) / image.size.height * 0.8; //调整缩放因子以使图片更好适应
        textAttachment.image = [UIImage imageWithData:imageData scale:scaleFactor];
        NSAttributedString *attachmentString = [NSAttributedString attributedStringWithAttachment:textAttachment];
        [mutableAttributedString appendAttributedString:attachmentString];
        [mutableAttributedString appendAttributedString:[[NSAttributedString alloc] initWithString:@" "]];
    }
    
    NSString *title = [NSString stringWithFormat:@"%@ %@", owner, SBGLocalizedString(@"sbg_get_mic")];
    NSAttributedString *titleString = [[NSAttributedString alloc] initWithString:title];
    [mutableAttributedString appendAttributedString:titleString];
   // [self.statusView setAttributeWith:mutableAttributedString];
}
#pragma mark view helpers
- (void)prepareBgImage {
    if (self.roomModel.bgOption) {
        VLSBGSelBgModel *selBgModel = [VLSBGSelBgModel new];
        selBgModel.imageName = [NSString stringWithFormat:@"ktv_mvbg%ld", self.roomModel.bgOption];
        selBgModel.isSelect = YES;
        self.choosedBgModel = selBgModel;
    }
}

//更换MV背景
- (void)popSelMVBgView {
    [LSTPopView popSBGSelMVBgViewWithParentView:self.view
                                     bgModel:self.choosedBgModel
                                withDelegate:self];
}

//弹出更多
- (void)popSelMoreView {
    [LSTPopView popSBGSelMoreViewWithParentView:self.view
                                withDelegate:self];
}

//弹出下麦视图
- (void)popDropLineViewWithSeatModel:(VLSBGRoomSeatModel *)seatModel {
    [LSTPopView popSBGDropLineViewWithParentView:self.view
                                withSeatModel:seatModel
                                 withDelegate:self];
}

//弹出美声视图
- (void)popBelcantoView {
    [LSTPopView popSBGBelcantoViewWithParentView:self.view
                            withBelcantoModel:self.selBelcantoModel
                                 withDelegate:self];
}

//弹出点歌视图
- (void)popUpChooseSongView:(BOOL)ifChorus {
    LSTPopView* popChooseSongView =
    [LSTPopView popSBGUpChooseSongViewWithParentView:self.view
                                         isChorus:ifChorus
                                  chooseSongArray:self.selSongsArray
                                       withRoomNo:self.roomModel.roomNo
                                     withDelegate:self];
    
    self.chooseSongView = (VLSBGPopSongList*)popChooseSongView.currCustomView;
}

//专业主播
- (void)popVoicePerView {
    LSTPopView* popView =
    [LSTPopView popSBGVoicePerViewWithParentView:self.view perView:self.voicePerShowView withDelegate:self];
    self.voicePerShowView = (VLSBGVoicePerShowView*)popView.currCustomView;
    [self.voicePerShowView setPerSelected: self.isProfessional];
}

//网络差视图
- (void)popBadNetWrokTipView {
    [LSTPopView popSBGBadNetWrokTipViewWithParentView:self.view
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
    [SBGDebugInfo setSelectedStatus:enable forKey:key];
    [SBGDebugManager reLoadParamAll];
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
    [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage sbg_sceneImageWithName:@"empty"] message:SBGLocalizedString(@"sbg_owner_leave") buttonTitle:SBGLocalizedString(@"sbg_confirm") completion:^(bool flag, NSString * _Nullable text) {
        for (VLBaseViewController *vc in weakSelf.navigationController.childViewControllers) {
            if ([vc isKindOfClass:[VLSBGOnLineListVC class]]) {
//                [weakSelf destroyMediaPlayer];
//                [weakSelf leaveRTCChannel];
                [weakSelf.navigationController popToViewController:vc animated:YES];
                [AgoraEntLog autoUploadLogWithScene:SBGLog.kLogKey];
            }
        }
        [[VLKTVAlert shared] dismiss];
    }];
}

- (void)showSettingView {
    LSTPopView* popView = [LSTPopView popSBGSettingViewWithParentView:self.view
                                                       settingView:self.settingView
                                                      withDelegate:self];
    
    self.settingView = (VLSBGSettingView*)popView.currCustomView;
}

- (void)showScoreViewWithScore:(NSInteger)score {
                        //  song:(VLRoomSelSongModel *)song {
    if (score < 0) return;
    //if (self.scoreView.hidden == NO) return;
    if(_scoreView == nil) {
        _scoreView = [[VLSBGPopScoreView alloc] initWithFrame:self.view.bounds withDelegate:self];
        [self.view addSubview:_scoreView];
    }
    SBGLogInfo(@"Avg score for the song: %ld", (long)score);
    [_scoreView configScore:(int)score];
    [self.view bringSubviewToFront:_scoreView];
    self.scoreView.hidden = NO;
}

- (void)popScoreViewDidClickConfirm
{
    SBGLogInfo(@"Using as score view hidding");
    self.scoreView = nil;
}

#pragma mark - rtc callbacks
- (void)rtcEngine:(AgoraRtcEngineKit *)engine didJoinedOfUid:(NSUInteger)uid elapsed:(NSInteger)elapsed
{
    SBGLogInfo(@"didJoinedOfUid: %ld", uid);
//    [self.SBGApi mainRtcEngine:engine didJoinedOfUid:uid elapsed:elapsed];
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine
reportAudioVolumeIndicationOfSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers
      totalVolume:(NSInteger)totalVolume {
}

-(NSMutableArray *)scoreArray {
    if(!_scoreArray){
       _scoreArray = [NSMutableArray array];
    }
    return _scoreArray;
}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
receiveStreamMessageFromUid:(NSUInteger)uid
         streamId:(NSInteger)streamId
             data:(NSData * _Nonnull)data {    //接收到对方的RTC消息
    
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:data];
    kWeakSelf(self);
    if ([dict[@"cmd"] isEqualToString:@"SingingScore"]) {
        int score = [dict[@"score"] intValue];
        NSString *userName = dict[@"userName"];
        NSString *userId = dict[@"userId"];
        NSString *poster = dict[@"poster"];
        //把用户信息存进数组，需要在这里合并数据
        SubRankModel *model = [[SubRankModel alloc]init];
        model.userName = userName;
        model.poster = poster;
        model.score = score;
        model.songNum = 1;
        model.userId = userId;
        [self.scoreArray addObject:model];
        
        //这个时候表示当前歌曲已结束
        self.currentSelSong = nil;
        [self.statusView.lrcView resetLrc];
        
        if([self isRoomOwner]){
            NSLog(@"removeCurrentSongWithSync: receiveStreamMessageFromUid");
            [self removeCurrentSongWithSync:YES];
        }
        if(self.totalCount > self.hasPlayedCount){
            dispatch_async(dispatch_get_main_queue(), ^{
                self.statusView.state =  score > 50 ? SBGStateSingSuccess : SBGStateSingFailed;
                [self.statusView setFight:score > 50  score:score];
            });
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW , (int64_t)(5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                weakself.statusView.state = SBGStateNext;
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW , (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    //开始下一轮抢唱
                    [weakself updateSBGUI];
                });
            });
        }else {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.statusView.state =  score > 50 ? SBGStateSingSuccess : SBGStateSingFailed;
                [self.statusView setFight:score > 50  score:score];
            });
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW , (int64_t)(5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                weakself.gameModel.status = SingBattleGameStatusEnded;
                if([weakself isRoomOwner]){
    //                //房主把分数给到服务端
                    weakself.gameModel.rank = [weakself convertScoreArrayToRank];
                    [[AppContext sbgServiceImp] innerUpdateSingBattleGameInfo:weakself.gameModel completion:^(NSError * _Nullable err) {
                                    
                    }];
                }
            });
        }
        
        SBGLogInfo(@"score: %ds",score);
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
            [self.statusView.lrcView updateScoreWith:score cumulativeScore:cumulativeScore totalScore:total];
        });
        NSLog(@"index: %li, score: %li, cumulativeScore: %li, total: %li", index, score, cumulativeScore, total);
    } else if ([dict[@"cmd"] isEqualToString:@"StartSingBattleCountDown"]) {
        //开始321倒计时
        [self querySbgStatusAndUpdateUI];
        [self updateSBGCountDown];
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
    SBGLogInfo(@"tokenPrivilegeWillExpire: %@", token);
    [[NetworkManager shared] generateTokenWithChannelName:self.roomModel.roomNo
                                                    appId: nil
                                                      uid:VLUserCenter.user.id
                                                    types:@[@(AgoraTokenTypeRtc), @(AgoraTokenTypeRtm)]
                                                   expire:1500
                                                  success:^(NSString * token) {
        SBGLogInfo(@"tokenPrivilegeWillExpire rtc renewToken: %@", token);
        [self.RTCkit renewToken:token];
        //TODO(chenpan): mcc missing
//        [self.AgoraMcc renewToken:token];
    }];
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine contentInspectResult:(AgoraContentInspectResult)result {
    SBGLogInfo(@"contentInspectResult: %ld", result);
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine localAudioStats:(AgoraRtcLocalAudioStats *)stats {
}

#pragma mark - action utils / business
- (void)stopPlaySong {
    self.isPause = false;
    [self.SBGApi switchSingerRoleWithNewRole:KTVSingRoleAudience onSwitchRoleState:^(enum KTVSwitchRoleState state, enum KTVSwitchRoleFailReason reason) {
        
    }];
}

- (void)loadAndPlaySongWith:(KTVPlayerTrackMode)mode{
    //清空分数
    [self.statusView.lrcView resetScore];
    self.trackMode = mode;
    VLSBGRoomSelSongModel* model = [[self selSongsArray] firstObject];
    self.currentSelSong = model;
    //TODO: fix score view visible problem while owner reopen the room
   // [self.MVView updateUIWithSong:model role:self.singRole];
    [self setCoSingerStateWith:self.singRole];
    if(!model){
        return;
    }
    [self markSongPlaying:model];
    
    //TODO: will remove RS api adjust playout volume method
    [self setPlayoutVolume:50];
    
//    self.retryCount = 0;
    
    //判断用户角色需要根据点歌人和抢歌人
    KTVSingRole role = KTVSingRoleAudience;
    if(mode == KTVPlayerTrackModeOrigin){
        role = [self getUserSingRole];
        [self.statusView.lrcView setAudioTrackWithIndex:0];
    } else {
        role = [model.winnerNo isEqualToString:VLUserCenter.user.id] ? KTVSingRoleSoloSinger : KTVSingRoleAudience;
        [self.statusView.lrcView setAudioTrackWithIndex:1];
    }
    self.singRole = role;
    
    KTVSongConfiguration *songConfig = [[KTVSongConfiguration alloc] init];
    songConfig.mode = role == KTVSingRoleAudience ? KTVLoadMusicModeLoadLrcOnly : KTVLoadMusicModeLoadMusicAndLrc;
    songConfig.mainSingerUid = [model.userNo integerValue];
    //songCode需要特殊转换一下
    NSString *jsonStr;
    if([model.winnerNo isEqualToString:VLUserCenter.user.id]){
        jsonStr = @"{\"format\":{\"highPartIndex\":0}}";
        songConfig.songCutter = true;
        songConfig.mainSingerUid = [model.winnerNo integerValue];
    } else {
        jsonStr = @"{\"format\":{\"highPart\":0}}";
        songConfig.songCutter = false;
    }

    NSInteger songcode = [self.SBGApi.getMusicContentCenter getInternalSongCode: [model.songNo integerValue] jsonOption:jsonStr];
    songConfig.songIdentifier = [NSString stringWithFormat:@"%li", songcode];
    VL(weakSelf);
    self.loadMusicCallBack = ^(BOOL isSuccess, NSInteger songCode) {
        if (!isSuccess) {
            return;
        }
        if(weakSelf.singRole == KTVSingRoleSoloSinger){
            [weakSelf.SBGApi startSingWithSongCode:songcode startPos:0];
        }
    };
    SBGLogInfo(@"加载的歌曲:%@---%@---%ld", model.name, model.songNo, (long)songcode);
    [self.SBGApi loadMusicWithSongCode:songcode config:songConfig onMusicLoadStateListener:self];


//    [weakSelf.SBGApi switchSingerRoleWithNewRole:role
//                           onSwitchRoleState:^( SBGSwitchRoleState state, SBGSwitchRoleFailReason reason) {
//        if(state != SBGSwitchRoleStateSuccess) {
//            SBGLogError(@"switchSingerRole error: %ld", reason);
//            return;
//        }
//    }];
    
}

- (void)enterSeatWithIndex:(NSInteger)index completion:(void(^)(NSError*))completion {
    
    SBGOnSeatInputModel* inputModel = [SBGOnSeatInputModel new];
    inputModel.seatIndex = index;
//    VL(weakSelf);
    [[AppContext sbgServiceImp] enterSeatWithInput:inputModel
                                        completion:completion];
}

- (void)leaveSeatWithSeatModel:(VLSBGRoomSeatModel * __nonnull)seatModel
                 withCompletion:(void(^ __nullable)(NSError*))completion {
    if(seatModel.rtcUid == VLUserCenter.user.id) {
        if(seatModel.isVideoMuted == 1) {
            [self.RTCkit stopPreview];
        }
    }
    
    SBGOutSeatInputModel* inputModel = [SBGOutSeatInputModel new];
    inputModel.userNo = seatModel.userNo;
    inputModel.userId = seatModel.rtcUid;
    inputModel.userName = seatModel.name;
    inputModel.userHeadUrl = seatModel.headUrl;
    inputModel.seatIndex = seatModel.seatIndex;
    [[AppContext sbgServiceImp] leaveSeatWithInput:inputModel
                                        completion:completion];
}

- (void)refreshChoosedSongList:(void (^ _Nullable)(void))block{
    VL(weakSelf);
    [[AppContext sbgServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLSBGRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        
        weakSelf.selSongsArray = songArray;
        if(block) {
            block();
        }
    }];
}

- (void)markSongPlaying:(VLSBGRoomSelSongModel *)model {
    if (model.status == VLSBGSongPlayStatusPlaying) {
        return;
    }
//    [[AppContext sbgServiceImp] markSongDidPlayWithInput:model
//                                              completion:^(NSError * error) {
//    }];
}

- (void)syncChoruScore:(NSInteger)score {
    NSDictionary *dict = @{
        @"cmd":@"SingingScore",
        @"score":@(score),
        @"userName":VLUserCenter.user.name,
        @"userId":VLUserCenter.user.id,
        @"poster":VLUserCenter.user.headUrl
    };
    [self sendStreamMessageWithDict:dict success:nil];
}

- (void)removeCurrentSongWithSync:(BOOL)sync
{
    VLSBGRoomSelSongModel* top = [self.selSongsArray firstObject];
    if(top && top.songNo.length != 0) {
        [self removeSelSongWithSongNo:[top.songNo integerValue] sync:sync];
    }
}

- (BOOL)removeSelSongWithSongNo:(NSInteger)songNo sync:(BOOL)sync {
    __block VLSBGRoomSelSongModel *removed = nil;
    
    BOOL isTopSong = [self.selSongsArray count] > 0 && [self.selSongsArray.firstObject.songNo integerValue] == songNo;
    
    if (isTopSong) {
        [self stopPlaySong];
    }
    
    NSMutableArray<VLSBGRoomSelSongModel *> *updatedList = [NSMutableArray arrayWithArray:[self.selSongsArray filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(VLSBGRoomSelSongModel *evaluatedObject, NSDictionary *bindings) {
        if ([evaluatedObject.songNo integerValue] == songNo) {
            removed = evaluatedObject;
            return NO;
        }
        return YES;
    }]]];
    
    if (removed != nil) {
        // Did remove
        self.selSongsArray = updatedList;

        if (sync) {
            SBGRemoveSongInputModel *inputModel = [SBGRemoveSongInputModel new];
            inputModel.songNo = removed.songNo;
            inputModel.objectId = removed.objectId;
            
            [[AppContext sbgServiceImp] removeSongWithInput:inputModel completion:^(NSError *error) {
                if (error) {
                    SBGLogInfo(@"deleteSongEvent fail: %@ %ld", removed.songName, error.code);
                }
            }];
        }
        
        return YES;
    } else {
        return NO;
    }
}

//- (BOOL)removeSelSongWithSongNo:(NSInteger)songNo sync:(BOOL)sync {
//    __block VLSBGRoomSelSongModel* removed;
//    BOOL isTopSong = [self.selSongsArray.firstObject.songNo integerValue] == songNo;
//    
//    if (isTopSong) {
//        [self stopPlaySong];
//    }
//    
//    NSMutableArray<VLSBGRoomSelSongModel*> *updatedList = [NSMutableArray arrayWithArray:[self.selSongsArray filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(VLSBGRoomSelSongModel*  _Nullable evaluatedObject, NSDictionary<NSString *,id> * _Nullable bindings) {
//        if([evaluatedObject.songNo integerValue] == songNo) {
//            removed = evaluatedObject;
//            return NO;
//        }
//        return YES;
//    }]]];
//    
//    if(removed != nil) {
//        //did remove
//        self.selSongsArray = updatedList;
//
//        if(sync) {
//            SBGRemoveSongInputModel* inputModel = [SBGRemoveSongInputModel new];
//            inputModel.songNo = removed.songNo;
//            inputModel.objectId = removed.objectId;
//            [[AppContext sbgServiceImp] removeSongWithInput:inputModel
//                                                 completion:^(NSError * error) {
//                if (error) {
//                    SBGLogInfo(@"deleteSongEvent fail: %@ %ld", removed.songName, error.code);
//                }
//            }];
//        }
//        
//        return YES;
//    } else {
//        return NO;
//    }
//}

//- (void)replaceSelSongWithInfo:(VLRoomSelSongModel*)songInfo {
//    self.selSongsArray = [SBGSyncManagerServiceImp sortChooseSongWithSongList:self.selSongsArray];
//}

- (void)leaveRoom {
    VL(weakSelf);
    [[AppContext sbgServiceImp] leaveRoomWithCompletion:^(NSError * error) {
        if (error != nil) {
            return;
        }
        
        for (VLBaseViewController *vc in weakSelf.navigationController.childViewControllers) {
            if ([vc isKindOfClass:[VLSBGOnLineListVC class]]) {
                [weakSelf.navigationController popToViewController:vc animated:YES];
                [AgoraEntLog autoUploadLogWithScene:SBGLog.kLogKey];
            }
        }
    }];
}

//设置人声突出
-(void)checkVoiceShowEffect:(NSInteger)index {
    VLSBGRoomSeatModel *model = [self getChorusSingerArrayWithSeatArray:self.seatsArray][index];
    
    NSDictionary *dict = @{
        @"cmd": @"checkVoiceHighlight",
        @"uid": model.userNo,
    };
    [self sendStreamMessageWithDict:dict success:^(BOOL flag) {
            
    }];
    if([model.userNo isEqualToString:VLUserCenter.user.id]){
        _isHighlightSinger = YES;
        [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetOff];
        [self sendVoiceShowEffect];
    }
}

//设置人声突出
-(void)sendVoiceShowEffect {
    NSDictionary *dict = @{
        @"cmd": @"sendVoiceHighlight",
        @"preset": @(self.currentSelectEffect)
    };
    [self sendStreamMessageWithDict:dict success:^(BOOL flag) {
            
    }];
}

#pragma mark - rtc utils
- (void)setupContentInspectConfig {
    AgoraContentInspectConfig* config = [AgoraContentInspectConfig new];
    NSDictionary* dic = @{
        @"userNo": [VLUserCenter user].id ? : @"unknown",
        @"sceneName": @"RS"
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
                                                sceneType:@"ktv"
                                                  success:^(NSString * msg) {
        SBGLogInfo(@"voiceIdentify success: %@", msg);
    }];
}

- (void)joinRTCChannel {
    AgoraRtcEngineConfig* rtcConfig = [AgoraRtcEngineConfig new];
    rtcConfig.appId = [AppContext.shared appId];
    rtcConfig.channelProfile = AgoraChannelProfileLiveBroadcasting;
    AgoraLogConfig* logConfig = [AgoraLogConfig new];
    logConfig.filePath = [AgoraEntLog sdkLogPath];
    rtcConfig.logConfig = logConfig;
    self.RTCkit = [AgoraRtcEngineKit sharedEngineWithConfig:rtcConfig delegate:self];
    //setup private param
//    [self.RTCkit setParameters:@"{\"rtc.debug.enable\": true}"];
//    [self.RTCkit setParameters:@"{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}"];
    
    //use game streaming in so mode, chrous profile in chrous mode
    [self.RTCkit setAudioScenario:AgoraAudioScenarioGameStreaming];
    [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
    
    /// 开启唱歌评分功能
    int code = [self.RTCkit enableAudioVolumeIndication:50 smooth:10 reportVad:true];
    
    if (code == 0) {
        SBGLogInfo(@"评分回调开启成功\n");
    } else {
        SBGLogInfo(@"评分回调开启失败：%d\n",code);
    }
    
    [self.RTCkit enableVideo];
    [self.RTCkit enableAudio];
    
    [self setupContentInspectConfig];
    
    VLSBGRoomSeatModel* myseat = [self.seatsArray objectAtIndex:0];
    
    self.isNowMicMuted = myseat.isAudioMuted;
    self.isNowCameraMuted = myseat.isVideoMuted;
    self.trackMode = KTVPlayerTrackModeOrigin;
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
    [self.RTCkit createDataStream:&SBGStreamId
                           config:config];
    
    KTVApiConfig *apiConfig = [[KTVApiConfig alloc]initWithAppId:[[AppContext shared] appId]
                                                              rtmToken:VLUserCenter.user.agoraRTMToken
                                                                engine:self.RTCkit
                                                            channelName:self.roomModel.roomNo
                                                                localUid:[VLUserCenter.user.id integerValue]
                                                        chorusChannelName:@""
                                                        chorusChannelToken:@""
                                                                    type:KTVTypeSingbattle
                                                       musicType:loadMusicTypeMcc
                                                            maxCacheSize:10
                                                                mccDomain:nil];
    self.SBGApi = [[KTVApiImpl alloc] init];
    [self.SBGApi createKtvApiWithConfig:apiConfig];
    [self.SBGApi renewInnerDataStreamId];
    [self.SBGApi setLrcViewWithView:self.statusView.lrcView];
    [self.SBGApi muteMicWithMuteStatus:self.isNowMicMuted];
    [self.SBGApi addEventHandlerWithKtvApiEventHandler:self];
//    VL(weakSelf);
    SBGLogInfo(@"Agora - joining RTC channel with token: %@, for roomNo: %@, with uid: %@", VLUserCenter.user.agoraRTCToken, self.roomModel.roomNo, VLUserCenter.user.id);
    int ret =
    [self.RTCkit joinChannelByToken:VLUserCenter.user.agoraRTCToken
                          channelId:self.roomModel.roomNo
                                uid:[VLUserCenter.user.id integerValue]
                       mediaOptions:[self channelMediaOptions]
                        joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        SBGLogInfo(@"Agora - 加入RTC成功");
//        [weakSelf.RTCkit setParameters: @"{\"che.audio.enable.md \": false}"];sin
    }];
    if (ret != 0) {
        SBGLogError(@"joinChannelByToken fail: %d, uid: %ld, token: %@", ret, [VLUserCenter.user.id integerValue], VLUserCenter.user.agoraRTCToken);
    }
}

- (void)leaveRTCChannel {
    [self.SBGApi removeEventHandlerWithKtvApiEventHandler:self];
    [self.SBGApi cleanCache];
    self.SBGApi = nil;
    self.loadMusicCallBack = nil;
    [self.RTCkit leaveChannel:^(AgoraChannelStats * _Nonnull stat) {
        SBGLogInfo(@"Agora - Leave RTC channel");
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
    [option setPublishMediaPlayerId:[[self.SBGApi getMusicPlayer] getMediaPlayerId]];
    [option setEnableAudioRecordingOrPlayout:YES];
    return option;
}

- (void)sendStreamMessageWithDict:(NSDictionary *)dict
                         success:(void (^ _Nullable)(BOOL))success {
//    VLLog(@"sendStremMessageWithDict:::%@",dict);
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    
    int code = [self.RTCkit sendStreamMessage:SBGStreamId
                                         data:messageData];
    if (code == 0 && success) {
        success(YES);
    } else{
//        VLLog(@"发送失败-streamId:%ld\n",streamId);
    };
}

#pragma mark -- VLSBGStatusViewDelegate
- (void)didSbgActionChanged:(enum SBGClickAction)action{
    switch (action) {
        case SBGClickActionChooseSong://选歌
            [self popUpChooseSongView:NO];
            break;
        case SBGClickActionRandomSelectSong://随机选歌
            break;
        case SBGClickActionAgain://再来一轮
            self.gameModel.status = SingBattleGameStatusWaiting;
            [[AppContext sbgServiceImp] innerUpdateSingBattleGameInfo:self.gameModel completion:^(NSError *error) {
                if(error){
                    NSLog(@"%@", error.localizedDescription);
                }
            }];
            break;
        case SRClickActionRetryLrc:
            [self reloadMusic];
            break;
        default:
            break;
    }
}

-(void)onKaraokeViewWithScore:(NSInteger)score totalScore:(NSInteger)totalScore lineScore:(NSInteger)lineScore lineIndex:(NSInteger)lineIndex {
    if(self.singRole == KTVSingRoleAudience){
        return;
    }

   [self sendMainSingerLineScoreToAudienceWith:score totalScore:totalScore lineScore:lineScore lineIndex:lineIndex];
   [self.statusView.lrcView updateScoreWith:lineScore cumulativeScore:score totalScore:totalScore];
}

- (void)onTokenPrivilegeWillExpire{
    
}

-(void)didLrcViewActionChangedWithState:(enum SBGClickAction)state{
    switch (state) {
        case SBGClickActionAac://aac
            [self.SBGApi.getMusicPlayer selectMultiAudioTrack:1 publishTrackIndex:1];
            break;
        case SBGClickActionOrigin://原唱
            [self.SBGApi.getMusicPlayer selectMultiAudioTrack:0 publishTrackIndex:0];
            break;
        case SBGClickActionSbg://抢唱发起
            [self startSBGGrap];
            break;
        case SBGClickActionEffect://调音
            [self showSettingView];
            break;
        case SBGClickActionNextSong://切歌
            //挑战失败
            //下一首 如果还有歌曲就下一首 不然就结算
            [self changeToNextSong];
            break;
        default:
            break;
    }
}

- (void)changeToNextSong {
    NSString *title = SBGLocalizedString(@"sbg_change_song");
    NSString *message = SBGLocalizedString(@"sbg_change_next_song");
    NSArray *array = [[NSArray alloc]initWithObjects:SBGLocalizedString(@"sbg_cancel"),SBGLocalizedString(@"sbg_confirm"), nil];
    kWeakSelf(self);
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:title message:message placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == YES){
            [weakself.statusView.lrcView resetLrc];
            [weakself syncChoruScore:0];
            weakself.currentSelSong = nil;
            //把自己的信息存进去
            SubRankModel *model = [[SubRankModel alloc]init];
            VLSBGRoomSelSongModel *currentSong = weakself.selSongsArray.firstObject;
            model.userName = VLUserCenter.user.name;
            model.poster = currentSong.imageUrl;
            model.score = 0;
            model.songNum = 1;
            model.userId = VLUserCenter.user.id;
            [weakself.scoreArray addObject:model];
            [weakself handleSingFailedNextMusicWithScore: 0];
            weakself.isNowMicMuted = true;
            [[AppContext sbgServiceImp] updateSeatAudioMuteStatusWithMuted:weakself.isNowMicMuted
                                                                completion:^(NSError * error) {
            }];
        }
        [[VLAlert shared] dismiss];
    }];
}

-(void)handleSingFailedNextMusicWithScore:(NSInteger)score{
    if([self isRoomOwner]){
        NSLog(@"removeCurrentSongWithSync: handleSingFailedNextMusicWithScore");
        [self removeCurrentSongWithSync:YES];
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [self stopSingAndShowFailedStateWithScore:score];
    });
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW , 5 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        [self removeCurrentSongAndPlayNext];
    });
}

- (void)stopSingAndShowFailedStateWithScore:(NSInteger)score {
    [self.SBGApi stopSing];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.statusView setFight:false score:score];
        self.statusView.state = SBGStateSingFailed;
    });
}

-(void)handleSingSuccessNextMusicWithScore:(NSInteger)score{
    if([self isRoomOwner]){
        NSLog(@"removeCurrentSongWithSync: handleSingSuccessNextMusicWithScore");
        [self removeCurrentSongWithSync:YES];
    }
    [self stopSingAndShowSuccessStateWithScore:score];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW , 5 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        [self removeCurrentSongAndPlayNext];
    });
}

- (void)stopSingAndShowSuccessStateWithScore:(NSInteger)score {
    [self.SBGApi stopSing];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.statusView setFight:true score:score];
        self.statusView.state = SBGStateSingSuccess;
    });
}

- (void)removeCurrentSongAndPlayNext {
    if (self.totalCount > self.hasPlayedCount) {
        [self playNextSong];
    } else {
        if([self isRoomOwner]){
//            //房主把分数给到服务端
            self.gameModel.status = SingBattleGameStatusEnded;
            self.gameModel.rank = [self convertScoreArrayToRank];
            [[AppContext sbgServiceImp] innerUpdateSingBattleGameInfo:self.gameModel completion:^(NSError * _Nullable err) {
                            
            }];
        }
    }
}

- (void)playNextSong {
    dispatch_async(dispatch_get_main_queue(), ^{
        self.statusView.state = SBGStateNext;
    });
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW , 3 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        [self updateSBGUI];
    });
}

//抢唱发起
-(void)startSBGGrap {
    VLSBGRoomSelSongModel* model = [[self selSongsArray] firstObject];
    kWeakSelf(self);
    [[NetworkManager shared] startSongGrab:[AppContext.shared appId] sceneId:@"scene_singbattle_5.0.0" roomId:_roomModel.roomNo headUrl:@"12345" userId:VLUserCenter.user.id userName:VLUserCenter.user.name songCode:model.songNo success:^(BOOL flag) {
        if(flag){
            //抢唱成功
            NSLog(@"抢唱成功");
            //立即更新service
            if([self isRoomOwner]){
                [self.SBGApi switchSingerRoleWithNewRole:KTVSingRoleAudience onSwitchRoleState:^(enum KTVSwitchRoleState state, enum KTVSwitchRoleFailReason reason) {
                    
                }];
            }
            VLSBGRoomSelSongModel *model = weakself.selSongsArray.firstObject;
            model.winnerNo = VLUserCenter.user.id;
            model.name = VLUserCenter.user.name;
            model.imageUrl = VLUserCenter.user.headUrl;
            [[AppContext sbgServiceImp] updateChooseSongWithSongInfo:model finished:^(NSError * _Nullable err) {
                NSLog(@"song: %@", model.name);
            }];
        }
    }];
}

-(void)sbgQuery {
    __block VLSBGRoomSelSongModel* model = [[self selSongsArray] firstObject];
    if (model.winnerNo == nil || [model.winnerNo isEqualToString:@""]) {
        //表示无人抢唱
    } else {
        //表示有人抢唱了
        return;
    }
    if(!self.currentSelSong){
        return;
    }
    kWeakSelf(self);
    [[NetworkManager shared] songGrabQuery:[AppContext.shared appId] sceneId:@"scene_singbattle_5.0.0" roomId:_roomModel.roomNo songCode:model.songNo src:@"postman" success:^(NSString *userId,NSString *userName, BOOL flag) {
        if(flag){
            return;
        }
        //房主同步进度为waiting
        if([weakself isRoomOwner]){
            [weakself.SBGApi stopSing];
            [weakself removeCurrentSongWithSync:YES];
            [weakself.statusView.lrcView resetLrc];
        }
        if(self.totalCount > self.hasPlayedCount){
            dispatch_async(dispatch_get_main_queue(), ^{
                weakself.statusView.state = SBGStateSbgNobody;
            });
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW , (int64_t)(5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                weakself.statusView.state = SBGStateNext;
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW , (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    //开始下一轮抢唱
                    [weakself updateSBGUI];
                });
            });
        }else {
                //房主把分数给到服务端
            dispatch_async(dispatch_get_main_queue(), ^{
                weakself.statusView.state = SBGStateSbgNobody;
            });
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW , (int64_t)(5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                weakself.gameModel.status = SingBattleGameStatusEnded;
                if([weakself isRoomOwner]){
                    weakself.gameModel.rank = [weakself convertScoreArrayToRank];
                    [[AppContext sbgServiceImp] innerUpdateSingBattleGameInfo:weakself.gameModel completion:^(NSError * _Nullable err) {
                        
                    }];
                }
            });
        }
    }];
}

#pragma mark -- VLSBGAPIDelegate

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

//- (void)didJoinChours {
//    //加入合唱
// //   self.MVView.joinCoSingerState = RSJoinCoSingerStateJoinNow;
//    [self joinChorus];
//}
//
//-(void)didLeaveChours {
//    //退出合唱
//    [[AppContext sbgServiceImp] coSingerLeaveChorusWithCompletion:^(NSError * error) {
//    }];
//    [self stopPlaySong];
//    self.isNowMicMuted = true;
//   // [self.MVView.gradeView reset];
//    [[AppContext sbgServiceImp] updateSeatAudioMuteStatusWithMuted:YES
//                                                        completion:^(NSError * error) {
//    }];
//}

#pragma mark -- VLSBGTopViewDelegate
- (void)onVLSBGTopView:(VLSBGTopView *)view closeBtnTapped:(id)sender {
    VL(weakSelf);
    NSString *title = VLUserCenter.user.ifMaster ? SBGLocalizedString(@"sbg_disband_room") : SBGLocalizedString(@"sbg_exit_room");
    NSString *message = VLUserCenter.user.ifMaster ? SBGLocalizedString(@"sbg_confirm_disband_room") : SBGLocalizedString(@"sbg_confirm_exit_room");
    NSArray *array = [[NSArray alloc]initWithObjects:SBGLocalizedString(@"sbg_cancel"),SBGLocalizedString(@"sbg_confirm"), nil];
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:title message:message placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == YES){
            [weakSelf leaveRoom];
        }
        [[VLAlert shared] dismiss];
    }];
}

- (void)onVLSBGTopView:(VLSBGTopView *)view moreBtnTapped:(id)sender{
    AUiMoreDialog* dialog = [[AUiMoreDialog alloc] initWithFrame:self.view.bounds];
    [self.view addSubview:dialog];
    [dialog show];
}

#pragma mark - VLPopMoreSelViewDelegate
- (void)onVLSBGMoreSelView:(VLSBGPopMoreSelView *)view
                 btnTapped:(id)sender
                 withValue:(VLSBGMoreBtnClickType)typeValue {
    [[LSTPopView getSBGPopViewWithCustomView:view] dismiss];
    switch (typeValue) {
//        case VLSBGMoreBtnClickTypeBelcanto:
//            [self popBelcantoView];
//            break;
        case VLSBGMoreBtnClickTypeSound:
            break;
        case VLSBGMoreBtnClickTypeSetting:
            [self popVoicePerView];
            break;
        case VLSBGMoreBtnClickTypeMV:
            [self popSelMVBgView];
            break;
        default:
            break;
    }
}

#pragma mark - VLSBGBottomViewDelegate
- (void)onVLSBGBottomView:(VLSBGBottomToolbar *)view
                btnTapped:(id)sender
               withValues:(VLSBGBottomBtnClickType)typeValue {
    switch (typeValue) {
        case VLSBGBottomBtnClickTypeMore:  //更多
//            [self popSelMVBgView];
            [self popSelMoreView];
            break;
        case VLSBGBottomBtnClickTypeJoinChorus:
            [self popUpChooseSongView:YES];
            break;
        case VLSBGBottomBtnClickTypeChoose:
            [self popUpChooseSongView:NO];
            break;
        case VLSBGBottomBtnClickTypeShowVoice://人声突出
            break;
        case VLSBGBottomBtnClickTypeAudio:
            self.isNowMicMuted = !self.isNowMicMuted;
            [[AppContext sbgServiceImp] updateSeatAudioMuteStatusWithMuted:self.isNowMicMuted
                                                                completion:^(NSError * error) {
            }];
            break;
        case VLSBGBottomBtnClickTypeVideo:
            self.isNowCameraMuted = !self.isNowCameraMuted;
            [[AppContext sbgServiceImp] updateSeatVideoMuteStatusWithMuted:self.isNowCameraMuted
                                                                completion:^(NSError * error) {
            }];
            break;
        default:
            break;
    }
}

#pragma mark - VLRoomPersonViewDelegate
- (void)onVLRoomPersonView:(VLSBGMicSeatList *)view
   seatItemTappedWithModel:(VLSBGRoomSeatModel *)model
                   atIndex:(NSInteger)seatIndex {
    if(VLUserCenter.user.ifMaster) {
        //is owner
        if(self.gameModel.status == SingBattleGameStatusStarted){
            if(seatIndex != 0){
                [VLToast toast:SBGLocalizedString(@"sbg_game_start_cant_make_down")];
            } else {
                [VLToast toast:SBGLocalizedString(@"sbg_game_start_cant_down")];
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
        
        if(self.gameModel.status == SingBattleGameStatusStarted){
            if([self isOnMicSeat]){
                [VLToast toast:SBGLocalizedString(@"sbg_game_start_cant_down")];
            } else {
                [VLToast toast:SBGLocalizedString(@"sbg_game_start_join_before")];
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

- (void)onVLRoomPersonView:(VLSBGMicSeatList *)view onRenderVideo:(VLSBGRoomSeatModel *)model inView:(UIView *)videoView atIndex:(NSInteger)seatIndex
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
-(void)onVLPopSelBgView:(VLSBGPopSelBgView *)view
       tappedWithAction:(VLSBGSelBgModel *)selBgModel
                atIndex:(NSInteger)index {
    SBGChangeMVCoverInputModel* inputModel = [SBGChangeMVCoverInputModel new];
//    inputModel.roomNo = self.roomModel.roomNo;
    inputModel.mvIndex = index;
//    inputModel.userNo = VLUserCenter.user.id;
    VL(weakSelf);
    [[AppContext sbgServiceImp] changeMVCoverWithParams:inputModel
                                             completion:^(NSError * error) {
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }
        
        [[LSTPopView getSBGPopViewWithCustomView:view] dismiss];
        weakSelf.choosedBgModel = selBgModel;
    }];
}

#pragma mark VLPopChooseSongViewDelegate
- (void)chooseSongView:(VLSBGPopSongList*)view tabbarDidClick:(NSUInteger)tabIndex {
    if(tabIndex == 2){
       //抢唱
        [self.roomPersonView updateSingBtnWithChoosedSongArray:_selSongsArray];
//        VLSBGRoomSelSongModel* originalTopSong = [oldSongsArray firstObject];
//        VLSBGRoomSelSongModel* updatedTopSong = [selSongsArray firstObject];
//        SBGLogInfo(@"setSelSongsArray current top: songName: %@, status: %ld",
//                   updatedTopSong.songName, updatedTopSong.status);
//        SBGLogInfo(@"setSelSongsArray orig top: songName: %@, status: %ld",
//                   originalTopSong.songName, originalTopSong.status);
//        if(![updatedTopSong.songNo isEqualToString:originalTopSong.songNo]){
//          //  [self.MVView reset];
           // [self.lrcControl resetLrc];
            //song changes
        [LSTPopView removeAllPopView];
        self.maxCount = self.selSongsArray.count;
        //更新UI为倒计时状态并倒计时
        [self stopPlaySong];
//        SingBattleGameModel *model = self.gameModel;
//        model.status = SingBattleGameStatusStarted;
//        self.gameModel = model;
        self.gameModel.status = SingBattleGameStatusStarted;
        [[AppContext sbgServiceImp] innerUpdateSingBattleGameInfo:self.gameModel completion:^(NSError * _Nullable err) {
                    
        }];
    }
    if (tabIndex != 1) {
        return;
    }
    
    [self refreshChoosedSongList:nil];
}

#pragma mark - VLChooseBelcantoViewDelegate
- (void)onVLChooseBelcantoView:(VLSBGAudioEffectPicker *)view
                    itemTapped:(VLSBGBelcantoModel *)model
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
- (void)onVLDropOnLineView:(VLSBGDropOnLineView *)view action:(VLSBGRoomSeatModel *)seatModel {
    [self leaveSeatWithSeatModel:seatModel withCompletion:^(NSError *error) {
        [[LSTPopView getSBGPopViewWithCustomView:view] dismiss];
    }];
}

#pragma mark VLTouristOnLineViewDelegate
//上麦方式
- (void)requestOnlineAction {
}
#pragma mark - MVViewDelegate

- (void)reloadMusic{
    VLSBGRoomSelSongModel* model = [[self selSongsArray] firstObject];
    KTVSongConfiguration* songConfig = [[KTVSongConfiguration alloc] init];
    songConfig.mode = KTVLoadMusicModeLoadLrcOnly;
    songConfig.mainSingerUid = [model.userNo integerValue];
    songConfig.songIdentifier = model.songNo;
    
    VL(weakSelf);
    self.loadMusicCallBack = ^(BOOL isSuccess, NSInteger songCode) {
        if (!isSuccess) {
            return;
        }
    };
    
    [self.SBGApi loadMusicWithSongCode:[model.songNo integerValue] config:songConfig onMusicLoadStateListener:self];
}

#pragma mark - VLSBGSettingViewDelegate
- (void)settingViewSettingChanged:(VLSBGSettingModel *)setting
              valueDidChangedType:(VLSBGValueDidChangedType)type {
    if (type == VLSBGValueDidChangedTypeEar) { // 耳返设置
        // 用户必须使用有线耳机才能听到耳返效果
        // 1、不在耳返中添加audiofilter
        // AgoraEarMonitoringFilterNone
        // 2: 在耳返中添加人声效果 audio filter。如果你实现了美声、音效等功能，用户可以在耳返中听到添加效果后的声音。
        // AgoraEarMonitoringFilterBuiltInAudioFilters
        // 4: 在耳返中添加降噪 audio filter。
        // AgoraEarMonitoringFilterNoiseSuppression
        // [self.RTCkit enableInEarMonitoring:setting.soundOn includeAudioFilters:AgoraEarMonitoringFilterBuiltInAudioFilters | AgoraEarMonitoringFilterNoiseSuppression];
        self.isEarOn = setting.soundOn;
    } else if (type == VLSBGValueDidChangedTypeMV) { // MV
        
    } else if (type == VLSBGValueDidChangedRiseFall) { // 升降调
        // 调整当前播放的媒体资源的音调
        // 按半音音阶调整本地播放的音乐文件的音调，默认值为 0，即不调整音调。取值范围为 [-12,12]，每相邻两个值的音高距离相差半音。取值的绝对值越大，音调升高或降低得越多
        NSInteger value = setting.toneValue * 2 - 12;
        [[self.SBGApi getMusicPlayer] setAudioPitch:value];
    } else if (type == VLSBGValueDidChangedTypeSound) { // 音量
        // 调节音频采集信号音量、取值范围为 [0,400]
        // 0、静音 100、默认原始音量 400、原始音量的4倍、自带溢出保护
        [self.RTCkit adjustRecordingSignalVolume:setting.soundValue * 100];
        if(setting.soundOn) {
            [self.RTCkit setInEarMonitoringVolume:setting.soundValue * 100];
        }
    } else if (type == VLSBGValueDidChangedTypeAcc) { // 伴奏
        int value = setting.accValue * 100;
        self.playoutVolume = value;
    } else if (type == VLSBGValueDidChangedTypeListItem) {
        AgoraAudioEffectPreset preset = [self audioEffectPreset:setting.kindIndex];
        [self.RTCkit setAudioEffectPreset:preset];
    } else if (type == VLSBGValueDidChangedTypeRemoteValue) {
//        [self.SBGApi adjustChorusRemoteUserPlaybackVoulme:setting.remoteVolume];
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
  self.currentSelectEffect = [effects[effect] integerValue];
  [self.RTCkit setAudioEffectPreset: [effects[effect] integerValue]];
  if(self.isHighlightSinger){
     [self sendVoiceShowEffect];
  }
}

//人声突出设置
- (void)voiceItemClickAction:(NSInteger)ItemIndex {
    self.selectedVoiceShowIndex = ItemIndex;
    [self checkVoiceShowEffect: ItemIndex];
}

//- (void)soundEffectItemClickAction:(VLSBGSoundEffectType)effectType {
//    if (effectType == VLSBGSoundEffectTypeHeFeng) {
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:3 param2:4];
//    } else if (effectType == VLSBGSoundEffectTypeXiaoDiao){
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:2 param2:4];
//    } else if (effectType == VLSBGSoundEffectTypeDaDiao){
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:1 param2:4];
//    } else if (effectType == VLSBGSoundEffectTypeNone) {
//        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:0 param2:4];
//    }
//    SBGLogInfo(@"Agora - Setting effect type to %lu", effectType);
//}


#pragma mark --
//- (void)_fetchServiceAllData {
//    //请求已点歌曲
//    VL(weakSelf);
//    [self refreshChoosedSongList:^{
//        //请求歌词和歌曲
//        [weakSelf loadAndPlaySong];
//    }];
//}

#pragma mark - getter/handy utils
- (BOOL)isCurrentSongMainSinger:(NSString *)userNo {
    VLSBGRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    return [selSongModel.userNo isEqualToString:userNo];
}

- (BOOL)isRoomOwner {
    return [self.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
}

- (BOOL)isBroadcaster {
    return [self isRoomOwner] || self.isOnMicSeat;
}

- (VLSBGRoomSelSongModel*)selSongWithSongNo:(NSString*)songNo {
    __block VLSBGRoomSelSongModel* song = nil;
    [self.selSongsArray enumerateObjectsUsingBlock:^(VLSBGRoomSelSongModel * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([obj.songNo isEqualToString:songNo]) {
            song = obj;
            *stop = YES;
        }
    }];
    
    return song;
}

/// 获取当前用户的麦位
- (VLSBGRoomSeatModel*)getCurrentUserSeatInfo {
    for (VLSBGRoomSeatModel *model in self.seatsArray) {
        if ([model.userNo isEqualToString:VLUserCenter.user.id]) {
            return model;
        }
    }
    
    return nil;
}

/// 根据麦位索引获取麦位
/// @param seatIndex <#seatIndex description#>
- (VLSBGRoomSeatModel*)getUserSeatInfoWithIndex:(NSUInteger)seatIndex {
    for (VLSBGRoomSeatModel *model in self.seatsArray) {
        if (model.seatIndex == seatIndex) {
            return model;
        }
    }
    
    return nil;
}

/// 计算当前歌曲用户的演唱角色
- (KTVSingRole)getUserSingRole {
    VLSBGRoomSelSongModel* songModel = [[self selSongsArray] firstObject];
    if([songModel.userNo isEqualToString:VLUserCenter.user.id]){
        return KTVSingRoleSoloSinger;
    } else {
        return KTVSingRoleAudience;
    }
//    VLSBGRoomSelSongModel* songModel = [[self selSongsArray] firstObject];
//    BOOL currentSongIsJoinSing = [[self getCurrentUserSeatInfo].chorusSongCode isEqualToString:songModel.chorusSongId];
//    BOOL currentSongIsSongOwner = [songModel isSongOwner];
//    BOOL currentSongIsChorus = [self getChorusNumWithSeatArray:self.seatsArray] > 0;
//    if (currentSongIsSongOwner) {
//        return currentSongIsChorus ? SBGSingRoleLeadSinger : SBGSingRoleSoloSinger;
//    } else if (currentSongIsJoinSing) {
//        return SBGSingRoleCoSinger;
//    } else {
//        return SBGSingRoleAudience;
//    }
}


/// 计算合唱者数量
/// @param seatArray <#seatArray description#>
- (NSUInteger)getChorusNumWithSeatArray:(NSArray*)seatArray {
    NSUInteger chorusNum = 0;
    VLSBGRoomSelSongModel* topSong = [self.selSongsArray firstObject];
    for(VLSBGRoomSeatModel* seat in seatArray) {
        //TODO: validate songCode
        if([seat.chorusSongCode isEqualToString:[topSong chorusSongId]]) {
            chorusNum += 1;
        }
       // else if ([seat.chorusSongCode length] > 0) {
//            SBGLogError(@"calc seat chorus status fail! chorusSongCode: %@, playSongCode: %@", seat.chorusSongCode, topSong.songNo);
//        }
//        if ([seat.chorusSongCode length] > 0) {
//            chorusNum += 1;
//        }
    }
    
    return chorusNum;
}

- (NSMutableArray *)getChorusSingerArrayWithSeatArray:(NSArray<VLSBGRoomSeatModel *> *)seatArray {
    NSMutableArray<VLSBGRoomSeatModel *> *singerSeatArray = [NSMutableArray array];
    if (self.selSongsArray.count == 0) {
        return singerSeatArray;
    }
    VLSBGRoomSelSongModel *topSong = [self.selSongsArray firstObject];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"chorusSongCode == %@", topSong.chorusSongId];
    NSArray<VLSBGRoomSeatModel *> *matchedSeats = [seatArray filteredArrayUsingPredicate:predicate];
    [singerSeatArray addObject:seatArray.firstObject]; // 添加房主到列表中
    if (matchedSeats.count > 0) {
        [singerSeatArray addObjectsFromArray:matchedSeats]; // 添加匹配到的表演者
    }
    return singerSeatArray;
}

- (BOOL)getJoinChorusEnable {
    //不是观众不允许加入
    if ([self getUserSingRole] != KTVSingRoleAudience) {
        return NO;
    }
    
    VLSBGRoomSelSongModel* topSong = [[self selSongsArray] firstObject];
    //TODO: 不在播放不允许加入
    if (topSong.status != VLSBGSongPlayStatusPlaying) {
        return NO;
    }
    
    return YES;
}

//获取已经非麦下观众的总数
-(NSInteger)getOnMicUserCount{
    NSInteger num = 0;
    if(self.seatsArray){
        for(VLSBGRoomSeatModel *model in self.seatsArray){
            if(model.rtcUid){
                num++;
            }
        }
    }
    return num;
}

#pragma mark - setter
- (void)setSBGApi:(KTVApiImpl *)SBGApi {
    _SBGApi = SBGApi;
    [[AppContext shared] setSbgAPI:SBGApi];
}

- (void)setRoomUsersCount:(NSUInteger)userCount {
    self.roomModel.roomPeopleNum = [NSString stringWithFormat:@"%ld", userCount];
    self.topView.listModel = self.roomModel;
}

- (void)setChoosedBgModel:(VLSBGSelBgModel *)choosedBgModel {
    _choosedBgModel = choosedBgModel;
   // [self.MVView changeBgViewByModel:choosedBgModel];
}

- (void)setSeatsArray:(NSArray<VLSBGRoomSeatModel *> *)seatsArray {
    _seatsArray = seatsArray;
    
    //update booleans
    self.isOnMicSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
    
    self.roomPersonView.roomSeatsArray = self.seatsArray;
    [self.roomPersonView updateSingBtnWithChoosedSongArray:_selSongsArray];
   // self.chorusNum = [self getChorusNumWithSeatArray:seatsArray];
    [self onSeatFull];
}

-(void)setGameModel:(SingBattleGameModel *)gameModel {
    _gameModel = gameModel;
    kWeakSelf(self);
    if(gameModel.status == SingBattleGameStatusWaiting){
        /**
         1.房主是选歌状态
         2.观众是等待状态
         */
        self.statusView.state = [self isRoomOwner] ? SBGStateOwnerOrderMusic : SBGStateAudienceWating;
    } else if(gameModel.status == SingBattleGameStatusStarted){
        /**
         1.嗨唱开始
         2.所有人开始倒计时
         3.倒计时结束后，所有人开始抢唱
         4.抢唱时间为10S，时间内有人抢唱则变成抢唱中
         5.无人抢唱显示无人抢唱状态
         */
        [self updateSBGUI];
        //关闭卖位 等待解锁
        self.isNowMicMuted = true;
        [_bottomView setAudioBtnEnabled:false];
        [[AppContext sbgServiceImp] updateSeatAudioMuteStatusWithMuted:self.isNowMicMuted
                                                            completion:^(NSError * error) {
        }];
    } else if(gameModel.status == SingBattleGameStatusEnded){
        /**
         1.解析结算结果
         2.如果是下一轮，更新service为waiting
         */
        [_bottomView setAudioBtnEnabled:true];
        NSDictionary *dict = gameModel.rank;
        NSMutableArray *array = [NSMutableArray array];
        
        //把字典转换成数组SubRankModel
        for(NSString *key in dict){
            SubRankModel *model = [[SubRankModel alloc]init];
            model.songNum = [dict[key][@"songNum"] integerValue];;
            model.index = 0;
            model.userId = key;
            model.score = [dict[key][@"score"] integerValue];
            model.poster = dict[key][@"poster"];
            model.userName = dict[key][@"userName"];
            [array addObject:model];
        }
        
        NSArray *sortArray = sortModels(array, NO);
        
        dispatch_async(dispatch_get_main_queue(), ^{
            self.statusView.dataSource = sortArray;
            self.statusView.state = [self isRoomOwner] ? SBGStateResultOwner : SBGStateResultAudience;
            [self.scoreArray removeAllObjects];
            self.totalCount = -1;
            self.hasShowReady = false;
        });

    }
}

// 合并相同 user id 的模型，并累加分数
NSArray<SubRankModel *> *mergeModelsWithSameUserIds(NSArray<SubRankModel *> *models) {
    NSMutableDictionary *userDict = [NSMutableDictionary dictionary];
    for (SubRankModel *model in models) {
        NSLog(@"结果:%li---%li", model.score, model.songNum);
        SubRankModel *existModel = [userDict objectForKey:model.userId];
        if (!existModel) { // 新用户
            [userDict setObject:model forKey:model.userId];
        } else { // 已有该用户，累加分数
            existModel.score += model.score;
            existModel.songNum += model.songNum;
        }
    }
    return [userDict allValues];
}

//NSArray<SubRankModel *> *updateScoreWith(NSArray<SubRankModel *> *models) {
//    NSMutableArray *array = [NSMutableArray arrayWithCapacity:models.count];
//    for (SubRankModel *model in models) {
//        SubRankModel *updateModel = model;
//        updateModel.score = model.score / model.songNum;
//        [array addObject:updateModel];
//    }
//    return [array copy];
//}

NSArray<SubRankModel *> *sortModelsByCountAndScore(NSArray<SubRankModel *> *models, BOOL ascending) {
    NSSortDescriptor *countSorter = [NSSortDescriptor sortDescriptorWithKey:@"songNum" ascending:ascending];
    NSSortDescriptor *scoreSorter = [NSSortDescriptor sortDescriptorWithKey:@"score" ascending:ascending];
    NSArray *sortedArray = [models sortedArrayUsingDescriptors:@[scoreSorter, countSorter]];
    
    return sortedArray;
}

// 对数组中每个元素进行索引赋值，并返回结果数组
NSArray<SubRankModel *> *assignIndexesToModelsInArray(NSArray<SubRankModel *> *array) {
    if (!array || array.count == 0) return nil;
    NSMutableArray<SubRankModel *> *resultArray = [NSMutableArray arrayWithCapacity:array.count];
    for (NSInteger i = 0; i < array.count; i++) {
        SubRankModel *model = array[i];
        model.index = i + 1; // 索引从1开始计算
        [resultArray addObject:model];
    }
    return resultArray;
}

// 组合：合并相同 user id 的模型 -> 按分数排序 -> 分配索引
NSArray<SubRankModel *> *sortModels(NSArray<SubRankModel *> *models, BOOL ascending) {
    NSArray<SubRankModel *> *sortedByCountModels = sortModelsByCountAndScore(models, ascending);
    NSArray<SubRankModel *> *resultModels = assignIndexesToModelsInArray(sortedByCountModels);
    return resultModels;
}

-(NSDictionary *)convertScoreArrayToRank {
    NSMutableDictionary *muDict = [NSMutableDictionary dictionary];
    //先进性数组合并
    NSArray *mergeModels = mergeModelsWithSameUserIds(self.scoreArray);
 //   NSArray *updateScoreModels = updateScoreWith(mergeModels);
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

- (void)updateSBGUI {
    if(self.totalCount == -1){
        self.totalCount = self.selSongsArray.count;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        self.hasPlayedCount = self.totalCount - self.selSongsArray.count+1;
        self.statusView.numStr = [NSString stringWithFormat:@"%li/%li", self.hasPlayedCount, self.totalCount];
    });
    
    if(!self.hasShowReady){
        int count = 5;
        for (NSInteger i = count; i >= 0; i--) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)((count - i) * 1.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                if(i == count){
                    self.hasPlayedCount = self.totalCount - self.selSongsArray.count+1;
                    self.statusView.numStr = [NSString stringWithFormat:@"%li/%li", self.hasPlayedCount, self.totalCount];
                }
                // 在此处更新 UI
                if(i >= 0){
                    if(i == 5){
                        self.statusView.state = SBGStateReady;
                        self.hasShowReady = true;
                    } else {
                        if(i > 1){
                            self.statusView.contentStr = [NSString stringWithFormat:@"%li", i - 1];
                        } else if (i == 1) {
                            self.statusView.contentStr = @"Go";
                        } else {
                            [self loadAndPlaySongWith:KTVPlayerTrackModeOrigin];
                           // [self finalUpdateUI];
                            [self updateWatingUI];
                        }
                    }
                }
            });
        }
    } else {
        [self loadAndPlaySongWith:KTVPlayerTrackModeOrigin];
       // [self finalUpdateUI];
        [self updateWatingUI];
    }
}

-(void)updateWatingUI{
    self.statusView.state = [self isOnMicSeat] ? SBGStateTimeDownBroadcaster : SBGStateSbgingOffSeat;
}

-(void)updateSBGCountDown {
    int count = 3;
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        for (NSInteger i = count; i >= 0; i--) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)((count - i) * 1.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                if(i == count){
                    self.hasPlayedCount = self.totalCount - self.selSongsArray.count+1;
                    self.statusView.numStr = [NSString stringWithFormat:@"%li/%li", self.hasPlayedCount, self.totalCount];
                }
                // 在此处更新 UI
                if (i == 0) {
                    [self finalUpdateUI];
                } else {
                    [self updateSingleUIWith: i ];
                }
            });
        }
    });
}

-(void)querySbgStatusAndUpdateUI {
    
    dispatch_time_t queryTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(13 * NSEC_PER_SEC));
    dispatch_after(queryTime, dispatch_get_main_queue(), ^{
        [self sbgQuery];
    });

    
    dispatch_time_t updateTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(12 * NSEC_PER_SEC));
    dispatch_after(updateTime, dispatch_get_main_queue(), ^{
        VLSBGRoomSelSongModel* model = [[self selSongsArray] firstObject];
        if (model.winnerNo == nil || [model.winnerNo isEqualToString:@""]) {
            //表示无人抢唱
        } else {
            //表示有人抢唱了
            return;
        }
        if(!self.currentSelSong){
            return;
        }
        self.statusView.state = SBGStateSbgingOffSeat;
    });
}

- (void)updateSingleUIWith:(NSInteger)time {
    self.statusView.state = [self isOnMicSeat] ? SBGStateTimeDownBroadcaster : SBGStateTimeDownAudience;
    self.statusView.countTime = time;
}

- (void)finalUpdateUI {
    self.statusView.state = [self isOnMicSeat] ? SBGStateSbgingOnSeat : SBGStateSbgingOffSeat;
}

-(void)onSeatFull{
    if(self.singRole != KTVSingRoleAudience){
        return;
    }
    NSInteger count = [self getOnMicUserCount];
    if(!_isOnMicSeat && count >=8){
       // self.MVView.joinCoSingerState = RSJoinCoSingerStateIdle;
    } else {
        if(!self.isJoinChorus){
           // self.MVView.joinCoSingerState = RSJoinCoSingerStateWaitingForJoin;
        }
    }
}

//- (void)setChorusNum:(NSUInteger)chorusNum {
//    NSUInteger origChorusNum = _chorusNum;
//    _chorusNum = chorusNum;
//    if (origChorusNum != chorusNum) {
//        //主唱<->独唱切换，非歌曲owner不需要调用
//        if(![self isCurrentSongMainSinger:VLUserCenter.user.id]) {
//            return;
//        }
//        SBGLogInfo(@"seat array update chorusNum %ld->%ld", origChorusNum, chorusNum);
//        //lead singer <-> solo
//        SBGSingRole role = [self getUserSingRole];
//        [self.SBGApi switchSingerRoleWithNewRole:role
//                               onSwitchRoleState:^(SBGSwitchRoleState state, SBGSwitchRoleFailReason reason) {
//        }];
//    }
//}

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
    
    self.bottomView.hidden = !_isOnMicSeat;
    self.requestOnLineView.hidden = !self.bottomView.hidden;
    
    VLSBGRoomSeatModel* info = [self getCurrentUserSeatInfo];
    if(onMicSeatStatusDidChanged){
        if(info == nil){
            self.isNowMicMuted = true;
            self.isNowCameraMuted = true;
        } else {
            self.isNowMicMuted = info.isAudioMuted;
            self.isNowCameraMuted = info.isVideoMuted;
        }
    }
}

- (void)setIsNowMicMuted:(BOOL)isNowMicMuted {
    BOOL oldValue = _isNowMicMuted;
    _isNowMicMuted = isNowMicMuted;
    [self.SBGApi muteMicWithMuteStatus:isNowMicMuted];
    [self.RTCkit adjustRecordingSignalVolume:isNowMicMuted ? 0 : 100];
   // if(oldValue != isNowMicMuted) {
     [self.bottomView updateAudioBtn:isNowMicMuted];
    //}
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
//    [self.SBGApi adjustPlayoutVolume:playoutVolume];
    [[self.SBGApi getMusicPlayer] adjustPlayoutVolume:playoutVolume];
    
    // 调节远端用户听到的音量 取值范围[0、400]
    // 100: （默认）媒体文件的原始音量。400: 原始音量的四倍（自带溢出保护）
    [[self.SBGApi getMusicPlayer] adjustPublishSignalVolume:playoutVolume];
    
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

- (void)setSelSongsArray:(NSArray<VLSBGRoomSelSongModel *> *)selSongsArray {
    _selSongsArray = [NSMutableArray arrayWithArray:selSongsArray];
    
    if (self.chooseSongView) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.roomPersonView updateSingBtnWithChoosedSongArray:self.selSongsArray];
            self.chooseSongView.selSongsArray = selSongsArray; //刷新已点歌曲UI
        });
    }
}

- (void)setTrackMode:(KTVPlayerTrackMode)trackMode {
    SBGLogInfo(@"setTrackMode: %ld", trackMode);
    _trackMode = trackMode;
}

- (void)setSingRole:(KTVSingRole)singRole {
    _singRole = singRole;
    
   // self.MVView.isOriginLeader = (_singRole == SBGSingRoleSoloSinger || _singRole == SBGSingRoleLeadSinger);
   // VLSBGRoomSelSongModel *song = self.selSongsArray.firstObject;
  //  [self.MVView updateUIWithSong:song role:singRole];
   // [self setCoSingerStateWith:singRole];

}

-(void)setCoSingerStateWith:(KTVSingRole)role {
    switch (role) {
        case KTVSingRoleSoloSinger:
        case KTVSingRoleLeadSinger: {
           // self.MVView.joinCoSingerState = RSJoinCoSingerStateIdle;
        } break;
        case KTVSingRoleCoSinger: {
//        case SBGSingRoleFollowSinger:
           // self.MVView.joinCoSingerState = RSJoinCoSingerStateWaitingForLeave;
        } break;
        case KTVSingRoleAudience:
        default: {
            //self.MVView.joinCoSingerState = RSJoinCoSingerStateWaitingForJoin;
            [self onSeatFull];
        } break;
    }
}

#pragma mark SBGApiEventHandlerDelegate
- (void)onMusicPlayerStateChangedWithState:(AgoraMediaPlayerState)state reason:(AgoraMediaPlayerReason)reason isLocal:(BOOL)isLocal {
    dispatch_async(dispatch_get_main_queue(), ^{
        if(state == AgoraMediaPlayerStatePlaying) {
//            if(isLocal) {
//                //track has to be selected after loaded
//                self.trackMode = self.trackMode;
//            }
          //  [self.MVView updateMVPlayerState:VLSBGMVViewActionTypeMVPlay];
        } else if(state == AgoraMediaPlayerStatePaused) {
           // [self.MVView updateMVPlayerState:VLSBGMVViewActionTypeMVPause];
        } else if(state == AgoraMediaPlayerStateStopped) {

        } else if (state == AgoraMediaPlayerStateOpenCompleted) {
            if (isLocal) {
                int flag = self.trackMode == KTVPlayerTrackModeOrigin ? 0 : 1;
                [self.SBGApi.getMusicPlayer selectMultiAudioTrack:flag publishTrackIndex:flag];
            }
        } else if(state == AgoraMediaPlayerStatePlayBackAllLoopsCompleted || state == AgoraMediaPlayerStatePlayBackCompleted) {
            
            VLSBGRoomSelSongModel* model = [[self selSongsArray] firstObject];
            if (model.winnerNo == nil || [model.winnerNo isEqualToString:@""]) {
                //表示无人抢唱
                return;
            }
            if(isLocal) {
                SBGLogInfo(@"Playback all loop completed");
                if (self.isEarOn) { // 自己的片段播放完关闭耳返
                    self.isEarOn = false;
                }
                if(self.singRole != KTVSingRoleAudience){
                    self.currentSelSong = nil;
                    self.isNowMicMuted = true;
                    [[AppContext sbgServiceImp] updateSeatAudioMuteStatusWithMuted:self.isNowMicMuted
                                                                        completion:^(NSError * error) {
                    }];
                    if(self.singRole == KTVSingRoleLeadSinger || self.singRole == KTVSingRoleSoloSinger){
                        [self syncChoruScore:self.statusView.lrcView.finalScore];
                        //把自己的信息存进去
                        SubRankModel *model = [[SubRankModel alloc]init];
                        VLSBGRoomSelSongModel *currentSong = self.selSongsArray.firstObject;
                        model.userName = VLUserCenter.user.name;
                        model.poster = currentSong.imageUrl;
                        model.score = self.statusView.lrcView.finalScore;
                        model.songNum = 1;
                        model.userId = VLUserCenter.user.id;
                        [self.scoreArray addObject:model];
                    }
                }
                
                if(self.statusView.lrcView.finalScore  > 50) {
                    [self handleSingSuccessNextMusicWithScore:self.statusView.lrcView.finalScore];
                } else {
                    [self handleSingFailedNextMusicWithScore: self.statusView.lrcView.finalScore];
                }
                
                [self.statusView.lrcView resetLrc];
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
        SBGLogInfo(@"old role:%li is equal to new role", oldRole);
    }
    self.singRole = newRole;
}

#pragma mark RSMusicLoadStateListener

- (void)onMusicLoadProgressWithSongCode:(NSInteger)songCode
                                percent:(NSInteger)percent
                                  state:(AgoraMusicContentCenterPreloadState)state
                                    msg:(NSString *)msg
                               lyricUrl:(NSString *)lyricUrl {
    SBGLogInfo(@"load: %li, %li", state, percent);
    dispatch_async_on_main_queue(^{
        if(state == AgoraMusicContentCenterPreloadStateError){
            [VLToast toast:SBGLocalizedString(@"sbg_load_failed_and_change")];
           // [self.MVView setBotViewHidden:false];
           // self.MVView.loadingType = VLSBGMVViewStateIdle;
            return;
        }
        
        if (state == AgoraMusicContentCenterPreloadStateOK){
          //  self.MVView.loadingType = VLSBGMVViewStateIdle;
        }
      //  self.MVView.loadingProgress = percent;
    });
}

- (void)onMusicLoadFailWithSongCode:(NSInteger)songCode reason:(enum KTVLoadSongFailReason)reason{
    
    dispatch_async_on_main_queue(^{
        if(self.loadMusicCallBack) {
            self.loadMusicCallBack(NO, songCode);
            self.loadMusicCallBack = nil;
        }
        VLSBGRoomSelSongModel *model = self.selSongsArray.firstObject;
        SBGLogInfo(@"加载失败的歌曲为:%@---%@----%ld", model.songName, model.winnerNo, (long)songCode);
        if (reason == KTVLoadSongFailReasonNoLyricUrl) {
           // self.MVView.loadingType = VLSBGMVViewStateLoadFail;
            [VLToast toast:@"歌词加载失败"];
           // self.statusView.state = SBGStateLoadFailedLrc;
        } else {
            [VLToast toast:@"歌曲加载失败"];
           // self.statusView.state = SBGStateLoadFailedLrc;
           // self.MVView.loadingType = VLSBGMVViewStateIdle;
            //            if(reason == SBGLoadSongFailReasonMusicPreloadFail){
            //                if(self.retryCount < 3){
            //                    self.retryCount++;
            //                    [VLToast toast:@"歌曲加载失败"];
            //                    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
            //                    SBGSongConfiguration* songConfig = [[SBGSongConfiguration alloc] init];
            //                    songConfig.autoPlay = YES;
            //                    songConfig.songCode = [model.songNo integerValue];
            //                    songConfig.mainSingerUid = [model.userNo integerValue];
            //                    [self.SBGApi loadMusicWithConfig:songConfig mode:SBGLoadMusicModeLoadMusicAndLrc onMusicLoadStateListener:self];
            //                } else {
            //                    [VLToast toast:@"已尝试3次，请自动切歌"];
            //                }
            //            }
        }
        SBGLogError(@"onMusicLoadFail songCode: %ld error: %ld", songCode, reason);
    });
}

- (void)onMusicLoadSuccessWithSongCode:(NSInteger)songCode lyricUrl:(NSString * _Nonnull)lyricUrl {
    dispatch_async_on_main_queue(^{
        if(self.loadMusicCallBack){
            self.loadMusicCallBack(YES, songCode);
            self.loadMusicCallBack = nil;
        }
        if(lyricUrl.length > 0){
            SBGLogInfo(@"onMusicLoadSuccessWithSongCode: %ld", self.singRole);
        }
        self.retryCount = 0;
        VLSBGRoomSelSongModel *model = self.selSongsArray.firstObject;
        SBGLogInfo(@"加载成功的歌曲为:%@---%@---%li", model.songName, model.winnerNo, (long)self.singRole);
        if([model.winnerNo length] == 0 || model.winnerNo == nil){
            //如果是主唱歌曲加载成功 发送ds告诉观众同步进度
            if(self.singRole == KTVSingRoleSoloSinger){
                NSDictionary *dict = @{
                    @"cmd":@"StartSingBattleCountDown"
                };
                [self sendStreamMessageWithDict:dict success:^(BOOL flag) {
                    
                }];
                [self.SBGApi switchSingerRoleWithNewRole:KTVSingRoleSoloSinger onSwitchRoleState:^(enum KTVSwitchRoleState state, enum KTVSwitchRoleFailReason reason) {
                                    
                }];
                [self.SBGApi startSingWithSongCode:songCode startPos:0];
                [self querySbgStatusAndUpdateUI];
                [self updateSBGCountDown];
            }
        }
    });
}

-(void)startCountDownWithDurationTime:(NSTimeInterval)durationTime intervalCountDown:(NSTimeInterval)intervalCountDown countDownBlock:(CountDownBlock)countDownBlock completionBlock:(void (^)(void))completionBlock { __block NSTimeInterval
    leftTimeInterval = durationTime;

// 创建GCD定时器，每秒执行一次
    __block dispatch_source_t timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0));
    dispatch_source_set_timer(timer, dispatch_walltime(NULL, 0), intervalCountDown * NSEC_PER_SEC, 0);
    dispatch_source_set_event_handler(timer, ^{ leftTimeInterval -= intervalCountDown;
        if (leftTimeInterval <= 0) { // 取消定时器
            dispatch_source_cancel(timer);
            dispatch_async(dispatch_get_main_queue(), ^{ if (completionBlock) {
                completionBlock();
            }
            });
        } else {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (countDownBlock) {
                    countDownBlock(leftTimeInterval);
                }
            });
        }
    });
    dispatch_resume(timer);
}

@end



