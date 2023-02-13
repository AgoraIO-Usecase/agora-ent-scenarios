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

@interface VLMainTabBarController ()<CALayerDelegate>
@property (nonatomic, assign) NSInteger doubleCount;
@end

@implementation VLMainTabBarController

#pragma mark - Life Cycle Methods

- (instancetype)initWithContext:(NSString *)context {
    UIEdgeInsets imageInsets = UIEdgeInsetsZero;//UIEdgeInsetsMake(4.5, 0, -4.5, 0);
    UIOffset titlePositionAdjustment = IS_NOTCHED_SCREEN ? UIOffsetMake(0, -2) : UIOffsetMake(0, -5);;
    if (self = [super initWithViewControllers:[self viewControllers]
                        tabBarItemsAttributes:[self tabBarItemsAttributesForController]
                                  imageInsets:imageInsets
                      titlePositionAdjustment:titlePositionAdjustment
                                      context:context
                ]) {
        [self customizeTabBarAppearanceWithTitlePositionAdjustment:titlePositionAdjustment];
        self.delegate = self;
        self.navigationController.navigationBar.hidden = YES;
        self.selectedIndex = 0;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];

}
#pragma mark - Intial Methods
- (void)initSubviews {
    [self masLayoutSubViews];
}
- (void)masLayoutSubViews {
    
}
#pragma mark - Private Method

- (void)customizeTabBarAppearanceWithTitlePositionAdjustment:(UIOffset)titlePositionAdjustment {
    
    self.tabBarHeight = VLTABBAR_HEIGHT;
    self.tabBar.layer.shadowColor = [UIColorMakeWithHex(@"#000000") colorWithAlphaComponent:0.03].CGColor;
    self.tabBar.layer.shadowOffset = CGSizeMake(0, -2);
    self.tabBar.layer.shadowOpacity = 1;
    
    NSMutableDictionary *normalAttrs = [NSMutableDictionary dictionary];
    normalAttrs[NSForegroundColorAttributeName] = UIColorMakeWithHex(@"#979CBB");

    NSMutableDictionary *selectedAttrs = [NSMutableDictionary dictionary];
    selectedAttrs[NSForegroundColorAttributeName] = UIColorMakeWithHex(@"#979CBB");

    if (@available(iOS 13.0, *)) {
        UITabBarItemAppearance *inlineLayoutAppearance = [[UITabBarItemAppearance alloc] init];
        inlineLayoutAppearance.normal.titlePositionAdjustment = titlePositionAdjustment;
        [inlineLayoutAppearance.normal setTitleTextAttributes:normalAttrs];
        [inlineLayoutAppearance.selected setTitleTextAttributes:selectedAttrs];
        UITabBarAppearance *standardAppearance = [[UITabBarAppearance alloc] init];
        standardAppearance.stackedLayoutAppearance = inlineLayoutAppearance;
        standardAppearance.backgroundColor = UIColorMakeWithHex(@"#FFFFFF");
        standardAppearance.shadowColor = UIColor.clearColor;
        standardAppearance.shadowImage = [UIImage new];
        if (@available(iOS 15.0, *)) {
            self.tabBar.scrollEdgeAppearance = standardAppearance;
        }
        self.tabBar.standardAppearance = standardAppearance;
    } else {
        UITabBarItem *tabBar = [UITabBarItem appearance];
        [tabBar setTitleTextAttributes:normalAttrs forState:UIControlStateNormal];
        [tabBar setTitleTextAttributes:selectedAttrs forState:UIControlStateSelected];
        [[UITabBar appearance] setBackgroundImage:[[UIImage alloc] init]];
        [[UITabBar appearance] setShadowImage:[UIImage new]];
    }
}
- (NSArray *)viewControllers {
    VLHomeViewController *homeViewController = [[VLHomeViewController alloc] init];
    homeViewController.hidesBottomBarWhenPushed = false;
    BaseNavigationController *homeNavigationController = [[BaseNavigationController alloc]
                                                  initWithRootViewController:homeViewController];
//
//    VLDiscoveryViewController *discoveryVC = [[VLDiscoveryViewController alloc] init];
//    discoveryVC.hidesBottomBarWhenPushed = false;
//    BaseNavigationController *discoveryNavigationController = [[BaseNavigationController alloc]
//                                                  initWithRootViewController:discoveryVC];

    VLMineViewController *mineViewController = [[VLMineViewController alloc] init];
    mineViewController.hidesBottomBarWhenPushed = false;
    BaseNavigationController *mineNavigationController = [[BaseNavigationController alloc]
                                                   initWithRootViewController:mineViewController];

   NSArray *viewControllers = @[
                                   homeNavigationController,
//                                   discoveryNavigationController,
                                   mineNavigationController,
                                ];
   return viewControllers;
}

- (NSArray *)tabBarItemsAttributesForController {
   NSDictionary *homeTabBarItemsAttributes = @{
                                                CYLTabBarItemTitle : AGLocalizedString(@"首页"),
                                                CYLTabBarItemImage : @"Tab_home_normal",
                                                CYLTabBarItemSelectedImage : @"Tab_home_sel",
                                                };
//   NSDictionary *discoveryTabBarItemsAttributes = @{
//                                                 CYLTabBarItemTitle : AGLocalizedString(@"发现"),
//                                                 CYLTabBarItemImage : @"Tab_discovery_normal",
//                                                 CYLTabBarItemSelectedImage : @"Tab_discovery_sel",
//                                                 };

    NSDictionary *mineTabBarItemsAttributes = @{
                                                  CYLTabBarItemTitle : AGLocalizedString(@"我的"),
                                                  CYLTabBarItemImage : @"Tab_mine_normal",
                                                  CYLTabBarItemSelectedImage : @"Tab_mine_sel",
                                                  };


   NSArray *tabBarItemsAttributes = @[
                                       homeTabBarItemsAttributes,
//                                       discoveryTabBarItemsAttributes,
                                       mineTabBarItemsAttributes,
                                      ];
   return tabBarItemsAttributes;
}

#pragma mark - delegate

- (BOOL)tabBarController:(UITabBarController *)tabBarController shouldSelectViewController:(UIViewController *)viewController {
    BOOL should = YES;
    [self updateSelectionStatusIfNeededForTabBarController:tabBarController shouldSelectViewController:viewController shouldSelect:should];
    return should;
}
- (void)tabBarController:(UITabBarController *)tabBarController didSelectViewController:(UIViewController *)viewController {
}

- (void)tabBarController:(UITabBarController *)tabBarController didSelectControl:(UIControl *)control {
    
}

#pragma mark – Getters and Setters

@end
