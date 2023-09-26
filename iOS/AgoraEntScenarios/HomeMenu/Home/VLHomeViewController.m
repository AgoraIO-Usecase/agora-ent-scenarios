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
    NSArray* sceneNames = @[@"ChatRoom", @"SpatialAudioChatRoom", @"KTV", @"LiveShow"];
    [[NetworkManager shared] reportSceneClickWithSceneName:sceneNames[tagValue]];
    [[NetworkManager shared] reportDeviceInfoWithSceneName:sceneNames[tagValue]];
    [[NetworkManager shared] reportUserBehaviorWithSceneName:sceneNames[tagValue]];
    
    ShowRoomListVC *vc = [ShowRoomListVC new];
    [self.navigationController pushViewController:vc animated:YES];
}

@end
