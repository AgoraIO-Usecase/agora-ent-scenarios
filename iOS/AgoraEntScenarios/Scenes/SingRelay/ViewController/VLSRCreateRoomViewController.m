//
//  VLCreateRoomViewController.m
//  VoiceOnLine
//

#import "VLSRCreateRoomViewController.h"
#import <AgoraRtcKit/AgoraRtcKit.h>
#import "VLSRCreateRoomView.h"
#import "VLSRViewController.h"
#import "VLSRAddRoomModel.h"
#import "VLMacroDefine.h"
#import "VLUserCenter.h"
#import "VLToast.h"
#import "VLURLPathConfig.h"
#import "AppContext+SR.h"
#import "AESMacro.h"

@interface VLSRCreateRoomViewController ()<VLSRCreateRoomViewDelegate/*,AgoraRtmDelegate*/>
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;
@property (nonatomic, assign) BOOL isRoomPrivate;
@property (nonatomic, strong) VLSRCreateRoomView *createRoomView;
@end

@implementation VLSRCreateRoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    [self setUpUI];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
}

- (BOOL)preferredNavigationBarHidden {
    return true;
}

- (void)createBtnAction:(VLSRAddRoomModel *)roomModel {  //房主创建
    if (roomModel.isPrivate && roomModel.password.length != 4) {
        return;
    }
    
    SRCreateRoomInputModel* intputModel = [SRCreateRoomInputModel new];
    intputModel.belCanto = @"0";
    intputModel.icon = [NSString stringWithFormat:@"%@",roomModel.icon];
    intputModel.isPrivate = roomModel.isPrivate;
    intputModel.name = [NSString stringWithFormat:@"%@",roomModel.name];
    intputModel.password = roomModel.password.length > 0 ? [NSString stringWithFormat:@"%@",roomModel.password] : @"";
    intputModel.soundEffect = @"0";
//    intputModel.userNo = VLUserCenter.user.id;
    VL(weakSelf);
    self.view.userInteractionEnabled = NO;
    [[AppContext srServiceImp] createRoomWith:intputModel
                                         completion:^(NSError * error, SRCreateRoomOutputModel * outputModel) {
        weakSelf.view.userInteractionEnabled = YES;
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }

        //处理座位信息
        VLSRRoomListModel *listModel = [[VLSRRoomListModel alloc]init];
        listModel.roomNo = outputModel.roomNo;
        listModel.name = outputModel.name;
        listModel.bgOption = 0;
        listModel.creatorNo = VLUserCenter.user.id;
        listModel.creatorName = VLUserCenter.user.name;
        listModel.creatorAvatar = VLUserCenter.user.headUrl;
        VLSRViewController *srVC = [[VLSRViewController alloc]init];
        srVC.roomModel = listModel;
        srVC.seatsArray = outputModel.seatsArray;
        weakSelf.createRoomVCBlock(srVC);
    }];
}

- (void)setUpUI {
    VLSRCreateRoomView *createRoomView = [[VLSRCreateRoomView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, 343) withDelegate:self];
    [self.view addSubview:createRoomView];
    self.createRoomView = createRoomView;
}

- (void)keyboardWillShow:(NSNotification *)notification {
    self.createRoomBlock(self.isRoomPrivate ? 520 : 480);
    self.createRoomView.frame = CGRectMake(0, 0, SCREEN_WIDTH, self.isRoomPrivate ? 520 : 480);
}

-(void)didCreateRoomAction:(SRCreateRoomActionType)type{
    if(type == SRCreateRoomActionTypeNormal){
        self.isRoomPrivate = false;
        self.createRoomBlock(343);
        self.createRoomView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 343);
    } else if(type == SRCreateRoomActionTypeEncrypt) {
        self.isRoomPrivate = true;
        self.createRoomBlock(400);
        self.createRoomView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 400);
    }
}



@end
