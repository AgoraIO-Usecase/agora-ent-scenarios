//
//  VLOnLineListVC.m
//  VoiceOnLine
//

#import "VLOnLineListVC.h"
#import "VLHomeOnLineListView.h"
#import "VLKTVViewController.h"

#import "VLPopScoreView.h"
#import "VLLoginViewController.h"
#import "VLCreateRoomViewController.h"
#import "LSTPopView.h"
#import "VLUserCenter.h"
#import "VLMacroDefine.h"
#import "VLURLPathConfig.h"
#import "VLToast.h"
#import "AppContext+KTV.h"
#import "AESMacro.h"
#import "VLAlert.h"

@interface VLOnLineListVC ()<VLHomeOnLineListViewDelegate/*,AgoraRtmDelegate*/,VLPopScoreViewDelegate>

@property (nonatomic, strong) VLHomeOnLineListView *listView;
@end

@implementation VLOnLineListVC

- (void)dealloc {
    [AppContext unloadServiceImp];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        [AppContext setupKtvConfig];
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self commonUI];
    [self setUpUI];
}

- (void)commonUI {
    [self setBackgroundImage:@"online_list_BgIcon"];
    [self setNaviTitleName:KTVLocalizedString(@"ktv_online_ktv")];
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
    VLHomeOnLineListView *listView = [[VLHomeOnLineListView alloc]initWithFrame:CGRectMake(0, kTopNavHeight, SCREEN_WIDTH, SCREEN_HEIGHT-kTopNavHeight) withDelegate:self];
    self.listView = listView;
    [self.view addSubview:listView];
}

- (BOOL)checkIsLogin {
    if (![VLUserCenter center].isLogin) {
        VLLoginViewController *loginVC = [[VLLoginViewController alloc] init];
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
    
    VLCreateRoomViewController *createRoomVC = [[VLCreateRoomViewController alloc]init];
    [self.navigationController pushViewController:createRoomVC animated:YES];

}

- (void)listItemClickAction:(VLRoomListModel *)listModel {
    if (![self checkIsLogin]) return;
     
    if (listModel.isPrivate) {
        NSArray *array = [[NSArray alloc]initWithObjects:KTVLocalizedString(@"ktv_cancel"),KTVLocalizedString(@"ktv_confirm"), nil];
        VL(weakSelf);
        [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:KTVLocalizedString(@"ktv_input_pwd") message:@"" placeHolder:KTVLocalizedString(@"ktv_pls_input_pwd") type:ALERTYPETEXTFIELD buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
            [weakSelf joinInRoomWithModel:listModel withInPutText:text];
            [[VLAlert shared] dismiss];
        }];
    }else{
        [self joinInRoomWithModel:listModel withInPutText:@""];
    }
}

- (void)joinInRoomWithModel:(VLRoomListModel *)listModel withInPutText:(NSString *)inputText {
    if (listModel.isPrivate && ![listModel.password isEqualToString:inputText]) {
        [VLToast toast:KTVLocalizedString(@"PasswordError")];
        return;
    }
    
    KTVJoinRoomInputModel* inputModel = [KTVJoinRoomInputModel new];
    inputModel.roomNo = listModel.roomNo;
//    inputModel.userNo = VLUserCenter.user.id;
    inputModel.password = inputText;

    VL(weakSelf);
    [[AppContext ktvServiceImp] joinRoomWithInputModel:inputModel completion:^(NSError * _Nullable error, KTVJoinRoomOutputModel * _Nullable outputModel) {
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }
        
        listModel.creatorNo = outputModel.creatorNo;
        VLKTVViewController *ktvVC = [[VLKTVViewController alloc]init];
        ktvVC.roomModel = listModel;
        ktvVC.seatsArray = outputModel.seatsArray;
        [weakSelf.navigationController pushViewController:ktvVC animated:YES];
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
