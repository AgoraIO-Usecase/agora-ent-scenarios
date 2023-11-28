//
//  VLHomeViewController.m
//  VoiceOnLine
//

#import "VLHomeViewController.h"
#import "VLHomeView.h"
#import "VLOnLineListVC.h"
#import "VLMacroDefine.h"
#import "MenuUtils.h"
#import "AESMacro.h"
#import "VLToast.h"
#import "VLSBGOnLineListVC.h"

@import Pure1v1;
@import ShowTo1v1;
@import Joy;

@interface VLHomeViewController ()<VLHomeViewDelegate>

@end

@implementation VLHomeViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setBackgroundImage:@"home_bg_image"];
    [self setNaviTitleName:AGLocalizedString(@"agora")];
    
    [[NetworkManager shared] reportDeviceInfoWithSceneName: @""];
    
    [self setUpUI];
}

- (void)setUpUI {
    VLHomeView *homeView = [[VLHomeView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT-kBottomTabBarHeight) withDelegate:self];
    [self.view addSubview:homeView];
    
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
}

#pragma mark - Public Methods
- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}

- (void)itemClickAction:(int)tagValue {

    NSArray* sceneNames = @[@"ChatRoom", @"SpatialAudioChatRoom", @"KTV", @"LiveShow", @"", @"Pure1v1", @"ShowTo1v1", @"Joy"];
    [[NetworkManager shared] reportSceneClickWithSceneName:sceneNames[tagValue]];
    [[NetworkManager shared] reportDeviceInfoWithSceneName:sceneNames[tagValue]];
    [[NetworkManager shared] reportUserBehaviorWithSceneName:sceneNames[tagValue]];
    switch (tagValue) {
        case 0: {
            VRRoomsViewController *vc = [[VRRoomsViewController alloc] initWithUser:VLUserCenter.user];
            vc.hidesBottomBarWhenPushed = YES;
            [self.navigationController pushViewController:vc animated:YES];
        }
            break;
        case 2: {
            VLOnLineListVC *vc = [[VLOnLineListVC alloc]init];
            [self.navigationController pushViewController:vc animated:YES];
        } break;
        case 3: {
            ShowRoomListVC *vc = [ShowRoomListVC new];
            [self.navigationController pushViewController:vc animated:YES];
        } break;
        case 1: {
            SARoomsViewController *roomVc = [[SARoomsViewController alloc] initWithUser:VLUserCenter.user];
            roomVc.hidesBottomBarWhenPushed = YES;
            [self.navigationController pushViewController:roomVc animated:YES];
        } break;
        case 4: {
            VLSBGOnLineListVC *listVC = [[VLSBGOnLineListVC alloc]init];
            [self.navigationController pushViewController:listVC animated:YES];
        } break;
        case 5: {
            Pure1v1UserInfo* userInfo = [Pure1v1UserInfo new];
            userInfo.userId = VLUserCenter.user.id;
            userInfo.userName = VLUserCenter.user.name;
            userInfo.avatar = VLUserCenter.user.headUrl;
            [Pure1v1Context showSceneWithViewController:self
                                                  appId:KeyCenter.AppId
                                         appCertificate:KeyCenter.Certificate
                                               userInfo:userInfo];
        } break;
        case 6: {
            ShowTo1v1UserInfo* userInfo = [ShowTo1v1UserInfo new];
            userInfo.userId = VLUserCenter.user.id;
            userInfo.userName = VLUserCenter.user.name;
            userInfo.avatar = VLUserCenter.user.headUrl;
            [ShowTo1v1Context showSceneWithViewController:self
                                                  appId:KeyCenter.AppId
                                         appCertificate:KeyCenter.Certificate
                                               userInfo:userInfo];
        } break;
        case 7: {
            JoyUserInfo* userInfo = [JoyUserInfo new];
            userInfo.userId = [VLUserCenter.user.id unsignedIntegerValue];
            userInfo.userName = VLUserCenter.user.name;
            userInfo.avatar = VLUserCenter.user.headUrl;
            [JoyContext showSceneWithViewController:self
                                                  appId:KeyCenter.AppId
                                         appCertificate:KeyCenter.Certificate
                                               userInfo:userInfo];
        } break;
        default:
            break;
    }
}

@end
