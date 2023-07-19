//
//  VLMainTabBarController.m
//  VoiceOnLine
//
#import <UIKit/UIKit.h>
#import "VLMainTabBarController.h"
#import "VLHomeViewController.h"
#import "VLDiscoveryViewController.h"
#import "VLMineViewController.h"
#import "VLMacroDefine.h"
#import "BaseNavigationController.h"
#import "MenuUtils.h"
#import "AESMacro.h"

@interface VLMainTabBarController ()<CALayerDelegate, UITabBarControllerDelegate>
@property (nonatomic, assign) NSInteger doubleCount;
@end

@implementation VLMainTabBarController

#pragma mark - Life Cycle Methods

- (instancetype)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
//        self.delegate = self;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setViewControllers:[self tabViewControllers]];
    
    self.selectedIndex = 0;
    [UITabBar appearance].barStyle = UIBarStyleDefault;
    
    [self.tabBar setBackgroundColor:[UIColor whiteColor]];

    [self.tabBar setBackgroundImage:[UIImage new]];

}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
//    self.tabBar.hidden = NO;
}

- (NSArray *)tabViewControllers {
    VLHomeViewController *homeViewController = [[VLHomeViewController alloc] init];
    homeViewController.hidesBottomBarWhenPushed = NO;
    BaseNavigationController *homeNavigationController = [[BaseNavigationController alloc]
                                                          initWithRootViewController:homeViewController];
    homeViewController.tabBarItem = [self tabBarItemsWithIndex:0];
    
//    VLDiscoveryViewController *discoveryVC = [[VLDiscoveryViewController alloc] init];
//    discoveryVC.hidesBottomBarWhenPushed = NO;
//    BaseNavigationController *discoveryNavigationController = [[BaseNavigationController alloc]
//                                                  initWithRootViewController:discoveryVC];
//    discoveryVC.tabBarItem = [self tabBarItemsWithIndex:1];

    VLMineViewController *mineViewController = [[VLMineViewController alloc] init];
    mineViewController.hidesBottomBarWhenPushed = NO;
    BaseNavigationController *mineNavigationController = [[BaseNavigationController alloc]
                                                   initWithRootViewController:mineViewController];
    mineViewController.tabBarItem = [self tabBarItemsWithIndex:1];

   NSArray *viewControllers = @[
       homeNavigationController,
       mineNavigationController,
   ];
   return viewControllers;
}

- (UIImage*)tabbarImageWithImageNamed:(NSString*)name {
    UIImage* image = [UIImage imageNamed:name];
    image = [image imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    
    return image;
}

- (UITabBarItem*)tabBarItemsWithIndex:(NSUInteger)index {
    NSArray* tabBarItems = @[
        [[UITabBarItem alloc] initWithTitle:AGLocalizedString(@"app_title_home")
                                      image:[self tabbarImageWithImageNamed:@"Tab_home_normal"]
                              selectedImage:[self tabbarImageWithImageNamed:@"Tab_home_sel"]],
      //  [[UITabBarItem alloc] initWithTitle:AGLocalizedString(@"发现")
//                                      image:[self tabbarImageWithImageNamed:@"Tab_discovery_normal"]
//                              selectedImage:[self tabbarImageWithImageNamed:@"Tab_discovery_sel"]],
        [[UITabBarItem alloc] initWithTitle:AGLocalizedString(@"app_title_mine")
                                      image:[self tabbarImageWithImageNamed:@"Tab_mine_normal"]
                              selectedImage:[self tabbarImageWithImageNamed:@"Tab_mine_sel"]]
    ];
    
    return [tabBarItems objectAtIndex:index];
}

#pragma mark - UITabBarControllerDelegate

//- (BOOL)tabBarController:(UITabBarController *)tabBarController shouldSelectViewController:(UIViewController *)viewController {
//    BOOL should = YES;
//    [self updateSelectionStatusIfNeededForTabBarController:tabBarController shouldSelectViewController:viewController shouldSelect:should];
//    return should;
//}
- (void)tabBarController:(UITabBarController *)tabBarController didSelectViewController:(UIViewController *)viewController {
}

- (void)tabBarController:(UITabBarController *)tabBarController didSelectControl:(UIControl *)control {
    
}

#pragma mark – Getters and Setters

@end
