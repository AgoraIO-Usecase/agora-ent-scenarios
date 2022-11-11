//
//  VLOnLineListVC.m
//  VoiceOnLine
//

#import "VLOnLineListVC.h"
#import "VLHomeOnLineListView.h"
#import "VLKTVViewController.h"
//#import "AgoraRtm.h"
#import "VLRoomListModel.h"
#import "VLRoomSeatModel.h"

#import "VLPopScoreView.h"
#import "VLLoginViewController.h"
#import "VLCreateRoomViewController.h"
#import "LSTPopView.h"
#import "VLUserCenter.h"
#import "VLMacroDefine.h"
//#import "VLAPIRequest.h"
#import "VLURLPathConfig.h"
#import "VLToast.h"
#import "AppContext+KTV.h"
#import "KTVMacro.h"
@import LEEAlert;

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
    [self setNaviTitleName:KTVLocalizedString(@"在线K歌房")];
    if ([VLUserCenter center].isLogin) {
        [self setBackBtn];
    }
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    [VLUserCenter clearUserRoomInfo];
}

- (void)setUpUI {
    VLHomeOnLineListView *listView = [[VLHomeOnLineListView alloc]initWithFrame:CGRectMake(0, kTopNavHeight, SCREEN_WIDTH, SCREEN_HEIGHT-kTopNavHeight) withDelegate:self];
    self.listView = listView;
    [self.view addSubview:listView];
    
    [self.listView getRoomListIfRefresh:YES];
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
        __block UITextField *TF = nil;
        
        [LEEAlert alert].config
        .LeeTitle(KTVLocalizedString(@"输入密码"))
        .LeeAddTextField(^(UITextField *textField) {
            textField.placeholder = KTVLocalizedString(@"请输入房间密码");
            textField.textColor = UIColorBlack;
            textField.clearButtonMode=UITextFieldViewModeWhileEditing;
            textField.font = UIFontMake(15);
            textField.keyboardType = UIKeyboardTypeNumberPad;
            [textField becomeFirstResponder];
            TF = textField; //赋值
        })
        .LeeAddAction(^(LEEAction *action) {
            action.type = LEEActionTypeCancel;
            action.title = @"取消";
            action.titleColor = UIColorMakeWithHex(@"#000000");
            action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
            action.cornerRadius = 20;
            action.height = 40;
            action.font = UIFontBoldMake(16);
            action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
            action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
            action.clickBlock = ^{
                
            };
        })
        .LeeAddAction(^(LEEAction *action) {
            VL(weakSelf);
            action.type = LEEActionTypeCancel;
            action.title = KTVLocalizedString(@"确认");
            action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
            action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
            action.cornerRadius = 20;
            action.height = 40;
            action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
            action.font = UIFontBoldMake(16);
            action.clickBlock = ^{
                [weakSelf joinInRoomWithModel:listModel withInPutText:TF.text];
            };
        })
        .leeShouldActionClickClose(^(NSInteger index){
            // 是否可以关闭回调, 当即将关闭时会被调用 根据返回值决定是否执行关闭处理
            // 这里演示了与输入框非空校验结合的例子
            BOOL result = ![TF.text isEqualToString:@""];
            result = index == 1 ? result : YES;
            return result;
        })
        .LeeShow();
    }else{
        [self joinInRoomWithModel:listModel withInPutText:@""];
    }
}

- (void)joinInRoomWithModel:(VLRoomListModel *)listModel withInPutText:(NSString *)inputText {
    KTVJoinRoomInputModel* inputModel = [KTVJoinRoomInputModel new];
    inputModel.roomNo = listModel.roomNo;
//    inputModel.userNo = VLUserCenter.user.userNo;
    inputModel.password = inputText;

    VL(weakSelf);
    [[AppContext ktvServiceImp] joinRoomWithInput:inputModel
                        completion:^(NSError * error, KTVJoinRoomOutputModel * outputModel) {
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }
        
        listModel.creator = outputModel.creator;
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
