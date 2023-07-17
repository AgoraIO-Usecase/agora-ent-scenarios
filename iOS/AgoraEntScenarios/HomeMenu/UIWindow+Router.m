//
//  UIWindow+Router.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/17.
//

#import "UIWindow+Router.h"
#import "BaseNavigationController.h"
#import "VLLoginViewController.h"
#import "VLMainTabBarController.h"
#import "VLUserCenter.h"
#import "AESMacro.h"

@implementation UIWindow (Router)
- (void)configRootViewController {
    UIViewController* rootVC = nil;
    if ([VLUserCenter center].isLogin) {
        rootVC = [[VLMainTabBarController alloc] init];
    } else {
        rootVC = [[VLLoginViewController alloc] init];
    }
    BaseNavigationController *navi = [[BaseNavigationController alloc] initWithRootViewController:rootVC];
    self.rootViewController = navi;
}
@end
