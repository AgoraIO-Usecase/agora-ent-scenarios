//
//  VLCreateRoomViewController.m
//  VoiceOnLine
//

#import "VLCreateRoomViewController.h"
#import <AgoraRtcKit/AgoraRtcKit.h>
#import "VLCreateRoomView.h"
#import "VLKTVViewController.h"
#import "VLAddRoomModel.h"
#import "VLMacroDefine.h"
#import "VLUserCenter.h"
#import "VLToast.h"
#import "VLURLPathConfig.h"
#import "AppContext+KTV.h"
#import "AESMacro.h"
#import <AgoraEntScenarios-Swift.h>

@interface VLCreateRoomViewController ()<VLCreateRoomViewDelegate/*,AgoraRtmDelegate*/>
@property (nonatomic, strong) AgoraRtcEngineKit *RTCkit;
@property (nonatomic, assign) BOOL isRoomPrivate;
@property (nonatomic, strong) VLCreateRoomView *createRoomView;
@end

@implementation VLCreateRoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    [self setUpUI];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
}

- (BOOL)preferredNavigationBarHidden {
    return true;
}

- (void)createBtnAction:(VLAddRoomModel *)roomModel {  //房主创建
    if (roomModel.isPrivate && roomModel.password.length != 4) {
        return;
    }
    
    KTVCreateRoomInputModel* intputModel = [KTVCreateRoomInputModel new];
    intputModel.belCanto = @"0";
    intputModel.icon = [NSString stringWithFormat:@"%@",roomModel.icon];
    intputModel.isPrivate = roomModel.isPrivate ? @(1) : @(0);
    intputModel.name = [NSString stringWithFormat:@"%@",roomModel.name];
    intputModel.password = roomModel.password.length > 0 ? [NSString stringWithFormat:@"%@",roomModel.password] : @"";
    intputModel.soundEffect = @"0";
    
    VL(weakSelf);
    self.view.userInteractionEnabled = NO;
    [[AppContext ktvServiceImp] createRoomWithInputModel:intputModel
                                              completion:^(NSError * error, KTVCreateRoomOutputModel * outputModel) {
        weakSelf.view.userInteractionEnabled = YES;
        if (error != nil) {
            [VLToast toast:error.description];
            return;
        }
        //处理座位信息 
        VLRoomListModel *listModel = [[VLRoomListModel alloc]init];
        listModel.roomNo = outputModel.roomNo;
        listModel.name = outputModel.name;
        listModel.bgOption = 0;
        listModel.creatorNo = VLUserCenter.user.id;
        listModel.creatorName = VLUserCenter.user.name;
        listModel.creatorAvatar = VLUserCenter.user.headUrl;
        VLKTVViewController *ktvVC = [[VLKTVViewController alloc]init];
        ktvVC.roomModel = listModel;
        ktvVC.seatsArray = outputModel.seatsArray;
        weakSelf.createRoomVCBlock(ktvVC);
    }];
}

- (void)setUpUI {
    VLCreateRoomView *createRoomView = [[VLCreateRoomView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, 343) withDelegate:self];
    [self.view addSubview:createRoomView];
    self.createRoomView = createRoomView;
}

- (void)keyboardWillShow:(NSNotification *)notification {
    self.createRoomBlock(self.isRoomPrivate ? 520 : 480);
    self.createRoomView.frame = CGRectMake(0, 0, SCREEN_WIDTH, self.isRoomPrivate ? 520 : 480);
}

-(void)didCreateRoomAction:(CreateRoomActionType)type{
    if(type == CreateRoomActionTypeNormal){
        self.isRoomPrivate = false;
        self.createRoomBlock(343);
        self.createRoomView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 343);
    } else if(type == CreateRoomActionTypeEncrypt) {
        self.isRoomPrivate = true;
        self.createRoomBlock(400);
        self.createRoomView.frame = CGRectMake(0, 0, SCREEN_WIDTH, 400);
    }
}


@end
