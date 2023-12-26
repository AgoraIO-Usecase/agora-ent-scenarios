//
//  UIWindow+Router.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/17.
//

#import "UIWindow+Router.h"
#import "BaseNavigationController.h"
#import "VLMainTabBarController.h"
#import "VLUserCenter.h"
#import "AESMacro.h"
#import "AgoraEntScenarios-Swift.h"

@implementation UIWindow (Router)
- (void)configRootViewController {
    if ([VLUserCenter center].isLogin) {
        self.rootViewController = [[VLMainTabBarController alloc] init];
    } else {
        VLLoginController *rootVC = [[VLLoginController alloc] init];
        BaseNavigationController *nav = [[BaseNavigationController alloc] initWithRootViewController:rootVC];
        self.rootViewController = nav;
    }
}
@end
