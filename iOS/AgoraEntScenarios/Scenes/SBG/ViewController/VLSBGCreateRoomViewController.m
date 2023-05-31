//
//  VLSBGCreateRoomViewController.m
//  VoiceOnLine
//

#import "VLSBGCreateRoomViewController.h"
#import <AgoraRtcKit/AgoraRtcKit.h>
#import "VLSBGCreateRoomView.h"
#import "VLSBGViewController.h"
#import "VLSBGRoomSeatModel.h"
#import "VLSBGRoomListModel.h"
#import "VLSBGAddRoomModel.h"
#import "VLMacroDefine.h"
#import "VLUserCenter.h"
#import "VLToast.h"
#import "VLURLPathConfig.h"
#import "AppContext+SBG.h"
#import "SBGMacro.h"

@interface VLSBGCreateRoomViewController ()<VLSBGCreateRoomViewDelegate/*,AgoraRtmDelegate*/>
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;

@end

@implementation VLSBGCreateRoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
//    AgoraRtcEngineConfig *config = [[AgoraRtcEngineConfig alloc]init];
//    config.appId = [AppContext.shared appId];
//    config.audioScenario = AgoraAudioScenarioChorus;
//    config.channelProfile = AgoraChannelProfileLiveBroadcasting;
//    self.RTCkit = [AgoraRtcEngineKit sharedEngineWithConfig:config delegate:nil];
//    /// 开启唱歌评分功能
//    int code = [self.RTCkit enableAudioVolumeIndication:20 smooth:3 reportVad:YES];
//    if (code == 0) {
//        VLLog(@"评分回调开启成功\n");
//    } else {
//        VLLog(@"评分回调开启失败：%d\n",code);
//    }
    [self commonUI];
    [self setUpUI];
    
}

- (void)commonUI {
    [self setBackgroundImage:@"online_list_BgIcon"];
    [self setNaviTitleName:SBGLocalizedString(@"创建房间")];
    [self setBackBtn];
}

#pragma mark - Public Methods
- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return true;
}

- (void)createBtnAction:(VLSBGAddRoomModel *)roomModel {  //房主创建
    if (roomModel.isPrivate && roomModel.password.length != 4) {
        return;
    }
    
    SBGCreateRoomInputModel* intputModel = [SBGCreateRoomInputModel new];
    intputModel.belCanto = @"0";
    intputModel.icon = [NSString stringWithFormat:@"%@",roomModel.icon];
    intputModel.isPrivate = roomModel.isPrivate ? @(1) : @(0);
    intputModel.name = [NSString stringWithFormat:@"%@",roomModel.name];
    intputModel.password = roomModel.password.length > 0 ? [NSString stringWithFormat:@"%@",roomModel.password] : @"";
    intputModel.soundEffect = @"0";
//    intputModel.userNo = VLUserCenter.user.id;
    VL(weakSelf);
    self.view.userInteractionEnabled = NO;
    [[AppContext sbgServiceImp] createRoomWithInput:intputModel
                                         completion:^(NSError * error, SBGCreateRoomOutputModel * outputModel) {
        weakSelf.view.userInteractionEnabled = YES;
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }
        
//        [self.RTCkit joinChannelByToken:VLUserCenter.user.agoraRTCToken
//                              channelId:outputModel.roomNo
//                                   info:nil uid:[VLUserCenter.user.id integerValue]
//                            joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
//            VLLog(@"Agora - 加入RTC成功");
//            [self.RTCkit setClientRole:AgoraClientRoleBroadcaster];
//        }];
        //处理座位信息
        VLSBGRoomListModel *listModel = [[VLSBGRoomListModel alloc]init];
        listModel.roomNo = outputModel.roomNo;
        listModel.name = outputModel.name;
        listModel.bgOption = 0;
        listModel.creatorNo = VLUserCenter.user.id;
        VLSBGViewController *rsVC = [[VLSBGViewController alloc]init];
        rsVC.roomModel = listModel;
        rsVC.seatsArray = outputModel.seatsArray;
        [weakSelf.navigationController pushViewController:rsVC animated:YES];
    }];
}

//- (NSArray *)configureSeatsWithArray:(NSArray *)seatsArray songArray:(NSArray *)songArray {
//    NSMutableArray *seatMuArray = [NSMutableArray array];
//    NSArray *modelArray = [VLRoomSeatModel vj_modelArrayWithJson:seatsArray];
//    for (int i=0; i<8; i++) {
//        BOOL ifFind = NO;
//        for (VLRoomSeatModel *model in modelArray) {
//            if (model.seatIndex == i) { //这个位置已经有人了
//                ifFind = YES;
//                if(songArray != nil && [songArray count] >= 1) {
//                    if([model.userNo isEqualToString:songArray[0][@"userNo"]]) {
//                        model.isSelTheSingSong = YES;
//                    }
//                    else if([model.userNo isEqualToString:songArray[0][@"chorusNo"]]) {
//                        model.isJoinedChorus = YES;
//                    }
//                }
//                
//                [seatMuArray addObject:model];
//            }
//        }
//        if (!ifFind) {
//            VLRoomSeatModel *model = [[VLRoomSeatModel alloc]init];
//            model.seatIndex = i;
//            [seatMuArray addObject:model];
//        }
//    }
//    return seatMuArray.mutableCopy;
//}

- (void)setUpUI {
    VLSBGCreateRoomView *createRoomView = [[VLSBGCreateRoomView alloc]initWithFrame:CGRectMake(0, kTopNavHeight, SCREEN_WIDTH, SCREEN_HEIGHT-kTopNavHeight) withDelegate:self];
    [self.view addSubview:createRoomView];
}



@end
