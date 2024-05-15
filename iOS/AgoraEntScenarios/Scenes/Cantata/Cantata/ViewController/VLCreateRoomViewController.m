//
//  VLCreateRoomViewController.m
//  VoiceOnLine
//

#import "VLCreateRoomViewController.h"
#import <AgoraRtcKit/AgoraRtcKit.h>
#import "VLCreateRoomView.h"
//#import "VLKTVViewController.h"
#import "VLAddRoomModel.h"
#import "AppContext+DHCKTV.h"
#import <SVProgressHUD/SVProgressHUD.h>
@interface VLCreateRoomViewController ()<VLCreateRoomViewDelegate/*,AgoraRtmDelegate*/>
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;
@property (nonatomic, strong) DHCVLCreateRoomView *createView;
@property (nonatomic, assign) BOOL isRoomPrivate;
@property (nonatomic, assign) BOOL isCreating;
@end

@implementation VLCreateRoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
}

#pragma mark - Public Methods
- (BOOL)preferredNavigationBarHidden {
    return true;
}

- (void)createBtnAction:(VLAddRoomModel *)roomModel {  //房主创建
    self.createView.createBtn.userInteractionEnabled = NO;
    if (_isCreating) {
        return;
    }
    self.isCreating = true;
    if (roomModel.isPrivate && roomModel.password.length != 4) {
        return;
    }
    
    KTVCreateRoomInputModel* intputModel = [KTVCreateRoomInputModel new];
    intputModel.belCanto = @"0";
    intputModel.icon = [NSString stringWithFormat:@"%@",roomModel.icon];
    intputModel.isPrivate = roomModel.isPrivate == true ? @(1) : @(0);
    intputModel.name = [NSString stringWithFormat:@"%@",roomModel.name];
    intputModel.password = roomModel.password.length > 0 ? [NSString stringWithFormat:@"%@",roomModel.password] : @"";
    intputModel.soundEffect = @"0";
    intputModel.streamMode = roomModel.streamMode;
    intputModel.creatorAvatar = VLUserCenter.user.headUrl;
//    intputModel.userNo = VLUserCenter.user.id;
    VL(weakSelf);
    [[AppContext dhcServiceImp] createRoomWith:intputModel
                                         completion:^(NSError * error, KTVCreateRoomOutputModel * outputModel) {
        self.createView.createBtn.userInteractionEnabled = YES;
        self.isCreating = false;
        [SVProgressHUD dismiss];
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }

        //处理座位信息
        UIViewController *topViewController = self.navigationController.viewControllers.lastObject;
        if(![topViewController isMemberOfClass:[VLCreateRoomViewController class]]){
            return;
        }
        if (error == nil) {
            VLRoomListModel *listModel = [[VLRoomListModel alloc]init];
            listModel.roomNo = outputModel.roomNo;
            listModel.name = outputModel.name;
            listModel.bgOption = 0;
            listModel.creatorAvatar = outputModel.creatorAvatar;
            listModel.creatorNo = VLUserCenter.user.id;
            listModel.streamMode = roomModel.streamMode;
            UIViewController *VC = [ViewControllerFactory createCustomViewControllerWithTitle:listModel seatsArray:outputModel.seatsArray];
            weakSelf.createRoomVCBlock(VC);
        }
    }];
}

- (void)setUpUI {
    VLCreateRoomView *createRoomView = [[VLCreateRoomView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, 393) withDelegate:self];
    [self.view addSubview:createRoomView];
    self.createView = createRoomView;
}

- (void)keyboardWillShow:(NSNotification *)notification {
    self.createRoomBlock(self.isRoomPrivate ? 570 : 530);
    self.createView.frame = CGRectMake(0, 0, SCREEN_WIDTH, self.isRoomPrivate ? 570 : 530);
}

-(void)didCreateRoomAction:(DHCCreateRoomActionType)type{
    if(type == DHCCreateRoomActionTypeNormal){
        self.isRoomPrivate = false;
        self.createRoomBlock(393);
        self.createView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 393);
    } else if(type == DHCCreateRoomActionTypeEncrypt) {
        self.isRoomPrivate = true;
        self.createRoomBlock(450);
        self.createView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 450);
    }
}



@end
