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
#import "VLBelcantoModel.h"
#import "VLNoBodyOnLineView.h"
#import "VLOnLineListVC.h"
//弹框视图
#import "VLPopSelBgView.h"
#import "VLPopMoreSelView.h"
#import "VLDropOnLineView.h"
#import "VLPopOnLineTypeView.h"
#import "VLChooseBelcantoView.h"
#import "VLPopChooseSongView.h"
#import "VLsoundEffectView.h"
#import "VLBadNetWorkView.h"

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
//#import "VLAPIRequest.h"
#import "UIView+VL.h"
#import "AppContext+KTV.h"
#import "KTVMacro.h"
#import <AgoraLyricsScore-Swift.h>
@import LSTPopView;
@import AgoraRtcKit;

@import LEEAlert;
@import YYCategories;
@import SDWebImage;

typedef void (^sendStreamSuccess)(BOOL ifSuccess);

//typedef enum : NSUInteger {
//    VLSendMessageTypeOnSeat = 0,         // 上麦
//    VLSendMessageTypeDropSeat = 1,       // 下麦
//    VLSendMessageTypeChooseSong = 2,     // 点歌
//    VLSendMessageTypeChangeSong = 3,     // 切歌
//    VLSendMessageTypeCloseRoom = 4,      // 关闭房间
//    VLSendMessageTypeChangeMVBg = 5,     // 切换MV背景
//
//    VLSendMessageTypeAudioMute= 9,      // 静音
//    VLSendMessageTypeVideoIfOpen = 10,    // 摄像头
//    VLSendMessageTypeTellSingerSomeBodyJoin = 11, //通知主唱有人加入合唱
//    VLSendMessageTypeTellJoinUID = 12, //通知合唱者 主唱UID
//    VLSendMessageTypeSoloSong = 13,  //独唱
//    VLSendMessageTypeSeeScore = 14,   //观众看到分数
//
//    VLSendMessageAuditFail = 20,
//} VLSendMessageType;

static NSInteger streamId = -1;

@interface VLKTVViewController ()<
VLKTVTopViewDelegate,
VLKTVMVViewDelegate,
VLRoomPersonViewDelegate,
VLKTVBottomViewDelegate,
VLPopSelBgViewDelegate,
VLPopMoreSelViewDelegate,
VLDropOnLineViewDelegate,
VLTouristOnLineViewDelegate,
VLPopOnLineTypeViewDelegate,
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
@property (nonatomic, strong) LSTPopView *popSelBgView;       //切换MV背景
@property (nonatomic, strong) VLKTVTopView *topView;
@property (nonatomic, strong) LSTPopView *popMoreView;        //更多视图
@property (nonatomic, strong) LSTPopView *dropLineView;       //下麦视图
@property (nonatomic, strong) LSTPopView *popOnLineTypeView;  //上麦类型视图
@property (nonatomic, strong) LSTPopView *belcantoView;       //美声视图
@property (nonatomic, strong) LSTPopView *popChooseSongView;  //点歌
@property (nonatomic, strong) LSTPopView *popSoundEffectView; //音效设置
@property (nonatomic, strong) LSTPopView *popBadNetWorkView;  //网络差视图
@property (nonatomic, strong) VLKTVSettingView *settingView;
@property (nonatomic, strong) VLRoomPersonView *roomPersonView; //房间麦位视图
@property (nonatomic, strong) VLTouristOnLineView *requestOnLineView;//空位上麦
@property (nonatomic, strong)  VLPopChooseSongView *chooseSongView; //点歌视图
@property (nonatomic, strong) VLsoundEffectView *soundEffectView; // 音效视图

@property (nonatomic, strong) AgoraRtcChannelMediaOptions *mediaoption;

@property (nonatomic, strong) NSArray *selSongsArray;
@property (nonatomic, strong) id<AgoraMusicPlayerProtocol> rtcMediaPlayer;
@property (nonatomic, strong) AgoraMusicContentCenter *AgoraMcc;
@property (nonatomic, strong) VLSongItmModel *choosedSongModel; //点的歌曲
@property (nonatomic, assign) float currentTime;
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;

@property (nonatomic, strong) VLPopScoreView *scoreView;

@property (nonatomic, strong) AgoraRtcConnection *mediaPlayerConnection;
@property (nonatomic, strong) NSString *mutedRemoteUserId;
@property (nonatomic, strong) NSString *currentPlayingSongNo;


/// agora mcc request lyrics id
@property (nonatomic, copy, nullable) NSString* mccRequestId;

@property (nonatomic, assign) BOOL isEarOn;
@property (nonatomic, assign) BOOL isNowMicMuted;
@property (nonatomic, assign) BOOL isNowCameraMuted;

@end

@implementation VLKTVViewController


#pragma mark setter
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

#pragma mark lazy
- (id<AgoraRtcMediaPlayerProtocol>)rtcMediaPlayer {
    if (!_rtcMediaPlayer) {
//        _rtcMediaPlayer = [self.RTCkit createMediaPlayerWithDelegate:self];
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

#pragma mark view lifecycles
- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = UIColor.blackColor;

    [self resetMicAndCameraStatus];
//    [self createChannel:self.roomModel.roomNo];
    
    [self addServiceHandler];
    
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
//    self.bottomView.hidden = YES;
    [self.view addSubview:bottomView];
    
    //空位上麦视图
    VLTouristOnLineView *requestOnLineView = [[VLTouristOnLineView alloc]initWithFrame:CGRectMake(0, SCREEN_HEIGHT-kSafeAreaBottomHeight-56-VLREALVALUE_WIDTH(30), SCREEN_WIDTH, 56) withDelegate:self];
    self.requestOnLineView = requestOnLineView;
    [self.view addSubview:requestOnLineView];
    
    if (VLUserCenter.user.ifMaster) {
        self.bottomView.hidden = NO;
        self.requestOnLineView.hidden = YES;
    }else{
        BOOL ifOnSeat = NO;
        for (VLRoomSeatModel *model in self.seatsArray) {
            if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {
                ifOnSeat = YES;
            }
        }
        self.bottomView.hidden = !ifOnSeat;
        self.requestOnLineView.hidden = ifOnSeat;
    }
    
    //start join
    if (VLUserCenter.user.ifMaster || [self.roomModel.creator isEqualToString:VLUserCenter.user.userNo] || [self isOnSeat]) { //自己是房间的创建者
        [self joinRTCChannelIfRequestOnSeat:YES];
    }else{
        [self joinRTCChannelIfRequestOnSeat:NO];
    }
    
    //添加通知
    [self addNotification];
    //处理背景
    [self dealWithSelBg];
    [[UIApplication sharedApplication] setIdleTimerDisabled: YES];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [UIViewController popGestureClose:self];
    
    _isEarOn = NO;
    //请求已点歌曲
    [self userFirstGetInRoom];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [UIViewController popGestureOpen:self];
    [self leaveRTCChannel];
    [[UIApplication sharedApplication] setIdleTimerDisabled: NO];
}

- (void) viewDidDisappear:(BOOL)animated
{
    streamId = -1;
    self.rtcMediaPlayer = nil;

    if(self.mediaPlayerConnection) {
        [self disableMediaChannel];
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
    [[NSNotificationCenter defaultCenter]removeObserver:self];
}

- (void)dealloc {
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
- (void)addNotification {
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(dianGeSuccessEvent:) name:kDianGeSuccessNotification object:nil];
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(makeTopSuccessEvent) name:kMakeTopNotification object:nil];
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(deleteSuccessEvent) name:kDeleteSuccessNotification object:nil];
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(updateSelSongEvent) name:kUpdateSelSongArrayNotification object:nil];
}

- (void)addServiceHandler {
    VL(weakSelf);
    [[AppContext ktvServiceImp] subscribeUserListCountWithChanged:^(NSUInteger count) {
        weakSelf.roomModel.roomPeopleNum = [NSString stringWithFormat:@"%ld", count];
        weakSelf.topView.listModel = weakSelf.roomModel;
    }];
    
    [[AppContext ktvServiceImp] subscribeSeatListWithChanged:^(KTVSubscribe status, VLRoomSeatModel* seatModel) {
        //TODO(wushengtao): add model will return KTVSubscribeUpdated
        if (status == KTVSubscribeCreated || status == KTVSubscribeUpdated) {
            //上麦消息
            for (VLRoomSeatModel *model in weakSelf.seatsArray) {
                if (model.onSeat == seatModel.onSeat) {
                    model.isMaster = seatModel.isMaster;
                    model.headUrl = seatModel.headUrl;
                    model.onSeat = seatModel.onSeat;
                    model.name = seatModel.name;
                    model.userNo = seatModel.userNo;
                    model.id = seatModel.id;
                    
                    if([weakSelf ifMainSinger:model.userNo]) {
                        model.ifSelTheSingSong = YES;
                        [weakSelf.MVView setPlayerViewsHidden:NO nextButtonHidden:NO];
                    }
                    VLRoomSelSongModel *song = weakSelf.selSongsArray.count ? weakSelf.selSongsArray.firstObject : nil;
                    if (song != nil && song.isChorus && [song.chorusNo isEqualToString:seatModel.userNo]) {
                        model.ifJoinedChorus = YES;
                    }
                }
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf.roomPersonView setSeatsArray:weakSelf.seatsArray];
            });
            
            if (status == KTVSubscribeUpdated) {
                //是否打开视频 & 是否静音
                for (VLRoomSeatModel *model in self.seatsArray) {
                    if ([seatModel.userNo isEqualToString:model.userNo]) {
                        model.isVideoMuted = seatModel.isVideoMuted;
                        model.isSelfMuted = seatModel.isSelfMuted;
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [self.roomPersonView updateSeatsByModel:model];
                        });
                    }
                }
            }
        } else if (status == KTVSubscribeDeleted) {
            // 下麦消息
            VLRoomSelSongModel *song = weakSelf.selSongsArray.count ? weakSelf.selSongsArray.firstObject : nil;
            
            // 被下麦用户刷新UI
            if ([seatModel.userNo isEqualToString:VLUserCenter.user.userNo]) {
                //当前的座位用户离开RTC通道
                [weakSelf.MVView updateUIWithUserOnSeat:NO song:song];
                weakSelf.bottomView.hidden = YES;
                // 取出对应模型、防止数组越界
                [weakSelf setSelfAudience];
                [weakSelf resetChorusStatus:seatModel.userNo];
                
                if (weakSelf.seatsArray.count - 1 >= seatModel.onSeat) {
                    // 下麦重置占位模型
                    VLRoomSeatModel *indexSeatModel = weakSelf.seatsArray[seatModel.onSeat];
                    [indexSeatModel resetLeaveSeat];
                }
                
                // If I was dropped off mic and I am current singer, then we should play next song.
                if([/*member.userId*/seatModel.id isEqualToString:VLUserCenter.user.id] == NO && [self ifMainSinger:VLUserCenter.user.userNo]) {
                    [weakSelf prepareNextSong];
                    [weakSelf getChoosedSongsList];
                }
            } else{
                for (VLRoomSeatModel *model in weakSelf.seatsArray) {
                    if ([seatModel.userNo isEqualToString:model.userNo]) {
                        [model resetLeaveSeat];
                        [weakSelf resetChorusStatus:seatModel.userNo];
                    }
                }
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf.roomPersonView setSeatsArray:weakSelf.seatsArray];
            });
        }
    }];
    
    [[AppContext ktvServiceImp] subscribeRoomStatusWithChanged:^(KTVSubscribe status, VLRoomListModel * roomInfo) {
        if (KTVSubscribeUpdated == status) {
            //切换背景
            
            //will be mv bg did changed or room member count did changed
            VLKTVSelBgModel* selBgModel = [VLKTVSelBgModel new];
            selBgModel.imageName = [NSString stringWithFormat:@"ktv_mvbg%ld", roomInfo.bgOption];
            selBgModel.ifSelect = YES;
            weakSelf.choosedBgModel = selBgModel;
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf.MVView changeBgViewByModel:selBgModel];
            });
        } else if (status == KTVSubscribeDeleted) {
            //房主关闭房间
            if ([roomInfo.creator isEqualToString:VLUserCenter.user.userNo]) {
                return;
            }
            //发送通知
            [[NSNotificationCenter defaultCenter]postNotificationName:kExitRoomNotification object:nil];
            [weakSelf popForceLeaveRoom];
        }
    }];
    
    //callback if choose song list didchanged
    [[AppContext ktvServiceImp] subscribeChooseSongWithChanged:^(KTVSubscribe status, VLRoomSelSongModel * songInfo) {
        if (KTVSubscribeCreated == status || KTVSubscribeUpdated == status) {
            
            if (KTVSubscribeUpdated == status) {
                //有人加入合唱
                if(songInfo.isChorus
                   && weakSelf.currentPlayingSongNo == nil
                   && songInfo.chorusNo != nil) {
                    [weakSelf.MVView setJoinInViewHidden];
                    [weakSelf setUserJoinChorus:songInfo.chorusNo];
                    [weakSelf joinChorusConfig:@""];
                    return;
                }
                
                //观众看到打分
                if (songInfo.status == 2) {
                    double voicePitch = songInfo.score;
                    [weakSelf.MVView setVoicePitch:@[@(voicePitch)]];
                    return;
                }
            }
            
            
            //收到点歌的消息
            VLRoomSelSongModel *song = weakSelf.selSongsArray.firstObject;
            if(song == nil && [song.userId isEqualToString:VLUserCenter.user.id] == NO) {
                [weakSelf getChoosedSongsList];
            }
            else {
                [weakSelf getChoosedSongsList];
            }
            
            
            
        } else if (KTVSubscribeDeleted == status) {
            VLRoomSelSongModel *selSongModel = weakSelf.selSongsArray.firstObject;
            if (![selSongModel.songNo isEqualToString:songInfo.songNo]) {
                [weakSelf getChoosedSongsList];
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
                [weakSelf prepareNextSong];
                [weakSelf getChoosedSongsList];
            }
        }
    }];
}

#pragma mark view helpers
- (void)dealWithSelBg {
    if (self.roomModel.bgOption) {
        VLKTVSelBgModel *selBgModel = [VLKTVSelBgModel new];
        selBgModel.imageName = [NSString stringWithFormat:@"ktv_mvbg%d",(int)self.roomModel.bgOption];
        selBgModel.ifSelect = YES;
        self.choosedBgModel = selBgModel;
        [self.MVView changeBgViewByModel:self.choosedBgModel];
    }
}

//更换MV背景
- (void)popSelMVBgView {
    CGFloat popViewH = (SCREEN_WIDTH-60)/3.0*0.75*3+100+kSafeAreaBottomHeight;
    VLPopSelBgView *changeBgView = [[VLPopSelBgView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) withDelegate:self];
    changeBgView.selBgModel = self.choosedBgModel;
    
    self.popSelBgView = [self setPopCommenSettingWithContentView:changeBgView ifClickBackDismiss:YES];
    [self.popSelBgView pop];
}

//弹出更多
- (void)popSelMoreView {
    CGFloat popViewH = 190+kSafeAreaBottomHeight;
    VLPopMoreSelView *moreView = [[VLPopMoreSelView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) withDelegate:self];
    
    self.popMoreView = [self setPopCommenSettingWithContentView:moreView ifClickBackDismiss:YES];
    [self.popMoreView pop];
}

//弹出下麦视图
- (void)popDropLineViewWithSeatModel:(VLRoomSeatModel *)seatModel {
    CGFloat popViewH = 212+kSafeAreaBottomHeight+32;
    VLDropOnLineView *dropLineView = [[VLDropOnLineView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) withDelegate:self];
    dropLineView.seatModel = seatModel;
    
    self.dropLineView = [self setPopCommenSettingWithContentView:dropLineView ifClickBackDismiss:YES];
    [self.dropLineView pop];
}

//弹出美声视图
- (void)popBelcantoView {
    CGFloat popViewH = 175+kSafeAreaBottomHeight;
    VLChooseBelcantoView *belcantoView = [[VLChooseBelcantoView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) withDelegate:self];
    belcantoView.selBelcantoModel = self.selBelcantoModel;
    self.belcantoView = [self setPopCommenSettingWithContentView:belcantoView ifClickBackDismiss:YES];
    [self.belcantoView pop];
}

//弹出点歌视图
- (void)popUpChooseSongView:(BOOL)ifChorus {
    CGFloat popViewH = SCREEN_HEIGHT*0.7;
    VLPopChooseSongView *chooseSongView = [[VLPopChooseSongView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) withDelegate:self withRoomNo:self.roomModel.roomNo ifChorus:ifChorus];
    self.chooseSongView = chooseSongView;
    self.chooseSongView.selSongsArray = self.selSongsArray;
    self.popChooseSongView = [self setPopCommenSettingWithContentView:chooseSongView ifClickBackDismiss:YES];
    self.popChooseSongView.isAvoidKeyboard = NO;
    [self.popChooseSongView pop];
}

//弹出音效
- (void)popSetSoundEffectView {
    CGFloat popViewH = 88+17+270+kSafeAreaBottomHeight;
    if(_soundEffectView == nil) {
        _soundEffectView = [[VLsoundEffectView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, popViewH) withDelegate:self];
    }
    
    self.popSoundEffectView = [self setPopCommenSettingWithContentView:self.soundEffectView ifClickBackDismiss:YES];
    [self.popSoundEffectView pop];
}

//网络差视图
- (void)popBadNetWrokTipView {
    CGFloat popViewH = 276;
    VLBadNetWorkView *badNetView = [[VLBadNetWorkView alloc]initWithFrame:CGRectMake(40, 0, SCREEN_WIDTH-80, popViewH) withDelegate:self];
    
    LSTPopView *popView = [LSTPopView initWithCustomView:badNetView parentView:self.view popStyle:LSTPopStyleFade dismissStyle:LSTDismissStyleFade];
    popView.hemStyle = LSTHemStyleCenter;
    popView.popDuration = 0.5;
    popView.dismissDuration = 0.5;
    popView.cornerRadius = 20;
    self.popBadNetWorkView = popView;
    popView.isClickFeedback = NO;
    
    [self.popBadNetWorkView pop];
}


//用户弹框离开房间
- (void)popForceLeaveRoom {
    [LEEAlert alert].config
        .LeeAddTitle(^(UILabel *label) {
            label.text = KTVLocalizedString(@"房主已解散房间,请确认离开房间");
            label.textColor = UIColorMakeWithHex(@"#040925");
            label.font = UIFontBoldMake(16);
        })
        .LeeAddAction(^(LEEAction *action) {
            VL(weakSelf);
            action.type = LEEActionTypeCancel;
            action.title = KTVLocalizedString(@"确定");
            action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
            action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
            action.cornerRadius = 20;
            action.height = 40;
            action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
            action.font = UIFontBoldMake(16);
            action.clickBlock = ^{
                for (BaseViewController *vc in self.navigationController.childViewControllers) {
                    if ([vc isKindOfClass:[VLOnLineListVC class]]) {
                        [weakSelf destroyMediaPlayer];
                        [weakSelf leaveRTCChannel];
                        [weakSelf.navigationController popToViewController:vc animated:YES];
                    }
                }
            };
        })
        .LeeShow();
}

//公共弹窗视图设置
- (LSTPopView *)setPopCommenSettingWithContentView:(UIView *)contentView ifClickBackDismiss:(BOOL)dismiss{
    LSTPopView *popView = [LSTPopView initWithCustomView:contentView parentView:self.view popStyle:LSTPopStyleSmoothFromBottom dismissStyle:LSTDismissStyleSmoothToBottom];
    popView.hemStyle = LSTHemStyleBottom;
    popView.popDuration = 0.5;
    popView.dismissDuration = 0.5;
    popView.cornerRadius = 20;
    LSTPopViewWK(popView)
    if (dismiss) {
        popView.isClickFeedback = YES;
        popView.bgClickBlock = ^{
            [wk_popView dismiss];
        };
    }else{
        popView.isClickFeedback = NO;
    }
    popView.rectCorners = UIRectCornerTopLeft | UIRectCornerTopRight;
    
    return  popView;
    
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

#pragma mark helper apis
-(BOOL)isOnSeat{
    for (VLRoomSeatModel *seatModel in self.seatsArray) {
        if (seatModel.id != nil) {
            if ([seatModel.id isEqual:VLUserCenter.user.id]) {
                return YES;
            }
        }
    }
    return NO;
}

- (void)loadMusicWithLrcUrl:(NSString *)lrc songCode:(NSString *)songCode {
    [self.MVView loadLrcURL:lrc];
//    [self.rtcMediaPlayer open:url startPos:0];
    NSInteger songCodeIntValue = [songCode integerValue];
    NSInteger error = [self.AgoraMcc isPreloadedWithSongCode:songCodeIntValue];
    if(error == 0) {
        VLLog(@"Agora - loadMusicWithURL play music");
        [self playMusic:songCodeIntValue];
    }
    else {
        error = [self.AgoraMcc preloadWithSongCode:songCodeIntValue jsonOption:nil];
    }
    VLLog(@"_rtcMediaPlayer--------是否静音:%d",[_rtcMediaPlayer getMute]);
}

- (void)playMusic:(NSInteger )songCode {
//    [self.rtcMediaPlayer open:songCode startPos:0];
    VLLog(@"Agora - MediaPlayer playing %ld", songCode);
    if(self.rtcMediaPlayer != nil) {
        [self.rtcMediaPlayer openMediaWithSongCode:songCode startPos:0];
//        [self playSongWithPlayer:self.rtcMediaPlayer];
    }
}

#pragma mark - 评分相关
- (void)rtcEngine:(AgoraRtcEngineKit *_Nonnull)engine
  didOfflineOfUid:(NSUInteger)uid
           reason:(AgoraUserOfflineReason)reason{
    VLLog(@"下线了：：%ld::reason:%ld",uid,reason);
}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
   didJoinedOfUid:(NSUInteger)uid
          elapsed:(NSInteger)elapsed{
    VLLog(@"收到了视频信息：：%ld",(long)uid);
}
- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
  localAudioStats:(AgoraRtcLocalAudioStats * _Nonnull)stats{
}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
 remoteAudioStats:(AgoraRtcRemoteAudioStats * _Nonnull)stats{

}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
firstLocalAudioFrame:(NSInteger)elapsed{
}


- (void)rtcEngine:(AgoraRtcEngineKit *_Nonnull)engine
rtmpStreamingChangedToState:(NSString *_Nonnull)url
            state:(AgoraRtmpStreamingState)state
        errorCode:(AgoraRtmpStreamingErrorCode)errorCode{
    VLLog(@"收到了数据流状态改变：：%lu",state);

}
- (void)rtcEngine:(AgoraRtcEngineKit *)engine
reportAudioVolumeIndicationOfSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers
      totalVolume:(NSInteger)totalVolume {
    if (speakers.count) {
        if([self ifMainSinger:VLUserCenter.user.userNo]) {
            double voicePitch = (double)totalVolume;
            [self.MVView setVoicePitch:@[@(voicePitch)]];
            [[AppContext ktvServiceImp] updateSingingScoreWithTotalVolume:totalVolume];
        }
    }
}

#pragma mark -  播放状态

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
          didChangedToState:(AgoraMediaPlayerState)state
                      error:(AgoraMediaPlayerError)error {
    dispatch_async(dispatch_get_main_queue(), ^{
        VLLog(@"AgoraMediaPlayerState---%ld\n",state);
        if (state == AgoraMediaPlayerStateOpenCompleted) {
//            [playerKit setPlaybackSpeed:400];
            [self playSongWithPlayer:playerKit];
        } else if (state == AgoraMediaPlayerStatePlayBackCompleted) {
            VLLog(@"Playback Completed");
        } else if (state == AgoraMediaPlayerStatePlayBackAllLoopsCompleted) {
            dispatch_async(dispatch_get_main_queue(), ^{
                VLLog(@"Playback all loop completed");
                VLRoomSelSongModel *songModel = self.selSongsArray.firstObject;
                if([self ifMainSinger:VLUserCenter.user.userNo]) {
                    [self showScoreViewWithScore:[self.MVView getAvgSongScore] song:songModel];
                }
                [self playNextSong:0];
            });
        } else if (state == AgoraMediaPlayerStateStopped) {
        }
    });
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

#pragma mark --播放进度回调
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

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
              didOccurEvent:(AgoraMediaPlayerEvent)eventCode
                elapsedTime:(NSInteger)elapsedTime
                    message:(NSString *_Nullable)message { //报告当前播放器发生的事件，如定位开始、定位成功或定位失败。
    if (eventCode == AgoraMediaPlayerEventSeekComplete) {
//        [_MVView start];
//        [_MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
//        [self playSongWithPlayer:playerKit];
    }
}

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
             didReceiveData:(NSString *_Nullable)data
                     length:(NSInteger)length {
//    VLLog(@"didReceiveData-----%@,%ld",data,length);
}

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
       didPlayBufferUpdated:(NSInteger)playCachedBuffer {
//    VLLog(@"didPlayBufferUpdated-----%@,%ld",playerKit,playCachedBuffer);
    
}

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
            didPreloadEvent:(AgoraMediaPlayerPreloadEvent)event {
//    VLLog(@"didPreloadEvent-----%@,%ld",playerKit,event);
    if (event == 1) {
        
    }
}

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
     playerSrcInfoDidChange:(AgoraMediaPlayerSrcInfo *_Nonnull)to
                       from:(AgoraMediaPlayerSrcInfo *_Nonnull)from {
//    VLLog(@"playerSrcInfoDidChange-----%@,%@,%@",playerKit,[to yy_modelDescription],[from yy_modelDescription]);
}

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
                infoUpdated:(AgoraMediaPlayerUpdatedInfo *_Nonnull)info
{
//    VLLog(@"AgoraRtcMediaPlayer-----%@,%@",playerKit,[info yy_modelDescription]);
}

- (void)onAgoraCDNTokenWillExpire {
//    VLLog(@"onAgoraCDNTokenWillExpire");
}

- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol> _Nonnull)playerKit
 volumeIndicationDidReceive:(NSInteger)volume {
//    NSLog(@"volumeIndicationDidReceive-----%@,%ld",playerKit,volume);
}

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
    }else{
//                    VLLog(@"发送失败-streamId:%ld\n",streamId);
    };
}

#pragma mark - content center
- (void)onLyricResult:(nonnull NSString *)requestId
             lyricUrl:(nonnull NSString *)lyricUrl {
    
    if (![requestId isEqualToString: self.mccRequestId]
        || [lyricUrl length] == 0) {
        return;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
        selSongModel.lyric = lyricUrl;
        [self.MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
        [self loadMusicWithLrcUrl:selSongModel.lyric
                         songCode:selSongModel.songNo];
        [self.MVView updateUIWithUserOnSeat:NO
                                       song:selSongModel];
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
        [self playMusic:songCode];
    }
    else if(status == AgoraMusicContentCenterPreloadStatusPreloading) {
        // Do nothing.
    }
    else {
        dispatch_main_async_safe(^{
            [VLToast toast:KTVLocalizedString(@"加载歌曲失败")];
        });
    }
}

#pragma mark - action utils / business
- (void)muteLocalAudio:(BOOL)mute {
    if (mute) {
        AgoraRtcChannelMediaOptions *option = [[AgoraRtcChannelMediaOptions alloc] init];
        option.publishMicrophoneTrack = [AgoraRtcBoolOptional of:NO];
        option.publishCameraTrack = [AgoraRtcBoolOptional of:(self.isNowCameraMuted?NO:YES)];
        [self.RTCkit updateChannelWithMediaOptions:option];
        if(self.isEarOn) {
            [self.RTCkit enableInEarMonitoring:NO];
        }
        self.isNowMicMuted = YES;
    }
    else{
        AgoraRtcChannelMediaOptions *option = [[AgoraRtcChannelMediaOptions alloc] init];
        option.publishMicrophoneTrack = [AgoraRtcBoolOptional of:YES];
        option.publishCameraTrack = [AgoraRtcBoolOptional of:(self.isNowCameraMuted?NO:YES)];
        [self.RTCkit updateChannelWithMediaOptions:option];
        if(self.isEarOn) {
            [self.RTCkit enableInEarMonitoring:YES];
        }
        self.isNowMicMuted = NO;
    }
    [self.bottomView updateAudioBtn:self.isNowMicMuted];
    [self.MVView validateSingType];
    
    [[AppContext ktvServiceImp] openAudioStatusWithStatus:!mute
                                               completion:^(NSError * error) {
        
    }];
}

- (void)muteLocalVideo:(BOOL)mute {
    if (!mute) {
        [self.RTCkit enableLocalVideo:YES];
        [self.RTCkit muteLocalVideoStream:NO];
        _isNowCameraMuted = NO;
    }
    else{
        [self.RTCkit enableLocalVideo:NO];
        [self.RTCkit muteLocalVideoStream:YES];
        _isNowCameraMuted = YES;
    }
    [self.bottomView updateVideoBtn:self.isNowCameraMuted];
    
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

- (void)leaveMicSeat:(void (^)(void))completion {
    BOOL ifOnSeat = NO;
    NSInteger seatIndex = -1;
    for (VLRoomSeatModel *model in self.seatsArray) {
        if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {
            ifOnSeat = YES;
            seatIndex = model.onSeat;
        }
    }
    if (ifOnSeat) { //在麦位
        KTVOutSeatInputModel* inputModel = [KTVOutSeatInputModel new];
        inputModel.userNo = VLUserCenter.user.userNo;
        inputModel.userId = VLUserCenter.user.id;
        inputModel.userName = VLUserCenter.user.name;
        inputModel.userHeadUrl = VLUserCenter.user.headUrl ? VLUserCenter.user.headUrl:@"";
        inputModel.userOnSeat = seatIndex;
        [[AppContext ktvServiceImp] outSeatWithInput:inputModel
                                          completion:^(NSError * error) {
            if (error.code == 20007) {
                // 房间已关闭
                [self popForceLeaveRoom];
                return;
            }
            if (error != nil) {
                return;
            }

            VLLog(@"发送下麦消息成功");
            [VLUserCenter clearUserRoomInfo];
            completion();
        }];
    }else{
        completion();
    }
}

- (void)leaveRoom {
    VL(weakSelf);
    [[AppContext ktvServiceImp] leaveRoomWithCompletion:^(NSError * error) {
        if (error != nil) {
            return;
        }
        
        [[NSNotificationCenter defaultCenter]postNotificationName:kExitRoomNotification object:nil];
        [weakSelf destroyMediaPlayer];
        for (BaseViewController *vc in weakSelf.navigationController.childViewControllers) {
            if ([vc isKindOfClass:[VLOnLineListVC class]]) {
                [weakSelf.navigationController popToViewController:vc animated:YES];
            }
        }
    }];
}


// TODO
// Play song locally and update UI
- (void)playSongWithPlayer:(id<AgoraRtcMediaPlayerProtocol>)player {
    if (self.selSongsArray.count > 0) {
        VLRoomSelSongModel *model = self.selSongsArray.firstObject;
        if ([model.userNo isEqualToString:VLUserCenter.user.userNo] ||
                ([self ifChorusSinger:VLUserCenter.user.userNo]
                 && [model.chorusNo isEqualToString:VLUserCenter.user.userNo])) {
            [player play];
            
            [_MVView start];
            [_MVView updateMVPlayerState:VLKTVMVViewActionTypeMVPlay];
            
            if ([self ifMainSinger:VLUserCenter.user.userNo]
                && self.selSongsArray.count) {
                [self tellSeverTheCurrentPlaySongWithModel:self.selSongsArray.firstObject];
            }
        }
    }
}

- (void)prepareNextSong {
    self.currentTime = 0;
    self.currentPlayingSongNo = nil;
    [self.MVView stop];
    [self.MVView reset];
    [self.MVView cleanMusicText];
    [self.rtcMediaPlayer stop];
    [self resetPlayer];
}

- (void)playNextSong:(int)isMasterInterrupt {
    [self prepareNextSong];
    [self deleteSongEvent:self.selSongsArray.firstObject
        isMasterInterrupt:isMasterInterrupt];
    VLLog(@"RTC media player stop");
}

- (void)requestOnSeatWithIndex:(NSInteger)index {
    
    KTVOnSeatInputModel* inputModel = [KTVOnSeatInputModel new];
    inputModel.seatIndex = index;
    VL(weakSelf);
    [[AppContext ktvServiceImp] onSeatWithInput:inputModel
                                     completion:^(NSError * error) {
        if (error != nil) {
            return;
        }
        
        VLLog(@"发送上麦消息成功");
        dispatch_async(dispatch_get_main_queue(), ^{ //自己在该位置刷新UI
            for (VLRoomSeatModel *model in weakSelf.seatsArray) {
                if (model.onSeat == index) {
                    model.isMaster = false;
                    model.headUrl = VLUserCenter.user.headUrl;
                    model.onSeat = index;
                    model.name = VLUserCenter.user.name;
                    model.userNo = VLUserCenter.user.userNo;
                    model.id = VLUserCenter.user.id;
                    
                    if([weakSelf ifMainSinger:VLUserCenter.user.userNo]) {
                        model.ifSelTheSingSong = YES;
                        [weakSelf.MVView setPlayerViewsHidden:NO
                                             nextButtonHidden:NO];
                    }
                    
                    VLRoomSelSongModel *selSongModel = weakSelf.selSongsArray.firstObject;
                    if(selSongModel != nil) {
                        if (selSongModel.isChorus
                            && [selSongModel.chorusNo isEqualToString:VLUserCenter.user.userNo]) {
                            model.ifJoinedChorus = YES;
                        }
                    }
                }
            }
            [weakSelf.roomPersonView setSeatsArray:weakSelf.seatsArray];
            weakSelf.requestOnLineView.hidden = YES;
            weakSelf.bottomView.hidden = NO;
            [weakSelf.bottomView resetBtnStatus];
            [weakSelf.MVView updateUIWithUserOnSeat:YES
                                               song:weakSelf.selSongsArray.firstObject];
            [weakSelf.RTCkit setClientRole:AgoraClientRoleBroadcaster];
            [weakSelf muteLocalAudio:false];
        });
    }];
}


#pragma mark private method
- (void)_leaveSeat {
    VLRoomSeatModel* seatModel = [VLRoomSeatModel new];
    seatModel.userNo = VLUserCenter.user.userNo;
    seatModel.headUrl = VLUserCenter.user.headUrl;
    seatModel.name = VLUserCenter.user.name;
    seatModel.id = VLUserCenter.user.id;
    seatModel.onSeat = YES;
    [self _leaveSeatWithSeatModel: seatModel];
}

- (void)_leaveSeatWithSeatModel:(VLRoomSeatModel * __nonnull)seatModel {
    NSString *userNo = seatModel.userNo;
    NSString *userHeadUrl = seatModel.headUrl;
    NSString *userName = seatModel.name;
    NSString *userid = seatModel.id;
    NSInteger userOnSeat = seatModel.onSeat;
    
    if(seatModel!= nil && seatModel.id == VLUserCenter.user.id) {
        if(seatModel.isVideoMuted == 1) {
            [self.RTCkit stopPreview];
        }
    }
    
    KTVOutSeatInputModel* inputModel = [KTVOutSeatInputModel new];
    inputModel.userNo = userNo;
    inputModel.userId = userid;
    inputModel.userName = userName;
    inputModel.userHeadUrl = userHeadUrl;
    inputModel.userOnSeat = userOnSeat;
    [[AppContext ktvServiceImp] outSeatWithInput:inputModel
                                      completion:^(NSError * error) {
        if (error != nil) {
            return;
        }
        
        //房主自己手动更新视图
        if ([userNo isEqualToString:VLUserCenter.user.userNo]) {//如果自己主动下麦
            [self.MVView updateUIWithUserOnSeat:NO
                                           song:self.selSongsArray.firstObject];
            [self.MVView setPlayerViewsHidden:YES
                             nextButtonHidden:YES];
            self.bottomView.hidden = YES;
            self.requestOnLineView.hidden = NO;
            if([self ifMainSinger:VLUserCenter.user.userNo]) {
                [self playNextSong:0];
            }
            
            [self resetMicAndCameraStatus];
            
            [self setSelfAudience];
        }
        else if([self ifIAmRoomMaster]
                && [self ifMainSinger:userNo]==YES) {
            [self playNextSong:1];
        }
        
        [self resetChorusStatus:userNo];
        
        for (VLRoomSeatModel *model in self.seatsArray) {
            if (model.onSeat == userOnSeat) {
                [model resetLeaveSeat];
            }
        }
        [self.roomPersonView setSeatsArray:self.seatsArray];
        [self.dropLineView dismiss];
    }];
}

#pragma mark - rtc utils
- (void)resetMicAndCameraStatus
{
    _isNowMicMuted = NO;
    _isNowCameraMuted = YES;
}

/// 销毁播放器
- (void)destroyMediaPlayer {
    [self.rtcMediaPlayer stop];
    VLLog(@"Agora - RTCMediaPlayer stop");
    [self.RTCkit destroyMediaPlayer:self.rtcMediaPlayer];
    VLLog(@"Agora - Destroy media player");
}

- (void)joinRTCChannelIfRequestOnSeat:(BOOL)ifRequestOnSeat {
    [self.RTCkit leaveChannel:nil];
    [AgoraRtcEngineKit destroy];
//    [AgoraMusicContentCenter destroy];
    self.RTCkit = nil;
    
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
    
    if (ifRequestOnSeat) {
        [self setSelfBroadcaster];
    }else{
        [self setSelfAudience];
    }
    AgoraVideoEncoderConfiguration *encoderConfiguration = [[AgoraVideoEncoderConfiguration alloc] initWithSize:CGSizeMake(100, 100)
                                                                                                      frameRate:AgoraVideoFrameRateFps7
                                                                                                        bitrate:20
                                                                                                orientationMode:AgoraVideoOutputOrientationModeFixedLandscape
                                                                                                     mirrorMode:AgoraVideoMirrorModeAuto];
    [self.RTCkit setVideoEncoderConfiguration:encoderConfiguration];
    VLLog(@"Agora - joining RTC channel with token: %@, for roomNo: %@, with uid: %@", VLUserCenter.user.agoraRTCToken, self.roomModel.roomNo, VLUserCenter.user.id);
//    [self.RTCkit joinChannelByToken:VLUserCenter.user.agoraRTCToken
//                          channelId:self.roomModel.roomNo info:nil
//                                uid:[VLUserCenter.user.id integerValue]
//                        joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
    AgoraRtcChannelMediaOptions *options = [AgoraRtcChannelMediaOptions new];
    options.publishCameraTrack = [AgoraRtcBoolOptional of:NO];
    options.publishMicrophoneTrack = [AgoraRtcBoolOptional of:ifRequestOnSeat];
    [self.RTCkit joinChannelByToken:VLUserCenter.user.agoraRTCToken
                          channelId:self.roomModel.roomNo
                                uid:[VLUserCenter.user.id integerValue]
                       mediaOptions:options
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
//    [self.AgoraMcc registerEventDelegate:self];

    [UIApplication sharedApplication].idleTimerDisabled = YES;
}

- (void)setSelfAudience {
    [self.RTCkit muteLocalVideoStream:YES];
    [self.RTCkit muteLocalAudioStream:YES];
    [self.RTCkit setClientRole:AgoraClientRoleAudience];
    [self resetPlayer];
}

- (void)setSelfBroadcaster {
    [self.RTCkit muteLocalVideoStream:NO];
    [self.RTCkit muteLocalAudioStream:NO];
    [self.RTCkit setClientRole:AgoraClientRoleBroadcaster];
}

- (void)leaveRTCChannel {
    [self.RTCkit leaveChannel:^(AgoraChannelStats * _Nonnull stat) {
        VLLog(@"Agora - Leave RTC channel");
    }];
}

- (AgoraRtcConnection *)enableMediaChannel:(BOOL)doPublish {
    AgoraRtcChannelMediaOptions *option = [AgoraRtcChannelMediaOptions new];
    [option setClientRoleType:[AgoraRtcIntOptional of:AgoraClientRoleBroadcaster]];
    [option setPublishCameraTrack:[AgoraRtcBoolOptional of:NO]];
    [option setPublishCustomAudioTrack:[AgoraRtcBoolOptional of:NO]];
    [option setEnableAudioRecordingOrPlayout:[AgoraRtcBoolOptional of:NO]];
    [option setAutoSubscribeAudio:[AgoraRtcBoolOptional of:NO]];

    [option setPublishMicrophoneTrack:[AgoraRtcBoolOptional of:NO]];

    [option setAutoSubscribeVideo:[AgoraRtcBoolOptional of:NO]];

    [option setPublishMediaPlayerId:[AgoraRtcIntOptional of:[self.rtcMediaPlayer getMediaPlayerId]]];
    [option setPublishMediaPlayerAudioTrack:[AgoraRtcBoolOptional of:doPublish]];
    
    AgoraRtcConnection *connection = [AgoraRtcConnection new];
    connection.channelId = self.roomModel.roomNo;
    connection.localUid = [VLGlobalHelper getAgoraPlayerUserId:VLUserCenter.user.id];

    VLLog(@"Agora - Join channelex with token: %@, userid: %lu for channel: %@ for mediaplayer id: %d",
          VLUserCenter.user.agoraPlayerRTCToken,
          connection.localUid,
          connection.channelId,
          [self.rtcMediaPlayer getMediaPlayerId]);
    
    int ret  = [self.RTCkit joinChannelExByToken:VLUserCenter.user.agoraPlayerRTCToken
                                      connection:connection delegate:self
                                    mediaOptions:option
                                     joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        VLLog(@"Agora - Join channel ex succ");
    }];
    
    if(ret == 0) {
        VLLog(@"Agora - Join media player connection succ");
        return connection;
    }
    else {
        VLLog(@"Agora - Join media player connection failed");
        return nil;
    }
}

- (BOOL)disableMediaChannel {
    if(self.mediaPlayerConnection == nil) {
        return YES;
    }
    
    int ret = [self.RTCkit leaveChannelEx:self.mediaPlayerConnection
                        leaveChannelBlock:^(AgoraChannelStats* stat) {
        
    }];
    
    if(ret == 0) {
        self.mediaPlayerConnection = nil;
        VLLog(@"Agora - Leave media channel succ");
        return YES;
    }
    else {
        VLLog(@"Agora - Leave media channel failed");
        return NO;
    }
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
    //TODO
    if (VLUserCenter.user.ifMaster) { //自己是房主关闭房间
        [LEEAlert alert].config
        .LeeAddTitle(^(UILabel *label) {
            label.text = KTVLocalizedString(@"解散房间");
            label.textColor = UIColorMakeWithHex(@"#040925");
            label.font = UIFontBoldMake(16);
        })
        .LeeAddContent(^(UILabel *label) {
            label.text = KTVLocalizedString(@"确定解散该房间吗？");
            label.textColor = UIColorMakeWithHex(@"#6C7192");
            label.font = UIFontMake(14);
            
        })
        .LeeAddAction(^(LEEAction *action) {
            action.type = LEEActionTypeCancel;
            action.title = KTVLocalizedString(@"取消");
            action.titleColor = UIColorMakeWithHex(@"#000000");
            action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
            action.cornerRadius = 20;
            action.height = 40;
            action.font = UIFontBoldMake(16);
            action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
            action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
            action.clickBlock = ^{
                // 取消点击事件Block
            };
        })
        .LeeAddAction(^(LEEAction *action) {
            VL(weakSelf);
            action.type = LEEActionTypeCancel;
            action.title = KTVLocalizedString(@"确定");
            action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
            action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
            action.cornerRadius = 20;
            action.height = 40;
            action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
            action.font = UIFontBoldMake(16);
            action.clickBlock = ^{
                [weakSelf leaveRoom];
            };
        })
        .LeeShow();
    }else{
        [LEEAlert alert].config
        .LeeAddTitle(^(UILabel *label) {
            label.text = KTVLocalizedString(@"退出房间");
            label.textColor = UIColorMakeWithHex(@"#040925");
            label.font = UIFontBoldMake(16);
        })
        .LeeAddContent(^(UILabel *label) {
            label.text = KTVLocalizedString(@"确定退出该房间吗？");
            label.textColor = UIColorMakeWithHex(@"#6C7192");
            label.font = UIFontMake(14);
            
        })
        .LeeAddAction(^(LEEAction *action) {
            action.type = LEEActionTypeCancel;
            action.title = KTVLocalizedString(@"取消");
            action.titleColor = UIColorMakeWithHex(@"#000000");
            action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
            action.cornerRadius = 20;
            action.height = 40;
            action.font = UIFontBoldMake(16);
            action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
            action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
            action.clickBlock = ^{
                // 取消点击事件Block
            };
        })
        .LeeAddAction(^(LEEAction *action) {
            VL(weakSelf);
            action.type = LEEActionTypeCancel;
            action.title = KTVLocalizedString(@"确定");
            action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
            action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
            action.cornerRadius = 20;
            action.height = 40;
            action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
            action.font = UIFontBoldMake(16);
            action.clickBlock = ^{
                if([weakSelf currentUserIsOnSeat]) {
                    // Drop mic first
                    [weakSelf _leaveSeat];
                }
                [weakSelf resetChorusStatus:VLUserCenter.user.userNo];
                VLRoomSelSongModel *song = self.selSongsArray.count ? self.selSongsArray.firstObject : nil;
                if(song != nil && song.isOwnSong) {
                    //mvview action type exit
                    [weakSelf playNextSong:0];
                }
                [weakSelf leaveMicSeat:^{
                    [weakSelf leaveRoom];
                }];
            };
        })
        .LeeShow();
    }
}

#pragma mark - VLPopMoreSelViewDelegate
- (void)onVLKTVMoreSelView:(VLPopMoreSelView *)view btnTapped:(id)sender withValue:(VLKTVMoreBtnClickType)typeValue {
    switch (typeValue) {
        case VLKTVMoreBtnClickTypeBelcanto:
            [self.popMoreView dismiss];
            [self popBelcantoView];
            break;
        case VLKTVMoreBtnClickTypeSound:
            [self.popMoreView dismiss];
            [self popSetSoundEffectView];
            break;
        case VLKTVMoreBtnClickTypeMV:
            [self.popMoreView dismiss];
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
        }else{
            //empty
            BOOL ifOnSeat = NO;
            for (VLRoomSeatModel *model in self.seatsArray) {
                if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {
                    ifOnSeat = YES;
                }
            }
            if (!ifOnSeat) {
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
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf.popSelBgView dismiss];
            weakSelf.choosedBgModel = selBgModel;
            [weakSelf.MVView changeBgViewByModel:selBgModel];
        });
    }];
}

#pragma mark - VLPopOnLineTypeViewDelegate
//can move to view internal
- (void)onVLPopOnLineTypeView:(VLPopOnLineTypeView *)view backBtnTapped:(id)sender {
    [self.popOnLineTypeView dismiss];
}

#pragma mark - VLChooseBelcantoViewDelegate
//can move to view internal
- (void)onVLChooseBelcantoView:(VLChooseBelcantoView *)view backBtnTapped:(id)sender {
    [self.belcantoView dismiss];
}

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
    if([self ifChorusSinger:userNo]) {
        [self setSelfChorusUserNo:nil];
        if([userNo isEqualToString:VLUserCenter.user.userNo]) {
            if(self.rtcMediaPlayer != nil) {
                [self.rtcMediaPlayer stop];
            }
            [self resetPlayer];
            [self disableMediaChannel];
        }
    }
    else if(userNo == nil) {
        [self setSelfChorusUserNo:nil];
    }
}

#pragma mark VLDropOnLineViewDelegate
- (void)onVLDropOnLineView:(VLDropOnLineView *)view action:(VLRoomSeatModel *)seatModel {
    [self _leaveSeatWithSeatModel:seatModel];
}

#pragma mark VLBadNetWorkViewDelegate
//网络差知道了点击事件
- (void)onVLBadNetworkView:(id)view dismiss:(id)sender {
    [self.popBadNetWorkView dismiss];
}

#pragma mark VLTouristOnLineViewDelegate
//上麦方式
- (void)requestOnlineAction {
}

#pragma mark - AgoraLrcViewDelegate
-(NSTimeInterval)getTotalTime{
    NSTimeInterval time = [_rtcMediaPlayer getDuration];
    NSTimeInterval real = time / 1000;
    return real;
}

- (NSTimeInterval)getPlayerCurrentTime
{
    VLRoomSelSongModel *model = self.selSongsArray.firstObject;
    if ([model.userNo isEqualToString:VLUserCenter.user.userNo]) {
        NSTimeInterval time = [_rtcMediaPlayer getPosition];
        NSTimeInterval real = time / 1000;
        return real;
    }else{
        return self.currentTime;
    }
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
        [LEEAlert alert].config
            .LeeAddTitle(^(UILabel *label) {
                label.text = KTVLocalizedString(@"切换歌曲");
                label.textColor = UIColorMakeWithHex(@"#040925");
                label.font = UIFontBoldMake(16);
            })
            .LeeAddContent(^(UILabel *label) {
                label.text = KTVLocalizedString(@"切换下一首歌曲？");
                label.textColor = UIColorMakeWithHex(@"#6C7192");
                label.font = UIFontMake(14);
                
            })
            .LeeAddAction(^(LEEAction *action) {
                action.type = LEEActionTypeCancel;
                action.title = KTVLocalizedString(@"取消");
                action.titleColor = UIColorMakeWithHex(@"#000000");
                action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
                action.cornerRadius = 20;
                action.height = 40;
                action.font = UIFontBoldMake(16);
                action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
                action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
                action.clickBlock = ^{
                    // 取消点击事件Block
                };
            })
            .LeeAddAction(^(LEEAction *action) {
                VL(weakSelf);
                action.type = LEEActionTypeCancel;
                action.title = KTVLocalizedString(@"确定");
                action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
                action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
                action.cornerRadius = 20;
                action.height = 40;
                action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
                action.font = UIFontBoldMake(16);
                action.clickBlock = ^{
                    if (weakSelf.selSongsArray.count >= 1) {
                        if([weakSelf ifIAmRoomMaster]
                           && [weakSelf ifMainSinger:VLUserCenter.user.userNo] == NO) {
                            [weakSelf playNextSong:1];
                        }
                        else {
                            [weakSelf playNextSong:0];
                        }
                        
                        VLLog(@"---Change song---");
                    }
                };
            })
            .LeeShow();
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
    // 独唱
    if (singType == VLKTVMVViewSingActionTypeSolo) {
        [self startSinging];
        //发送独唱的消息
        [self becomeSolo];
    } else if (singType == VLKTVMVViewSingActionTypeJoinChorus) { // 加入合唱
        if(![self currentUserIsOnSeat]) {
            [VLToast toast:KTVLocalizedString(@"请先上坐")];
        } else {
            [self setMyselfJoinChorusSong];
            [self startSinging];
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
        _isEarOn = setting.soundOn;
        if(self.isNowMicMuted) {
            [self.RTCkit enableInEarMonitoring:NO];
        }
        else {
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
    switch (index) {
        case 0:
            return AgoraAudioEffectPresetOff;
        case 1:
            return AgoraAudioEffectPresetRoomAcousticsKTV;
        case 2:
            return AgoraAudioEffectPresetRoomAcousVocalConcer;
        case 3:
            return AgoraAudioEffectPresetRoomAcousStudio;
        case 4:
            return AgoraAudioEffectPresetRoomAcousPhonograph;
        case 5:
            return AgoraAudioEffectPresetRoomAcousSpatial;
        case 6:
            return AgoraAudioEffectPresetRoomAcousEthereal;
        case 7:
            return AgoraAudioEffectPresetStyleTransformationPopular;
        case 8:
            return AgoraAudioEffectPresetStyleTransformationRnb;
        default:
            return AgoraAudioEffectPresetOff;
    }
}

//音效设置
- (void)soundEffectViewBackBtnAction {
    [self.popSoundEffectView dismiss];
}

- (void)soundEffectItemClickAction:(VLKTVSoundEffectType)effectType {
    if (effectType == VLKTVSoundEffectTypeHeFeng) {
        [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:3 param2:4];
    }else if (effectType == VLKTVSoundEffectTypeXiaoDiao){
        [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:2 param2:4];
    }else if (effectType == VLKTVSoundEffectTypeDaDiao){
        [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:1 param2:4];
    } else if (effectType == VLKTVSoundEffectTypeNone) {
        [self.RTCkit setAudioProfile:AgoraAudioProfileMusicHighQuality];
        [self.RTCkit setAudioEffectParameters:AgoraAudioEffectPresetPitchCorrection param1:0 param2:4];
    }
    VLLog(@"Agora - Setting effect type to %lu", effectType);
}


#pragma mark -- 收到RTC消息
- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
receiveStreamMessageFromUid:(NSUInteger)uid
         streamId:(NSInteger)streamId
             data:(NSData * _Nonnull)data {    //接收到对方的RTC消息
    
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:data];
    if (![dict[@"cmd"] isEqualToString:@"setLrcTime"] && ![dict[@"cmd"] isEqualToString:@"testDelay"]){
        VLLog(@"receiveStreamMessageFromUid::%ld---message::%@",uid, dict);
    }
    VLLog(@"返回数据:%@,streamID:%d,uid:%d",dict,(int)streamId,(int)uid);
    
    if ([dict[@"cmd"] isEqualToString:@"setLrcTime"]) {  //同步歌词
        long type = [dict[@"time"] longValue];
        if(type == 0) {
            if (self.rtcMediaPlayer.getPlayerState == AgoraMediaPlayerStatePaused) {
                [self.rtcMediaPlayer resume];
            }
        }
        else if(type == -1) {
            
            if (self.rtcMediaPlayer.getPlayerState == AgoraMediaPlayerStatePlaying) {
                [self.rtcMediaPlayer pause];
            }
        }
        else {
            RtcMusicLrcMessage *musicLrcMessage = [RtcMusicLrcMessage vj_modelWithDictionary:dict];
            float postion = musicLrcMessage.time / 1000.0;
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
    }else if([dict[@"cmd"] isEqualToString:@"countdown"]){  //倒计时
        int leftSecond = [dict[@"time"] intValue];
        VLRoomSelSongModel *song = self.selSongsArray.count ? self.selSongsArray.firstObject : nil;
        if(self.currentPlayingSongNo == nil) {
            [self.MVView receiveCountDown:leftSecond
                                   onSeat:[self currentUserIsOnSeat]
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
        }
        else if(txQuality == AgoraNetworkQualityPoor || txQuality == AgoraNetworkQualityBad) {
            // Bad quality
            [self.topView setNetworkQuality:1];
        }
        else if(txQuality == AgoraNetworkQualityVBad || txQuality == AgoraNetworkQualityDown) {
            // Barely usable
            [self.topView setNetworkQuality:2];
        }
        else {
            // Unknown or detecting
            [self.topView setNetworkQuality:3];
        }
    }
}

#pragma mark --
//TODO: remove it?
- (void)dianGeSuccessEvent:(NSNotification *)notification {
    [self getChoosedSongsList];
}
- (void)makeTopSuccessEvent {
    [self choosedSongsListToChangeUI];
}

- (void)deleteSuccessEvent {
    [self choosedSongsListToChangeUI];
}

- (void)updateSelSongEvent {
    self.selSongsArray = [self.chooseSongView validateSelSongArray];;
}

- (void)getChoosedSongsList {
    
    VL(weakSelf);
    [[AppContext ktvServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        
        weakSelf.selSongsArray = songArray;
        if (weakSelf.chooseSongView) {
            weakSelf.chooseSongView.selSongsArray = weakSelf.selSongsArray; //刷新已点歌曲UI
        }
        
        if([weakSelf.selSongsArray count] == 0) {
            [weakSelf.MVView updateUIWithSong:nil
                                       onSeat:[weakSelf currentUserIsOnSeat]];
            [weakSelf.roomPersonView updateSingBtnWithChoosedSongArray:nil];
            return;
        }
        
        [weakSelf.MVView updateUIWithSong:weakSelf.selSongsArray.firstObject
                                   onSeat:[weakSelf currentUserIsOnSeat]];
        [weakSelf.roomPersonView updateSingBtnWithChoosedSongArray:weakSelf.selSongsArray];
        
        [weakSelf startSingingIfNeed];
    }];
}

- (void)setMyselfJoinChorusSong {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    selSongModel.chorusNo = VLUserCenter.user.userNo;
    return;
}

- (void)startSingingIfNeed {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    if(!selSongModel.isChorus
       && ![self.currentPlayingSongNo isEqualToString:selSongModel.songNo] ) {
        [self startSinging];
    }
}

- (void)startSinging {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    
    if(self.mediaPlayerConnection) {
        [self disableMediaChannel];
        self.mediaPlayerConnection = nil;
    }
    
    if([self ifMainSinger:VLUserCenter.user.userNo]) {
        self.mediaPlayerConnection = [self enableMediaChannel:YES];
    }
    else if([self ifChorusSinger:VLUserCenter.user.userNo]) {
        self.mediaPlayerConnection = [self enableMediaChannel:NO];
    }
    
    VLLog(@"Agora - Song is chorus: %d, I am chorus singer: %d",
          [self isCurrentSongChorus],
          [self ifChorusSinger:VLUserCenter.user.userNo]);
    
    if([self isCurrentSongChorus] && [self ifChorusSinger:VLUserCenter.user.userNo]) {
        self.mutedRemoteUserId = selSongModel.userId;//[self getMainSingerUserNo];
        if(self.mutedRemoteUserId != nil) {
            [self updateRemoteUserMuteStatus:self.mutedRemoteUserId doMute:YES];
        }
    }
    else if([self ifMainSinger:VLUserCenter.user.userNo]) {
        self.mutedRemoteUserId = VLUserCenter.user.id;
        if(self.mutedRemoteUserId != nil) {
            [self updateRemoteUserMuteStatus:self.mutedRemoteUserId doMute:YES];
        }
    }
    
    self.currentPlayingSongNo = selSongModel.songNo;
    
    self.mccRequestId =
    [self.AgoraMcc getLyricWithSongCode:[selSongModel.songNo integerValue] lyricType:0];
}

- (void)choosedSongsListToChangeUI {
    VL(weakSelf);
    [[AppContext ktvServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        
        weakSelf.selSongsArray = songArray;
        if (weakSelf.chooseSongView) {
            weakSelf.chooseSongView.selSongsArray = weakSelf.selSongsArray; //刷新已点歌曲UI
        }
        //刷新MV里的视图
        [weakSelf.MVView updateUIWithSong:weakSelf.selSongsArray.firstObject
                                   onSeat:[weakSelf currentUserIsOnSeat]];
        
    }];
}

- (void)userFirstGetInRoom {
    VL(weakSelf);
    [[AppContext ktvServiceImp] getChoosedSongsListWithCompletion:^(NSError * error, NSArray<VLRoomSelSongModel *> * songArray) {
        if (error != nil) {
            return;
        }
        
        weakSelf.selSongsArray = songArray;
        if (weakSelf.chooseSongView) {
            weakSelf.chooseSongView.selSongsArray = weakSelf.selSongsArray; //刷新已点歌曲UI
        }
        //刷新MV里的视图
        [weakSelf.MVView updateUIWithSong:weakSelf.selSongsArray.firstObject
                                   onSeat:[weakSelf currentUserIsOnSeat]];
        if (!(weakSelf.selSongsArray.count > 0)) {
            return;
        }
        //拿到当前歌的歌词去播放和同步歌词
        VLRoomSelSongModel *selSongModel = weakSelf.selSongsArray.firstObject;
        if (selSongModel.status == 2) { //歌曲正在播放
            //请求歌词和歌曲
            self.mccRequestId =
            [self.AgoraMcc getLyricWithSongCode:[selSongModel.songNo integerValue] lyricType:0];
        }
    }];
}

//主唱告诉后台当前播放的歌曲
- (void)tellSeverTheCurrentPlaySongWithModel:(VLRoomSelSongModel *)selSongModel {
    [[AppContext ktvServiceImp] markSongDidPlayWithInput:selSongModel
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
    }else if (type == -1){
        dict = @{
            @"cmd":@"setLrcTime",
            @"time":@"-1"
        };
    }
    [self sendStremMessageWithDict:dict
                           success:^(BOOL ifSuccess) {
        if (ifSuccess) {
        }
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
        if (ifSuccess) {
        }
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
- (BOOL)ifMainSinger:(NSString *)userNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    if (selSongModel != nil && [selSongModel.userNo isEqualToString:userNo]) {
        return YES;
    }
    else {
        return NO;
    }
}

- (BOOL)ifChorusSinger:(NSString *)userNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    VLLog(@"Agora - Song chorusNo: %@, userNo: %@", selSongModel.chorusNo, userNo);
    if(selSongModel != nil && selSongModel.isChorus && [selSongModel.chorusNo isEqualToString:userNo]) {
        return YES;
    }
    else {
        return NO;
    }
}

- (BOOL) ifIAmRoomMaster {
    return (VLUserCenter.user.ifMaster ? YES : NO);
}

- (NSString *)getMainSingerUserNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    if(selSongModel != nil) {
        return selSongModel.userNo;
    }
    else {
        return nil;
    }
}

- (NSString *)getChrousSingerUserNo {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    if(selSongModel != nil && selSongModel.isChorus && selSongModel.chorusNo != nil) {
        return selSongModel.chorusNo;
    }
    else {
        return nil;
    }
}

- (BOOL)isCurrentSongChorus {
    VLRoomSelSongModel *selSongModel = self.selSongsArray.firstObject;
    if(selSongModel != nil) {
        return selSongModel.isChorus;
    }
    else {
        return NO;
    }
}

/// 加入合唱配置
- (void)joinChorusConfig:(NSString *)remoteUserId {
    for (VLRoomSelSongModel *selSongModel in self.selSongsArray) {
        if ([selSongModel.userNo isEqualToString:VLUserCenter.user.userNo]) {
            //            AgoraRtcChannelMediaOptions *option = [AgoraRtcChannelMediaOptions new];
            //            [option setAutoSubscribeAudio:[AgoraRtcBoolOptional of:YES]];
            //            [option setAutoSubscribeVideo:[AgoraRtcBoolOptional of:YES]];
            //            [option setPublishAudioTrack:[AgoraRtcBoolOptional of:YES]];
            //            [option setPublishMediaPlayerId:[AgoraRtcIntOptional of:[self.rtcMediaPlayer getMediaPlayerId]]];
            //            // 发布播放器音频流
            //            option.publishMediaPlayerAudioTrack = [AgoraRtcBoolOptional of:YES];
            //            option.enableAudioRecordingOrPlayout = [AgoraRtcBoolOptional of:NO];
            //            AgoraRtcConnection *connection = [AgoraRtcConnection new];
            //            connection.channelId = self.roomModel.roomNo;
            //            connection.localUid = 0;
            //
            //            int ret  = [self.RTCkit joinChannelExByToken:VLUserCenter.user.agoraRTCToken connection:connection delegate:self mediaOptions:option joinSuccess:nil];
            //            [self.RTCkit muteRemoteAudioStream:[remoteStreamId integerValue] mute:YES];
            //            if (ret == 0) {
            VLLog(@"成功了!!!!!!!!!!!!!!!!!!!!!!!1");
            //                [self playSongWithPlayer:self.rtcMediaPlayer];
            [self startSinging];
            //            }
            return;
        }
        else {
            [self startSinging];
        }
    }
}

#pragma mark other
- (void)setSelfChorusUserNo:(NSString *)userNo
{
    VLRoomSelSongModel *song = self.selSongsArray.count ? self.selSongsArray.firstObject : nil;
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
            seat.ifJoinedChorus = YES;
            break;
        }
    }
    [self.roomPersonView setSeatsArray:self.seatsArray];
}

/// 当前用户是否在麦上
- (BOOL)currentUserIsOnSeat {
    if (!self.seatsArray.count) return NO;
    bool onSeat = NO;
    for (VLRoomSeatModel *seat in self.seatsArray) {
        if ([seat.userNo isEqualToString:VLUserCenter.user.userNo]) {
            return YES;
        }
    }
    return onSeat;
}

@end
