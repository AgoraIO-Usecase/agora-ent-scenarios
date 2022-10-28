//
//  VLHomeViewController.m
//  VoiceOnLine
//

#import "VLHomeViewController.h"
#import "VLHomeView.h"
#import "VLOnLineListVC.h"
#import "VLMacroDefine.h"
#import "AgoraEntScenarios-Swift.h"

@interface VLHomeViewController ()<VLHomeViewDelegate>

@end

@implementation VLHomeViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setBackgroundImage:@"home_bg_image"];
    [self setNaviTitleName:NSLocalizedString(@"声网", nil)];
    
    [self setUpUI];
}

- (void)setUpUI {
    VLHomeView *homeView = [[VLHomeView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT-kBottomTabBarHeight) withDelegate:self];
    [self.view addSubview:homeView];
    
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.hidesBottomBarWhenPushed = YES;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    self.hidesBottomBarWhenPushed = NO;
}


#pragma mark - Public Methods
- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return true;
}


- (void)itemClickAction:(int)tagValue {
    switch (tagValue) {
        case 0:
        {
            VRRoomsViewController *roomVc = [[VRRoomsViewController alloc] initWithUser:VLUserCenter.user];
            roomVc.hidesBottomBarWhenPushed = YES;
            [self.navigationController pushViewController:roomVc animated:YES];
        }
            break;
        case 1:
        {
            VLOnLineListVC *listVC = [[VLOnLineListVC alloc]init];
            [self.navigationController pushViewController:listVC animated:YES];
        }
            break;
        default:
            break;
    }

}

@end
