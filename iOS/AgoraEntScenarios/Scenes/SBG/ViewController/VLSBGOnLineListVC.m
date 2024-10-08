//
//  VLSBGOnLineListVC.m
//  VoiceOnLine
//

#import "VLSBGOnLineListVC.h"
#import "VLSBGHomeOnLineListView.h"
#import "VLSBGViewController.h"
#import "VLSBGRoomListModel.h"
#import "VLSBGRoomSeatModel.h"

#import "VLSBGPopScoreView.h"
#import "VLSBGCreateRoomViewController.h"
#import "LSTPopView.h"
#import "VLUserCenter.h"
#import "VLMacroDefine.h"
#import "VLURLPathConfig.h"
#import "VLToast.h"
#import "AppContext+SBG.h"
#import "SBGMacro.h"
#import "VLAlert.h"
#import "AgoraEntScenarios-Swift.h"

@interface VLSBGOnLineListVC ()<VLSBGHomeOnLineListViewDelegate/*,AgoraRtmDelegate*/,VLSBGPopScoreViewDelegate>

@property (nonatomic, strong) VLSBGHomeOnLineListView *listView;
@end

@implementation VLSBGOnLineListVC

- (void)dealloc {
    [AppContext unloadServiceImp];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        [AppContext setupSbgConfig];
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self commonUI];
    [self setUpUI];
}

- (void)commonUI {
    [self setBackgroundImage:@"online_list_BgIcon" bundleName:@"sbgResource"];
    [self setNaviTitleName:SBGLocalizedString(@"sbg_name")];
    if ([VLUserCenter center].isLogin) {
        [self setBackBtn];
    }
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    [VLUserCenter clearUserRoomInfo];
    [self.listView getRoomListIfRefresh:YES];
}

- (void)setUpUI {
    VLSBGHomeOnLineListView *listView = [[VLSBGHomeOnLineListView alloc]initWithFrame:CGRectMake(0, kTopNavHeight, SCREEN_WIDTH, SCREEN_HEIGHT-kTopNavHeight) withDelegate:self];
    self.listView = listView;
    [self.view addSubview:listView];
}

- (BOOL)checkIsLogin {
    if (![VLUserCenter center].isLogin) {
        VLLoginController *loginVC = [[VLLoginController alloc] init];
        [self.navigationController pushViewController:loginVC animated:YES];
        return NO;
    }
    return YES;
}

#pragma mark - Public Methods

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return true;
}

#pragma mark --NetWork

#pragma mark - deleagate
- (void)createBtnAction {
    if (![self checkIsLogin]) return;
    
    VLSBGCreateRoomViewController *createRoomVC = [[VLSBGCreateRoomViewController alloc]init];
    createRoomVC.createRoomBlock = ^(CGFloat height) {
        [[KTVCreateRoomPresentView shared] update:height];
    };
    
    kWeakSelf(self);
    createRoomVC.createRoomVCBlock = ^(UIViewController *vc) {
        [[KTVCreateRoomPresentView shared] dismiss];
        [weakself.navigationController pushViewController:vc animated:true];
    };
    KTVCreateRoomPresentView *presentView = [KTVCreateRoomPresentView shared];
    [presentView showViewWith:CGRectMake(0, SCREEN_HEIGHT - 343, SCREEN_WIDTH, 343) vc:createRoomVC];
    [self.view addSubview:presentView];

}

- (void)listItemClickAction:(VLSBGRoomListModel *)listModel {
    if (![self checkIsLogin]) return;
     
    if (listModel.isPrivate) {
        NSArray *array = [[NSArray alloc]initWithObjects:SBGLocalizedString(@"sbg_cancel"),SBGLocalizedString(@"sbg_confirm"), nil];
        VL(weakSelf);
        [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:SBGLocalizedString(@"sbg_input_pwd") message:@"" placeHolder:SBGLocalizedString(@"sbg_pls_input_pwd") type:ALERTYPETEXTFIELD buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
            [weakSelf joinInRoomWithModel:listModel withInPutText:text];
            [[VLAlert shared] dismiss];
        }];
    }else{
        [self joinInRoomWithModel:listModel withInPutText:@""];
    }
}

- (void)joinInRoomWithModel:(VLSBGRoomListModel *)listModel withInPutText:(NSString *)inputText {
    if (listModel.isPrivate && ![listModel.password isEqualToString:inputText]) {
        [VLToast toast:SBGLocalizedString(@"PasswordError")];
        return;
    }
    
    SBGJoinRoomInputModel* inputModel = [SBGJoinRoomInputModel new];
    inputModel.roomNo = listModel.roomNo;
//    inputModel.userNo = VLUserCenter.user.id;
    inputModel.password = inputText;

    VL(weakSelf);
    [[AppContext sbgServiceImp] joinRoomWithInput:inputModel
                                       completion:^(NSError * error, SBGJoinRoomOutputModel * outputModel) {
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }
        
        listModel.creatorNo = outputModel.creatorNo;
        VLSBGViewController *rsVC = [[VLSBGViewController alloc]init];
        rsVC.roomModel = listModel;
        rsVC.seatsArray = outputModel.seatsArray;
        [weakSelf.navigationController pushViewController:rsVC animated:YES];
    }];
}

//- (NSArray *)configureSeatsWithArray:(NSArray *)seatsArray songArray:(NSArray *)songArray {
//    NSMutableArray *seatMuArray = [NSMutableArray array];
//
//    NSArray *modelArray = [VLRoomSeatModel vj_modelArrayWithJson:seatsArray];
//    for (int i=0; i<8; i++) {
//        BOOL ifFind = NO;
//        for (VLRoomSeatModel *model in modelArray) {
//            if (model.onSeat == i) { //这个位置已经有人了
//                ifFind = YES;
//                if(songArray != nil && [songArray count] >= 1) {
//                    if([model.userNo isEqualToString:songArray[0][@"userNo"]]) {
//                        model.ifSelTheSingSong = YES;
//                    }
//                    else if([model.userNo isEqualToString:songArray[0][@"chorusNo"]]) {
//                        model.ifJoinedChorus = YES;
//                    }
//                }
//                [seatMuArray addObject:model];
//            }
//        }
//        if (!ifFind) {
//            VLRoomSeatModel *model = [[VLRoomSeatModel alloc]init];
//            model.onSeat = i;
//            [seatMuArray addObject:model];
//        }
//    }
//    return seatMuArray.mutableCopy;
//}

- (LSTPopView *)setPopCommenSettingWithContentView:(UIView *)contentView ifClickBackDismiss:(BOOL)dismiss{
    LSTPopView *popView = [LSTPopView initWithCustomView:contentView parentView:self.view popStyle:LSTPopStyleFade dismissStyle:LSTDismissStyleFade];
    popView.hemStyle = LSTHemStyleCenter;
    popView.popDuration = 0.5;
    popView.dismissDuration = 0.5;
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


@end
