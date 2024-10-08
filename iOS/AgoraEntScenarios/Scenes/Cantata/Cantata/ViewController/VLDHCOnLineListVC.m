//
//  VLOnLineListVC.m
//  VoiceOnLine
//

#import "VLDHCOnLineListVC.h"
#import "VLHomeOnLineListView.h"
#import "VLCreateRoomViewController.h"
#import "LSTPopView.h"
#import "AppContext+DHCKTV.h"
@import AgoraCommon;

@interface VLDHCOnLineListVC ()<VLHomeOnLineListViewDelegate>

@property (nonatomic, strong) VLHomeOnLineListView *listView;
@end

@implementation VLDHCOnLineListVC

- (void)dealloc {
    [AppContext unloadServiceImp];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        [AppContext setupDhcConfig];
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self commonUI];
    [self setUpUI];
}

- (void)commonUI {
    [self setBackgroundImage:@"online_list_BgIcon" bundleName:@"DHCResource"];
    [self setNaviTitleName:@"ktv_cantata".toSceneLocalization];
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

//- (BOOL)checkIsLogin {
//#if DEBUG
//#else
//#warning  fix it by  chenpan
//    if (![VLUserCenter center].isLogin) {
//        VLLoginViewController *loginVC = [[VLLoginViewController alloc] init];
//        [self.navigationController pushViewController:loginVC animated:YES];
//        return NO;
//    }
//#endif
//    return YES;
//}

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
    //cp todo
    //if (![self checkIsLogin]) return;
    
    VLCreateRoomViewController *createRoomVC = [[VLCreateRoomViewController alloc]init];
    createRoomVC.createRoomBlock = ^(CGFloat height) {
        [[KTVCreateRoomPresentView shared] update:height];
    };
    
    kWeakSelf(self);
    createRoomVC.createRoomVCBlock = ^(UIViewController *vc) {
        [[KTVCreateRoomPresentView shared] dismiss];
        
        [weakself.navigationController pushViewController:vc animated:true];
    };
    KTVCreateRoomPresentView *presentView = [KTVCreateRoomPresentView shared];

    [presentView showViewWith:CGRectMake(0, SCREEN_HEIGHT - 393, SCREEN_WIDTH, 393) vc:createRoomVC];

    [self.view addSubview:presentView];
    

}

- (void)listItemClickAction:(VLRoomListModel *)listModel {
    //cp todo
    //if (![self checkIsLogin]) return;
     
    if (listModel.isPrivate) {
        NSArray *array = [[NSArray alloc]initWithObjects:DHCLocalizedString(@"ktv_cancel"),DHCLocalizedString(@"ktv_confirm"), nil];
        VL(weakSelf);
        [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:DHCLocalizedString(@"ktv_input_pwd") message:@"" placeHolder:DHCLocalizedString(@"ktv_pls_input_pwd") type:ALERTYPETEXTFIELD buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
            [weakSelf joinInRoomWithModel:listModel withInPutText:text];
            [[VLAlert shared] dismiss];
        }];
    }else{
        [self joinInRoomWithModel:listModel withInPutText:@""];
    }
}

- (void)joinInRoomWithModel:(VLRoomListModel *)listModel withInPutText:(NSString *)inputText {
    if (listModel.isPrivate && ![listModel.password isEqualToString:inputText]) {
        [VLToast toast:DHCLocalizedString(@"PasswordError")];
        return;
    }
    
    KTVJoinRoomInputModel* inputModel = [[KTVJoinRoomInputModel alloc]init];
    inputModel.roomNo = listModel.roomNo;
//    inputModel.userNo = VLUserCenter.user.id;
    inputModel.password = inputText;
    inputModel.streamMode = listModel.streamMode;
    
    VL(weakSelf);
    VLSceneConfigsNetworkModel *api = [VLSceneConfigsNetworkModel new];
    api.appId = AppContext.shared.appId;
    [api requestWithCompletion:^(NSError * _Nullable error, id _Nullable data) {
        if(![data isKindOfClass:VLSceneConfigsModel.class]) {
            [VLToast toast:error.description];
            return;
        }
        AppContext.shared.sceneConfig = data;
        [[AppContext dhcServiceImp] joinRoomWith:inputModel completion:^(NSError * _Nullable error, KTVJoinRoomOutputModel * _Nullable outputModel) {
            if (error != nil) {
                [VLToast toast:error.description];
                return;
            }
            
            listModel.creatorNo = outputModel.creatorNo;
            listModel.streamMode = outputModel.streamMode;
            UIViewController *VC = [ViewControllerFactory createCustomViewControllerWithTitle:listModel seatsArray:outputModel.seatsArray];
            [weakSelf.navigationController pushViewController:VC animated:YES];
        }];
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
