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
#import "KTVDebugInfo.h"
#import "VLKTVSettingView.h"
//model
#import "VLSongItmModel.h"
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
#import "AESMacro.h"
#import "LSTPopView+KTVModal.h"
//#import "HWWeakTimer.h"
#import "VLAlert.h"
#import "VLKTVAlert.h"
#import "KTVDebugManager.h"
#import "VLVoicePerShowView.h"
#import "HeadSetManager.h"
#import "AgoraEntScenarios-swift.h"
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
IMusicLoadStateListener,
VLVoicePerShowViewDelegate,
VLEarSettingViewViewDelegate,
VLDebugViewDelegate,
KTVServiceListenerProtocol
>

typedef void (^CompletionBlock)(BOOL isSuccess, NSInteger songCode);
@property (nonatomic, assign) BOOL isEnterSeatNotFirst;
@property (nonatomic, strong) VLKTVMVView *MVView;
@property (nonatomic, strong) VLKTVSelBgModel *choosedBgModel;
@property (nonatomic, strong) VLKTVBottomToolbar *bottomView;
@property (nonatomic, strong) VLBelcantoModel *selBelcantoModel;
@property (nonatomic, strong) VLKTVMVIdleView *noBodyOnLineView; // mv空页面
@property (nonatomic, strong) VLKTVTopView *topView;
@property (nonatomic, weak) VLKTVSettingView *settingView;
@property (nonatomic, strong) VLMicSeatList *roomPersonView; //房间麦位视图
@property (nonatomic, strong) VLAudienceIndicator *requestOnLineView;//空位上麦
@property (nonatomic, strong) VLPopSongList *chooseSongView; //点歌视图
@property (nonatomic, strong) VLVoicePerShowView *voicePerShowView; //专业主播
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
@property (nonatomic, assign) int soundVolume;
@property (nonatomic, assign) KTVPlayerTrackMode trackMode;  //合唱/伴奏

@property (nonatomic, strong) NSArray <VLRoomSelSongModel*>* selSongsArray;
@property (nonatomic, strong) KTVApiImpl* ktvApi;
@property (nonatomic, assign) NSInteger selectedVoiceShowIndex;
@property (nonatomic, assign) BOOL isProfessional;
@property (nonatomic, assign) BOOL isDelay;
@property (nonatomic, strong) LyricModel *lyricModel;
@property (nonatomic, strong) KTVLrcControl *lrcControl;
@property (nonatomic, copy, nullable) CompletionBlock loadMusicCallBack;
@property (nonatomic, assign) NSInteger selectedEffectIndex;
@property (nonatomic, assign) BOOL isPause;
@property (nonatomic, assign) NSInteger retryCount;
@property (nonatomic, assign) BOOL isJoinChorus;
@property (nonatomic, assign) NSInteger coSingerDegree;
@property (nonatomic, assign) NSInteger currentSelectEffect;
@property (nonatomic, assign) NSInteger aecGrade;
@property (nonatomic, assign) NSInteger volGrade;
@property (nonatomic, assign) CGFloat earValue;
//@property (nonatomic, assign) checkAuthType checkType;
@property (nonatomic, assign) BOOL isDumpMode;
@property (nonatomic, assign) BOOL voiceShowHasSeted;
@property (nonatomic, assign) BOOL aecState; //AIAEC开关
@property (nonatomic, assign) NSInteger aecLevel; //AEC等级
@property (nonatomic, assign) NSString *selectUserNo;
@property (nonatomic, strong) UIButton *testButton;
@property (nonatomic, assign) BOOL soundOpen;
@property (nonatomic, copy)  NSString *gainValue;
@property (nonatomic, assign) NSInteger typeValue;
@property (nonatomic, assign) NSInteger effectType;
@property (nonatomic, strong) SoundCardSettingView *soundSettingView;
@property (nonatomic, strong) LSTPopView *popSoundSettingView;
@property (nonatomic, strong) HeadSetManager *headeSet;
@property (nonatomic, assign) NSInteger roomPeopleCount;
@property (nonatomic, strong) VLKTVSettingModel *settingModel;
@end

@implementation VLKTVViewController

- (VLAudienceIndicator *)requestOnLineView {
    if (!_requestOnLineView) {
        _requestOnLineView = [[VLAudienceIndicator alloc] initWithFrame:CGRectMake(0, SCREEN_HEIGHT-kSafeAreaBottomHeight-56-VLREALVALUE_WIDTH(30), SCREEN_WIDTH, 56)
                                                           withDelegate:self];
    }
    
    return _requestOnLineView;
}

#pragma mark view lifecycles
- (void)dealloc {
    KTVLogInfo(@"dealloc:%s",__FUNCTION__);
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        [self subscribeServiceEvent];
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = UIColor.blackColor;
    self.selectedVoiceShowIndex = -1;
    self.selectUserNo = @"";
    self.soundOpen = false;
    self.gainValue = @"1.0";
    self.effectType = 0;
    self.typeValue = 4;
    self.isDelay = true;
  //  self.checkType = checkAuthTypeAll;
    [self subscribeServiceEvent];
    
    // setup view
    [self setBackgroundImage:@"bg-main" bundleName:@"KtvResource"];
    
//    UIView *bgView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)];
//    bgView.backgroundColor = UIColorMakeWithRGBA(0, 0, 0, 0.6);
//    [self.view addSubview:bgView];
    //头部视图
    VLKTVTopView *topView = [[VLKTVTopView alloc]initWithFrame:CGRectMake(0, kStatusBarHeight, SCREEN_WIDTH, 60) withDelegate:self];
    [self.view addSubview:topView];
    self.topView = topView;
    topView.listModel = self.roomModel;
    
    //底部按钮视图
    VLKTVBottomToolbar *bottomView = [[VLKTVBottomToolbar alloc] initWithFrame:CGRectMake(0, SCREEN_HEIGHT-64-kSafeAreaBottomHeight, SCREEN_WIDTH, 64) withDelegate:self withRoomNo:self.roomModel.roomNo withData:self.seatsArray];
    bottomView.backgroundColor = [UIColor clearColor];
    self.bottomView = bottomView;
    [self.view addSubview:bottomView];
    [bottomView setHidden:!self.requestOnLineView.isHidden];
    
    //去掉首尾的高度
    CGFloat musicHeight = SCREEN_HEIGHT -64 - kSafeAreaBottomHeight - kStatusBarHeight - 60 - 20;
    
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
    [self.view addSubview:self.requestOnLineView];
    
    //start join
    [self joinRTCChannel];
    
//    self.isOnMicSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
    
    //处理背景
    [self prepareBgImage];
    [[UIApplication sharedApplication] setIdleTimerDisabled: YES];
    
    //add debug
    //[self.topView addGestureRecognizer:[KTVDebugManager createStartGesture]];
    
    self.earValue = 100;
    
    //测试使用
    _testButton = [[UIButton alloc]initWithFrame:CGRectMake((SCREEN_WIDTH - 60)/2.0, SCREEN_HEIGHT - 60, 60, 40)];
    _testButton.backgroundColor = [UIColor redColor];
    [_testButton addTarget:self action:@selector(testCosinger) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:_testButton];
    [_testButton setHidden:true];
    
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
    kWeakSelf(self);
    self.headeSet = [HeadSetManager initHeadsetObserverWithCallback:^(BOOL inserted) {
        if(!inserted){
            //拔下耳机了 关闭耳返
            if(weakself.isEarOn){
                [weakself onVLKTVEarSettingViewSwitchChanged:false];
                [VLToast toast:KTVLocalizedString(@"ktv_earback_micphone_pull")];
            }
        }
    }];
}

-(void)testCosinger{
    [self.ktvApi switchSingerRoleWithNewRole:KTVSingRoleLeadSinger onSwitchRoleState:^(KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
    }];
}

-(void)showDebug {
    [LSTPopView popDebugViewWithParentView:self.view channelName:self.roomModel.roomNo sdkVer:[AgoraRtcEngineKit getSdkVersion]  isDebugMode:self.isDumpMode withDelegate:self];
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
- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}
// 是否允许手动滑回 @return true 是、 false否
- (BOOL)forceEnableInteractivePopGestuzreRecognizer {
    return NO;
}

#pragma mark service handler
- (void)subscribeServiceEvent {
    [[AppContext ktvServiceImp] subscribeWithListener:self];
}

-(void)setMVViewStateWith:(VLRoomSelSongModel *)song {
    if(!song){
        self.MVView.mvState = VLKTVMVViewStateNone;
    } else {
        KTVLogInfo(@"setMVViewState: singRole: %ld, isRoomOwner: %ld", self.singRole, [self isRoomOwner]);
        switch (self.singRole) {
            case KTVSingRoleSoloSinger:
                self.MVView.mvState = VLKTVMVViewStateOwnerSing;
                [self.MVView setPlayState:_isPause];
                break;
            case KTVSingRoleLeadSinger:
                self.MVView.mvState = VLKTVMVViewStateOwnerSing;
                [self.MVView setPlayState:_isPause];
                break;
            case KTVSingRoleCoSinger:
                self.MVView.mvState = [self isRoomOwner] ? VLKTVMVViewStateOwnerChorus : VLKTVMVViewStateNotOwnerChorus;
                break;
            case KTVSingRoleAudience:
                self.MVView.mvState = [self isRoomOwner] ? VLKTVMVViewStateOwnerAudience : VLKTVMVViewStateAudience;
                break;
            default:
                break;
        }
        [self.MVView setSongNameWith:[NSString stringWithFormat:@"%@-%@", song.songName, song.singer]];
    }
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
                                          isOwner:[self.roomModel.creatorNo isEqualToString:VLUserCenter.user.userNo]
                                         isChorus:ifChorus
                                  chooseSongArray:self.selSongsArray
                                       withRoomNo:self.roomModel.roomNo
                                     withDelegate:self];
    
    self.chooseSongView = (VLPopSongList*)popChooseSongView.currCustomView;
}

//专业主播
- (void)popVoicePerView {
    LSTPopView* popView =
    [LSTPopView popVoicePerViewWithParentView:self.view isProfessional:self.isProfessional aecState: self.aecState aecLevel:self.aecLevel isDelay:self.isDelay volGrade: self.volGrade grade: self.aecGrade isRoomOwner:[self isRoomOwner] perView:self.voicePerShowView withDelegate:self];
    self.voicePerShowView = (VLVoicePerShowView*)popView.currCustomView;
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
    [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage ktv_sceneImageWithName:@"empty" ] message:KTVLocalizedString(@"ktv_owner_leave") buttonTitle:KTVLocalizedString(KTVLocalizedString(@"ktv_confirm")) completion:^(bool flag, NSString * _Nullable text) {
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
                                                           setting: self.settingModel
                                                       settingView:self.settingView
                                                      withDelegate:self];
    
    self.settingView = (VLKTVSettingView*)popView.currCustomView;
    BOOL flag = self.selSongsArray.count > 0;
    if (flag == false) {
        [self.settingView setIspause:true];
    } else {
        [self.settingView setIspause:self.isPause];
    }
    [self.settingView setAEC:self.aecState level:self.aecLevel];
    [self.settingView setChorusStatus: flag];
}

- (void)showScoreViewWithScore:(NSInteger)score {
                        //  song:(VLRoomSelSongModel *)song {
    if (score < 0) return;
    if(_scoreView == nil) {
        _scoreView = [[VLPopScoreView alloc] initWithFrame:self.view.bounds withDelegate:self];
        [self.view addSubview:_scoreView];
    }
    KTVLogInfo(@"Avg score for the song: %ld", (long)score);
    [_scoreView configScore:(int)score];
    [self.view bringSubviewToFront:_scoreView];
    self.scoreView.hidden = NO;
}

- (void)popScoreViewDidClickConfirm
{
    KTVLogInfo(@"Using as score view hidding");
    self.scoreView = nil;
}

#define VLEarSettingView
-(void)onVLKTVEarSettingViewValueChanged:(double)Value{
    if(self.earValue == Value){
        return;
    }
    self.earValue = Value;
    KTVLogInfo(@"ear vol:%f", Value);
    [self.RTCkit setInEarMonitoringVolume:Value];
}

- (void)onVLKTVEarSettingViewSwitchChanged:(BOOL)flag{
    self.isEarOn = flag;
    [self.RTCkit enableInEarMonitoring:flag];
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
    [KTVDebugInfo setSelectedStatus:enable forKey:key];
    [KTVDebugManager reLoadParamAll];
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

- (void)didUpdateGainValue:(NSString *)value{
    self.gainValue = value;
    int index = 0;
    if (self.effectType < 2) {
        index = 2;
    } else if (self.effectType < 4) {
        index = 3;
    } else {
        index = 4;
    }
    
    [self.RTCkit setParameters:[NSString stringWithFormat:@"{\"che.audio.virtual_soundcard\":{\"preset\":%ld,\"gain\":%@,\"gender\":%d,\"effect\":%d}}", (long)self.typeValue, value, self.effectType/2 == 0 ? 0 : 1, index]];
}

- (void)didUpdateTypeValue:(NSInteger)value{
    self.typeValue = value;
    int index = 0;
    if (self.effectType < 2) {
        index = 2;
    } else if (self.effectType < 4) {
        index = 3;
    } else {
        index = 4;
    }
    [self.RTCkit setParameters:[NSString stringWithFormat:@"{\"che.audio.virtual_soundcard\":{\"preset\":%ld,\"gain\":%@,\"gender\":%d,\"effect\":%d}}", (long)value, self.gainValue, self.effectType/2 == 0 ? 0 : 1, index]];
}

- (void)didUpdateSoundSetting:(BOOL)isEnabled{
    self.soundOpen = isEnabled;
    [self.settingView setUseSoundCard:isEnabled];
    if (isEnabled) {
        self.gainValue = @"1.0";
        self.effectType = 0;
        self.typeValue = 4;
        [self.RTCkit setParameters:@"{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":0,\"effect\":2}}"];
    } else {
        [self.RTCkit setParameters:@"{\"che.audio.virtual_soundcard\":{\"preset\":-1,\"gain\":-1.0,\"gender\":-1,\"effect\":-1}}"];
    }
}

- (void)didUpdateEffectValue:(NSInteger)value{
    self.gainValue = @"1.0";
    self.effectType = value;
    self.typeValue = 4;
    switch (value) {
        case 0:
            [self.RTCkit setParameters:@"{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":0,\"effect\":2}}"];
            break;
        case 1:
            [self.RTCkit setParameters:@"{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":1,\"effect\":2}}"];
            break;
        case 2:
            [self.RTCkit setParameters:@"{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":0,\"effect\":3}}"];
            break;
        case 3:
            [self.RTCkit setParameters:@"{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":1,\"effect\":3}}"];
            break;
        case 4:
            [self.RTCkit setParameters:@"{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":0,\"effect\":4}}"];
            break;
        case 5:
            [self.RTCkit setParameters:@"{\"che.audio.virtual_soundcard\":{\"preset\":4,\"gain\":1.0,\"gender\":1,\"effect\":4}}"];
            break;
        default:
            break;
    }
}

#pragma mark - rtc callbacks
- (void)rtcEngine:(AgoraRtcEngineKit *)engine didJoinedOfUid:(NSUInteger)uid elapsed:(NSInteger)elapsed
{
    KTVLogInfo(@"didJoinedOfUid: %ld", uid);
//    [self.ktvApi mainRtcEngine:engine didJoinedOfUid:uid elapsed:elapsed];
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine didAudioRouteChanged:(AgoraAudioOutputRouting)routing {
    
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine didAudioPublishStateChange:(NSString *)channelId oldState:(AgoraStreamPublishState)oldState newState:(AgoraStreamPublishState)newState elapseSinceLastState:(int)elapseSinceLastState {
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine
reportAudioVolumeIndicationOfSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers
      totalVolume:(NSInteger)totalVolume {
    [self.roomPersonView updateVolumeForSpeakers:speakers];
}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
receiveStreamMessageFromUid:(NSUInteger)uid
         streamId:(NSInteger)streamId
             data:(NSData * _Nonnull)data {    //接收到对方的RTC消息
    
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:data];
//    KTVLogInfo(@"receiveStreamMessageFromUid:%@,streamID:%d,uid:%d",dict,(int)streamId,(int)uid);
    if ([dict[@"cmd"] isEqualToString:@"SingingScore"]) {
        //伴唱显示自己的分数，观众显示主唱的分数
        int score = [dict[@"score"] intValue];
        dispatch_async(dispatch_get_main_queue(), ^{
            if(self.singRole == KTVSingRoleCoSinger){
                [self showScoreViewWithScore:[self.lrcControl getAvgScore]];
                return;
            }

            [self showScoreViewWithScore:score];
        });
        
        KTVLogInfo(@"score: %ds",score);
        return;
    } else if([dict[@"cmd"] isEqualToString:@"singleLineScore"]) {//观众接收主唱的分数
        KTVLogInfo(@"index: %li", [dict[@"index"] integerValue]);
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
        KTVLogInfo(@"index: %li, score: %li, cumulativeScore: %li, total: %li", index, score, cumulativeScore, total);
    } else if([dict[@"cmd"] isEqualToString:@"sendVoiceHighlight"]) {
        if(self.singRole == KTVSingRoleAudience){
            return;
        };

        NSInteger audioEffectPreset = [dict[@"preset"] integerValue];
        switch (audioEffectPreset) {
            case AgoraAudioEffectPresetOff:
                [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetOffHarmony];
                KTVLogInfo(@"effect:Off");
                break;
            case AgoraAudioEffectPresetRoomAcousticsKTV:
                [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetRoomAcousticsKTVHarmony];
                KTVLogInfo(@"effect:KTV");
                break;
            case AgoraAudioEffectPresetRoomAcousVocalConcer:
                [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetRoomAcousVocalConcerHarmony];
                KTVLogInfo(@"effect:Concer");
                break;
            case AgoraAudioEffectPresetRoomAcousStudio:
                [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetRoomAcousStudioHarmony];
                KTVLogInfo(@"effect:Studio");
                break;
            case AgoraAudioEffectPresetRoomAcousPhonograph:
                [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetRoomAcousPhonographHarmony];
                KTVLogInfo(@"effect:graph");
                break;
            default:
                [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetOffHarmony];
                KTVLogInfo(@"effect:Off");
                break;
        }
    } else if([dict[@"cmd"] isEqualToString:@"cancelVoiceHighlight"]) {
        //人生突出实效
        [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetOff];
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
                                                   expire:1500
                                                  success:^(NSString * token) {
        KTVLogInfo(@"tokenPrivilegeWillExpire rtc renewToken: %@", token);
        [self.RTCkit renewToken:token];
    }];
    
    //TODO: mcc missing token expire callback
    [[NetworkManager shared] generateTokenWithChannelName:self.roomModel.roomNo
                                                      uid:VLUserCenter.user.id
                                                tokenType:TokenGeneratorTypeToken006
                                                     type:AgoraTokenTypeRtm
                                                   expire:1500
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
}

#pragma mark - action utils / business
- (void)stopPlaySong {
    self.isPause = false;
   // [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetOff];
   // self.MVView.joinCoSi ngerState = KTVJoinCoSingerStateWaitingForJoin;
    [self.MVView setOriginBtnState: VLKTVMVViewActionTypeSingAcc];
    [self.MVView setMvState:[self isRoomOwner] ? VLKTVMVViewStateOwnerAudience : VLKTVMVViewStateAudience];
    [self.ktvApi switchSingerRoleWithNewRole:KTVSingRoleAudience
                           onSwitchRoleState:^(KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
    }];
    self.aecLevel = 1;
    self.aecState = true;
    // 歌曲播完关闭耳返状态
//    if(self.isEarOn){
//        self.isEarOn = false;
//        [self.RTCkit enableInEarMonitoring:_isEarOn includeAudioFilters:AgoraEarMonitoringFilterNone];
//    }
}

- (void)loadAndPlaySong{
    self.voiceShowHasSeted = false;
    self.selectedVoiceShowIndex = -1;
    self.selectUserNo = @"";
    self.currentSelectEffect = 0;
  //  [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetOff];
    
//    if([self isRoomOwner]){
//        [self.MVView setPerViewAvatar:@""];
//    }
    
    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
    [self.MVView setMvState: VLKTVMVViewStateMusicLoading];
    if(!model){
        return;
    }
    [self markSongPlaying:model];
    
    //TODO: will remove ktv api adjust playout volume method
    [self setPlayoutVolume:50];

    KTVSingRole role = [self getUserSingRole];
    KTVLogInfo(@"loadAndPlaySong[%@][%@]: role: %ld", model.songNo, model.songName, role);
    KTVSongConfiguration* songConfig = [[KTVSongConfiguration alloc] init];
  //  songConfig.autoPlay = (role == KTVSingRoleAudience || role == KTVSingRoleCoSinger) ? NO : YES ;
    songConfig.mode = (role == KTVSingRoleAudience || role == KTVSingRoleCoSinger) ? KTVLoadMusicModeLoadLrcOnly : KTVLoadMusicModeLoadMusicAndLrc;
    songConfig.mainSingerUid = [model.owner.userId integerValue];
    songConfig.songIdentifier = model.songNo;

    VL(weakSelf);
    self.loadMusicCallBack = ^(BOOL isSuccess, NSInteger songCode) {
        if (!isSuccess) {
            return;
        }
        if(role == KTVSingRoleCoSinger){
            weakSelf.singRole = KTVSingRoleCoSinger;
        }
        
        if(role == KTVSingRoleCoSinger){
            [weakSelf.ktvApi startSingWithSongCode:songCode startPos:0];
        }
        
        [weakSelf.ktvApi switchSingerRoleWithNewRole:role
                               onSwitchRoleState:^( KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
            if(state != KTVSwitchRoleStateSuccess && role != KTVSingRoleAudience) {
                //TODO(chenpan): error toast and retry?
                KTVLogError(@"switchSingerRole error: %ld", reason);
                return;
            } else {
                if(role == KTVSingRoleSoloSinger || role == KTVSingRoleLeadSinger){
                    [weakSelf.ktvApi startSingWithSongCode:songCode startPos:0];
                    weakSelf.aecLevel = 0;
                    weakSelf.aecState = false;
                }
                [weakSelf setMVViewStateWith:model];
            }
        }];
    };
    
    [self.lrcControl resetShowOnce];
    [self.ktvApi loadMusicWithSongCode:[model.songNo integerValue] config:songConfig onMusicLoadStateListener:self];

}

- (void)enterSeatWithIndex:(NSNumber*)index completion:(void(^)(NSError*))completion {
    [[AppContext ktvServiceImp] enterSeatWithSeatIndex:index completion:completion];
    [self _checkEnterSeatAudioAuthorized];
}

- (void)_checkEnterSeatAudioAuthorized {
    if (self.isEnterSeatNotFirst) {
        return;
    }
    
    self.isEnterSeatNotFirst = YES;
    [AgoraEntAuthorizedManager checkAudioAuthorizedWithParent:self completion:nil];
}

- (void)leaveSeatWithSeatModel:(VLRoomSeatModel * __nonnull)seatModel
                 withCompletion:(void(^ __nullable)(NSError*))completion {
//    if([seatModel.owner.userId isEqualToString:VLUserCenter.user.id]) {
//        if(seatModel.isVideoMuted == 1) {
//            [self.RTCkit stopPreview];
//        }
//    }
    if ([seatModel.owner.userId isEqualToString:VLUserCenter.user.id]) {
        [[AppContext ktvServiceImp] leaveSeatWithCompletion:completion];
    } else {
        [[AppContext ktvServiceImp] kickSeatWithSeatIndex:seatModel.seatIndex completion:completion];
    }
}

- (void)refreshChoosedSongList:(void (^ _Nullable)(void))block{
    VL(weakSelf);
    [[AppContext ktvServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        if(songArray.count == 0){
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
    [[AppContext ktvServiceImp] markSongDidPlayWithSongCode:model.songNo completion:^(NSError * error) {
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

    [self.MVView setMvState:VLKTVMVViewStateJoinChorus];
    self.isJoinChorus = YES;
    if([self getOnMicUserCount] == 8 && !_isOnMicSeat){
        [VLToast toast:KTVLocalizedString(@"ktv_mic_full")];
        return;
    }
    
    if(self.RTCkit.getConnectionState != AgoraConnectionStateConnected){
        [self.MVView setMvState: [self isRoomOwner] ? VLKTVMVViewStateOwnerAudience : VLKTVMVViewStateAudience];
        [VLToast toast:KTVLocalizedString(@"ktv_join_chorus_failed")];
        return;
    }
    
    if (![self getJoinChorusEnable]) {
        KTVLogInfo(@"getJoinChorusEnable false");
        [self.MVView setMvState: [self isRoomOwner] ? VLKTVMVViewStateOwnerAudience : VLKTVMVViewStateAudience];
        [VLToast toast:KTVLocalizedString(@"ktv_join_chorus_failed")];
        return;
    }
    
    //没有上麦需要先上麦
    if ([self getCurrentUserSeatInfo] == nil) {
        VL(weakSelf);
        [self enterSeatWithIndex:nil completion:^(NSError *error) {
            if(error){
                KTVLogError(@"enterSeat error:%@", error.description);
               // weakSelf.MVView.joinCoSingerState = KTVJoinCoSingerStateWaitingForJoin;
                [self.MVView setMvState: [self isRoomOwner] ? VLKTVMVViewStateOwnerAudience : VLKTVMVViewStateAudience];
                weakSelf.isJoinChorus = NO;
                [VLToast toast:KTVLocalizedString(@"ktv_join_chorus_failed")];
                return;
            }
            [weakSelf _joinChorus];
        }];
        
//        //TODO(chenpan):没有空麦位，show error
//        [VLToast toast:KTVLocalizedString(@"ktv_mic_full")];
        return;
    }
    
    [self _joinChorus];
}

-(void)joinChorusFailedAndUIUpadte {
    
}

- (void)_joinChorus {
    
    [self.MVView.incentiveView reset];
    
    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
    KTVSingRole role = KTVSingRoleCoSinger;
    KTVSongConfiguration* songConfig = [[KTVSongConfiguration alloc] init];
    songConfig.mode = KTVLoadMusicModeLoadMusicOnly;
    songConfig.mainSingerUid = [model.owner.userId integerValue];
    songConfig.songIdentifier = model.songNo;
    
    VL(weakSelf);
    self.loadMusicCallBack = ^(BOOL isSuccess, NSInteger songCode) {
        if (!isSuccess) {
            weakSelf.isJoinChorus = NO;
           [VLToast toast:KTVLocalizedString(@"ktv_join_chorus_failed")];
            return;
        }
        
        [weakSelf.ktvApi startSingWithSongCode:songCode startPos:0];
        KTVLogInfo(@"before switch role, load music success");
        [weakSelf.ktvApi switchSingerRoleWithNewRole:role
                                   onSwitchRoleState:^( KTVSwitchRoleState state, KTVSwitchRoleFailReason reason) {
            if (state == KTVSwitchRoleStateFail && reason != KTVSwitchRoleFailReasonNoPermission) {
                [weakSelf.MVView setMvState: [weakSelf isRoomOwner] ? VLKTVMVViewStateOwnerAudience : VLKTVMVViewStateAudience];
                weakSelf.isJoinChorus = NO;
                [VLToast toast:[NSString stringWithFormat:@"join chorus fail: %ld", reason]];
                KTVLogInfo(@"join chorus fail");
                //TODO: error toast?
                [VLToast toast:KTVLocalizedString(@"ktv_join_chorus_failed")];
                return;
            }

            [weakSelf.MVView setMvState: [weakSelf isRoomOwner] ? VLKTVMVViewStateOwnerChorus : VLKTVMVViewStateNotOwnerChorus];
            weakSelf.isJoinChorus = NO;
            
            weakSelf.isNowMicMuted = role == KTVSingRoleAudience;

            VLRoomSelSongModel *selSongModel = weakSelf.selSongsArray.firstObject;
            [[AppContext ktvServiceImp] joinChorusWithSongCode:selSongModel.songNo completion:^(NSError * error) {
            }];
            
            //开麦
            [[AppContext ktvServiceImp] updateSeatAudioMuteStatusWithMuted:NO completion:^(NSError * error) {
            }];
            weakSelf.aecLevel = 0;
            weakSelf.aecState = false;
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
            [[AppContext ktvServiceImp] removeSongWithSongCode:removed.songNo completion:^(NSError * error) {
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
                                                sceneType:@"ktv"
                                                  success:^(NSString * msg) {
        KTVLogInfo(@"voiceIdentify success: %@", msg);
    }];
}

- (void)joinRTCChannel {
    KTVLogInfo(@"joinRTCChannel");
    self.RTCkit = [AgoraRtcEngineKit sharedEngineWithAppId:[AppContext.shared appId] delegate:self];
    
    //use game streaming in so mode, chrous profile in chrous mode
    [self.RTCkit setAudioScenario:AgoraAudioScenarioGameStreaming];
    [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQualityStereo];
    [self.RTCkit setChannelProfile:AgoraChannelProfileLiveBroadcasting];
    if(AppContext.shared.isDebugMode){
        [self.RTCkit setParameters: @"{\"che.audio.neteq.dump_level\": 1}"];
    }
    [self.RTCkit setParameters: @"{\"che.audio.input_sample_rate\": 48000}"];
    
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
    self.aecLevel = 1;
    self.aecState = true;
    [self.RTCkit setEnableSpeakerphone:YES];
    
    AgoraDataStreamConfig *config = [AgoraDataStreamConfig new];
    config.ordered = NO;
    config.syncWithAudio = NO;
    [self.RTCkit createDataStream:&ktvStreamId
                           config:config];
    
    NSString* exChannelToken = AppContext.shared.agoraRTCToken;
    KTVApiConfig* apiConfig = [[KTVApiConfig alloc] initWithAppId:[[AppContext shared] appId]
                                                         rtmToken:AppContext.shared.agoraRTMToken
                                                           engine:self.RTCkit
                                                      channelName:self.roomModel.roomNo
                                                         localUid:[VLUserCenter.user.id integerValue]
                                                        chorusChannelName:[NSString stringWithFormat:@"%@_ex", self.roomModel.roomNo] chorusChannelToken:exChannelToken
                                                             type: KTVTypeNormal
                                                        musicType:loadMusicTypeMcc
                                                        maxCacheSize:10
                                                        mccDomain: AppContext.shared.isDebugMode ? @"api-test.agora.io" : nil
    ];
    self.ktvApi = [[KTVApiImpl alloc] init];
    [self.ktvApi createKtvApiWithConfig:apiConfig];
    [self.ktvApi renewInnerDataStreamId];
    KTVLrcControl* lrcControl = [[KTVLrcControl alloc] initWithLrcView:self.MVView.karaokeView];
    [self.ktvApi setLrcViewWithView:lrcControl];
    self.lrcControl = lrcControl;
    self.lrcControl.delegate = self;
    VL(weakSelf);
    lrcControl.skipCallBack = ^(NSInteger time, BOOL flag) {
        NSInteger seekTime = flag ? [weakSelf.ktvApi getMusicPlayer].getDuration - 800 : time;
        [weakSelf.ktvApi seekSingWithTime:seekTime];
    };
    [self.ktvApi muteMicWithMuteStatus:self.isNowMicMuted];
    [self.ktvApi addEventHandlerWithKtvApiEventHandler:self];
    
    [self.RTCkit setParameters:@"{\"che.audio.ains_mode\": -1}"];
    
    [self.RTCkit setAudioEffectPreset:AgoraAudioEffectPresetRoomAcousticsKTV];
    
//    VL(weakSelf);
    KTVLogInfo(@"Agora - joining RTC channel for roomNo: %@, with uid: %@", self.roomModel.roomNo, VLUserCenter.user.id);
    int ret =
    [self.RTCkit joinChannelByToken:AppContext.shared.agoraRTCToken
                          channelId:self.roomModel.roomNo
                                uid:[VLUserCenter.user.id integerValue]
                       mediaOptions:[self channelMediaOptions]
                        joinSuccess:nil];
    if (ret != 0) {
        KTVLogError(@"joinChannelByToken fail: %d, uid: %ld, token: %@", ret, [VLUserCenter.user.id integerValue], AppContext.shared.agoraRTCToken);
    }
    
    VLRoomSeatModel* info = [self getCurrentUserSeatInfo];
    if (info) {
        [self _checkEnterSeatAudioAuthorized];
//
//        if (!info.isVideoMuted) {
//            [AgoraEntAuthorizedManager checkCameraAuthorizedWithParent:self completion:nil];
//        }
        self.isNowMicMuted = info.isAudioMuted;
        self.isNowCameraMuted = info.isVideoMuted;
    } else {
        self.isNowMicMuted = YES;
        self.isNowCameraMuted = YES;
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
    [option setPublishMediaPlayerId:[[self.ktvApi getMusicPlayer] getMediaPlayerId]];
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
    };
}

#pragma mark -- VLKTVAPIDelegate

- (void)didLrcViewDragedToPos:(NSInteger)pos score:(NSInteger)score totalScore:(NSInteger)totalScore{
    [self.ktvApi.getMusicPlayer seekToPosition:pos];
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
    KTVLogInfo(@"index: %li, score: %li, cumulativeScore: %li, total: %li", lineIndex, lineScore, cumulativeScore, totalScore);
}

- (void)didSongLoadedWith:(LyricModel *)model{
    self.lyricModel = model;
}

- (void)onChorusChannelAudioVolumeIndicationWithSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers totalVolume:(NSInteger)totalVolume{
    
}

- (void)didJoinChours {
    //加入合唱
    [self.MVView setMvState:VLKTVMVViewStateJoinChorus];
    [self joinChorus];
}

-(void)didLeaveChours {
    //退出合唱
    [[AppContext ktvServiceImp] leaveChorusWithSongCode:self.selSongsArray.firstObject.songNo
                                             completion:^(NSError * error) {
    }];
    [self stopPlaySong];
    self.isNowMicMuted = true;
    [self.MVView.gradeView reset];
    [self.MVView.incentiveView reset];
    [self.MVView setOriginBtnState: VLKTVMVViewActionTypeSingAcc];
    [[AppContext ktvServiceImp] updateSeatAudioMuteStatusWithMuted:YES
                                                        completion:^(NSError * error) {
    }];
}

#pragma mark -- VLKTVTopViewDelegate
- (void)onVLKTVTopView:(VLKTVTopView *)view closeBtnTapped:(id)sender {
    VL(weakSelf);
    BOOL isOwner = [self.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
    NSString *title = isOwner ? KTVLocalizedString(@"ktv_disband_room") : KTVLocalizedString(@"ktv_exit_room");
    NSString *message = isOwner ? KTVLocalizedString(@"ktv_confirm_disband_room") : KTVLocalizedString(@"ktv_confirm_exit_room");
    NSArray *array = [[NSArray alloc]initWithObjects:KTVLocalizedString(@"ktv_cancel"),KTVLocalizedString(@"ktv_confirm"), nil];
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:title message:message placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == YES){
            [weakSelf leaveRoom];
        }
        [[VLAlert shared] dismiss];
    }];
}

- (void)onVLKTVTopView:(VLKTVTopView *)view moreBtnTapped:(id)sender {
    AUiMoreDialog* dialog = [[AUiMoreDialog alloc] initWithFrame:self.view.bounds];
    [self.view addSubview:dialog];
    [dialog show];
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
//        case VLKTVMoreBtnClickTypeSound:
//            [self popSetSoundEffectView];
//            break;
        case VLKTVMoreBtnClickTypeSetting:
            [self popVoicePerView];
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
//            [self popSelMoreView];
            [self showSettingView];
            break;
        case VLKTVBottomBtnClickTypeJoinChorus:
            [self popUpChooseSongView:YES];
            break;
        case VLKTVBottomBtnClickTypeChoose:
            [self popUpChooseSongView:NO];
            break;
        case VLKTVBottomBtnClickTypeAudio:
            [[AppContext ktvServiceImp] updateSeatAudioMuteStatusWithMuted:!self.isNowMicMuted
                                                                completion:^(NSError * error) {
            }];
            break;
        case VLKTVBottomBtnClickTypeVideo:
            [[AppContext ktvServiceImp] updateSeatVideoMuteStatusWithMuted:!self.isNowCameraMuted
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
    
    BOOL isOwner = [self.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
    if(isOwner) {
        //is owner
        if ([model.owner.userId isEqualToString:VLUserCenter.user.id]) {
            //self, return
            return;
        }
        if (model.owner.userId.length > 0) {
            return [self popDropLineViewWithSeatModel:model];
        }
    } else {
        if (model.owner.userId.length > 0) {
            //occupied
            if ([model.owner.userId isEqualToString:VLUserCenter.user.id]) {//点击的是自己
                return [self popDropLineViewWithSeatModel:model];
            }
        } else{
            //empty
            BOOL isOnSeat = [self getCurrentUserSeatInfo] == nil ? NO : YES;
            if (!isOnSeat) {
                //not yet seated
                [self enterSeatWithIndex:@(seatIndex) completion:^(NSError *error) {
                }];
            }
        }
    }
}

- (void)onVLRoomPersonView:(VLMicSeatList *)view onRenderVideo:(VLRoomSeatModel *)model inView:(UIView *)videoView atIndex:(NSInteger)seatIndex
{
    AgoraRtcVideoCanvas *videoCanvas = [[AgoraRtcVideoCanvas alloc] init];
    videoCanvas.uid = [model.owner.userId unsignedIntegerValue];
    videoCanvas.view = videoView;
    videoCanvas.renderMode = AgoraVideoRenderModeHidden;
    if([model.owner.userId isEqual:VLUserCenter.user.id]) {
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
    NSAssert(true, @"need remove");
//    KTVChangeMVCoverInputModel* inputModel = [KTVChangeMVCoverInputModel new];
////    inputModel.roomNo = self.roomModel.roomNo;
//    inputModel.mvIndex = index;
////    inputModel.userNo = VLUserCenter.user.id;
//    VL(weakSelf);
//    [[AppContext ktvServiceImp] changeMVCoverWithInputModel:inputModel
//                                                 completion:^(NSError * error) {
//        if (error != nil) {
//            [VLToast toast:error.description];
//            return;
//        }
//        
//        [[LSTPopView getPopViewWithCustomView:view] dismiss];
//        weakSelf.choosedBgModel = selBgModel;
//    }];
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
            [VLToast toast:KTVLocalizedString(@"ktv_change_failed")];
            return;
        }
        
        VL(weakSelf);

        NSString *title = KTVLocalizedString(@"ktv_change_song");
        NSString *message = KTVLocalizedString(@"ktv_change_next_song");
        NSArray *array = [[NSArray alloc]initWithObjects:KTVLocalizedString(@"ktv_cancel"),KTVLocalizedString(@"ktv_confirm"), nil];
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
    } else if (type == VLKTVMVViewActionTypeSingLead){
        self.trackMode = KTVPlayerTrackModeLead; //导唱
    }
}

- (void)onKTVMView:(VLKTVMVView *)view lrcViewDidScrolled:(NSInteger)position {
    [[self.ktvApi getMusicPlayer] seekToPosition:position];
}

- (void)reloadMusic{
    VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
    KTVSongConfiguration* songConfig = [[KTVSongConfiguration alloc] init];
//    KTVSingRole role = [self getUserSingRole];
    songConfig.mode = KTVLoadMusicModeLoadLrcOnly;
    songConfig.mainSingerUid = [model.owner.userId integerValue];
    songConfig.songIdentifier = model.songNo;

    [self.MVView setMvState:VLKTVMVViewStateMusicLoading];
    VL(weakSelf);
    self.loadMusicCallBack = ^(BOOL isSuccess, NSInteger songCode) {
        if (!isSuccess) {
            return;
        }
        [weakSelf setMVViewStateWith:model];
    };
    
    [self.ktvApi loadMusicWithSongCode:[model.songNo integerValue] config:songConfig onMusicLoadStateListener:self];
}

#pragma mark - VLKTVSettingViewDelegate
- (void)settingViewSettingChanged:(VLKTVSettingModel *)setting
              valueDidChangedType:(VLKTVValueDidChangedType)type {
    self.settingModel = setting;
    if (type == VLKTVValueDidChangedTypeEar) { // 耳返设置
        // 用户必须使用有线耳机才能听到耳返效果
        // 1、不在耳返中添加audiofilter
        // AgoraEarMonitoringFilterNone
        // 2: 在耳返中添加人声效果 audio filter。如果你实现了美声、音效等功能，用户可以在耳返中听到添加效果后的声音。
        // AgoraEarMonitoringFilterBuiltInAudioFilters
        // 4: 在耳返中添加降噪 audio filter。
        // AgoraEarMonitoringFilterNoiseSuppression
        // [self.RTCkit enableInEarMonitoring:setting.soundOn includeAudioFilters:AgoraEarMonitoringFilterBuiltInAudioFilters | AgoraEarMonitoringFilterNoiseSuppression];
        [self showEarSettingView];
    } else if (type == VLKTVValueDidChangedTypeMV) { // MV
        
    } else if (type == VLKTVValueDidChangedTypeSoundCard) { // 虚拟声卡
        [self showSoundCardView];
    } else if (type == VLKTVValueDidChangedRiseFall) { // 升降调
        // 调整当前播放的媒体资源的音调
        // 按半音音阶调整本地播放的音乐文件的音调，默认值为 0，即不调整音调。取值范围为 [-12,12]，每相邻两个值的音高距离相差半音。取值的绝对值越大，音调升高或降低得越多
        NSInteger value = setting.toneValue * 2 - 12;
        [[self.ktvApi getMusicPlayer] setAudioPitch:value];
    } else if (type == VLKTVValueDidChangedTypeSound) { // 音量
        // 调节音频采集信号音量、取值范围为 [0,400]impl
        // 0、静音 100、默认原始音量 400、原始音量的4倍、自带溢出保护
        if(self.soundVolume != setting.soundValue){
            [self.RTCkit adjustRecordingSignalVolume:setting.soundValue];
            self.soundVolume = setting.soundValue;
        }
    } else if (type == VLKTVValueDidChangedTypeAcc) { // 伴奏
        int value = setting.accValue;
        if(self.playoutVolume != value){
            self.playoutVolume = value;
        }
    } else if (type == VLKTVValueDidChangedTypeListItem) {
        AgoraAudioEffectPreset preset = [self audioEffectPreset:setting.kindIndex];
        [self.RTCkit setAudioEffectPreset:preset];
    } else if (type == VLKTVValueDidChangedTypeRemoteValue) {
        [self.RTCkit adjustPlaybackSignalVolume:setting.remoteVolume];
    } else if (type == VLKTVValueDidChangedTypeLrc) {//歌词等级
        LRCLEVEL level = LRCLEVELMID;
        if(setting.lrcLevel == 0){
            level = LRCLEVELOW;
        } else if(setting.lrcLevel == 1){
            level = LRCLEVELMID;
        } else if(setting.lrcLevel == 2){
            level = LRCLEVELHIGH;
        }
        [self.MVView setLrcLevelWith:level];
    } else if (type == VLKTVValueDidChangedTypeVqs) {//音质
        [self didVolQualityGradeChangedWithIndex:setting.vqs];
    } else if (type == VLKTVValueDidChangedTypeAns) {//降噪
        [self didAIAECGradeChangedWithIndex:setting.ans];
    } else if (type == VLKTVValueDidChangedTypeaiaec) {//AIAEC
        [self didAECStateChange:setting.enableAec];
    } else if (type == VLKTVValueDidChangedTypebro) {//专业主播
        [self voicePerItemSelectedAction:setting.isPerBro];
    } else if (type == VLKTVValueDidChangedTypeAecLevel){
        [self didAECLevelSetWith:setting.aecLevel];
    } else if (type == VLKTVValueDidChangedTypeenableMultipath){
        [self.ktvApi enableMutipathWithEnable:setting.enableMultipath];
    }
}

- (void)settingViewSettingChanged:(VLKTVSettingModel *)setting effectChoosed:(NSInteger)effectIndex { 
    
}


-(void)showEarSettingView {
    [LSTPopView popEarSettingViewWithParentView:self.view isEarOn:_isEarOn vol:self.earValue withDelegate:self];
}

-(void)showSoundCardView {
    self.soundSettingView = [[SoundCardSettingView alloc] init];
    self.soundSettingView.soundOpen = self.soundOpen;
    self.soundSettingView.gainValue = [self.gainValue floatValue];
    self.soundSettingView.effectType = self.effectType;
    self.soundSettingView.typeValue = self.typeValue;
    kWeakSelf(self);
    self.soundSettingView.clicKBlock = ^(NSInteger index) {
        if(index == 2){
            //音效设置
            [weakself showSoundEffectView];
        } else if (index == 4) {
            //麦克风设置
            [weakself showSoundMicTypeView];
        }
    };
    self.soundSettingView.gainBlock = ^(float gain) {
        NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
        [formatter setRoundingMode:NSNumberFormatterRoundHalfUp];
        [formatter setMaximumFractionDigits:1];
        NSString *formattedString = [formatter stringFromNumber:@(gain)];
        weakself.gainValue = formattedString;
        [weakself didUpdateGainValue:formattedString];
    };
    
    self.soundSettingView.typeBlock = ^(NSInteger index) {
        weakself.typeValue = index;
        [weakself didUpdateTypeValue:index];
    };
    
    self.soundSettingView.soundCardBlock = ^(BOOL flag) {
        weakself.soundOpen = flag;
        [weakself didUpdateSoundSetting:flag];
    };
    self.popSoundSettingView = [LSTPopView popSoundCardViewWithParentView:self.view soundCardView:self.soundSettingView];
}

-(void)showSoundEffectView {
    SoundCardEffectView *effectView = [[SoundCardEffectView alloc]init];
    effectView.effectType = self.effectType;
    LSTPopView* popEffectView = [LSTPopView popSoundCardViewWithParentView:self.view soundCardView:effectView];
    kWeakSelf(self);
    effectView.clickBlock = ^(NSInteger index) {
        [LSTPopView removePopView:popEffectView];
        //根据不同的音效设置不同的参数 同时更新设置界面UI
        if(index >= 0){
            weakself.effectType = index;
            [weakself didUpdateEffectValue:index];
            [LSTPopView removePopView:self.popSoundSettingView];
            [weakself showSoundCardView];
        } else if (index == -2){
            //            [weakself.soundSettingView setSoundOpen:false];
            //            [LSTPopView removePopView:self.popSoundSettingView];
            //            [weakself showSoundCardView];
        }
    };
}

-(void)showSoundMicTypeView {
    SoundCardMicTypeView *micTypeView = [[SoundCardMicTypeView alloc]init];
    micTypeView.micType = self.typeValue;
    LSTPopView* popMicView = [LSTPopView popSoundCardViewWithParentView:self.view soundCardView:micTypeView];
    kWeakSelf(self);
    micTypeView.clickBlock = ^(NSInteger index) {
        [LSTPopView removePopView:popMicView];
        //根据不同的音效设置不同的参数 同时更新设置界面UI
        if(index >= 0){
            weakself.typeValue = index;
            [weakself didUpdateTypeValue:index];
            [LSTPopView removePopView:self.popSoundSettingView];
            [weakself showSoundCardView];
        }  else if (index == -2){
            //            [weakself.soundSettingView setSoundOpen:false];
            //            [LSTPopView removePopView:self.popSoundSettingView];
            //            [weakself showSoundCardView];
        }
    };
}

- (void)settingViewEffectChoosed:(NSInteger)effectIndex {
    self.selectedEffectIndex = effectIndex;
    NSArray *effects = @[@(AgoraAudioEffectPresetRoomAcousticsKTV),
                         @(AgoraAudioEffectPresetOff),
                         @(AgoraAudioEffectPresetRoomAcousVocalConcer),
                         @(AgoraAudioEffectPresetRoomAcousStudio),
                         @(AgoraAudioEffectPresetRoomAcousPhonograph),
                         @(AgoraAudioEffectPresetRoomAcousSpatial),
                         @(AgoraAudioEffectPresetRoomAcousEthereal),
                         @(AgoraAudioEffectPresetStyleTransformationPopular),
                         @(AgoraAudioEffectPresetStyleTransformationRnb)];
    self.currentSelectEffect = [effects[effectIndex] integerValue];
    [self.RTCkit setAudioEffectPreset: [effects[effectIndex] integerValue]];
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
}

//专业主播设置
- (void)voicePerItemSelectedAction:(BOOL)isSelected {
    self.isProfessional = isSelected;
    [self.ktvApi enableProfessionalStreamerMode:isSelected];
}

- (void)didAIAECGradeChangedWithIndex:(NSInteger)index{
    self.aecGrade = index;
    [self onAINSModeChangedWithMode:index];
}

-(void)didVolQualityGradeChangedWithIndex:(NSInteger)index {
    self.volGrade = index;
    [self onAECLevelChangedWithLevel:index];
}

- (void)voiceDelaySelectedAction:(BOOL)isSelected{
    self.isDelay = isSelected;
    [self enableLowLatencyMode:isSelected];
}

- (void)onAECLevelChangedWithLevel:(NSInteger)level {
    if (level == 0) {
        [self.RTCkit setParameters:@"{\"che.audio.aec.split_srate_for_48k\": 16000}"];
    } else if (level == 1) {
        [self.RTCkit setParameters:@"{\"che.audio.aec.split_srate_for_48k\": 24000}"];
    } else if (level == 2) {
        [self.RTCkit setParameters:@"{\"che.audio.aec.split_srate_for_48k\": 48000}"];
    }
}

- (void)onAINSModeChangedWithMode:(NSInteger)mode {
    if (mode == 0) {
        // 关闭
        [self.RTCkit setParameters:@"{\"che.audio.ains_mode\": 0}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.lowerBound\": 80}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.lowerMask\": 50}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.statisticalbound\": 5}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.finallowermask\": 30}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.enhfactorstastical\": 200}"];
    } else if (mode == 1) {
        // 中
        [self.RTCkit setParameters:@"{\"che.audio.ains_mode\": 2}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.lowerBound\": 80}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.lowerMask\": 50}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.statisticalbound\": 5}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.finallowermask\": 30}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.enhfactorstastical\": 200}"];
    } else if (mode == 2) {
        // 高
        [self.RTCkit setParameters:@"{\"che.audio.ains_mode\": 2}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.lowerBound\": 10}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.lowerMask\": 10}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.statisticalbound\": 0}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.finallowermask\": 8}"];
        [self.RTCkit setParameters:@"{\"che.audio.nsng.enhfactorstastical\": 200}"];
    }
}

- (void)enableLowLatencyMode:(BOOL)enable {
    if (enable) {
        //  [self.RTCkit setParameters:@"{\"che.audio.aiaec.working_mode\": 0}"];
        [self.RTCkit setParameters:@"{\"che.audio.ains_mode\": -1}"];
        //        [self.RTCkit setParameters:@"{\"che.audio.aec.nlp_size\": 128}"];
        //        [self.RTCkit setParameters:@"{\"che.audio.aec.nlp_size\": 64}"];
    } else {
        //   [self.RTCkit setParameters:@"{\"che.audio.aiaec.working_mode\": 0}"];
        [self.RTCkit setParameters:@"{\"che.audio.ains_mode\": 0}"];
        //        [self.RTCkit setParameters:@"{\"che.audio.aiaec.working_mode\": 512}"];
        //        [self.RTCkit setParameters:@"{\"che.audio.aec.nlp_hop_size\": 64}"];
    }
}

-(void)didAECStateChange:(BOOL)enable{
    self.aecState = enable;
    if(enable){
        [self.RTCkit setParameters:@"{\"che.audio.aiaec.working_mode\": 1}"];
    } else {
        [self.RTCkit setParameters:@"{\"che.audio.aiaec.working_mode\": 0}"];
    }
}

-(void)didAECLevelSetWith:(NSInteger)level{
    self.aecLevel = level;
    [self.RTCkit setParameters:[NSString stringWithFormat:@"{\"che.audio.aiaec.postprocessing_strategy\":%li}", (long)level]];
}

#pragma mark --
- (void)_fetchServiceAllData {
    //请求已点歌曲
    VL(weakSelf);
    [self refreshChoosedSongList:^{
        //请求歌词和歌曲
        if(weakSelf.selSongsArray.count == 0){
            return;
        }
        [weakSelf loadAndPlaySong];
    }];
}

#pragma mark - getter/handy utils
- (BOOL)isCurrentSongMainSinger:(NSString *)userNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    return [selSongModel.owner.userId isEqualToString:userNo];
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
    return [self gettUserSeatInfoWithUserId:VLUserCenter.user.id];
}

- (VLRoomSeatModel*)gettUserSeatInfoWithUserId:(NSString*)userId {
    for (VLRoomSeatModel *model in self.seatsArray) {
        if ([model.owner.userId isEqualToString:userId]) {
            return model;
        }
    }
    
    return nil;
}

-(BOOL)checkIfCosingerWith:(NSInteger)index{
    VLRoomSeatModel *model = self.seatsArray[index];
    return [AppContext isKtvChorusingWithSeat:model];
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
    BOOL currentSongIsJoinSing = [AppContext isKtvChorusingWithUserId:VLUserCenter.user.id];
    BOOL currentSongIsSongOwner = [AppContext isKtvSongOwnerWithUserId:VLUserCenter.user.id];
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
    for(VLRoomSeatModel* seat in seatArray) {
        //TODO: validate songCode
        if([AppContext isKtvChorusingWithSeat:seat]) {
            chorusNum += 1;
        }
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
            if(model.owner.userId.length > 0){
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
    self.roomModel.roomPeopleNum = userCount;
    self.topView.listModel = self.roomModel;
}

- (void)setChoosedBgModel:(VLKTVSelBgModel *)choosedBgModel {
    _choosedBgModel = choosedBgModel;
    [self.MVView changeBgViewByModel:choosedBgModel];
}

-(void)setVoiceShowHasSeted:(BOOL)voiceShowHasSeted {
    _voiceShowHasSeted = voiceShowHasSeted;
}

-(void)setSelectedVoiceShowIndex:(NSInteger)selectedVoiceShowIndex {
    _selectedVoiceShowIndex = selectedVoiceShowIndex;
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
    if(!_isOnMicSeat && count >= 8){
        VLRoomSelSongModel *topSong = self.selSongsArray.firstObject;
        if(topSong){
            [self.MVView setMvState:[self isRoomOwner] ? VLKTVMVViewStateOwnerAudience : VLKTVMVViewStateAudience];
        } else {
            [self.MVView setMvState:VLKTVMVViewStateNone];
        }
    } else {
        if(!self.isJoinChorus){
            VLRoomSelSongModel *topSong = self.selSongsArray.firstObject;
            if(topSong){
                [self.MVView setMvState:[self isRoomOwner] ? VLKTVMVViewStateOwnerAudience : VLKTVMVViewStateAudience];
            } else {
                [self.MVView setMvState:VLKTVMVViewStateNone];
            }
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
    
    self.bottomView.hidden = !isOnMicSeat;
    self.requestOnLineView.hidden = isOnMicSeat;
    
//    VLRoomSeatModel* info = [self getCurrentUserSeatInfo];
//    if(onMicSeatStatusDidChanged){
//        if(info == nil){
//            self.isNowMicMuted = true;
//            self.isNowCameraMuted = true;
//        } else {
//            self.isNowMicMuted = info.isAudioMuted;
//            self.isNowCameraMuted = info.isVideoMuted;
//        }
//    }
}

- (void)setIsNowMicMuted:(BOOL)isNowMicMuted {
    BOOL oldValue = _isNowMicMuted;
    _isNowMicMuted = isNowMicMuted;
    
    [self.ktvApi muteMicWithMuteStatus:isNowMicMuted];
    [self.RTCkit adjustRecordingSignalVolume:isNowMicMuted ? 0 : 100];
    if(oldValue != isNowMicMuted) {
        [self.bottomView updateAudioBtn:isNowMicMuted];
    }
}

- (void)setIsNowCameraMuted:(BOOL)isNowCameraMuted {
    BOOL oldValue = _isNowCameraMuted;
    _isNowCameraMuted = isNowCameraMuted;
    
    [self.RTCkit enableLocalVideo:!isNowCameraMuted];
    if (isNowCameraMuted) {
        [self.RTCkit stopPreview];
    }
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
    [[self.ktvApi getMusicPlayer] adjustPlayoutVolume:playoutVolume];
    
    // 调节远端用户听到的音量 取值范围[0、400]
    // 100: （默认）媒体文件的原始音量。400: 原始音量的四倍（自带溢出保护）
    [[self.ktvApi getMusicPlayer] adjustPublishSignalVolume:playoutVolume];
    
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
        BOOL isOwner = [self.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
        [self.chooseSongView setSelSongsArray:_selSongsArray isOwner:isOwner]; //刷新已点歌曲UI
    }
    
    [self.roomPersonView updateSingBtnWithChoosedSongArray:_selSongsArray];
    VLRoomSelSongModel* originalTopSong = [oldSongsArray firstObject];
    VLRoomSelSongModel* updatedTopSong = [selSongsArray firstObject];
    KTVLogInfo(@"setSelSongsArray current top[%@] songName: %@, status: %ld",
               updatedTopSong.songNo, updatedTopSong.songName, updatedTopSong.status);
    KTVLogInfo(@"setSelSongsArray orig top[%@] songName: %@, status: %ld",
               updatedTopSong.songNo, originalTopSong.songName, originalTopSong.status);
    if(![updatedTopSong.songNo isEqualToString:originalTopSong.songNo]){
        [self.MVView reset];
        [self.lrcControl resetLrc];
        //song changes
        [self stopPlaySong];
        
        if(self.selSongsArray.count == 0){
            [self.MVView setMvState:VLKTVMVViewStateNone];
            return;
        }
        
        [self loadAndPlaySong];
    }
}

- (void)setTrackMode:(KTVPlayerTrackMode)trackMode {
    KTVLogInfo(@"setTrackMode: %ld", trackMode);
    _trackMode = trackMode;
    VLKTVMVViewActionType type = VLKTVMVViewActionTypeSingAcc;
    if(self.singRole == KTVSingRoleCoSinger){
        [[self.ktvApi getMusicPlayer] selectAudioTrack:self.trackMode == KTVPlayerTrackModeOrigin ? 0 : 1];
        type = trackMode == KTVPlayerTrackModeOrigin ? VLKTVMVViewActionTypeSingOrigin : VLKTVMVViewActionTypeSingAcc;
        [self.MVView setOriginBtnState: type];
        return;
    } else if(self.singRole == KTVSingRoleSoloSinger || self.singRole == KTVSingRoleLeadSinger) {
        [self.MVView setOriginBtnState: trackMode == KTVPlayerTrackModeOrigin ? VLKTVMVViewActionTypeSingOrigin : VLKTVMVViewActionTypeSingAcc];
        [[self.ktvApi getMusicPlayer] selectMultiAudioTrack:trackMode == KTVPlayerTrackModeAcc ? 1 : 0 publishTrackIndex:trackMode == KTVPlayerTrackModeOrigin ? 0 : 1 ];
        
        switch (trackMode) {
            case KTVPlayerTrackModeOrigin:
                type = VLKTVMVViewActionTypeSingOrigin;
                break;
            case KTVPlayerTrackModeAcc:
                type = VLKTVMVViewActionTypeSingAcc;
                break;
            case KTVPlayerTrackModeLead:
                type = VLKTVMVViewActionTypeSingLead;
                break;
            default:
                break;
        }
        [self.MVView setOriginBtnState: type];
    }
}

- (void)setSingRole:(KTVSingRole)singRole {
    KTVLogInfo(@"setSingRole: %ld", singRole);
    _singRole = singRole;
    self.lrcControl.lrcView.lyricsView.draggable = false;
    self.lrcControl.isMainSinger = (_singRole == KTVSingRoleSoloSinger || _singRole == KTVSingRoleLeadSinger);
    
    self.MVView.isOriginLeader = (_singRole == KTVSingRoleSoloSinger || _singRole == KTVSingRoleLeadSinger);
}

#pragma mark KTVApiEventHandlerDelegate
- (void)onMusicPlayerStateChangedWithState:(AgoraMediaPlayerState)state error:(AgoraMediaPlayerError)error isLocal:(BOOL)isLocal {
    dispatch_async(dispatch_get_main_queue(), ^{
        if(state == AgoraMediaPlayerStatePlaying) {
            //显示跳过前奏
            if(self.singRole == KTVSingRoleSoloSinger || self.singRole == KTVSingRoleLeadSinger){
                [self.lrcControl showPreludeEnd];
            }
        } else if(state == AgoraMediaPlayerStatePaused) {
            //    [self.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPause];
            [self.lrcControl hideSkipViewWithFlag:true];
        } else if(state == AgoraMediaPlayerStateStopped) {
            
        } else if(state == AgoraMediaPlayerStatePlayBackAllLoopsCompleted || state == AgoraMediaPlayerStatePlayBackCompleted) {
            if(isLocal) {
                KTVLogInfo(@"Playback all loop completed");
                // if(self.singRole != KTVSingRoleAudience){
                //伴唱和房主都用自己的分数
                if(self.singRole == KTVSingRoleLeadSinger || self.singRole == KTVSingRoleSoloSinger){
                    [self syncChoruScore:[self.lrcControl getAvgScore]];
                    [self showScoreViewWithScore: [self.lrcControl getAvgScore]];
                    [self removeCurrentSongWithSync:YES];
                }
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
    
    KTVLogInfo(@"onSingerRoleChangedWithOldRole oldRole: %ld, newRole: %ld", oldRole, newRole);
    self.singRole = newRole;
}

- (void)onSingingScoreResultWithScore:(float)score {
}

- (void)onTokenPrivilegeWillExpire {
    
}

- (void)onMusicPlayerProgressChangedWith:(NSInteger)progress { 
    
}


#pragma mark KTVMusicLoadStateListener

- (void)onMusicLoadProgressWithSongCode:(NSInteger)songCode
                                percent:(NSInteger)percent
                                 status:(AgoraMusicContentCenterPreloadStatus)status
                                    msg:(NSString *)msg
                               lyricUrl:(NSString *)lyricUrl {
    dispatch_async_on_main_queue(^{
        VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
        if (![model.songNo isEqualToString:[NSString stringWithFormat:@"%ld", songCode]]) {
            KTVLogInfo(@"onMusicLoadProgressWithSongCode break songCode missmatch %@/%ld percent: %ld", model.songNo, songCode, percent);
            return;
        }
        KTVLogInfo(@"onMusicLoadProgressWithSongCode songCode %@/%ld percent: %ld", model.songNo, songCode, percent);
        if(status == AgoraMusicContentCenterPreloadStatusError){
            [VLToast toast:KTVLocalizedString(@"ktv_load_failed_and_change")];
            if(self.loadMusicCallBack) {
                self.loadMusicCallBack(NO, songCode);
                self.loadMusicCallBack = nil;
            }
            return;
        }
        
        if (status == AgoraMusicContentCenterPreloadStatusOK){
        }
        
        VLRoomSelSongModel *topSong = self.selSongsArray.firstObject;
        
        if(self.singRole == KTVSingRoleSoloSinger 
           || self.singRole == KTVSingRoleLeadSinger
           || self.isJoinChorus
           || [topSong.owner.userId isEqualToString:VLUserCenter.user.id]){
            self.MVView.loadingProgress = percent;
        }
    });
}

- (void)onMusicLoadFailWithSongCode:(NSInteger)songCode reason:(enum KTVLoadSongFailReason)reason{
    dispatch_async_on_main_queue(^{
        KTVLogError(@"onMusicLoadFail songCode: %ld reason: %ld", songCode, reason);
        VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
        if (![model.songNo isEqualToString:[NSString stringWithFormat:@"%ld", songCode]]) {
            KTVLogInfo(@"onMusicLoadFail break songCode missmatch %@/%ld", model.songNo, songCode);
            return;
        }
        if(self.loadMusicCallBack) {
            self.loadMusicCallBack(NO, songCode);
            self.loadMusicCallBack = nil;
        }
        self.MVView.loadingProgress = 100;
        if (reason == KTVLoadSongFailReasonNoLyricUrl) {
            [self.MVView setMvState:[self isRoomOwner] ? VLKTVMVViewStateMusicOwnerLoadLrcFailed : VLKTVMVViewStateMusicLoadLrcFailed];
        } else {
            BOOL isOwner = [self isRoomOwner] || [AppContext isKtvSongOwnerWithUserId:VLUserCenter.user.id];
            [self.MVView setMvState:isOwner ? VLKTVMVViewStateMusicOwnerLoadFailed : VLKTVMVViewStateMusicLoadFailed];
        }
    });
}

- (void)onMusicLoadSuccessWithSongCode:(NSInteger)songCode lyricUrl:(NSString * _Nonnull)lyricUrl {
    dispatch_async_on_main_queue(^{
        KTVLogInfo(@"onMusicLoadSuccess songCode: %ld, lyricUrl: %@", songCode, lyricUrl);
        VLRoomSelSongModel* model = [[self selSongsArray] firstObject];
        if (![model.songNo isEqualToString:[NSString stringWithFormat:@"%ld", songCode]]) {
            KTVLogInfo(@"onMusicLoadSuccess break songCode missmatch %@/%ld", model.songNo, songCode);
            return;
        }
        if(self.loadMusicCallBack){
            self.loadMusicCallBack(YES, songCode);
            self.loadMusicCallBack = nil;
            //清空分数
            [self.MVView.gradeView reset];
            [self.MVView.incentiveView reset];
        }
        
        self.MVView.loadingProgress = 100;
        if(lyricUrl.length > 0){
            KTVLogInfo(@"onMusicLoadSuccessWithSongCode: %ld role:%ld", songCode, self.singRole);
        }
        self.retryCount = 0;
    });
}


#pragma mark KTVServiceListenerProtocol
    
- (void)onRoomDidDestroy {
    KTVLogInfo(@"onRoomDidDestroy");
    BOOL isOwner = [self.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
    //房主关闭房间
    if (isOwner) {
        kWeakSelf(self);
        NSString *mes = KTVLocalizedString(@"ktv_room_exit");
        [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage ktv_sceneImageWithName:@"empty" ] message:mes buttonTitle:KTVLocalizedString(@"ktv_confirm") completion:^(bool flag, NSString * _Nullable text) {
            [[VLKTVAlert shared]dismiss];
            [weakself leaveRoom];
        }];
        return;
    }
    
    [self popForceLeaveRoom];
}

- (void)onRoomDidExpire {
    KTVLogInfo(@"onRoomDidExpire");
    BOOL isOwner = [self.roomModel.creatorNo isEqualToString:VLUserCenter.user.id];
    NSString *mes = isOwner ? KTVLocalizedString(@"ktv_room_timeout") : KTVLocalizedString(@"ktv_room_offline");
    kWeakSelf(self);
    [[VLKTVAlert shared]showKTVToastWithFrame:UIScreen.mainScreen.bounds image:[UIImage ktv_sceneImageWithName:@"empty" ] message:mes buttonTitle:KTVLocalizedString(@"ktv_confirm") completion:^(bool flag, NSString * _Nullable text) {
        [[VLKTVAlert shared]dismiss];
        [weakself leaveRoom];
    }];
}
    
    
- (void)onUserCountUpdateWithUserCount:(NSUInteger)userCount {
    [self setRoomUsersCount:userCount];
}
    
- (void)onMicSeatSnapshotWithSeat:(NSDictionary<NSString *,VLRoomSeatModel *> *)seat {
    NSMutableArray<VLRoomSeatModel*>* setsArray = [NSMutableArray array];
    for (int i = 0; i < seat.count; i++) {
        VLRoomSeatModel* model = [seat objectForKey:[NSString stringWithFormat:@"%d", i]];
        if ([model.owner.userId isEqualToString:VLUserCenter.user.id]) {
            [self updateNowMicMuted:model.isAudioMuted];
            [self updateNowCameraMuted:model.isVideoMuted];
        }
        [setsArray addObject:model];
    }
    [self setSeatsArray:setsArray];
    [self.roomPersonView reloadData];
}
    
- (void)onUserEnterSeatWithSeatIndex:(NSInteger)seatIndex user:(SyncUserThumbnailInfo *)user {
    KTVLogInfo(@"onUserEnterSeatWithSeatIndex: seatIndex:%ld userId: %@", seatIndex, user.userId);
    VLRoomSeatModel* model = [self getUserSeatInfoWithIndex:seatIndex];
    if (model == nil) {
        return;
    }
    
    //上麦消息 / 是否打开视频 / 是否静音
    model.owner = user;
    [self setSeatsArray:self.seatsArray];
    
    VLRoomSelSongModel *song = self.selSongsArray.firstObject;
    if(!self.isJoinChorus){
        [self setMVViewStateWith:song];
    }
    [self.roomPersonView reloadSeatIndex:model.seatIndex];
    
    [self onSeatFull];
}

- (void)onUserLeaveSeatWithSeatIndex:(NSInteger)seatIndex user:(SyncUserThumbnailInfo *)user {
    KTVLogInfo(@"onUserLeaveSeatWithSeatIndex: seatIndex:%ld userId: %@", seatIndex, user.userId);
    VLRoomSeatModel* model = [self getUserSeatInfoWithIndex:seatIndex];
    if (model == nil) {
        return;
    }
    
    // 下麦消息
    // 下麦重置占位模型
    model.owner = [SyncUserThumbnailInfo new];
    [self setSeatsArray:self.seatsArray];
    
    VLRoomSelSongModel *song = self.selSongsArray.firstObject;
    if(!self.isJoinChorus){
        [self setMVViewStateWith:song];
    }
    [self.roomPersonView reloadSeatIndex:model.seatIndex];
    [self onSeatFull];
}

- (void)updateNowMicMuted:(BOOL)isMute {
    self.isNowMicMuted = isMute;
    // 开关麦克风会对耳返状态进行检查并临时关闭
    if(self.isEarOn){
        [self.RTCkit enableInEarMonitoring:!self.isNowMicMuted includeAudioFilters:AgoraEarMonitoringFilterNone];
    }
    if (!isMute) {
        [AgoraEntAuthorizedManager checkAudioAuthorizedWithParent:self completion:nil];
    }
}
  
- (void)onSeatAudioMuteWithSeatIndex:(NSInteger)seatIndex isMute:(BOOL)isMute {
    KTVLogInfo(@"onSeatAudioMuteWithSeatIndex: seatIndex:%ld isMute: %d", seatIndex, isMute);
    VLRoomSeatModel* model = [self getUserSeatInfoWithIndex:seatIndex];
    if (model == nil) {
        return;
    }
    
    if ([model.owner.userId isEqualToString:VLUserCenter.user.id]) {
        [self updateNowMicMuted:isMute];
    }
    
    model.isAudioMuted = isMute;
    [self.roomPersonView reloadSeatIndex:model.seatIndex];
}

- (void)updateNowCameraMuted: (BOOL)isMute {
    self.isNowCameraMuted = isMute;
    if (!isMute) {
        [AgoraEntAuthorizedManager checkCameraAuthorizedWithParent:self completion:nil];
    }
}
    
- (void)onSeatVideoMuteWithSeatIndex:(NSInteger)seatIndex isMute:(BOOL)isMute {
    KTVLogInfo(@"onSeatVideoMuteWithSeatIndex: seatIndex:%ld isMute: %d", seatIndex, isMute);
    VLRoomSeatModel* model = [self getUserSeatInfoWithIndex:seatIndex];
    if (model == nil) {
        return;
    }
    if ([model.owner.userId isEqualToString:VLUserCenter.user.id]) {
        [self updateNowCameraMuted:isMute];
    }
    
    model.isVideoMuted = isMute;
    [self.roomPersonView reloadSeatIndex:model.seatIndex];
}

- (void)onUserSeatUpdateWithSeat:(VLRoomSeatModel *)seat {
    
}

- (void)onAddChooseSongWithSong:(VLRoomSelSongModel * _Nonnull)songInfo {
    KTVLogInfo(@"onAddChooseSongWithSong songNo: %@, songName: %@, owner: %@, status: %ld", songInfo.songNo, songInfo.songName, songInfo.owner.userName, songInfo.status);
    [self _checkInEarMonitoring];
    
    NSMutableArray* songArray = [NSMutableArray arrayWithArray:self.selSongsArray];
    self.selSongsArray = [NSMutableArray arrayWithArray:songArray];
}
    

//- (void)onRemoveChooseSongWithSong:(VLRoomSelSongModel * _Nonnull)songInfo {
//    [self _checkInEarMonitoring];
//    NSMutableArray* songArray = [NSMutableArray arrayWithArray:self.selSongsArray];
//    BOOL success = [self removeSelSongWithSongNo:[songInfo.songNo integerValue] sync:NO];
//    if (!success) {
//        self.selSongsArray = songArray;
//        KTVLogInfo(@"removeSelSongWithSongNo fail, reload it");
//    }
//    //清除合唱者总分
//    self.coSingerDegree = 0;
//    [LSTPopView removeAllPopView];
//}


- (void)onChosenSongListDidChangedWithSongs:(NSArray<VLRoomSelSongModel *> *)songs {
    [self _checkInEarMonitoring];
    NSString* origTopSongNo = NullToString(self.selSongsArray.firstObject.songNo);
    NSString* currentTopSongNo = NullToString(songs.firstObject.songNo);
    if (![origTopSongNo isEqualToString:currentTopSongNo]) {
        KTVLogInfo(@"clean old song: %@", origTopSongNo);
        [self.ktvApi removeMusicWithSongCode:[origTopSongNo integerValue]];
        [self stopPlaySong];
        self.coSingerDegree = 0;
        [LSTPopView removeAllPopView];
    }
    self.selSongsArray = [NSMutableArray arrayWithArray:songs];
}

- (void)onChoristerDidEnterWithChorister:(KTVChoristerModel *)chorister {
    KTVLogInfo(@"onChoristerDidEnterWithChorister: %@", chorister.userId);
    VLRoomSeatModel* model = [self gettUserSeatInfoWithUserId:chorister.userId];
    if (model == nil) {
        return;
    }
    [self.roomPersonView reloadSeatIndex:model.seatIndex];
}

- (void)onChoristerDidLeaveWithChorister:(KTVChoristerModel *)chorister {
    KTVLogInfo(@"onChoristerDidLeaveWithChorister: %@", chorister.userId);
    VLRoomSeatModel* model = [self gettUserSeatInfoWithUserId:chorister.userId];
    if (model == nil) {
        return;
    }
    [self.roomPersonView reloadSeatIndex:model.seatIndex];
}

@end




